package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DensityAnalyzerTest {

    private final DensityAnalyzer analyzer = new DensityAnalyzer();

    @Test
    void emptyGraphWithMoreThanOneVertexShouldHaveZeroDensity() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        assertEquals(0.0, analyzer.density(graph));
    }

    @Test
    void completeDirectedGraphShouldHaveDensityOne() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        for (int source = 0; source < 3; source++) {
            for (int target = 0; target < 3; target++) {
                if (source != target) {
                    graph.addEdge(source, target);
                }
            }
        }
        assertEquals(1.0, analyzer.density(graph));
    }

    @Test
    void partialGraphShouldHaveCorrectDensity() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertEquals(2.0 / 12.0, analyzer.density(graph), 0.0001);
    }
}
