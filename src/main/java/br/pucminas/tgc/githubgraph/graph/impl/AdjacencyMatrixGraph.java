package br.pucminas.tgc.githubgraph.graph.impl;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

/**
 * Grafo simples e direcionado representado por matriz de adjacência.
 */
public class AdjacencyMatrixGraph extends AbstractGraph {

    private final boolean[][] adjacency;

    public AdjacencyMatrixGraph(int vertexCount) {
        super(vertexCount);
        this.adjacency = new boolean[vertexCount][vertexCount];
    }

    @Override
    protected boolean internalHasEdge(int u, int v) {
        return adjacency[u][v];
    }

    @Override
    protected void internalAddEdge(int u, int v) {
        adjacency[u][v] = true;
    }

    @Override
    protected void internalRemoveEdge(int u, int v) {
        adjacency[u][v] = false;
    }
}
