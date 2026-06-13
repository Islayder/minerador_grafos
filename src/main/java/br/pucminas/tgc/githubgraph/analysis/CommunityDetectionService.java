package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.service.UserIndexMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Deteccao de comunidades por label propagation deterministica e modularidade em projecao fraca.
 */
public final class CommunityDetectionService {

    private static final int MAX_ITERATIONS = 50;
    private static final int TOP_COMMUNITIES = 5;
    private static final int TOP_MEMBERS_PER_COMMUNITY = 5;

    public CommunityReport detect(AbstractGraph graph) {
        return detect(graph, null);
    }

    public CommunityReport detect(AbstractGraph graph, UserIndexMapper mapper) {
        WeakStructuralIndex index = new WeakStructuralIndex(graph);
        int vertexCount = index.getVertexCount();
        if (vertexCount == 0) {
            return emptyReport();
        }

        int[] labels = new int[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            labels[vertex] = vertex;
        }

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            boolean changed = false;
            for (int vertex = 0; vertex < vertexCount; vertex++) {
                int newLabel = mostFrequentNeighborLabel(index, labels, vertex);
                if (newLabel != labels[vertex]) {
                    labels[vertex] = newLabel;
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        Map<Integer, Integer> vertexCommunity = new LinkedHashMap<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            vertexCommunity.put(vertex, labels[vertex]);
        }

        Map<Integer, List<Integer>> membersByCommunity = groupByCommunity(vertexCommunity);
        int communityCount = membersByCommunity.size();
        int largestSize = membersByCommunity.values().stream().mapToInt(List::size).max().orElse(0);
        double largestPercent = vertexCount == 0 ? 0.0 : (100.0 * largestSize / vertexCount);
        double modularity = modularity(index, labels);

        List<CommunityReport.CommunitySummary> topCommunities = membersByCommunity.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Integer, List<Integer>>>comparingInt(entry -> entry.getValue().size())
                        .reversed()
                        .thenComparingInt(Map.Entry::getKey))
                .limit(TOP_COMMUNITIES)
                .map(entry -> new CommunityReport.CommunitySummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        topMemberLogins(entry.getValue(), index, mapper)))
                .collect(Collectors.toList());

        return new CommunityReport(
                communityCount,
                largestSize,
                largestPercent,
                modularity,
                vertexCommunity,
                topCommunities);
    }

    private static CommunityReport emptyReport() {
        return new CommunityReport(0, 0, 0.0, 0.0, Map.of(), List.of());
    }

    private static int mostFrequentNeighborLabel(WeakStructuralIndex index, int[] labels, int vertex) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int neighbor : index.weakNeighbors(vertex)) {
            int label = labels[neighbor];
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }
        if (counts.isEmpty()) {
            return labels[vertex];
        }

        int bestLabel = labels[vertex];
        int bestCount = -1;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int label = entry.getKey();
            int count = entry.getValue();
            if (count > bestCount || (count == bestCount && label < bestLabel)) {
                bestCount = count;
                bestLabel = label;
            }
        }
        return bestLabel;
    }

    private static Map<Integer, List<Integer>> groupByCommunity(Map<Integer, Integer> vertexCommunity) {
        Map<Integer, List<Integer>> grouped = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : vertexCommunity.entrySet()) {
            grouped.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
        }
        return grouped;
    }

    private static List<String> topMemberLogins(List<Integer> members, WeakStructuralIndex index, UserIndexMapper mapper) {
        List<Integer> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparingInt((Integer vertex) -> index.getTotalDegree(vertex)).reversed()
                .thenComparingInt(vertex -> vertex));
        List<String> logins = new ArrayList<>();
        for (int vertex : sorted) {
            if (logins.size() >= TOP_MEMBERS_PER_COMMUNITY) {
                break;
            }
            if (mapper == null) {
                logins.add("v" + vertex);
            } else {
                logins.add(mapper.getUser(vertex).getLogin());
            }
        }
        return List.copyOf(logins);
    }

    private static double modularity(WeakStructuralIndex index, int[] labels) {
        List<int[]> edges = index.undirectedEdges();
        if (edges.isEmpty()) {
            return 0.0;
        }

        int vertexCount = index.getVertexCount();
        double twoM = 2.0 * edges.size();
        double[] strength = new double[vertexCount];
        for (int[] edge : edges) {
            strength[edge[0]]++;
            strength[edge[1]]++;
        }

        double modularity = 0.0;
        for (int[] edge : edges) {
            int first = edge[0];
            int second = edge[1];
            if (labels[first] == labels[second]) {
                modularity += 1.0 - (strength[first] * strength[second]) / twoM;
            }
        }
        return modularity / twoM;
    }
}
