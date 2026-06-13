package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClusteringCoefficientAnalyzerTest {

    private final ClusteringCoefficientAnalyzer analyzer = new ClusteringCoefficientAnalyzer();

    @Test
    void completeTriangleShouldReturnOne() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        assertEquals(1.0, analyzer.localClusteringCoefficient(graph, 0), 1e-9);
        assertEquals(1.0, analyzer.averageClusteringCoefficient(graph), 1e-9);
    }

    @Test
    void simplePathShouldReturnZero() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        assertEquals(0.0, analyzer.averageClusteringCoefficient(graph), 1e-9);
    }

    @Test
    void isolatedVertexShouldReturnZero() {
        AdjacencyListGraph graph = new AdjacencyListGraph(1);
        assertEquals(0.0, analyzer.localClusteringCoefficient(graph, 0), 1e-9);
    }
}
