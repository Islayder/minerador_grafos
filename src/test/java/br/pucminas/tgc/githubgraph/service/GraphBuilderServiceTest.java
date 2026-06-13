package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.GitHubUser;
import br.pucminas.tgc.githubgraph.model.InteractionType;
import br.pucminas.tgc.githubgraph.model.RepositoryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphBuilderServiceTest {

    private GitHubUser alice;
    private GitHubUser bob;
    private GitHubUser carol;
    private RepositoryData repositoryData;
    private GraphBuildResult result;

    @BeforeEach
    void setUp() {
        alice = new GitHubUser("alice");
        bob = new GitHubUser("bob");
        carol = new GitHubUser("carol");

        repositoryData = new RepositoryData(
                "giscus",
                "giscus",
                List.of(
                        new GitHubInteraction(alice, bob, InteractionType.COMMENT),
                        new GitHubInteraction(alice, bob, InteractionType.COMMENT),
                        new GitHubInteraction(bob, alice, InteractionType.PR_REVIEW),
                        new GitHubInteraction(carol, alice, InteractionType.PR_APPROVAL),
                        new GitHubInteraction(bob, carol, InteractionType.PR_MERGE),
                        new GitHubInteraction(carol, bob, InteractionType.ISSUE_CLOSED)));

        result = new GraphBuilderService().build(repositoryData);
    }

    @Test
    void shouldCreateFourGraphsWithCorrectVertexCount() {
        assertEquals(3, result.getUserIndexMapper().getUserCount());
        assertEquals(3, result.getCommentsGraph().getVertexCount());
        assertEquals(3, result.getIssueClosureGraph().getVertexCount());
        assertEquals(3, result.getPullRequestGraph().getVertexCount());
        assertEquals(3, result.getIntegratedGraph().getVertexCount());
    }

    @Test
    void shouldMapUsersToVertices() {
        UserIndexMapper mapper = result.getUserIndexMapper();
        int aliceIndex = mapper.getIndex(alice);
        int bobIndex = mapper.getIndex(bob);
        int carolIndex = mapper.getIndex(carol);

        AbstractGraph integrated = result.getIntegratedGraph();
        assertTrue(integrated.hasEdge(aliceIndex, bobIndex));
        assertTrue(integrated.hasEdge(bobIndex, aliceIndex));
        assertTrue(integrated.hasEdge(carolIndex, aliceIndex));
        assertTrue(integrated.hasEdge(bobIndex, carolIndex));
        assertTrue(integrated.hasEdge(carolIndex, bobIndex));
    }

    @Test
    void separateGraphsShouldContainEdgesInCorrectCategories() {
        UserIndexMapper mapper = result.getUserIndexMapper();
        int aliceIndex = mapper.getIndex(alice);
        int bobIndex = mapper.getIndex(bob);
        int carolIndex = mapper.getIndex(carol);

        assertEquals(1, result.getCommentsGraph().getEdgeCount());
        assertTrue(result.getCommentsGraph().hasEdge(aliceIndex, bobIndex));

        assertEquals(1, result.getIssueClosureGraph().getEdgeCount());
        assertTrue(result.getIssueClosureGraph().hasEdge(carolIndex, bobIndex));

        assertEquals(3, result.getPullRequestGraph().getEdgeCount());
        assertTrue(result.getPullRequestGraph().hasEdge(bobIndex, aliceIndex));
        assertTrue(result.getPullRequestGraph().hasEdge(carolIndex, aliceIndex));
        assertTrue(result.getPullRequestGraph().hasEdge(bobIndex, carolIndex));
    }

    @Test
    void integratedGraphShouldAccumulateWeightsWithoutDuplicatingEdges() {
        UserIndexMapper mapper = result.getUserIndexMapper();
        int aliceIndex = mapper.getIndex(alice);
        int bobIndex = mapper.getIndex(bob);
        int carolIndex = mapper.getIndex(carol);

        AbstractGraph integrated = result.getIntegratedGraph();

        assertEquals(5, integrated.getEdgeCount());
        assertEquals(4.0, integrated.getEdgeWeight(aliceIndex, bobIndex));
        assertEquals(4.0, integrated.getEdgeWeight(bobIndex, aliceIndex));
        assertEquals(4.0, integrated.getEdgeWeight(carolIndex, aliceIndex));
        assertEquals(5.0, integrated.getEdgeWeight(bobIndex, carolIndex));
        assertEquals(3.0, integrated.getEdgeWeight(carolIndex, bobIndex));
    }

    @Test
    void shouldAllowAntiparallelEdges() {
        UserIndexMapper mapper = result.getUserIndexMapper();
        AbstractGraph integrated = result.getIntegratedGraph();

        assertTrue(integrated.hasEdge(mapper.getIndex(alice), mapper.getIndex(bob)));
        assertTrue(integrated.hasEdge(mapper.getIndex(bob), mapper.getIndex(alice)));
    }

    @Test
    void selfInteractionShouldBeRejectedAtModelLevel() {
        assertThrows(IllegalArgumentException.class,
                () -> new GitHubInteraction(alice, alice, InteractionType.COMMENT));
    }

    @Test
    void emptyRepositoryShouldProduceGraphsWithoutEdges() {
        RepositoryData empty = new RepositoryData("giscus", "giscus", List.of());
        GraphBuildResult emptyResult = new GraphBuilderService().build(empty);

        assertEquals(0, emptyResult.getUserIndexMapper().getUserCount());
        assertTrue(emptyResult.getIntegratedGraph().isEmptyGraph());
        assertEquals(0, emptyResult.getIntegratedGraph().getEdgeCount());
    }
}
