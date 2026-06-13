package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.RepositoryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Orquestra a coleta de dados do GitHub e produz {@link RepositoryData}.
 */
public final class GitHubDataCollector {

    private final GitHubRawDataClient client;
    private final GitHubJsonParser parser;
    private final GitHubInteractionMapper mapper;

    public GitHubDataCollector() {
        this(new GitHubApiClient(), new GitHubJsonParser(), new GitHubInteractionMapper());
    }

    public GitHubDataCollector(
            GitHubRawDataClient client,
            GitHubJsonParser parser,
            GitHubInteractionMapper mapper) {
        this.client = client;
        this.parser = parser;
        this.mapper = mapper;
    }

    public RepositoryData collect(GitHubConfig config, CollectionProfile profile) {
        return collect(config, profile, CollectionProgressListener.noop());
    }

    public RepositoryData collect(
            GitHubConfig config,
            CollectionProfile profile,
            CollectionProgressListener progress) {
        return collect(config, profile, progress, new CollectionStatistics()).repositoryData();
    }

    public RepositoryCollectionResult collect(
            GitHubConfig config,
            CollectionProfile profile,
            CollectionProgressListener progress,
            CollectionStatistics statistics) {
        List<GitHubInteraction> interactions = Collections.synchronizedList(new ArrayList<>());
        Map<String, Integer> pullRequestsOpenedByUser = Collections.synchronizedMap(new LinkedHashMap<>());
        int parallelism = Math.max(1, profile.getConcurrency());
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        try {
            if (profile.isFullRepository()) {
                progress.onProgress("Mineracao FULL_REPOSITORY iniciada para " + profile.getRepositorySlug());
                collectAllIssues(config, profile, executor, interactions, progress, statistics);
                collectAllPullRequests(config, profile, executor, interactions, pullRequestsOpenedByUser, progress, statistics);
            } else {
                collectLimitedIssues(config, profile, executor, interactions, statistics);
                collectLimitedPullRequests(config, profile, executor, interactions, pullRequestsOpenedByUser, statistics);
            }
        } finally {
            executor.shutdown();
        }
        progress.onProgress("Coleta finalizada: " + interactions.size() + " interacoes.");
        return new RepositoryCollectionResult(
                new RepositoryData(
                        config.getOwner(),
                        config.getRepository(),
                        List.copyOf(interactions),
                        Map.copyOf(pullRequestsOpenedByUser)),
                statistics);
    }

    private void collectAllIssues(
            GitHubConfig config,
            CollectionProfile profile,
            ExecutorService executor,
            List<GitHubInteraction> interactions,
            CollectionProgressListener progress,
            CollectionStatistics statistics) {
        int perPage = profile.getPerPage();
        int page = 1;
        int totalIssues = 0;
        List<Callable<Void>> tasks = new ArrayList<>();

        while (true) {
            long pageStart = System.nanoTime();
            statistics.recordIssueListPageCall();
            String json = client.getIssuesPage(config, page, perPage);
            List<GitHubIssueData> issues = parser.parseIssues(json);
            statistics.addIssueListingNanos(System.nanoTime() - pageStart);

            if (issues.isEmpty()) {
                break;
            }

            progress.onProgress("Issues: pagina " + page + " (" + issues.size() + " itens na pagina)");

            for (GitHubIssueData issue : issues) {
                if (issue.isPullRequest()) {
                    continue;
                }
                totalIssues++;

                if (issue.getAuthorLogin() != null && issue.getClosedByLogin() != null) {
                    interactions.addAll(mapper.mapIssueClosed(
                            issue.getClosedByLogin(),
                            issue.getAuthorLogin()));
                }

                if (issue.getAuthorLogin() != null) {
                    tasks.add(() -> {
                        appendAllIssueComments(config, profile, issue, interactions, statistics);
                        return null;
                    });
                }
            }

            if (issues.size() < perPage) {
                break;
            }
            page++;
        }

        statistics.recordIssuesListed(totalIssues);
        progress.onProgress("Issues processadas: " + totalIssues);
        awaitTasks(tasks, executor);
    }

    private void collectAllPullRequests(
            GitHubConfig config,
            CollectionProfile profile,
            ExecutorService executor,
            List<GitHubInteraction> interactions,
            Map<String, Integer> pullRequestsOpenedByUser,
            CollectionProgressListener progress,
            CollectionStatistics statistics) {
        int perPage = profile.getPerPage();
        int page = 1;
        int totalPullRequests = 0;
        List<Callable<Void>> tasks = new ArrayList<>();

        while (true) {
            long pageStart = System.nanoTime();
            statistics.recordPullRequestListPageCall();
            String json = client.getPullRequestsPage(config, page, perPage);
            List<GitHubPullRequestData> pullRequests = parser.parsePullRequests(json);
            statistics.addPullRequestListingNanos(System.nanoTime() - pageStart);

            if (pullRequests.isEmpty()) {
                break;
            }

            progress.onProgress("Pull requests: pagina " + page + " (" + pullRequests.size() + " itens na pagina)");

            for (GitHubPullRequestData pullRequest : pullRequests) {
                totalPullRequests++;

                if (pullRequest.getAuthorLogin() != null) {
                    pullRequestsOpenedByUser.merge(pullRequest.getAuthorLogin(), 1, Integer::sum);
                }

                if (pullRequest.isMerged() && pullRequest.getMergedByLogin() != null
                        && pullRequest.getAuthorLogin() != null) {
                    interactions.addAll(mapper.mapMerge(
                            pullRequest.getMergedByLogin(),
                            pullRequest.getAuthorLogin()));
                }

                if (pullRequest.getAuthorLogin() != null) {
                    tasks.add(() -> {
                        appendAllPullRequestDetails(config, profile, pullRequest, interactions, statistics);
                        return null;
                    });
                }
            }

            if (pullRequests.size() < perPage) {
                break;
            }
            page++;
        }

        statistics.recordPullRequestsListed(totalPullRequests);
        progress.onProgress("Pull requests processados: " + totalPullRequests);
        awaitTasks(tasks, executor);
    }

    private void collectLimitedIssues(
            GitHubConfig config,
            CollectionProfile profile,
            ExecutorService executor,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        int collected = 0;
        int page = 1;
        int perPage = profile.getPerPage();
        List<Callable<Void>> tasks = new ArrayList<>();

        while (collected < config.getMaxIssues()) {
            long pageStart = System.nanoTime();
            statistics.recordIssueListPageCall();
            String json = client.getIssuesPage(config, page, perPage);
            List<GitHubIssueData> issues = parser.parseIssues(json);
            statistics.addIssueListingNanos(System.nanoTime() - pageStart);

            if (issues.isEmpty()) {
                break;
            }

            for (GitHubIssueData issue : issues) {
                if (issue.isPullRequest()) {
                    continue;
                }
                if (collected >= config.getMaxIssues()) {
                    awaitTasks(tasks, executor);
                    return;
                }
                collected++;

                if (issue.getAuthorLogin() != null && issue.getClosedByLogin() != null) {
                    interactions.addAll(mapper.mapIssueClosed(
                            issue.getClosedByLogin(),
                            issue.getAuthorLogin()));
                }

                if (issue.getAuthorLogin() != null) {
                    tasks.add(() -> {
                        appendLimitedIssueComments(config, issue, interactions, statistics);
                        return null;
                    });
                }
            }

            if (issues.size() < perPage) {
                break;
            }
            page++;
        }
        statistics.recordIssuesListed(collected);
        awaitTasks(tasks, executor);
    }

    private void collectLimitedPullRequests(
            GitHubConfig config,
            CollectionProfile profile,
            ExecutorService executor,
            List<GitHubInteraction> interactions,
            Map<String, Integer> pullRequestsOpenedByUser,
            CollectionStatistics statistics) {
        int collected = 0;
        int page = 1;
        int perPage = profile.getPerPage();
        List<Callable<Void>> tasks = new ArrayList<>();

        while (collected < config.getMaxPullRequests()) {
            long pageStart = System.nanoTime();
            statistics.recordPullRequestListPageCall();
            String json = client.getPullRequestsPage(config, page, perPage);
            List<GitHubPullRequestData> pullRequests = parser.parsePullRequests(json);
            statistics.addPullRequestListingNanos(System.nanoTime() - pageStart);

            if (pullRequests.isEmpty()) {
                break;
            }

            for (GitHubPullRequestData pullRequest : pullRequests) {
                if (collected >= config.getMaxPullRequests()) {
                    awaitTasks(tasks, executor);
                    return;
                }
                collected++;

                if (pullRequest.getAuthorLogin() != null) {
                    pullRequestsOpenedByUser.merge(pullRequest.getAuthorLogin(), 1, Integer::sum);
                }

                if (pullRequest.isMerged() && pullRequest.getMergedByLogin() != null
                        && pullRequest.getAuthorLogin() != null) {
                    interactions.addAll(mapper.mapMerge(
                            pullRequest.getMergedByLogin(),
                            pullRequest.getAuthorLogin()));
                }

                if (pullRequest.getAuthorLogin() != null) {
                    tasks.add(() -> {
                        appendLimitedPullRequestDetails(config, pullRequest, interactions, statistics);
                        return null;
                    });
                }
            }

            if (pullRequests.size() < perPage) {
                break;
            }
            page++;
        }
        statistics.recordPullRequestsListed(collected);
        awaitTasks(tasks, executor);
    }

    private void appendAllIssueComments(
            GitHubConfig config,
            CollectionProfile profile,
            GitHubIssueData issue,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        if (issue.shouldSkipCommentsFetch()) {
            statistics.recordIssueCommentSkipped();
            return;
        }

        long phaseStart = System.nanoTime();
        try {
            int perPage = profile.getPerPage();
            int page = 1;
            String authorLogin = issue.getAuthorLogin();
            while (true) {
                statistics.recordIssueCommentApiCall();
                String commentsJson = client.getIssueCommentsPage(config, issue.getNumber(), page, perPage);
                List<GitHubCommentData> comments = parser.parseComments(commentsJson);
                if (comments.isEmpty()) {
                    break;
                }
                for (GitHubCommentData comment : comments) {
                    interactions.addAll(mapper.mapComment(
                            comment.getAuthorLogin(),
                            authorLogin,
                            true));
                }
                if (comments.size() < perPage) {
                    break;
                }
                page++;
            }
        } finally {
            statistics.addIssueCommentsNanos(System.nanoTime() - phaseStart);
        }
    }

    private void appendAllPullRequestDetails(
            GitHubConfig config,
            CollectionProfile profile,
            GitHubPullRequestData pullRequest,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        appendAllPullRequestComments(config, profile, pullRequest, interactions, statistics);
        appendAllPullRequestReviews(config, profile, pullRequest, interactions, statistics);
    }

    private void appendAllPullRequestComments(
            GitHubConfig config,
            CollectionProfile profile,
            GitHubPullRequestData pullRequest,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        if (pullRequest.shouldSkipCommentsFetch()) {
            statistics.recordPullRequestCommentSkipped();
            return;
        }

        long phaseStart = System.nanoTime();
        try {
            int perPage = profile.getPerPage();
            int page = 1;
            String authorLogin = pullRequest.getAuthorLogin();
            while (true) {
                statistics.recordPullRequestCommentApiCall();
                String commentsJson = client.getIssueCommentsPage(config, pullRequest.getNumber(), page, perPage);
                List<GitHubCommentData> comments = parser.parseComments(commentsJson);
                if (comments.isEmpty()) {
                    break;
                }
                for (GitHubCommentData comment : comments) {
                    interactions.addAll(mapper.mapComment(
                            comment.getAuthorLogin(),
                            authorLogin,
                            true));
                }
                if (comments.size() < perPage) {
                    break;
                }
                page++;
            }
        } finally {
            statistics.addPullRequestCommentsNanos(System.nanoTime() - phaseStart);
        }
    }

    private void appendAllPullRequestReviews(
            GitHubConfig config,
            CollectionProfile profile,
            GitHubPullRequestData pullRequest,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        long phaseStart = System.nanoTime();
        try {
            int perPage = profile.getPerPage();
            int page = 1;
            while (true) {
                statistics.recordReviewApiCall();
                String reviewsJson = client.getPullRequestReviewsPage(config, pullRequest.getNumber(), page, perPage);
                List<GitHubReviewData> reviews = parser.parseReviews(reviewsJson);
                if (reviews.isEmpty()) {
                    break;
                }
                for (GitHubReviewData review : reviews) {
                    interactions.addAll(mapper.mapReview(
                            review.getAuthorLogin(),
                            pullRequest.getAuthorLogin(),
                            review.getState()));
                }
                if (reviews.size() < perPage) {
                    break;
                }
                page++;
            }
        } finally {
            statistics.addReviewsNanos(System.nanoTime() - phaseStart);
        }
    }

    private void appendLimitedIssueComments(
            GitHubConfig config,
            GitHubIssueData issue,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        if (issue.shouldSkipCommentsFetch()) {
            statistics.recordIssueCommentSkipped();
            return;
        }

        long phaseStart = System.nanoTime();
        try {
            statistics.recordIssueCommentApiCall();
            String commentsJson = client.getIssueComments(config, issue.getNumber());
            List<GitHubCommentData> comments = parser.parseComments(commentsJson);
            int limit = Math.min(comments.size(), config.getMaxCommentsPerItem());
            for (int index = 0; index < limit; index++) {
                interactions.addAll(mapper.mapComment(
                        comments.get(index).getAuthorLogin(),
                        issue.getAuthorLogin(),
                        true));
            }
        } finally {
            statistics.addIssueCommentsNanos(System.nanoTime() - phaseStart);
        }
    }

    private void appendLimitedPullRequestDetails(
            GitHubConfig config,
            GitHubPullRequestData pullRequest,
            List<GitHubInteraction> interactions,
            CollectionStatistics statistics) {
        if (!pullRequest.shouldSkipCommentsFetch()) {
            long commentsStart = System.nanoTime();
            try {
                statistics.recordPullRequestCommentApiCall();
                String commentsJson = client.getIssueComments(config, pullRequest.getNumber());
                List<GitHubCommentData> comments = parser.parseComments(commentsJson);
                int limit = Math.min(comments.size(), config.getMaxCommentsPerItem());
                for (int index = 0; index < limit; index++) {
                    interactions.addAll(mapper.mapComment(
                            comments.get(index).getAuthorLogin(),
                            pullRequest.getAuthorLogin(),
                            true));
                }
            } finally {
                statistics.addPullRequestCommentsNanos(System.nanoTime() - commentsStart);
            }
        } else {
            statistics.recordPullRequestCommentSkipped();
        }

        long reviewsStart = System.nanoTime();
        try {
            statistics.recordReviewApiCall();
            String reviewsJson = client.getPullRequestReviews(config, pullRequest.getNumber());
            List<GitHubReviewData> reviews = parser.parseReviews(reviewsJson);
            int limit = Math.min(reviews.size(), config.getMaxReviewsPerPullRequest());
            for (int index = 0; index < limit; index++) {
                GitHubReviewData review = reviews.get(index);
                interactions.addAll(mapper.mapReview(
                        review.getAuthorLogin(),
                        pullRequest.getAuthorLogin(),
                        review.getState()));
            }
        } finally {
            statistics.addReviewsNanos(System.nanoTime() - reviewsStart);
        }
    }

    private static void awaitTasks(List<Callable<Void>> tasks, ExecutorService executor) {
        if (tasks.isEmpty()) {
            return;
        }
        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GitHubApiException(-1, "Coleta interrompida: " + exception.getMessage());
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new GitHubApiException(-1, "Falha na coleta paralela: " + cause.getMessage());
        } finally {
            tasks.clear();
        }
    }
}
