package br.pucminas.tgc.githubgraph.analysis;

import java.util.List;
import java.util.Map;

/**
 * Resultado da deteccao de comunidades e modularidade aproximada (projecao fraca).
 */
public final class CommunityReport {

    public record CommunitySummary(int communityId, int size, List<String> topMemberLogins) {
    }

    private final int communityCount;
    private final int largestCommunitySize;
    private final double largestCommunityPercent;
    private final double modularityScore;
    private final Map<Integer, Integer> vertexCommunity;
    private final List<CommunitySummary> topCommunities;

    public CommunityReport(
            int communityCount,
            int largestCommunitySize,
            double largestCommunityPercent,
            double modularityScore,
            Map<Integer, Integer> vertexCommunity,
            List<CommunitySummary> topCommunities) {
        this.communityCount = communityCount;
        this.largestCommunitySize = largestCommunitySize;
        this.largestCommunityPercent = largestCommunityPercent;
        this.modularityScore = modularityScore;
        this.vertexCommunity = Map.copyOf(vertexCommunity);
        this.topCommunities = List.copyOf(topCommunities);
    }

    public int getCommunityCount() {
        return communityCount;
    }

    public int getLargestCommunitySize() {
        return largestCommunitySize;
    }

    public double getLargestCommunityPercent() {
        return largestCommunityPercent;
    }

    public double getModularityScore() {
        return modularityScore;
    }

    public Map<Integer, Integer> getVertexCommunity() {
        return vertexCommunity;
    }

    public List<CommunitySummary> getTopCommunities() {
        return topCommunities;
    }

    public int communityOf(int vertex) {
        return vertexCommunity.getOrDefault(vertex, vertex);
    }
}
