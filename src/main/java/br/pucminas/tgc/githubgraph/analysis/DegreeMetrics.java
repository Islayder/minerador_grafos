package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calcula graus de vértices em grafos direcionados.
 */
public final class DegreeMetrics {

    public int inDegree(AbstractGraph graph, int vertex) {
        return graph.getVertexInDegree(vertex);
    }

    public int outDegree(AbstractGraph graph, int vertex) {
        return graph.getVertexOutDegree(vertex);
    }

    public int totalDegree(AbstractGraph graph, int vertex) {
        return inDegree(graph, vertex) + outDegree(graph, vertex);
    }

    public Map<Integer, Integer> allInDegrees(AbstractGraph graph) {
        return computeDegrees(graph, true, false);
    }

    public Map<Integer, Integer> allOutDegrees(AbstractGraph graph) {
        return computeDegrees(graph, false, true);
    }

    public Map<Integer, Integer> allTotalDegrees(AbstractGraph graph) {
        return computeDegrees(graph, true, true);
    }

    private Map<Integer, Integer> computeDegrees(AbstractGraph graph, boolean includeIn, boolean includeOut) {
        Map<Integer, Integer> degrees = new LinkedHashMap<>();
        int vertexCount = graph.getVertexCount();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            int value = 0;
            if (includeIn) {
                value += graph.getVertexInDegree(vertex);
            }
            if (includeOut) {
                value += graph.getVertexOutDegree(vertex);
            }
            degrees.put(vertex, value);
        }
        return degrees;
    }
}
