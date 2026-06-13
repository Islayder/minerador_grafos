package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetweennessCentralityAnalyzerTest {

    private final BetweennessCentralityAnalyzer analyzer = new BetweennessCentralityAnalyzer();

    @Test
    void intermediateVertexInSimplePathShouldHaveHigherBetweenness() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        Map<Integer, Double> betweenness = analyzer.betweenness(graph);

        assertTrue(betweenness.get(1) > betweenness.get(0));
        assertTrue(betweenness.get(1) > betweenness.get(2));
    }

    @Test
    void simpleCycleShouldNotBreak() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        Map<Integer, Double> betweenness = analyzer.betweenness(graph);

        assertEquals(3, betweenness.size());
    }

    @Test
    void disconnectedGraphShouldWork() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        Map<Integer, Double> first = analyzer.betweenness(graph);
        Map<Integer, Double> second = analyzer.betweenness(graph);

        assertEquals(first, second);
    }
}
