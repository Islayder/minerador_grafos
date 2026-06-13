package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DegreeMetricsTest {

    private final DegreeMetrics metrics = new DegreeMetrics();

    @Test
    void shouldComputeInOutAndTotalDegreesInDirectedGraph() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(2, 3);

        assertEquals(0, metrics.inDegree(graph, 0));
        assertEquals(2, metrics.outDegree(graph, 0));
        assertEquals(2, metrics.totalDegree(graph, 0));

        assertEquals(1, metrics.inDegree(graph, 1));
        assertEquals(0, metrics.outDegree(graph, 1));
        assertEquals(1, metrics.totalDegree(graph, 1));

        assertEquals(1, metrics.inDegree(graph, 2));
        assertEquals(1, metrics.outDegree(graph, 2));
        assertEquals(2, metrics.totalDegree(graph, 2));

        assertEquals(1, metrics.allInDegrees(graph).get(3));
        assertEquals(0, metrics.allOutDegrees(graph).get(3));
        assertEquals(1, metrics.allTotalDegrees(graph).get(3));
    }
}
