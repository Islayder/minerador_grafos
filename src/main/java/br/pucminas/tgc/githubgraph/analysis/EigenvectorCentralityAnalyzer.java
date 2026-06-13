package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralidade de autovetor por iteracao de potencias (influencia por arestas de entrada).
 */
public final class EigenvectorCentralityAnalyzer {

    private static final int MAX_ITERATIONS = 100;
    private static final double TOLERANCE = 1e-6;

    public Map<Integer, Double> eigenvectorCentrality(AbstractGraph graph) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        int vertexCount = index.getVertexCount();
        if (vertexCount == 0) {
            return Map.of();
        }

        double[] vector = new double[vertexCount];
        Arrays.fill(vector, 1.0 / Math.sqrt(vertexCount));

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double[] next = new double[vertexCount];
            for (int vertex = 0; vertex < vertexCount; vertex++) {
                double sum = 0.0;
                for (int predecessor : index.predecessors(vertex)) {
                    sum += vector[predecessor];
                }
                next[vertex] = sum;
            }
            normalize(next);
            if (converged(vector, next)) {
                vector = next;
                break;
            }
            vector = next;
        }

        return toMap(vector);
    }

    private static boolean converged(double[] previous, double[] current) {
        double delta = 0.0;
        for (int index = 0; index < previous.length; index++) {
            delta += Math.abs(current[index] - previous[index]);
        }
        return delta < TOLERANCE;
    }

    private static void normalize(double[] vector) {
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        if (norm <= 0.0) {
            Arrays.fill(vector, 0.0);
            if (vector.length > 0) {
                vector[0] = 1.0;
            }
            return;
        }
        double scale = 1.0 / Math.sqrt(norm);
        for (int index = 0; index < vector.length; index++) {
            vector[index] *= scale;
        }
    }

    private static Map<Integer, Double> toMap(double[] vector) {
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (int vertex = 0; vertex < vector.length; vertex++) {
            result.put(vertex, vector[vertex]);
        }
        return result;
    }
}
