package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Utilitários de travessia em grafos direcionados para métricas de centralidade.
 */
final class DirectedGraphTraversal {

    private DirectedGraphTraversal() {
    }

    static Map<Integer, Integer> bfsDistances(AbstractGraph graph, int source) {
        return bfsDistances(new GraphAdjacencyIndex(graph), source);
    }

    static Map<Integer, Integer> bfsDistances(GraphAdjacencyIndex index, int source) {
        int vertexCount = index.getVertexCount();
        Map<Integer, Integer> distances = new HashMap<>();
        boolean[] visited = new boolean[vertexCount];
        Queue<Integer> queue = new ArrayDeque<>();

        visited[source] = true;
        queue.add(source);
        distances.put(source, 0);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDistance = distances.get(current);
            for (int target : index.successors(current)) {
                if (!visited[target]) {
                    visited[target] = true;
                    distances.put(target, currentDistance + 1);
                    queue.add(target);
                }
            }
        }
        return distances;
    }

    static List<Integer> shortestPath(AbstractGraph graph, int source, int target) {
        return shortestPath(new GraphAdjacencyIndex(graph), source, target);
    }

    static List<Integer> shortestPath(GraphAdjacencyIndex index, int source, int target) {
        if (source == target) {
            return List.of(source);
        }

        int vertexCount = index.getVertexCount();
        int[] parent = new int[vertexCount];
        Arrays.fill(parent, -1);
        boolean[] visited = new boolean[vertexCount];
        Queue<Integer> queue = new ArrayDeque<>();

        visited[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int next : index.successors(current)) {
                if (visited[next]) {
                    continue;
                }
                visited[next] = true;
                parent[next] = current;
                if (next == target) {
                    return reconstructPath(parent, target);
                }
                queue.add(next);
            }
        }
        return List.of();
    }

    private static List<Integer> reconstructPath(int[] parent, int target) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        while (current != -1) {
            path.add(0, current);
            current = parent[current];
        }
        return path;
    }
}
