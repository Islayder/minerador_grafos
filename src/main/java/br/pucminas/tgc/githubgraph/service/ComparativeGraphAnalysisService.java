package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.analysis.DensityAnalyzer;
import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Compara os quatro grafos construídos a partir das interações do repositório.
 */
public final class ComparativeGraphAnalysisService {

    private final DensityAnalyzer densityAnalyzer = new DensityAnalyzer();

    public String compare(GraphBuildResult result) {
        Map<String, AbstractGraph> graphs = new LinkedHashMap<>();
        graphs.put("Grafo de comentários (commentsGraph)", result.getCommentsGraph());
        graphs.put("Grafo de fechamento de issues (issueClosureGraph)", result.getIssueClosureGraph());
        graphs.put("Grafo de pull requests (pullRequestGraph)", result.getPullRequestGraph());
        graphs.put("Grafo integrado ponderado (integratedGraph)", result.getIntegratedGraph());

        String densestName = null;
        double highestDensity = -1.0;
        String mostEdgesName = null;
        int highestEdgeCount = -1;

        StringBuilder text = new StringBuilder();
        text.append("Análise comparativa dos quatro grafos\n");
        text.append("Repositório: giscus/giscus\n\n");

        for (Map.Entry<String, AbstractGraph> entry : graphs.entrySet()) {
            AbstractGraph graph = entry.getValue();
            int edgeCount = graph.getEdgeCount();
            double density = densityAnalyzer.density(graph);

            text.append(entry.getKey()).append('\n');
            text.append("  Vértices: ").append(graph.getVertexCount()).append('\n');
            text.append("  Arestas: ").append(edgeCount).append('\n');
            text.append("  Densidade: ").append(String.format("%.4f", density)).append('\n');
            text.append("  Vazio: ").append(graph.isEmptyGraph()).append('\n');
            text.append('\n');

            if (density > highestDensity) {
                highestDensity = density;
                densestName = entry.getKey();
            }
            if (edgeCount > highestEdgeCount) {
                highestEdgeCount = edgeCount;
                mostEdgesName = entry.getKey();
            }
        }

        text.append("Síntese comparativa\n");
        text.append("  Grafo mais denso: ").append(densestName)
                .append(" (densidade ").append(String.format("%.4f", highestDensity)).append(")\n");
        text.append("  Grafo com mais relações: ").append(mostEdgesName)
                .append(" (").append(highestEdgeCount).append(" arestas)\n\n");

        text.append("""
                Interpretação:
                - O grafo de comentários concentra interações de discussão em issues e pull requests.
                - O grafo de fechamento de issues destaca quem encerra o ciclo de vida de issues de outros autores.
                - O grafo de pull requests evidencia revisões, aprovações e merges.
                - O grafo integrado combina todos os tipos e acumula pesos entre o mesmo par de colaboradores,
                  oferecendo visão global da intensidade de colaboração no giscus/giscus.
                """);

        return text.toString();
    }
}
