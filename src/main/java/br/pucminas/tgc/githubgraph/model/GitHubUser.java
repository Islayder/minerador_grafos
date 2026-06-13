package br.pucminas.tgc.githubgraph.model;

import java.util.Objects;

/**
 * Representa um usuário do GitHub identificado pelo login.
 */
public final class GitHubUser {

    private final String login;

    public GitHubUser(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login do usuário não pode ser nulo ou vazio.");
        }
        this.login = login.trim();
    }

    public String getLogin() {
        return login;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        GitHubUser that = (GitHubUser) other;
        return login.equals(that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "GitHubUser{login='" + login + "'}";
    }
}
