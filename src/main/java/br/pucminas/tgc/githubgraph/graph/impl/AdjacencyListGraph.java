package br.pucminas.tgc.githubgraph.graph.impl;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Grafo simples e direcionado representado por lista de adjacência.
 */
public class AdjacencyListGraph extends AbstractGraph {

    private final List<Set<Integer>> adjacency;
    private final int[] inDegrees;
    private final int[] outDegrees;

    public AdjacencyListGraph(int vertexCount) {
        super(vertexCount);
        this.adjacency = new ArrayList<>(vertexCount);
        this.inDegrees = new int[vertexCount];
        this.outDegrees = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new HashSet<>());
        }
    }

    public int[] getOutgoingTargets(int source) {
        validateVertex(source);
        Set<Integer> targets = adjacency.get(source);
        return targets.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int getVertexInDegree(int u) {
        validateVertex(u);
        return inDegrees[u];
    }

    @Override
    public int getVertexOutDegree(int u) {
        validateVertex(u);
        return outDegrees[u];
    }

    @Override
    protected boolean internalHasEdge(int u, int v) {
        return adjacency.get(u).contains(v);
    }

    @Override
    protected void internalAddEdge(int u, int v) {
        if (adjacency.get(u).add(v)) {
            outDegrees[u]++;
            inDegrees[v]++;
        }
    }

    @Override
    protected void internalRemoveEdge(int u, int v) {
        if (adjacency.get(u).remove(v)) {
            outDegrees[u]--;
            inDegrees[v]--;
        }
    }
}
