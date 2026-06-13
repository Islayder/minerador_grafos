package br.pucminas.tgc.githubgraph.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitHubUserTest {

    @Test
    void usersWithSameLoginShouldBeEqual() {
        GitHubUser first = new GitHubUser("alice");
        GitHubUser second = new GitHubUser("alice");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void usersWithDifferentLoginShouldNotBeEqual() {
        GitHubUser alice = new GitHubUser("alice");
        GitHubUser bob = new GitHubUser("bob");

        assertNotEquals(alice, bob);
    }

    @Test
    void nullLoginShouldBeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new GitHubUser(null));
    }

    @Test
    void blankLoginShouldBeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new GitHubUser("   "));
    }
}
