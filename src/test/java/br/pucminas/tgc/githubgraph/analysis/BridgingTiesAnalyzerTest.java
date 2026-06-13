package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgingTiesAnalyzerTest {

    private final CommunityDetectionService communityDetectionService = new CommunityDetectionService();
    private final BridgingTiesAnalyzer analyzer = new BridgingTiesAnalyzer();

    @Test
    void bridgeEdgeBetweenCommunitiesShouldBeDetected() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);
        graph.addEdge(1, 2);
        graph.setEdgeWeight(1, 2, 4.0);

        CommunityReport communities = new CommunityReport(
                2,
                2,
                50.0,
                0.2,
                Map.of(0, 10, 1, 10, 2, 20, 3, 20),
                List.of());
        List<BridgingTie> ties = analyzer.findTopBridgingTies(graph, communities, null, 10);

        assertFalse(ties.isEmpty());
        assertTrue(ties.stream().anyMatch(tie -> tie.getSourceVertex() == 1 && tie.getTargetVertex() == 2));
    }

    @Test
    void singleCommunityShouldReturnEmptyList() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        CommunityReport communities = communityDetectionService.detect(graph);
        List<BridgingTie> ties = analyzer.findTopBridgingTies(graph, communities, null, 10);

        assertTrue(ties.isEmpty());
    }

    @Test
    void orderingShouldBeDeterministic() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);
        graph.addEdge(1, 2);

        CommunityReport communities = communityDetectionService.detect(graph);
        List<BridgingTie> first = analyzer.findTopBridgingTies(graph, communities, null, 5);
        List<BridgingTie> second = analyzer.findTopBridgingTies(graph, communities, null, 5);

        assertEquals(first, second);
    }
}
