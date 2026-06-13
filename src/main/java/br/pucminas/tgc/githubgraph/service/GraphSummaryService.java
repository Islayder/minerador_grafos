package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.analysis.GraphAdjacencyIndex;
import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.model.GitHubUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gera resumos textuais dos grafos construídos a partir de interações.
 */
public final class GraphSummaryService {

    public String summarize(GraphBuildResult result) {
        StringBuilder summary = new StringBuilder();
        UserIndexMapper mapper = result.getUserIndexMapper();

        summary.append("Resumo da análise de colaboração\n");
        summary.append("Usuários (vértices): ").append(mapper.getUserCount()).append('\n');
        summary.append("Usuários mapeados: ");
        appendUserList(summary, mapper);
        summary.append('\n');

        summary.append(summarizeGraph("Grafo de comentários", result.getCommentsGraph()));
        summary.append(summarizeGraph("Grafo de fechamento de issues", result.getIssueClosureGraph()));
        summary.append(summarizeGraph("Grafo de pull requests", result.getPullRequestGraph()));
        summary.append(summarizeGraph("Grafo integrado ponderado", result.getIntegratedGraph()));

        summary.append(summarizeTopEdges(result, 8));
        summary.append(summarizeSampleDegrees(result, 4));

        return summary.toString();
    }

    public String summarizeGraph(String name, AbstractGraph graph) {
        StringBuilder summary = new StringBuilder();
        summary.append('\n').append(name).append('\n');
        summary.append("  Vértices: ").append(graph.getVertexCount()).append('\n');
        summary.append("  Arestas: ").append(graph.getEdgeCount()).append('\n');
        summary.append("  Vazio: ").append(graph.isEmptyGraph()).append('\n');
        summary.append("  Completo: ").append(graph.isCompleteGraph()).append('\n');
        return summary.toString();
    }

    public String summarizeTopEdges(GraphBuildResult result, int limit) {
        UserIndexMapper mapper = result.getUserIndexMapper();
        AbstractGraph integrated = result.getIntegratedGraph();
        List<EdgeSummary> edges = new ArrayList<>();

        GraphAdjacencyIndex index = new GraphAdjacencyIndex(integrated);
        for (int source = 0; source < index.getVertexCount(); source++) {
            for (int target : index.successors(source)) {
                edges.add(new EdgeSummary(
                        mapper.getUser(source).getLogin(),
                        mapper.getUser(target).getLogin(),
                        integrated.getEdgeWeight(source, target)));
            }
        }

        edges.sort(Comparator.comparingDouble(EdgeSummary::weight).reversed());

        StringBuilder summary = new StringBuilder();
        summary.append("\nPrincipais arestas do grafo integrado (por peso)\n");
        int count = Math.min(limit, edges.size());
        if (count == 0) {
            summary.append("  Nenhuma aresta encontrada.\n");
            return summary.toString();
        }

        for (int edgeIndex = 0; edgeIndex < count; edgeIndex++) {
            EdgeSummary edge = edges.get(edgeIndex);
            summary.append("  ")
                    .append(edge.sourceLogin())
                    .append(" -> ")
                    .append(edge.targetLogin())
                    .append(" (peso ")
                    .append(edge.weight())
                    .append(")\n");
        }
        return summary.toString();
    }

    private String summarizeSampleDegrees(GraphBuildResult result, int userLimit) {
        UserIndexMapper mapper = result.getUserIndexMapper();
        AbstractGraph integrated = result.getIntegratedGraph();

        StringBuilder summary = new StringBuilder();
        summary.append("\nGraus no grafo integrado (amostra)\n");

        int count = Math.min(userLimit, mapper.getUserCount());
        for (int index = 0; index < count; index++) {
            GitHubUser user = mapper.getUser(index);
            summary.append("  ")
                    .append(user.getLogin())
                    .append(": entrada=")
                    .append(integrated.getVertexInDegree(index))
                    .append(", saída=")
                    .append(integrated.getVertexOutDegree(index))
                    .append('\n');
        }
        return summary.toString();
    }

    private void appendUserList(StringBuilder summary, UserIndexMapper mapper) {
        for (int index = 0; index < mapper.getUserCount(); index++) {
            if (index > 0) {
                summary.append(", ");
            }
            summary.append(mapper.getUser(index).getLogin());
        }
    }

    private record EdgeSummary(String sourceLogin, String targetLogin, double weight) {
    }
}
