package br.pucminas.tgc.githubgraph.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryDataTest {

    private final GitHubUser alice = new GitHubUser("alice");
    private final GitHubUser bob = new GitHubUser("bob");
    private final GitHubUser carol = new GitHubUser("carol");

    @Test
    void shouldReturnUniqueUsers() {
        RepositoryData data = new RepositoryData(
                "giscus",
                "giscus",
                List.of(
                        new GitHubInteraction(alice, bob, InteractionType.COMMENT),
                        new GitHubInteraction(bob, carol, InteractionType.PR_MERGE),
                        new GitHubInteraction(carol, alice, InteractionType.PR_APPROVAL)));

        assertEquals(3, data.getAllUsers().size());
        assertTrue(data.getAllUsers().contains(alice));
        assertTrue(data.getAllUsers().contains(bob));
        assertTrue(data.getAllUsers().contains(carol));
    }

    @Test
    void shouldFilterInteractionsByType() {
        RepositoryData data = new RepositoryData(
                "giscus",
                "giscus",
                List.of(
                        new GitHubInteraction(alice, bob, InteractionType.COMMENT),
                        new GitHubInteraction(carol, bob, InteractionType.ISSUE_CLOSED)));

        assertEquals(1, data.getInteractionsByType(InteractionType.COMMENT).size());
        assertEquals(1, data.getInteractionsByType(InteractionType.ISSUE_CLOSED).size());
    }

    @Test
    void interactionsListShouldBeImmutableFromOutside() {
        List<GitHubInteraction> mutable = new ArrayList<>();
        mutable.add(new GitHubInteraction(alice, bob, InteractionType.COMMENT));

        RepositoryData data = new RepositoryData("giscus", "giscus", mutable);
        mutable.add(new GitHubInteraction(bob, carol, InteractionType.PR_MERGE));

        assertEquals(1, data.getInteractions().size());
        assertThrows(UnsupportedOperationException.class,
                () -> data.getInteractions().add(new GitHubInteraction(carol, alice, InteractionType.PR_REVIEW)));
    }
}
