package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.UserIndexMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orquestra o cálculo e a formatação de métricas de grafos.
 */
public final class GraphAnalysisService {

    private static final int HEAVY_METRICS_VERTEX_THRESHOLD = 8_000;
    private static final int BRIDGING_TIES_LIMIT = 15;

    private final DegreeMetrics degreeMetrics = new DegreeMetrics();
    private final DensityAnalyzer densityAnalyzer = new DensityAnalyzer();
    private final PageRankAnalyzer pageRankAnalyzer = new PageRankAnalyzer();
    private final EigenvectorCentralityAnalyzer eigenvectorCentralityAnalyzer = new EigenvectorCentralityAnalyzer();
    private final ClosenessCentralityAnalyzer closenessCentralityAnalyzer = new ClosenessCentralityAnalyzer();
    private final BetweennessCentralityAnalyzer betweennessCentralityAnalyzer = new BetweennessCentralityAnalyzer();
    private final ClusteringCoefficientAnalyzer clusteringCoefficientAnalyzer = new ClusteringCoefficientAnalyzer();
    private final DegreeAssortativityAnalyzer degreeAssortativityAnalyzer = new DegreeAssortativityAnalyzer();
    private final CommunityDetectionService communityDetectionService = new CommunityDetectionService();
    private final BridgingTiesAnalyzer bridgingTiesAnalyzer = new BridgingTiesAnalyzer();

    public GraphMetricsReport analyze(AbstractGraph graph) {
        return analyze(graph, Map.of());
    }

    public GraphMetricsReport analyze(GraphBuildResult buildResult) {
        return analyze(
                buildResult.getIntegratedGraph(),
                buildResult.getPullRequestsOpenedByUser(),
                buildResult.getUserIndexMapper());
    }

    public GraphMetricsReport analyze(AbstractGraph graph, Map<String, Integer> pullRequestsOpenedByUser) {
        return analyze(graph, pullRequestsOpenedByUser, null);
    }

    public GraphMetricsReport analyze(
            AbstractGraph graph,
            Map<String, Integer> pullRequestsOpenedByUser,
            UserIndexMapper mapper) {
        Map<String, Long> timings = new LinkedHashMap<>();
        boolean skipHeavy = graph.getVertexCount() > HEAVY_METRICS_VERTEX_THRESHOLD;

        long start = System.nanoTime();
        Map<Integer, Integer> inDegrees = degreeMetrics.allInDegrees(graph);
        Map<Integer, Integer> outDegrees = degreeMetrics.allOutDegrees(graph);
        Map<Integer, Integer> totalDegrees = degreeMetrics.allTotalDegrees(graph);
        timings.put("grau", elapsedMillis(start));

        start = System.nanoTime();
        double density = densityAnalyzer.density(graph);
        timings.put("densidade", elapsedMillis(start));

        start = System.nanoTime();
        Map<Integer, Double> pageRank = pageRankAnalyzer.pageRank(graph);
        timings.put("pageRank", elapsedMillis(start));

        start = System.nanoTime();
        Map<Integer, Double> eigenvector = skipHeavy
                ? emptyScores(graph.getVertexCount())
                : eigenvectorCentralityAnalyzer.eigenvectorCentrality(graph);
        timings.put("eigenvector", elapsedMillis(start));

        start = System.nanoTime();
        Map<Integer, Double> closeness = skipHeavy
                ? emptyScores(graph.getVertexCount())
                : closenessCentralityAnalyzer.closeness(graph);
        timings.put("closeness", elapsedMillis(start));

        start = System.nanoTime();
        Map<Integer, Double> betweenness = skipHeavy
                ? emptyScores(graph.getVertexCount())
                : betweennessCentralityAnalyzer.betweenness(graph);
        timings.put("betweenness", elapsedMillis(start));

        start = System.nanoTime();
        double averageClustering = clusteringCoefficientAnalyzer.averageClusteringCoefficient(graph);
        timings.put("clustering", elapsedMillis(start));

        start = System.nanoTime();
        double assortativity = degreeAssortativityAnalyzer.degreeAssortativity(graph);
        timings.put("assortatividade", elapsedMillis(start));

        start = System.nanoTime();
        CommunityReport communityReport = skipHeavy
                ? new CommunityReport(0, 0, 0.0, 0.0, Map.of(), List.of())
                : communityDetectionService.detect(graph, mapper);
        timings.put("comunidades", elapsedMillis(start));

        start = System.nanoTime();
        List<BridgingTie> bridgingTies = skipHeavy
                ? List.of()
                : bridgingTiesAnalyzer.findTopBridgingTies(graph, communityReport, mapper, BRIDGING_TIES_LIMIT);
        timings.put("bridgingTies", elapsedMillis(start));

        return new GraphMetricsReport(
                graph.getVertexCount(),
                graph.getEdgeCount(),
                density,
                inDegrees,
                outDegrees,
                totalDegrees,
                pageRank,
                eigenvector,
                closeness,
                betweenness,
                averageClustering,
                assortativity,
                communityReport,
                bridgingTies,
                pullRequestsOpenedByUser,
                skipHeavy,
                timings);
    }

    public String formatReport(GraphMetricsReport report) {
        return formatReport(report, null);
    }

    public String formatReport(GraphMetricsReport report, UserIndexMapper mapper) {
        StringBuilder text = new StringBuilder();
        text.append("Relatório de métricas do grafo integrado\n");
        text.append("Vértices: ").append(report.getVertexCount()).append('\n');
        text.append("Arestas: ").append(report.getEdgeCount()).append('\n');
        if (report.isHeavyMetricsSkipped()) {
            text.append("Nota: métricas pesadas omitidas (grafo com mais de ")
                    .append(HEAVY_METRICS_VERTEX_THRESHOLD)
                    .append(" vértices).\n");
        }
        appendTimings(text, report.getMetricTimingsMillis());

        text.append("\n=== 1. Grau ===\n");
        appendRanking(text, "Ranking por grau total", report.getTotalDegrees(), mapper, 10);

        text.append("\n=== 2. Densidade ===\n");
        text.append("Densidade: ").append(String.format("%.4f", report.getDensity())).append('\n');

        text.append("\n=== 3. PageRank ===\n");
        appendRanking(text, "Ranking por PageRank", report.getPageRank(), mapper, 10);

        text.append("\n=== 4. Eigenvector Centrality ===\n");
        text.append("Influência por arestas de entrada (power iteration).\n");
        appendRanking(text, "Ranking por eigenvector", report.getEigenvector(), mapper, 10);

        text.append("\n=== 5. Closeness Centrality ===\n");
        appendRanking(text, "Ranking por closeness", report.getCloseness(), mapper, 10);

        text.append("\n=== 6. Betweenness Centrality ===\n");
        text.append("Algoritmo de Brandes (grafo direcionado, não ponderado).\n");
        appendRanking(text, "Ranking por betweenness", report.getBetweenness(), mapper, 10);

        text.append("\n=== 7. Clustering Coefficient ===\n");
        text.append("Média local (vizinhança fraca; direção ignorada nesta métrica): ")
                .append(String.format("%.4f", report.getAverageClusteringCoefficient()))
                .append('\n');

        text.append("\n=== 8. Assortatividade ===\n");
        text.append("Assortatividade por grau total fraco (Pearson): ")
                .append(String.format("%.4f", report.getDegreeAssortativity()))
                .append('\n');

        text.append("\n=== 9. Comunidades / Modularidade ===\n");
        appendCommunitySection(text, report.getCommunityReport());

        text.append("\n=== 10. Bridging Ties ===\n");
        appendBridgingSection(text, report.getBridgingTies());

        text.append("\n=== 11. Aberturas de Pull Requests por usuário ===\n");
        text.append("Abertura de PR é evento individual do autor (não gera aresta no grafo).\n");
        appendPullRequestOpenings(text, report.getPullRequestsOpenedByUser());

        return text.toString();
    }

    private void appendTimings(StringBuilder text, Map<String, Long> timings) {
        if (timings.isEmpty()) {
            return;
        }
        text.append("\nTempos de cálculo (ms):\n");
        for (Map.Entry<String, Long> entry : timings.entrySet()) {
            text.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
    }

    private void appendCommunitySection(StringBuilder text, CommunityReport report) {
        text.append("Detecção: label propagation determinístico (vizinhança fraca).\n");
        text.append("Comunidades: ").append(report.getCommunityCount()).append('\n');
        text.append("Maior comunidade: ").append(report.getLargestCommunitySize())
                .append(" vértices (")
                .append(String.format("%.2f", report.getLargestCommunityPercent()))
                .append("%)\n");
        text.append("Modularidade (aprox., projeção fraca): ")
                .append(String.format("%.4f", report.getModularityScore()))
                .append('\n');

        if (report.getTopCommunities().isEmpty()) {
            text.append("  (sem comunidades)\n");
            return;
        }

        for (CommunityReport.CommunitySummary summary : report.getTopCommunities()) {
            text.append("  Comunidade ").append(summary.communityId())
                    .append(" — tamanho ").append(summary.size())
                    .append(" — membros: ")
                    .append(String.join(", ", summary.topMemberLogins()))
                    .append('\n');
        }
    }

    private void appendBridgingSection(StringBuilder text, List<BridgingTie> ties) {
        text.append("Quantidade: ").append(ties.size()).append('\n');
        if (ties.isEmpty()) {
            text.append("  (nenhuma aresta entre comunidades distintas)\n");
            return;
        }
        for (BridgingTie tie : ties) {
            text.append("  ")
                    .append(tie.getSourceLogin())
                    .append(" -> ")
                    .append(tie.getTargetLogin())
                    .append(" | comunidades ")
                    .append(tie.getSourceCommunity())
                    .append(" / ")
                    .append(tie.getTargetCommunity())
                    .append(" | peso ")
                    .append(String.format("%.1f", tie.getEdgeWeight()))
                    .append(" | score ")
                    .append(String.format("%.2f", tie.getBridgingScore()))
                    .append('\n');
        }
    }

    private void appendPullRequestOpenings(StringBuilder text, Map<String, Integer> openings) {
        if (openings.isEmpty()) {
            text.append("  (nenhuma abertura de PR registrada)\n");
            return;
        }
        int total = openings.values().stream().mapToInt(Integer::intValue).sum();
        text.append("Total de PRs abertos: ").append(total).append('\n');
        openings.entrySet().stream()
                .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                .limit(15)
                .forEach(entry -> text.append("  ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append('\n'));
    }

    private void appendRanking(
            StringBuilder text,
            String title,
            Map<Integer, ? extends Number> values,
            UserIndexMapper mapper,
            int limit) {
        text.append(title).append('\n');
        List<Map.Entry<Integer, ? extends Number>> sorted = values.entrySet().stream()
                .sorted((left, right) -> Double.compare(
                        right.getValue().doubleValue(),
                        left.getValue().doubleValue()))
                .limit(limit)
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            text.append("  (sem dados)\n");
            return;
        }

        for (Map.Entry<Integer, ? extends Number> entry : sorted) {
            text.append("  ")
                    .append(formatVertexLabel(entry.getKey(), mapper))
                    .append(": ")
                    .append(String.format("%.4f", entry.getValue().doubleValue()))
                    .append('\n');
        }
    }

    private String formatVertexLabel(int vertex, UserIndexMapper mapper) {
        if (mapper == null) {
            return "v" + vertex;
        }
        return mapper.getUser(vertex).getLogin() + " (v" + vertex + ")";
    }

    private static Map<Integer, Double> emptyScores(int vertexCount) {
        Map<Integer, Double> scores = new LinkedHashMap<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            scores.put(vertex, 0.0);
        }
        return scores;
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
