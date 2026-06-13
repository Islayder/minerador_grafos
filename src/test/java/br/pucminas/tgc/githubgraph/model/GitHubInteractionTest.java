package br.pucminas.tgc.githubgraph.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GitHubInteractionTest {

    private final GitHubUser alice = new GitHubUser("alice");
    private final GitHubUser bob = new GitHubUser("bob");

    @Test
    void sourceTargetAndTypeAreRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> new GitHubInteraction(null, bob, InteractionType.COMMENT));
        assertThrows(IllegalArgumentException.class,
                () -> new GitHubInteraction(alice, null, InteractionType.COMMENT));
        assertThrows(IllegalArgumentException.class,
                () -> new GitHubInteraction(alice, bob, null));
    }

    @Test
    void equalSourceAndTargetShouldBeInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new GitHubInteraction(alice, alice, InteractionType.COMMENT));
    }
}
