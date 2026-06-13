package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Closeness centrality baseada em BFS em grafos direcionados.
 */
public final class ClosenessCentralityAnalyzer {

    public Map<Integer, Double> closeness(AbstractGraph graph) {
        GraphAdjacencyIndex index = new GraphAdjacencyIndex(graph);
        int vertexCount = index.getVertexCount();
        Map<Integer, Double> result = new LinkedHashMap<>();

        for (int source = 0; source < vertexCount; source++) {
            Map<Integer, Integer> distances = DirectedGraphTraversal.bfsDistances(index, source);

            int reachableCount = 0;
            int sumDistances = 0;
            for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
                if (entry.getKey() == source) {
                    continue;
                }
                reachableCount++;
                sumDistances += entry.getValue();
            }

            if (reachableCount == 0 || sumDistances == 0) {
                result.put(source, 0.0);
            } else {
                result.put(source, (double) reachableCount / sumDistances);
            }
        }
        return result;
    }
}
