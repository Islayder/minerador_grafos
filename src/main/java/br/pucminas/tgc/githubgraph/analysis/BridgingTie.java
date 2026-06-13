package br.pucminas.tgc.githubgraph.analysis;

/**
 * Aresta que conecta comunidades distintas no grafo integrado.
 */
public final class BridgingTie {

    private final int sourceVertex;
    private final int targetVertex;
    private final String sourceLogin;
    private final String targetLogin;
    private final int sourceCommunity;
    private final int targetCommunity;
    private final double edgeWeight;
    private final double bridgingScore;

    public BridgingTie(
            int sourceVertex,
            int targetVertex,
            String sourceLogin,
            String targetLogin,
            int sourceCommunity,
            int targetCommunity,
            double edgeWeight,
            double bridgingScore) {
        this.sourceVertex = sourceVertex;
        this.targetVertex = targetVertex;
        this.sourceLogin = sourceLogin;
        this.targetLogin = targetLogin;
        this.sourceCommunity = sourceCommunity;
        this.targetCommunity = targetCommunity;
        this.edgeWeight = edgeWeight;
        this.bridgingScore = bridgingScore;
    }

    public int getSourceVertex() {
        return sourceVertex;
    }

    public int getTargetVertex() {
        return targetVertex;
    }

    public String getSourceLogin() {
        return sourceLogin;
    }

    public String getTargetLogin() {
        return targetLogin;
    }

    public int getSourceCommunity() {
        return sourceCommunity;
    }

    public int getTargetCommunity() {
        return targetCommunity;
    }

    public double getEdgeWeight() {
        return edgeWeight;
    }

    public double getBridgingScore() {
        return bridgingScore;
    }
}
