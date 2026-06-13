package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageRankAnalyzerTest {

    private final PageRankAnalyzer analyzer = new PageRankAnalyzer();

    @Test
    void shouldReturnPageRankForAllVerticesWithSumApproximatelyOne() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        Map<Integer, Double> ranks = analyzer.pageRank(graph, 50, 0.85);

        assertEquals(3, ranks.size());
        double sum = ranks.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 0.01);
    }

    @Test
    void hubVertexReceivingManyEdgesShouldHaveHigherPageRank() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(1, 0);
        graph.addEdge(2, 0);
        graph.addEdge(3, 0);

        Map<Integer, Double> ranks = analyzer.pageRank(graph, 80, 0.85);

        assertTrue(ranks.get(0) > ranks.get(1));
        assertTrue(ranks.get(0) > ranks.get(2));
        assertTrue(ranks.get(0) > ranks.get(3));
    }
}
