package br.pucminas.tgc.githubgraph.graph.impl;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.exception.InvalidVertexException;
import br.pucminas.tgc.githubgraph.graph.exception.LoopNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractGraphContractTest {

    protected abstract AbstractGraph createGraph(int vertexCount);

    @Test
    void shouldCreateGraphWithCorrectVertexCount() {
        AbstractGraph graph = createGraph(5);
        assertEquals(5, graph.getVertexCount());
    }

    @Test
    void newlyCreatedGraphShouldHaveZeroEdges() {
        AbstractGraph graph = createGraph(4);
        assertEquals(0, graph.getEdgeCount());
        assertTrue(graph.isEmptyGraph());
    }

    @Test
    void addEdgeShouldIncreaseEdgeCount() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        assertEquals(1, graph.getEdgeCount());
        assertTrue(graph.hasEdge(0, 1));
    }

    @Test
    void hasEdgeShouldRespectDirection() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        assertTrue(graph.hasEdge(0, 1));
        assertFalse(graph.hasEdge(1, 0));
    }

    @Test
    void antiparallelEdgesShouldBeAllowed() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        assertEquals(2, graph.getEdgeCount());
        assertTrue(graph.hasEdge(0, 1));
        assertTrue(graph.hasEdge(1, 0));
    }

    @Test
    void addEdgeShouldBeIdempotent() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        assertEquals(1, graph.getEdgeCount());
    }

    @Test
    void loopsShouldBeBlocked() {
        AbstractGraph graph = createGraph(3);
        assertThrows(LoopNotAllowedException.class, () -> graph.addEdge(1, 1));
    }

    @Test
    void invalidVertexIndexShouldThrowException() {
        AbstractGraph graph = createGraph(3);
        assertThrows(InvalidVertexException.class, () -> graph.addEdge(-1, 0));
        assertThrows(InvalidVertexException.class, () -> graph.addEdge(0, 3));
    }

    @Test
    void removeEdgeShouldRemoveExistingEdge() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        graph.removeEdge(0, 1);
        assertFalse(graph.hasEdge(0, 1));
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    void inDegreeAndOutDegreeShouldWork() {
        AbstractGraph graph = createGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(2, 1);
        graph.addEdge(1, 3);

        assertEquals(2, graph.getVertexInDegree(1));
        assertEquals(1, graph.getVertexOutDegree(1));
    }

    @Test
    void vertexWeightsShouldWork() {
        AbstractGraph graph = createGraph(2);
        assertEquals(0.0, graph.getVertexWeight(0));
        graph.setVertexWeight(0, 7.5);
        assertEquals(7.5, graph.getVertexWeight(0));
    }

    @Test
    void edgeWeightsShouldWork() {
        AbstractGraph graph = createGraph(2);
        graph.addEdge(0, 1);
        assertEquals(1.0, graph.getEdgeWeight(0, 1));
        graph.setEdgeWeight(0, 1, 4.0);
        assertEquals(4.0, graph.getEdgeWeight(0, 1));
    }

    @Test
    void isEmptyGraphShouldReflectAbsenceOfEdges() {
        AbstractGraph graph = createGraph(3);
        assertTrue(graph.isEmptyGraph());
        graph.addEdge(0, 1);
        assertFalse(graph.isEmptyGraph());
    }

    @Test
    void structuralRelationsShouldWork() {
        AbstractGraph graph = createGraph(4);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);

        assertTrue(graph.isSucessor(0, 1));
        assertTrue(graph.isPredessor(0, 1));
        assertTrue(graph.isDivergent(0, 1, 0, 2));
        assertTrue(graph.isConvergent(0, 2, 1, 2));
        assertTrue(graph.isIncident(0, 1, 0));
        assertFalse(graph.isIncident(0, 1, 2));
    }

    @Test
    void isConnectedShouldDetectWeakConnectivity() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(2, 1);
        assertTrue(graph.isConnected());
    }

    @Test
    void disconnectedGraphShouldNotBeConnected() {
        AbstractGraph graph = createGraph(4);
        graph.addEdge(0, 1);
        assertFalse(graph.isConnected());
    }

    @Test
    void isCompleteGraphShouldHoldForDirectedSimpleCompleteGraph() {
        AbstractGraph graph = createGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 0);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        graph.addEdge(2, 1);
        assertTrue(graph.isCompleteGraph());
    }

    @Test
    void exportToGephiShouldCreateBasicGexfFile(@TempDir Path tempDir) throws Exception {
        AbstractGraph graph = createGraph(2);
        graph.addEdge(0, 1);
        graph.setEdgeWeight(0, 1, 2.5);

        Path output = tempDir.resolve("grafo.gexf");
        graph.exportToGEPHI(output.toString());

        String content = Files.readString(output);
        assertTrue(Files.exists(output));
        assertTrue(content.contains("<gexf"));
        assertTrue(content.contains("<node id=\"0\""));
        assertTrue(content.contains("source=\"0\" target=\"1\" weight=\"2.5\""));
        assertTrue(content.contains("title=\"weight\""));
    }
}
