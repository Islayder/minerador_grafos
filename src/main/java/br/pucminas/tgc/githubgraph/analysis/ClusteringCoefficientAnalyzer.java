package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Coeficiente de aglomeracao local/medio usando vizinhanca fraca (direcao ignorada).
 */
public final class ClusteringCoefficientAnalyzer {

    public double localClusteringCoefficient(AbstractGraph graph, int vertex) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        if (vertex < 0 || vertex >= index.getVertexCount()) {
            throw new IllegalArgumentException("Vertice invalido: " + vertex);
        }
        return localClusteringCoefficient(index, vertex);
    }

    public Map<Integer, Double> allLocalClusteringCoefficients(AbstractGraph graph) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        Map<Integer, Double> coefficients = new LinkedHashMap<>();
        for (int vertex = 0; vertex < index.getVertexCount(); vertex++) {
            coefficients.put(vertex, localClusteringCoefficient(index, vertex));
        }
        return coefficients;
    }

    public double averageClusteringCoefficient(AbstractGraph graph) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        int vertexCount = index.getVertexCount();
        if (vertexCount == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            sum += localClusteringCoefficient(index, vertex);
        }
        return sum / vertexCount;
    }

    private double localClusteringCoefficient(WeakStructuralIndex index, int vertex) {
        int[] neighbors = index.weakNeighbors(vertex);
        int degree = neighbors.length;
        if (degree < 2) {
            return 0.0;
        }

        int linksBetweenNeighbors = 0;
        for (int leftIndex = 0; leftIndex < neighbors.length; leftIndex++) {
            int leftNeighbor = neighbors[leftIndex];
            for (int rightIndex = leftIndex + 1; rightIndex < neighbors.length; rightIndex++) {
                int rightNeighbor = neighbors[rightIndex];
                if (areWeaklyConnected(index, leftNeighbor, rightNeighbor)) {
                    linksBetweenNeighbors++;
                }
            }
        }

        int possibleLinks = degree * (degree - 1) / 2;
        return (double) linksBetweenNeighbors / possibleLinks;
    }

    private static boolean areWeaklyConnected(WeakStructuralIndex index, int first, int second) {
        for (int neighbor : index.weakNeighbors(first)) {
            if (neighbor == second) {
                return true;
            }
        }
        return false;
    }
}
