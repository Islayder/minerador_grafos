package br.pucminas.tgc.githubgraph.github;

import java.util.Objects;

/**
 * Configuracao de coleta real do GitHub (sem token).
 */
public final class CollectionProfile {

    private final GitHubCollectionMode mode;
    private final String owner;
    private final String repository;
    private final int perPage;
    private final int concurrency;
    private final boolean cacheEnabled;
    private final String description;
    private final int maxIssues;
    private final int maxPullRequests;
    private final int maxCommentsPerItem;
    private final int maxReviewsPerPullRequest;

    private CollectionProfile(
            GitHubCollectionMode mode,
            String owner,
            String repository,
            int perPage,
            int concurrency,
            boolean cacheEnabled,
            String description,
            int maxIssues,
            int maxPullRequests,
            int maxCommentsPerItem,
            int maxReviewsPerPullRequest) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.owner = owner;
        this.repository = repository;
        this.perPage = perPage;
        this.concurrency = concurrency;
        this.cacheEnabled = cacheEnabled;
        this.description = description == null ? "" : description.trim();
        this.maxIssues = maxIssues;
        this.maxPullRequests = maxPullRequests;
        this.maxCommentsPerItem = maxCommentsPerItem;
        this.maxReviewsPerPullRequest = maxReviewsPerPullRequest;
    }

    public static CollectionProfile fullRepository(
            String owner,
            String repository,
            int perPage,
            int concurrency,
            boolean cacheEnabled,
            String description) {
        if (perPage <= 0) {
            throw new IllegalArgumentException("perPage deve ser positivo.");
        }
        if (concurrency <= 0) {
            throw new IllegalArgumentException("concurrency deve ser positivo.");
        }
        return new CollectionProfile(
                GitHubCollectionMode.FULL_REPOSITORY,
                owner,
                repository,
                perPage,
                concurrency,
                cacheEnabled,
                description,
                0,
                0,
                0,
                0);
    }

    static CollectionProfile sampleLimitedForTests(
            String owner,
            String repository,
            int perPage,
            int concurrency,
            int maxIssues,
            int maxPullRequests,
            int maxCommentsPerItem,
            int maxReviewsPerPullRequest) {
        return new CollectionProfile(
                GitHubCollectionMode.SAMPLE_LIMITED,
                owner,
                repository,
                perPage,
                concurrency,
                false,
                "Amostragem limitada (testes)",
                maxIssues,
                maxPullRequests,
                maxCommentsPerItem,
                maxReviewsPerPullRequest);
    }

    public GitHubCollectionMode getMode() {
        return mode;
    }

    public boolean isFullRepository() {
        return mode == GitHubCollectionMode.FULL_REPOSITORY;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxIssues() {
        return maxIssues;
    }

    public int getMaxPullRequests() {
        return maxPullRequests;
    }

    public int getMaxCommentsPerItem() {
        return maxCommentsPerItem;
    }

    public int getMaxReviewsPerPullRequest() {
        return maxReviewsPerPullRequest;
    }

    public String getRepositorySlug() {
        return owner + "/" + repository;
    }

    public String toHumanReadableString() {
        if (isFullRepository()) {
            return """
                    Modo: FULL_REPOSITORY (mineracao completa, sem amostragem)
                    Repositorio: %s
                    per_page: %d
                    Concorrencia: %d
                    Cache local: %s
                    Descricao: %s
                    """.formatted(
                    getRepositorySlug(),
                    perPage,
                    concurrency,
                    cacheEnabled ? "ativado (cache/github/)" : "desativado",
                    description);
        }
        return """
                Modo: SAMPLE_LIMITED (apenas testes)
                Repositorio: %s
                Limites: issues=%d, pullRequests=%d, comentarios/item=%d, reviews/PR=%d
                per_page: %d | Concorrencia: %d | Cache: %s
                """.formatted(
                getRepositorySlug(),
                maxIssues,
                maxPullRequests,
                maxCommentsPerItem,
                maxReviewsPerPullRequest,
                perPage,
                concurrency,
                cacheEnabled ? "ativado" : "desativado");
    }
}
