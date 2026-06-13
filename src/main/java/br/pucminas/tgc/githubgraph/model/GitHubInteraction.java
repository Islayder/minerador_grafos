package br.pucminas.tgc.githubgraph.model;

import java.util.Objects;

/**
 * Interação direcionada entre dois usuários do GitHub.
 */
public final class GitHubInteraction {

    private final GitHubUser sourceUser;
    private final GitHubUser targetUser;
    private final InteractionType type;

    public GitHubInteraction(GitHubUser sourceUser, GitHubUser targetUser, InteractionType type) {
        if (sourceUser == null) {
            throw new IllegalArgumentException("O usuário de origem (sourceUser) é obrigatório.");
        }
        if (targetUser == null) {
            throw new IllegalArgumentException("O usuário de destino (targetUser) é obrigatório.");
        }
        if (type == null) {
            throw new IllegalArgumentException("O tipo de interação (type) é obrigatório.");
        }
        if (sourceUser.equals(targetUser)) {
            throw new IllegalArgumentException(
                    "sourceUser e targetUser não podem ser iguais: laços não são permitidos no grafo.");
        }
        this.sourceUser = sourceUser;
        this.targetUser = targetUser;
        this.type = type;
    }

    public GitHubUser getSourceUser() {
        return sourceUser;
    }

    public GitHubUser getTargetUser() {
        return targetUser;
    }

    public InteractionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        GitHubInteraction that = (GitHubInteraction) other;
        return sourceUser.equals(that.sourceUser)
                && targetUser.equals(that.targetUser)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceUser, targetUser, type);
    }

    @Override
    public String toString() {
        return "GitHubInteraction{"
                + "source=" + sourceUser.getLogin()
                + ", target=" + targetUser.getLogin()
                + ", type=" + type
                + '}';
    }
}
