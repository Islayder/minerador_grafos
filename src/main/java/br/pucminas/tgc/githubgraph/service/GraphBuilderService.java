package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.InteractionType;
import br.pucminas.tgc.githubgraph.model.RepositoryData;

/**
 * Constrói grafos separados e o grafo integrado ponderado a partir de interações.
 */
public final class GraphBuilderService {

    private final InteractionWeightResolver weightResolver = new InteractionWeightResolver();

    public GraphBuildResult build(RepositoryData repositoryData) {
        UserIndexMapper userIndexMapper = new UserIndexMapper(repositoryData.getAllUsers());
        int vertexCount = userIndexMapper.getUserCount();

        AbstractGraph commentsGraph = new AdjacencyListGraph(vertexCount);
        AbstractGraph issueClosureGraph = new AdjacencyListGraph(vertexCount);
        AbstractGraph pullRequestGraph = new AdjacencyListGraph(vertexCount);
        AbstractGraph integratedGraph = new AdjacencyListGraph(vertexCount);

        for (GitHubInteraction interaction : repositoryData.getInteractions()) {
            int source = userIndexMapper.getIndex(interaction.getSourceUser());
            int target = userIndexMapper.getIndex(interaction.getTargetUser());
            double weight = weightResolver.resolve(interaction.getType());

            addToSeparateGraph(resolveSeparateGraph(
                    interaction.getType(), commentsGraph, issueClosureGraph, pullRequestGraph),
                    source,
                    target,
                    weight);
            addToIntegratedGraph(integratedGraph, source, target, weight);
        }

        return new GraphBuildResult(
                userIndexMapper,
                commentsGraph,
                issueClosureGraph,
                pullRequestGraph,
                integratedGraph,
                repositoryData.getPullRequestsOpenedByUser());
    }

    private AbstractGraph resolveSeparateGraph(
            InteractionType type,
            AbstractGraph commentsGraph,
            AbstractGraph issueClosureGraph,
            AbstractGraph pullRequestGraph) {
        return switch (type) {
            case COMMENT, ISSUE_COMMENTED_BY_OTHER_USER -> commentsGraph;
            case ISSUE_CLOSED -> issueClosureGraph;
            case PR_REVIEW, PR_APPROVAL, PR_MERGE -> pullRequestGraph;
        };
    }

    private void addToSeparateGraph(AbstractGraph graph, int source, int target, double weight) {
        if (!graph.hasEdge(source, target)) {
            graph.addEdge(source, target);
            graph.setEdgeWeight(source, target, weight);
        }
    }

    private void addToIntegratedGraph(AbstractGraph graph, int source, int target, double weight) {
        if (graph.hasEdge(source, target)) {
            graph.setEdgeWeight(source, target, graph.getEdgeWeight(source, target) + weight);
        } else {
            graph.addEdge(source, target);
            graph.setEdgeWeight(source, target, weight);
        }
    }
}
