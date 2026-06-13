package br.pucminas.tgc.githubgraph.github;

/**
 * Modo de coleta real do GitHub.
 */
public enum GitHubCollectionMode {
    /** Minera o repositorio inteiro com paginacao ate o fim (fluxo principal). */
    FULL_REPOSITORY,
    /** Amostragem limitada — apenas para testes automatizados offline. */
    SAMPLE_LIMITED;

    public static GitHubCollectionMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return FULL_REPOSITORY;
        }
        return GitHubCollectionMode.valueOf(value.trim().toUpperCase());
    }
}
