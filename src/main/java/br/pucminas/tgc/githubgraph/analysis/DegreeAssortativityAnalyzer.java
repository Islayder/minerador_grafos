package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

/**
 * Assortatividade por grau total fraco (Pearson entre graus dos endpoints de cada aresta).
 */
public final class DegreeAssortativityAnalyzer {

    public double degreeAssortativity(AbstractGraph graph) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        int vertexCount = index.getVertexCount();
        if (vertexCount == 0) {
            return 0.0;
        }

        double sumX = 0.0;
        double sumY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;
        double sumXY = 0.0;
        int edgeCount = 0;

        for (int source = 0; source < vertexCount; source++) {
            int sourceDegree = index.getTotalDegree(source);
            for (int target : index.successors(source)) {
                int targetDegree = index.getTotalDegree(target);
                sumX += sourceDegree;
                sumY += targetDegree;
                sumX2 += (double) sourceDegree * sourceDegree;
                sumY2 += (double) targetDegree * targetDegree;
                sumXY += (double) sourceDegree * targetDegree;
                edgeCount++;
            }
        }

        if (edgeCount == 0) {
            return 0.0;
        }

        double meanX = sumX / edgeCount;
        double meanY = sumY / edgeCount;
        double varianceX = sumX2 / edgeCount - meanX * meanX;
        double varianceY = sumY2 / edgeCount - meanY * meanY;
        if (varianceX <= 0.0 || varianceY <= 0.0) {
            return 0.0;
        }
        double covariance = sumXY / edgeCount - meanX * meanY;
        return covariance / Math.sqrt(varianceX * varianceY);
    }
}
