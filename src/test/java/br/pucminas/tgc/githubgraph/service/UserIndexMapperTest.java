package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.model.GitHubUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserIndexMapperTest {

    @Test
    void shouldMapUsersToDeterministicIndices() {
        GitHubUser carol = new GitHubUser("carol");
        GitHubUser alice = new GitHubUser("alice");
        GitHubUser bob = new GitHubUser("bob");

        UserIndexMapper mapper = new UserIndexMapper(List.of(carol, alice, bob));

        assertEquals(3, mapper.getUserCount());
        assertEquals(0, mapper.getIndex(alice));
        assertEquals(1, mapper.getIndex(bob));
        assertEquals(2, mapper.getIndex(carol));
        assertEquals(alice, mapper.getUser(0));
        assertEquals(bob, mapper.getUser(1));
        assertEquals(carol, mapper.getUser(2));
    }
}
