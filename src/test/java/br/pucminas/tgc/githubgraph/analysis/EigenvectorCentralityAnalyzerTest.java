package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EigenvectorCentralityAnalyzerTest {

    private final EigenvectorCentralityAnalyzer analyzer = new EigenvectorCentralityAnalyzer();

    @Test
    void inStarShouldRankCenterHighest() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(1, 0);
        graph.addEdge(2, 0);
        graph.addEdge(3, 0);

        Map<Integer, Double> scores = analyzer.eigenvectorCentrality(graph);

        assertTrue(scores.get(0) > scores.get(1));
        assertTrue(scores.get(0) > scores.get(2));
        assertTrue(scores.get(0) > scores.get(3));
    }

    @Test
    void vectorShouldBeNormalized() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        Map<Integer, Double> scores = analyzer.eigenvectorCentrality(graph);
        double norm = scores.values().stream().mapToDouble(value -> value * value).sum();

        assertEquals(1.0, norm, 1e-5);
    }

    @Test
    void emptyGraphShouldNotBreak() {
        AdjacencyListGraph graph = new AdjacencyListGraph(0);
        assertTrue(analyzer.eigenvectorCentrality(graph).isEmpty());
    }

    @Test
    void graphWithoutEdgesShouldBeDeterministic() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);

        Map<Integer, Double> first = analyzer.eigenvectorCentrality(graph);
        Map<Integer, Double> second = analyzer.eigenvectorCentrality(graph);

        assertEquals(first, second);
    }
}
