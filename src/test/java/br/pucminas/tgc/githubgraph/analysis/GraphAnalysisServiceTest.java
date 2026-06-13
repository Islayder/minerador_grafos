package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.app.DemoDataFactory;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphAnalysisServiceTest {

    private final GraphAnalysisService analysisService = new GraphAnalysisService();

    @Test
    void analyzeShouldReturnFilledReport() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        GraphMetricsReport report = analysisService.analyze(graph);

        assertEquals(3, report.getVertexCount());
        assertEquals(2, report.getEdgeCount());
        assertEquals(3, report.getPageRank().size());
        assertEquals(3, report.getEigenvector().size());
        assertEquals(3, report.getCloseness().size());
        assertEquals(3, report.getBetweenness().size());
        assertFalse(report.getTotalDegrees().isEmpty());
    }

    @Test
    void analyzeBuildResultShouldIncludePullRequestOpenings() {
        GraphBuildResult buildResult = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        GraphMetricsReport report = analysisService.analyze(buildResult);

        assertFalse(report.getPullRequestsOpenedByUser().isEmpty());
        assertTrue(report.getPullRequestsOpenedByUser().containsKey("bob"));
    }

    @Test
    void formatReportShouldIncludeMainMetrics() {
        GraphBuildResult buildResult = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        GraphMetricsReport report = analysisService.analyze(buildResult);

        String text = analysisService.formatReport(report);

        assertTrue(text.contains("=== 3. PageRank ==="));
        assertTrue(text.contains("=== 4. Eigenvector Centrality ==="));
        assertTrue(text.contains("=== 7. Clustering Coefficient ==="));
        assertTrue(text.contains("=== 8. Assortatividade ==="));
        assertTrue(text.contains("=== 9. Comunidades / Modularidade ==="));
        assertTrue(text.contains("=== 10. Bridging Ties ==="));
        assertTrue(text.contains("=== 11. Aberturas de Pull Requests por usuário ==="));
    }

    @Test
    void formatReportShouldUseLoginsWhenMapperProvided() {
        GraphBuildResult buildResult = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        GraphMetricsReport report = analysisService.analyze(buildResult);

        String text = analysisService.formatReport(report, buildResult.getUserIndexMapper());

        assertTrue(text.contains("alice"));
        assertTrue(text.contains("bob"));
    }
}
