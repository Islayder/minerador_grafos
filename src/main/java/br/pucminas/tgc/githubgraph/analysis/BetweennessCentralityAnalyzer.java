package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Betweenness centrality por algoritmo de Brandes em grafo direcionado nao ponderado.
 */
public final class BetweennessCentralityAnalyzer {

    public Map<Integer, Double> betweenness(AbstractGraph graph) {
        GraphAdjacencyIndex index = new GraphAdjacencyIndex(graph);
        int vertexCount = index.getVertexCount();
        double[] scores = new double[vertexCount];

        for (int source = 0; source < vertexCount; source++) {
            runBrandesFromSource(index, source, scores);
        }

        Map<Integer, Double> result = new LinkedHashMap<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            result.put(vertex, scores[vertex]);
        }
        return result;
    }

    private static void runBrandesFromSource(GraphAdjacencyIndex index, int source, double[] scores) {
        int vertexCount = index.getVertexCount();
        int[] stack = new int[vertexCount];
        int stackSize = 0;
        List<Integer>[] predecessors = new List[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            predecessors[vertex] = new ArrayList<>();
        }

        double[] sigma = new double[vertexCount];
        int[] distance = new int[vertexCount];
        Arrays.fill(distance, -1);
        sigma[source] = 1.0;
        distance[source] = 0;

        int[] queue = new int[vertexCount];
        int queueHead = 0;
        int queueTail = 0;
        queue[queueTail++] = source;

        while (queueHead < queueTail) {
            int vertex = queue[queueHead++];
            stack[stackSize++] = vertex;
            for (int successor : index.successors(vertex)) {
                if (distance[successor] < 0) {
                    distance[successor] = distance[vertex] + 1;
                    queue[queueTail++] = successor;
                }
                if (distance[successor] == distance[vertex] + 1) {
                    sigma[successor] += sigma[vertex];
                    predecessors[successor].add(vertex);
                }
            }
        }

        double[] delta = new double[vertexCount];
        while (stackSize > 0) {
            int vertex = stack[--stackSize];
            for (int predecessor : predecessors[vertex]) {
                if (sigma[vertex] > 0.0) {
                    delta[predecessor] += (sigma[predecessor] / sigma[vertex]) * (1.0 + delta[vertex]);
                }
            }
            if (vertex != source) {
                scores[vertex] += delta[vertex];
            }
        }
    }
}
