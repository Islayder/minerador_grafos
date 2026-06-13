package br.pucminas.tgc.githubgraph.github;

/**
 * Dados intermediários de um comentário em issue ou pull request.
 */
public final class GitHubCommentData {

    private final String authorLogin;

    public GitHubCommentData(String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }
}
