package br.pucminas.tgc.githubgraph.graph;

import br.pucminas.tgc.githubgraph.export.GexfExporter;
import br.pucminas.tgc.githubgraph.graph.exception.InconsistentGraphOperationException;
import br.pucminas.tgc.githubgraph.graph.exception.InvalidVertexException;
import br.pucminas.tgc.githubgraph.graph.exception.LoopNotAllowedException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API comum para grafos simples e direcionados, com validações e armazenamento de pesos.
 */
public abstract class AbstractGraph {

    private static final double DEFAULT_VERTEX_WEIGHT = 0.0;
    private static final double DEFAULT_EDGE_WEIGHT = 1.0;

    private final int vertexCount;
    private final double[] vertexWeights;
    private final Map<Long, Double> edgeWeights;
    private int edgeCount;

    protected AbstractGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("O número de vértices não pode ser negativo.");
        }
        this.vertexCount = vertexCount;
        this.vertexWeights = new double[vertexCount];
        this.edgeWeights = new HashMap<>();
        this.edgeCount = 0;
    }

    protected abstract boolean internalHasEdge(int u, int v);

    protected abstract void internalAddEdge(int u, int v);

    protected abstract void internalRemoveEdge(int u, int v);

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public boolean hasEdge(int u, int v) {
        validateDistinctVertices(u, v);
        return internalHasEdge(u, v);
    }

    public void addEdge(int u, int v) {
        validateDistinctVertices(u, v);
        if (!internalHasEdge(u, v)) {
            internalAddEdge(u, v);
            edgeWeights.put(edgeKey(u, v), DEFAULT_EDGE_WEIGHT);
            edgeCount++;
        }
    }

    public void removeEdge(int u, int v) {
        validateDistinctVertices(u, v);
        if (internalHasEdge(u, v)) {
            internalRemoveEdge(u, v);
            edgeWeights.remove(edgeKey(u, v));
            edgeCount--;
        }
    }

    public boolean isSucessor(int u, int v) {
        return hasEdge(u, v);
    }

    public boolean isPredessor(int u, int v) {
        return hasEdge(u, v);
    }

    public boolean isDivergent(int u1, int v1, int u2, int v2) {
        validateEdgeEndpoints(u1, v1);
        validateEdgeEndpoints(u2, v2);
        return u1 == u2 && v1 != v2 && internalHasEdge(u1, v1) && internalHasEdge(u2, v2);
    }

    public boolean isConvergent(int u1, int v1, int u2, int v2) {
        validateEdgeEndpoints(u1, v1);
        validateEdgeEndpoints(u2, v2);
        return v1 == v2 && u1 != u2 && internalHasEdge(u1, v1) && internalHasEdge(u2, v2);
    }

    public boolean isIncident(int u, int v, int x) {
        validateEdgeEndpoints(u, v);
        validateVertex(x);
        return internalHasEdge(u, v) && (x == u || x == v);
    }

    public int getVertexInDegree(int u) {
        validateVertex(u);
        int degree = 0;
        for (int source = 0; source < vertexCount; source++) {
            if (internalHasEdge(source, u)) {
                degree++;
            }
        }
        return degree;
    }

    public int getVertexOutDegree(int u) {
        validateVertex(u);
        int degree = 0;
        for (int target = 0; target < vertexCount; target++) {
            if (internalHasEdge(u, target)) {
                degree++;
            }
        }
        return degree;
    }

    public void setVertexWeight(int v, double w) {
        validateVertex(v);
        vertexWeights[v] = w;
    }

    public double getVertexWeight(int v) {
        validateVertex(v);
        return vertexWeights[v];
    }

    public void setEdgeWeight(int u, int v, double w) {
        validateDistinctVertices(u, v);
        if (!internalHasEdge(u, v)) {
            throw new InconsistentGraphOperationException(
                    "Não é possível definir peso: aresta (" + u + " -> " + v + ") inexistente.");
        }
        edgeWeights.put(edgeKey(u, v), w);
    }

    public double getEdgeWeight(int u, int v) {
        validateDistinctVertices(u, v);
        if (!internalHasEdge(u, v)) {
            throw new InconsistentGraphOperationException(
                    "Não é possível obter peso: aresta (" + u + " -> " + v + ") inexistente.");
        }
        return edgeWeights.getOrDefault(edgeKey(u, v), DEFAULT_EDGE_WEIGHT);
    }

    public boolean isConnected() {
        if (vertexCount <= 1) {
            return true;
        }
        if (edgeCount == 0) {
            return false;
        }

        boolean[] visited = new boolean[vertexCount];
        traverseWeakly(0, visited);

        for (boolean seen : visited) {
            if (!seen) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmptyGraph() {
        return edgeCount == 0;
    }

    public boolean isCompleteGraph() {
        if (vertexCount <= 1) {
            return true;
        }
        return edgeCount == vertexCount * (vertexCount - 1);
    }

    public void exportToGEPHI(String path) {
        try {
            GexfExporter.export(this, path);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao exportar grafo para GEXF: " + path, e);
        }
    }

    protected void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new InvalidVertexException(vertex, vertexCount);
        }
    }

    protected void validateDistinctVertices(int u, int v) {
        validateVertex(u);
        validateVertex(v);
        if (u == v) {
            throw new LoopNotAllowedException(u);
        }
    }

    private void validateEdgeEndpoints(int u, int v) {
        validateVertex(u);
        validateVertex(v);
        if (u == v) {
            throw new LoopNotAllowedException(u);
        }
    }

    private static long edgeKey(int u, int v) {
        return ((long) u << 32) | (v & 0xffffffffL);
    }

    private void traverseWeakly(int start, boolean[] visited) {
        visited[start] = true;
        for (int other = 0; other < vertexCount; other++) {
            if (!visited[other] && (internalHasEdge(start, other) || internalHasEdge(other, start))) {
                traverseWeakly(other, visited);
            }
        }
    }
}
