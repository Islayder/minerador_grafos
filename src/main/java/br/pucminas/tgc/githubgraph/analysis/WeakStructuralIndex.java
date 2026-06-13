package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vizinhanca fraca (predecessores ∪ sucessores) para metricas estruturais nao direcionadas.
 */
public final class WeakStructuralIndex {

    private final int vertexCount;
    private final int[][] weakNeighbors;
    private final int[][] predecessors;
    private final int[][] successors;
    private final int[] weakDegrees;
    private final int[] totalDegrees;

    public WeakStructuralIndex(AbstractGraph graph) {
        vertexCount = graph.getVertexCount();
        List<List<Integer>> weakBuckets = new ArrayList<>(vertexCount);
        List<List<Integer>> predBuckets = new ArrayList<>(vertexCount);
        List<List<Integer>> succBuckets = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            weakBuckets.add(new ArrayList<>());
            predBuckets.add(new ArrayList<>());
            succBuckets.add(new ArrayList<>());
        }

        if (graph instanceof AdjacencyListGraph listGraph) {
            for (int source = 0; source < vertexCount; source++) {
                for (int target : listGraph.getOutgoingTargets(source)) {
                    succBuckets.get(source).add(target);
                    predBuckets.get(target).add(source);
                    addWeakNeighbor(weakBuckets, source, target);
                    addWeakNeighbor(weakBuckets, target, source);
                }
            }
        } else {
            for (int source = 0; source < vertexCount; source++) {
                for (int target = 0; target < vertexCount; target++) {
                    if (source != target && graph.hasEdge(source, target)) {
                        succBuckets.get(source).add(target);
                        predBuckets.get(target).add(source);
                        addWeakNeighbor(weakBuckets, source, target);
                        addWeakNeighbor(weakBuckets, target, source);
                    }
                }
            }
        }

        weakNeighbors = toArray(weakBuckets);
        predecessors = toArray(predBuckets);
        successors = toArray(succBuckets);
        weakDegrees = new int[vertexCount];
        totalDegrees = new int[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            weakDegrees[vertex] = weakNeighbors[vertex].length;
            totalDegrees[vertex] = predecessors[vertex].length + successors[vertex].length;
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getWeakDegree(int vertex) {
        return weakDegrees[vertex];
    }

    public int getTotalDegree(int vertex) {
        return totalDegrees[vertex];
    }

    public int[] weakNeighbors(int vertex) {
        return weakNeighbors[vertex];
    }

    public int[] predecessors(int vertex) {
        return predecessors[vertex];
    }

    public int[] successors(int vertex) {
        return successors[vertex];
    }

    public int countUndirectedEdges() {
        int count = 0;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            for (int neighbor : successors[vertex]) {
                if (vertex < neighbor) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<int[]> undirectedEdges() {
        List<int[]> edges = new ArrayList<>();
        for (int source = 0; source < vertexCount; source++) {
            for (int target : successors[source]) {
                if (source < target) {
                    edges.add(new int[] {source, target});
                }
            }
        }
        return edges;
    }

    private static void addWeakNeighbor(List<List<Integer>> buckets, int vertex, int neighbor) {
        List<Integer> neighbors = buckets.get(vertex);
        for (int existing : neighbors) {
            if (existing == neighbor) {
                return;
            }
        }
        neighbors.add(neighbor);
    }

    private static int[][] toArray(List<List<Integer>> buckets) {
        int[][] array = new int[buckets.size()][];
        for (int index = 0; index < buckets.size(); index++) {
            List<Integer> values = buckets.get(index);
            array[index] = values.stream().mapToInt(Integer::intValue).toArray();
        }
        return array;
    }
}
