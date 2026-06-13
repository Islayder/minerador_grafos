package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.GitHubUser;
import br.pucminas.tgc.githubgraph.model.InteractionType;
import br.pucminas.tgc.githubgraph.model.RepositoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gera {@link RepositoryData} simulado para demonstração offline da ferramenta.
 */
public final class DemoDataFactory {

    private DemoDataFactory() {
    }

    public static RepositoryData createGiscusDemo() {
        GitHubUser alice = new GitHubUser("alice");
        GitHubUser bob = new GitHubUser("bob");
        GitHubUser carol = new GitHubUser("carol");
        GitHubUser dave = new GitHubUser("dave");
        GitHubUser erin = new GitHubUser("erin");
        GitHubUser frank = new GitHubUser("frank");

        List<GitHubInteraction> interactions = new ArrayList<>();

        interactions.add(new GitHubInteraction(alice, bob, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(alice, bob, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(alice, bob, InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));

        interactions.add(new GitHubInteraction(bob, alice, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(bob, alice, InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));

        interactions.add(new GitHubInteraction(carol, bob, InteractionType.ISSUE_CLOSED));
        interactions.add(new GitHubInteraction(dave, bob, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(erin, frank, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(frank, erin, InteractionType.COMMENT));

        interactions.add(new GitHubInteraction(dave, erin, InteractionType.PR_REVIEW));
        interactions.add(new GitHubInteraction(frank, erin, InteractionType.PR_APPROVAL));
        interactions.add(new GitHubInteraction(bob, carol, InteractionType.PR_MERGE));

        interactions.add(new GitHubInteraction(bob, alice, InteractionType.PR_REVIEW));
        interactions.add(new GitHubInteraction(alice, bob, InteractionType.PR_APPROVAL));

        interactions.add(new GitHubInteraction(carol, dave, InteractionType.COMMENT));
        interactions.add(new GitHubInteraction(carol, dave, InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));
        interactions.add(new GitHubInteraction(erin, dave, InteractionType.PR_REVIEW));

        return new RepositoryData(
                "giscus",
                "giscus",
                interactions,
                Map.of(
                        "alice", 1,
                        "bob", 2,
                        "dave", 1,
                        "erin", 1));
    }
}
