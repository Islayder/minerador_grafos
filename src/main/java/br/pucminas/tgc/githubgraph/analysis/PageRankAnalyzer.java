package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PageRank simplificado para grafos direcionados.
 */
public final class PageRankAnalyzer {

    private static final int DEFAULT_ITERATIONS = 20;
    private static final double DEFAULT_DAMPING = 0.85;

    public Map<Integer, Double> pageRank(AbstractGraph graph) {
        return pageRank(graph, DEFAULT_ITERATIONS, DEFAULT_DAMPING);
    }

    public Map<Integer, Double> pageRank(AbstractGraph graph, int iterations, double dampingFactor) {
        GraphAdjacencyIndex index = new GraphAdjacencyIndex(graph);
        int vertexCount = index.getVertexCount();
        if (vertexCount == 0) {
            return Map.of();
        }

        double[] ranks = new double[vertexCount];
        double initial = 1.0 / vertexCount;
        Arrays.fill(ranks, initial);

        for (int iteration = 0; iteration < iterations; iteration++) {
            double[] nextRanks = new double[vertexCount];
            double teleport = (1.0 - dampingFactor) / vertexCount;

            double danglingMass = 0.0;
            for (int vertex = 0; vertex < vertexCount; vertex++) {
                if (index.getOutDegree(vertex) == 0) {
                    danglingMass += ranks[vertex];
                }
            }
            double baseRank = teleport + dampingFactor * danglingMass / vertexCount;
            Arrays.fill(nextRanks, baseRank);

            for (int source = 0; source < vertexCount; source++) {
                int outDegree = index.getOutDegree(source);
                if (outDegree == 0) {
                    continue;
                }
                double share = dampingFactor * ranks[source] / outDegree;
                for (int successor : index.successors(source)) {
                    nextRanks[successor] += share;
                }
            }
            ranks = nextRanks;
        }

        return normalize(ranks);
    }

    private Map<Integer, Double> normalize(double[] ranks) {
        double sum = 0.0;
        for (double rank : ranks) {
            sum += rank;
        }
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (int vertex = 0; vertex < ranks.length; vertex++) {
            result.put(vertex, sum == 0.0 ? 0.0 : ranks[vertex] / sum);
        }
        return result;
    }
}
