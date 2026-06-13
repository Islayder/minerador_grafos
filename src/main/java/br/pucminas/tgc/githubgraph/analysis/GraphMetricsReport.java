package br.pucminas.tgc.githubgraph.analysis;

import java.util.List;
import java.util.Map;

/**
 * Resultado estruturado das métricas calculadas sobre um grafo.
 */
public final class GraphMetricsReport {

    private final int vertexCount;
    private final int edgeCount;
    private final double density;
    private final Map<Integer, Integer> inDegrees;
    private final Map<Integer, Integer> outDegrees;
    private final Map<Integer, Integer> totalDegrees;
    private final Map<Integer, Double> pageRank;
    private final Map<Integer, Double> eigenvector;
    private final Map<Integer, Double> closeness;
    private final Map<Integer, Double> betweenness;
    private final double averageClusteringCoefficient;
    private final double degreeAssortativity;
    private final CommunityReport communityReport;
    private final List<BridgingTie> bridgingTies;
    private final Map<String, Integer> pullRequestsOpenedByUser;
    private final boolean heavyMetricsSkipped;
    private final Map<String, Long> metricTimingsMillis;

    public GraphMetricsReport(
            int vertexCount,
            int edgeCount,
            double density,
            Map<Integer, Integer> inDegrees,
            Map<Integer, Integer> outDegrees,
            Map<Integer, Integer> totalDegrees,
            Map<Integer, Double> pageRank,
            Map<Integer, Double> eigenvector,
            Map<Integer, Double> closeness,
            Map<Integer, Double> betweenness,
            double averageClusteringCoefficient,
            double degreeAssortativity,
            CommunityReport communityReport,
            List<BridgingTie> bridgingTies,
            Map<String, Integer> pullRequestsOpenedByUser,
            boolean heavyMetricsSkipped,
            Map<String, Long> metricTimingsMillis) {
        this.vertexCount = vertexCount;
        this.edgeCount = edgeCount;
        this.density = density;
        this.inDegrees = Map.copyOf(inDegrees);
        this.outDegrees = Map.copyOf(outDegrees);
        this.totalDegrees = Map.copyOf(totalDegrees);
        this.pageRank = Map.copyOf(pageRank);
        this.eigenvector = Map.copyOf(eigenvector);
        this.closeness = Map.copyOf(closeness);
        this.betweenness = Map.copyOf(betweenness);
        this.averageClusteringCoefficient = averageClusteringCoefficient;
        this.degreeAssortativity = degreeAssortativity;
        this.communityReport = communityReport;
        this.bridgingTies = List.copyOf(bridgingTies);
        this.pullRequestsOpenedByUser = Map.copyOf(pullRequestsOpenedByUser);
        this.heavyMetricsSkipped = heavyMetricsSkipped;
        this.metricTimingsMillis = Map.copyOf(metricTimingsMillis);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public double getDensity() {
        return density;
    }

    public Map<Integer, Integer> getInDegrees() {
        return inDegrees;
    }

    public Map<Integer, Integer> getOutDegrees() {
        return outDegrees;
    }

    public Map<Integer, Integer> getTotalDegrees() {
        return totalDegrees;
    }

    public Map<Integer, Double> getPageRank() {
        return pageRank;
    }

    public Map<Integer, Double> getEigenvector() {
        return eigenvector;
    }

    public Map<Integer, Double> getCloseness() {
        return closeness;
    }

    public Map<Integer, Double> getBetweenness() {
        return betweenness;
    }

    public double getAverageClusteringCoefficient() {
        return averageClusteringCoefficient;
    }

    public double getDegreeAssortativity() {
        return degreeAssortativity;
    }

    public CommunityReport getCommunityReport() {
        return communityReport;
    }

    public List<BridgingTie> getBridgingTies() {
        return bridgingTies;
    }

    public Map<String, Integer> getPullRequestsOpenedByUser() {
        return pullRequestsOpenedByUser;
    }

    public boolean isHeavyMetricsSkipped() {
        return heavyMetricsSkipped;
    }

    public Map<String, Long> getMetricTimingsMillis() {
        return metricTimingsMillis;
    }
}
