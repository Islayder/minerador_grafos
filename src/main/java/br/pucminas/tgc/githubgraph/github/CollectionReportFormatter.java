package br.pucminas.tgc.githubgraph.github;

/**
 * Textos de apoio para mineracao real (CLI e desktop).
 */
public final class CollectionReportFormatter {

    public static final String API_BOTTLENECK_MESSAGE =
            "A mineracao online depende da GitHub API, internet, token e rate limit. "
                    + "Nao e amostragem: o sistema pagina ate o fim. "
                    + "O processamento interno do grafo ocorre depois da coleta.";

    private CollectionReportFormatter() {
    }

    public static String formatPreCollection(CollectionProfile profile, boolean tokenPresent) {
        String tokenLine = tokenPresent
                ? "GITHUB_TOKEN: encontrado (variavel de ambiente ou .env)."
                : "GITHUB_TOKEN: nao encontrado — API publica com limites menores.";

        return """
                === Mineracao completa (GitHub API) ===
                %s
                %s
                %s
                """.formatted(profile.toHumanReadableString(), tokenLine, API_BOTTLENECK_MESSAGE);
    }

    public static String formatPostCollection(CollectionExecutionResult result) {
        String cacheLine = result.isCacheEnabled()
                ? "Requisicoes API: " + result.getApiRequests()
                        + " | Acertos de cache: " + result.getCacheHits()
                        + " (pasta cache/github/)"
                : "Cache desativado.";

        String performanceBlock = result.getCollectionStatistics() == null
                ? ""
                : System.lineSeparator() + result.getCollectionStatistics().formatPerformanceSummary(
                        result.getBuildPhaseMillis(),
                        result.getDurationMillis());

        return """
                Mineracao concluida.
                Tempo total: %.2f s (coleta: %.2f s, grafos: %.2f s)
                %s
                Usuarios (vertices): %d
                Interacoes coletadas: %d
                Arestas no grafo integrado: %d%s
                """.formatted(
                result.getDurationSeconds(),
                result.getCollectionPhaseMillis() / 1000.0,
                result.getBuildPhaseMillis() / 1000.0,
                cacheLine,
                result.getUserCount(),
                result.getInteractionCount(),
                result.getIntegratedEdgeCount(),
                performanceBlock);
    }
}
