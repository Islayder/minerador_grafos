package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.InteractionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubInteractionMapperTest {

    private GitHubInteractionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GitHubInteractionMapper();
    }

    @Test
    void commentFromAnotherUserShouldGenerateDirectedInteraction() {
        List<GitHubInteraction> interactions = mapper.mapComment("alice", "bob", false);

        assertEquals(1, interactions.size());
        assertEquals("alice", interactions.get(0).getSourceUser().getLogin());
        assertEquals("bob", interactions.get(0).getTargetUser().getLogin());
        assertEquals(InteractionType.COMMENT, interactions.get(0).getType());
    }

    @Test
    void commentOnIssueByAnotherUserShouldAlsoGenerateIssueCommentedType() {
        List<GitHubInteraction> interactions = mapper.mapComment("alice", "bob", true);

        assertEquals(2, interactions.size());
        assertTrue(interactions.stream().anyMatch(i -> i.getType() == InteractionType.COMMENT));
        assertTrue(interactions.stream().anyMatch(i -> i.getType() == InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));
    }

    @Test
    void selfCommentShouldBeIgnored() {
        assertTrue(mapper.mapComment("alice", "alice", true).isEmpty());
    }

    @Test
    void issueClosedByAnotherUserShouldGenerateIssueClosed() {
        List<GitHubInteraction> interactions = mapper.mapIssueClosed("carol", "bob");

        assertEquals(1, interactions.size());
        assertEquals(InteractionType.ISSUE_CLOSED, interactions.get(0).getType());
        assertEquals("carol", interactions.get(0).getSourceUser().getLogin());
        assertEquals("bob", interactions.get(0).getTargetUser().getLogin());
    }

    @Test
    void reviewShouldGeneratePrReview() {
        List<GitHubInteraction> interactions = mapper.mapReview("bob", "alice", "COMMENTED");

        assertEquals(1, interactions.size());
        assertEquals(InteractionType.PR_REVIEW, interactions.get(0).getType());
    }

    @Test
    void approvedReviewShouldGeneratePrApproval() {
        List<GitHubInteraction> interactions = mapper.mapReview("carol", "alice", "APPROVED");

        assertEquals(1, interactions.size());
        assertEquals(InteractionType.PR_APPROVAL, interactions.get(0).getType());
    }

    @Test
    void mergeByAnotherUserShouldGeneratePrMerge() {
        List<GitHubInteraction> interactions = mapper.mapMerge("bob", "carol");

        assertEquals(1, interactions.size());
        assertEquals(InteractionType.PR_MERGE, interactions.get(0).getType());
    }

    @Test
    void selfMergeShouldBeIgnored() {
        assertTrue(mapper.mapMerge("bob", "bob").isEmpty());
    }
}
