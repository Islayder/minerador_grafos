package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.model.RepositoryData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PullRequestOpeningMetricsTest {

    @Test
    void pullRequestOpeningsShouldBeCountedPerUser() {
        RepositoryData data = new RepositoryData(
                "giscus",
                "giscus",
                List.of(),
                Map.of("alice", 3, "bob", 1));

        GraphBuildResult result = new GraphBuilderService().build(data);

        assertEquals(3, result.getPullRequestsOpenedByUser().get("alice"));
        assertEquals(1, result.getPullRequestsOpenedByUser().get("bob"));
    }

    @Test
    void pullRequestOpeningAloneShouldNotCreateGraphEdge() {
        RepositoryData data = new RepositoryData(
                "giscus",
                "giscus",
                List.of(),
                Map.of("alice", 2));

        GraphBuildResult result = new GraphBuilderService().build(data);

        assertEquals(0, result.getIntegratedGraph().getEdgeCount());
        assertEquals(2, result.getPullRequestsOpenedByUser().get("alice"));
    }
}
