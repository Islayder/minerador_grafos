package br.pucminas.tgc.githubgraph.github;

/**
 * Contrato para obtencao de JSON bruto da GitHub REST API (permite implementacoes fake em testes).
 */
public interface GitHubRawDataClient {

    String get(String url);

    String getIssuesPage(GitHubConfig config, int page, int perPage);

    String getPullRequestsPage(GitHubConfig config, int page, int perPage);

    String getIssueCommentsPage(GitHubConfig config, int issueNumber, int page, int perPage);

    String getPullRequestReviewsPage(GitHubConfig config, int pullRequestNumber, int page, int perPage);

    default String getIssueComments(GitHubConfig config, int issueNumber) {
        return getIssueCommentsPage(config, issueNumber, 1, 100);
    }

    default String getPullRequestReviews(GitHubConfig config, int pullRequestNumber) {
        return getPullRequestReviewsPage(config, pullRequestNumber, 1, 100);
    }
}
