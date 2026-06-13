package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.app.DemoDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphSummaryServiceTest {

    private GraphBuildResult buildResult;
    private GraphSummaryService summaryService;

    @BeforeEach
    void setUp() {
        buildResult = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        summaryService = new GraphSummaryService();
    }

    @Test
    void summarizeShouldIncludeGraphNamesAndCounts() {
        String summary = summaryService.summarize(buildResult);

        assertTrue(summary.contains("Grafo de comentários"));
        assertTrue(summary.contains("Grafo de fechamento de issues"));
        assertTrue(summary.contains("Grafo de pull requests"));
        assertTrue(summary.contains("Grafo integrado ponderado"));
        assertTrue(summary.contains("Usuários (vértices): 6"));
        assertTrue(summary.contains("Arestas:"));
    }

    @Test
    void summarizeTopEdgesShouldShowIntegratedWeights() {
        String topEdges = summaryService.summarizeTopEdges(buildResult, 5);

        assertTrue(topEdges.contains("Principais arestas do grafo integrado"));
        assertTrue(topEdges.contains("peso"));
        assertTrue(topEdges.contains("alice"));
        assertTrue(topEdges.contains("bob"));
    }

    @Test
    void summarizeGraphShouldReportEmptyFlag() {
        String graphSummary = summaryService.summarizeGraph("Grafo integrado", buildResult.getIntegratedGraph());

        assertTrue(graphSummary.contains("Vértices: 6"));
        assertTrue(graphSummary.contains("Vazio: false"));
    }
}
