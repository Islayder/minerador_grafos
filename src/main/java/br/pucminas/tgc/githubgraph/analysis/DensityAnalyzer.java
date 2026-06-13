package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

/**
 * Calcula a densidade de grafos direcionados simples.
 */
public final class DensityAnalyzer {

    public double density(AbstractGraph graph) {
        int vertexCount = graph.getVertexCount();
        if (vertexCount <= 1) {
            return 0.0;
        }
        double maxEdges = (double) vertexCount * (vertexCount - 1);
        return graph.getEdgeCount() / maxEdges;
    }
}
