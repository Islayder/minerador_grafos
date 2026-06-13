package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Indice de sucessores para travessias sem varrer todos os vertices.
 */
public final class GraphAdjacencyIndex {

    private final int vertexCount;
    private final int[][] outgoing;
    private final int[] outDegrees;

    public GraphAdjacencyIndex(AbstractGraph graph) {
        vertexCount = graph.getVertexCount();
        List<List<Integer>> buckets = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            buckets.add(new ArrayList<>());
        }

        if (graph instanceof AdjacencyListGraph listGraph) {
            for (int source = 0; source < vertexCount; source++) {
                for (int target : listGraph.getOutgoingTargets(source)) {
                    buckets.get(source).add(target);
                }
            }
        } else {
            for (int source = 0; source < vertexCount; source++) {
                for (int target = 0; target < vertexCount; target++) {
                    if (source != target && graph.hasEdge(source, target)) {
                        buckets.get(source).add(target);
                    }
                }
            }
        }

        outgoing = new int[vertexCount][];
        outDegrees = new int[vertexCount];
        for (int source = 0; source < vertexCount; source++) {
            List<Integer> targets = buckets.get(source);
            outDegrees[source] = targets.size();
            outgoing[source] = targets.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getOutDegree(int vertex) {
        return outDegrees[vertex];
    }

    public int[] successors(int vertex) {
        return outgoing[vertex];
    }
}
