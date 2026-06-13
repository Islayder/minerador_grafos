package br.pucminas.tgc.githubgraph.github;

/**
 * Dados intermediários de um pull request obtido da API.
 */
public final class GitHubPullRequestData {

    private final int number;
    private final String authorLogin;
    private final String mergedByLogin;
    private final boolean merged;
    private final int commentsCount;
    private final String commentsUrl;

    public GitHubPullRequestData(
            int number,
            String authorLogin,
            String mergedByLogin,
            boolean merged,
            int commentsCount,
            String commentsUrl) {
        this.number = number;
        this.authorLogin = authorLogin;
        this.mergedByLogin = mergedByLogin;
        this.merged = merged;
        this.commentsCount = commentsCount;
        this.commentsUrl = commentsUrl;
    }

    public int getNumber() {
        return number;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public String getMergedByLogin() {
        return mergedByLogin;
    }

    public boolean isMerged() {
        return merged;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public String getCommentsUrl() {
        return commentsUrl;
    }

    public boolean shouldSkipCommentsFetch() {
        return commentsCount == 0;
    }
}
