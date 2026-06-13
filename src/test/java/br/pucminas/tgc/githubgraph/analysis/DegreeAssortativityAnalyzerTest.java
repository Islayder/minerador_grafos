package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DegreeAssortativityAnalyzerTest {

    private final DegreeAssortativityAnalyzer analyzer = new DegreeAssortativityAnalyzer();

    @Test
    void twoCliquesWithoutBridgeShouldBeMoreAssortativeThanWithBridge() {
        AdjacencyListGraph withoutBridge = new AdjacencyListGraph(4);
        withoutBridge.addEdge(0, 1);
        withoutBridge.addEdge(1, 0);
        withoutBridge.addEdge(2, 3);
        withoutBridge.addEdge(3, 2);

        AdjacencyListGraph withBridge = new AdjacencyListGraph(4);
        withBridge.addEdge(0, 1);
        withBridge.addEdge(1, 0);
        withBridge.addEdge(2, 3);
        withBridge.addEdge(3, 2);
        withBridge.addEdge(1, 2);

        assertTrue(analyzer.degreeAssortativity(withoutBridge)
                >= analyzer.degreeAssortativity(withBridge));
    }

    @Test
    void graphWithoutEdgesShouldReturnZero() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        assertEquals(0.0, analyzer.degreeAssortativity(graph), 1e-9);
    }

    @Test
    void uniformDegreeGraphShouldNotBreakWithZeroVariance() {
        AdjacencyListGraph graph = new AdjacencyListGraph(2);
        graph.addEdge(0, 1);

        assertEquals(0.0, analyzer.degreeAssortativity(graph), 1e-9);
    }

    @Test
    void resultShouldBeFinite() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        double value = analyzer.degreeAssortativity(graph);
        assertFalse(Double.isNaN(value));
        assertFalse(Double.isInfinite(value));
    }
}
