package br.pucminas.tgc.githubgraph.github;

/**
 * Dados intermediários de uma revisão de pull request.
 */
public final class GitHubReviewData {

    private final String authorLogin;
    private final String state;

    public GitHubReviewData(String authorLogin, String state) {
        this.authorLogin = authorLogin;
        this.state = state;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public String getState() {
        return state;
    }
}
