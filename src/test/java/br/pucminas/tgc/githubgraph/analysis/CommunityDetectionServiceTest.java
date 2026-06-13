package br.pucminas.tgc.githubgraph.analysis;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityDetectionServiceTest {

    private final CommunityDetectionService service = new CommunityDetectionService();

    @Test
    void twoDisconnectedGroupsShouldYieldTwoCommunities() {
        AdjacencyListGraph graph = new AdjacencyListGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);

        CommunityReport report = service.detect(graph);

        assertEquals(2, report.getCommunityCount());
    }

    @Test
    void stronglyConnectedGroupShouldStayTogether() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        CommunityReport report = service.detect(graph);

        assertEquals(1, report.getCommunityCount());
    }

    @Test
    void modularityShouldBeComputedWithoutError() {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        CommunityReport report = service.detect(graph);

        assertFalse(Double.isNaN(report.getModularityScore()));
    }

    @Test
    void detectionShouldBeDeterministic() {
        AdjacencyListGraph graph = new AdjacencyListGraph(5);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(3, 4);

        CommunityReport first = service.detect(graph);
        CommunityReport second = service.detect(graph);

        assertEquals(first.getVertexCommunity(), second.getVertexCommunity());
    }

    @Test
    void topCommunitiesShouldBeOrderedBySize() {
        AdjacencyListGraph graph = new AdjacencyListGraph(5);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);

        CommunityReport report = service.detect(graph);
        if (report.getTopCommunities().size() < 2) {
            return;
        }
        assertTrue(report.getTopCommunities().get(0).size()
                >= report.getTopCommunities().get(1).size());
    }
}
