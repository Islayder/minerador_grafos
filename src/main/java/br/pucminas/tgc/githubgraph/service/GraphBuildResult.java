package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.util.Map;

/**
 * Resultado da construção dos grafos a partir de interações de repositório.
 */
public final class GraphBuildResult {

    private final UserIndexMapper userIndexMapper;
    private final AbstractGraph commentsGraph;
    private final AbstractGraph issueClosureGraph;
    private final AbstractGraph pullRequestGraph;
    private final AbstractGraph integratedGraph;
    private final Map<String, Integer> pullRequestsOpenedByUser;

    public GraphBuildResult(
            UserIndexMapper userIndexMapper,
            AbstractGraph commentsGraph,
            AbstractGraph issueClosureGraph,
            AbstractGraph pullRequestGraph,
            AbstractGraph integratedGraph) {
        this(userIndexMapper, commentsGraph, issueClosureGraph, pullRequestGraph, integratedGraph, Map.of());
    }

    public GraphBuildResult(
            UserIndexMapper userIndexMapper,
            AbstractGraph commentsGraph,
            AbstractGraph issueClosureGraph,
            AbstractGraph pullRequestGraph,
            AbstractGraph integratedGraph,
            Map<String, Integer> pullRequestsOpenedByUser) {
        this.userIndexMapper = userIndexMapper;
        this.commentsGraph = commentsGraph;
        this.issueClosureGraph = issueClosureGraph;
        this.pullRequestGraph = pullRequestGraph;
        this.integratedGraph = integratedGraph;
        this.pullRequestsOpenedByUser = Map.copyOf(pullRequestsOpenedByUser);
    }

    public UserIndexMapper getUserIndexMapper() {
        return userIndexMapper;
    }

    public AbstractGraph getCommentsGraph() {
        return commentsGraph;
    }

    public AbstractGraph getIssueClosureGraph() {
        return issueClosureGraph;
    }

    public AbstractGraph getPullRequestGraph() {
        return pullRequestGraph;
    }

    public AbstractGraph getIntegratedGraph() {
        return integratedGraph;
    }

    public Map<String, Integer> getPullRequestsOpenedByUser() {
        return pullRequestsOpenedByUser;
    }
}
