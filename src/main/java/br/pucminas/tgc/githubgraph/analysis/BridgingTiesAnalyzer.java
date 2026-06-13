package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.service.UserIndexMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Identifica arestas entre comunidades distintas (bridging ties).
 */
public final class BridgingTiesAnalyzer {

    public List<BridgingTie> findTopBridgingTies(
            AbstractGraph graph,
            CommunityReport communityReport,
            UserIndexMapper mapper,
            int limit) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        List<BridgingTie> ties = new ArrayList<>();

        for (int source = 0; source < index.getVertexCount(); source++) {
            for (int target : index.successors(source)) {
                int sourceCommunity = communityReport.communityOf(source);
                int targetCommunity = communityReport.communityOf(target);
                if (sourceCommunity == targetCommunity) {
                    continue;
                }
                double weight = graph.getEdgeWeight(source, target);
                int sourceCommunitySize = communitySize(communityReport, sourceCommunity);
                int targetCommunitySize = communitySize(communityReport, targetCommunity);
                double score = 1.0
                        + Math.abs(index.getTotalDegree(source) - index.getTotalDegree(target))
                        + sourceCommunitySize
                        + targetCommunitySize;

                ties.add(new BridgingTie(
                        source,
                        target,
                        loginOf(mapper, source),
                        loginOf(mapper, target),
                        sourceCommunity,
                        targetCommunity,
                        weight,
                        score));
            }
        }

        ties.sort(Comparator.comparingDouble(BridgingTie::getBridgingScore).reversed()
                .thenComparingInt(BridgingTie::getSourceVertex)
                .thenComparingInt(BridgingTie::getTargetVertex));

        if (limit <= 0 || ties.size() <= limit) {
            return List.copyOf(ties);
        }
        return List.copyOf(ties.subList(0, limit));
    }

    private static int communitySize(CommunityReport report, int communityId) {
        int size = 0;
        for (int community : report.getVertexCommunity().values()) {
            if (community == communityId) {
                size++;
            }
        }
        return size;
    }

    private static String loginOf(UserIndexMapper mapper, int vertex) {
        if (mapper == null) {
            return "v" + vertex;
        }
        return mapper.getUser(vertex).getLogin();
    }
}
