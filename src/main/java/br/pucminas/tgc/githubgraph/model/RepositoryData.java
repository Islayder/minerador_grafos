package br.pucminas.tgc.githubgraph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Conjunto de interações simuladas ou coletadas de um repositório GitHub.
 */
public final class RepositoryData {

    private final String owner;
    private final String repository;
    private final List<GitHubInteraction> interactions;
    private final Map<String, Integer> pullRequestsOpenedByUser;

    public RepositoryData(String owner, String repository, List<GitHubInteraction> interactions) {
        this(owner, repository, interactions, Map.of());
    }

    public RepositoryData(
            String owner,
            String repository,
            List<GitHubInteraction> interactions,
            Map<String, Integer> pullRequestsOpenedByUser) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("O owner do repositório não pode ser nulo ou vazio.");
        }
        if (repository == null || repository.isBlank()) {
            throw new IllegalArgumentException("O nome do repositório não pode ser nulo ou vazio.");
        }
        if (interactions == null) {
            throw new IllegalArgumentException("A lista de interações não pode ser nula.");
        }
        if (pullRequestsOpenedByUser == null) {
            throw new IllegalArgumentException("O mapa de aberturas de PR não pode ser nulo.");
        }
        this.owner = owner.trim();
        this.repository = repository.trim();
        this.interactions = List.copyOf(interactions);
        this.pullRequestsOpenedByUser = Map.copyOf(pullRequestsOpenedByUser);
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public List<GitHubInteraction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    public Map<String, Integer> getPullRequestsOpenedByUser() {
        return Collections.unmodifiableMap(pullRequestsOpenedByUser);
    }

    public Set<GitHubUser> getAllUsers() {
        Set<GitHubUser> users = new LinkedHashSet<>();
        for (GitHubInteraction interaction : interactions) {
            users.add(interaction.getSourceUser());
            users.add(interaction.getTargetUser());
        }
        return Collections.unmodifiableSet(users);
    }

    public List<GitHubInteraction> getInteractionsByType(InteractionType type) {
        Objects.requireNonNull(type, "O tipo de interação não pode ser nulo.");
        List<GitHubInteraction> filtered = new ArrayList<>();
        for (GitHubInteraction interaction : interactions) {
            if (interaction.getType() == type) {
                filtered.add(interaction);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    @Override
    public String toString() {
        return "RepositoryData{"
                + "owner='" + owner + '\''
                + ", repository='" + repository + '\''
                + ", interactions=" + interactions.size()
                + ", pullRequestsOpened=" + pullRequestsOpenedByUser.size()
                + '}';
    }
}
