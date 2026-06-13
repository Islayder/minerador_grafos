package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClosenessCentralityAnalyzerTest {

    private final ClosenessCentralityAnalyzer analyzer = new ClosenessCentralityAnalyzer();

    @Test
    void isolatedVertexShouldHaveZeroCloseness() {
        AdjacencyListGraph graph = new AdjacencyListGraph(2);
        graph.addEdge(0, 1);

        Map<Integer, Double> closeness = analyzer.closeness(graph);
        assertEquals(0.0, closeness.get(1));
    }

    @Test
    void centralVertexInSimplePathShouldHaveHigherCloseness() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        Map<Integer, Double> closeness = analyzer.closeness(graph);

        assertTrue(closeness.get(1) > closeness.get(0));
        assertTrue(closeness.get(1) > closeness.get(2));
    }
}
