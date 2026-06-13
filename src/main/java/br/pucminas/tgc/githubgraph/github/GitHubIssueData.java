package br.pucminas.tgc.githubgraph.github;

/**
 * Dados intermediários de uma issue obtida da API.
 */
public final class GitHubIssueData {

    /** Valor quando o campo {@code comments} nao veio no JSON (comportamento seguro: buscar comentarios). */
    public static final int UNKNOWN_COMMENTS_COUNT = -1;

    private final int number;
    private final String authorLogin;
    private final String closedByLogin;
    private final boolean pullRequest;
    private final int commentsCount;
    private final String commentsUrl;

    public GitHubIssueData(
            int number,
            String authorLogin,
            String closedByLogin,
            boolean pullRequest,
            int commentsCount,
            String commentsUrl) {
        this.number = number;
        this.authorLogin = authorLogin;
        this.closedByLogin = closedByLogin;
        this.pullRequest = pullRequest;
        this.commentsCount = commentsCount;
        this.commentsUrl = commentsUrl;
    }

    public int getNumber() {
        return number;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public String getClosedByLogin() {
        return closedByLogin;
    }

    public boolean isPullRequest() {
        return pullRequest;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public String getCommentsUrl() {
        return commentsUrl;
    }

    /**
     * {@code true} quando a API informou {@code comments=0}; caso contrario busca comentarios (inclui desconhecido).
     */
    public boolean shouldSkipCommentsFetch() {
        return commentsCount == 0;
    }
}
