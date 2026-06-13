package br.pucminas.tgc.githubgraph.github;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Decorador que aplica cache local sobre um cliente GitHub real ou fake.
 */
public final class CachingGitHubRawDataClient implements GitHubRawDataClient {

    private final GitHubRawDataClient delegate;
    private final GitHubResponseCache cache;
    private final GitHubRequestStats stats;

    public CachingGitHubRawDataClient(
            GitHubRawDataClient delegate,
            GitHubResponseCache cache,
            GitHubRequestStats stats) {
        this.delegate = delegate;
        this.cache = cache;
        this.stats = stats;
    }

    public GitHubRequestStats getStats() {
        return stats;
    }

    @Override
    public String get(String url) {
        return fetch(url, () -> delegate.get(url));
    }

    @Override
    public String getIssuesPage(GitHubConfig config, int page, int perPage) {
        String url = buildIssuesUrl(config, page, perPage);
        return fetch(url, () -> delegate.getIssuesPage(config, page, perPage));
    }

    @Override
    public String getPullRequestsPage(GitHubConfig config, int page, int perPage) {
        String url = buildPullsUrl(config, page, perPage);
        return fetch(url, () -> delegate.getPullRequestsPage(config, page, perPage));
    }

    @Override
    public String getIssueCommentsPage(GitHubConfig config, int issueNumber, int page, int perPage) {
        String url = buildCommentsUrl(config, issueNumber, page, perPage);
        return fetch(url, () -> delegate.getIssueCommentsPage(config, issueNumber, page, perPage));
    }

    @Override
    public String getPullRequestReviewsPage(GitHubConfig config, int pullRequestNumber, int page, int perPage) {
        String url = buildReviewsUrl(config, pullRequestNumber, page, perPage);
        return fetch(url, () -> delegate.getPullRequestReviewsPage(config, pullRequestNumber, page, perPage));
    }

    private String fetch(String cacheKey, FetchOperation operation) {
        if (cache.isEnabled()) {
            try {
                String cached = cache.get(cacheKey);
                if (cached != null) {
                    stats.recordCacheHit();
                    return cached;
                }
            } catch (IOException exception) {
                throw new UncheckedIOException("Falha ao ler cache para URL: " + cacheKey, exception);
            }
        }

        stats.recordApiRequest();
        String body = operation.fetch();
        if (cache.isEnabled()) {
            try {
                cache.put(cacheKey, body);
            } catch (IOException exception) {
                throw new UncheckedIOException("Falha ao gravar cache para URL: " + cacheKey, exception);
            }
        }
        return body;
    }

    private static String buildIssuesUrl(GitHubConfig config, int page, int perPage) {
        return "https://api.github.com/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/issues?state=all&page=" + page + "&per_page=" + perPage;
    }

    private static String buildPullsUrl(GitHubConfig config, int page, int perPage) {
        return "https://api.github.com/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/pulls?state=all&page=" + page + "&per_page=" + perPage;
    }

    private static String buildCommentsUrl(GitHubConfig config, int issueNumber, int page, int perPage) {
        return "https://api.github.com/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/issues/" + issueNumber + "/comments?page=" + page + "&per_page=" + perPage;
    }

    private static String buildReviewsUrl(GitHubConfig config, int pullRequestNumber, int page, int perPage) {
        return "https://api.github.com/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/pulls/" + pullRequestNumber + "/reviews?page=" + page + "&per_page=" + perPage;
    }

    @FunctionalInterface
    private interface FetchOperation {
        String fetch();
    }
}
