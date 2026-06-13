package br.pucminas.tgc.githubgraph.graph.demo;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyMatrixGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphApiDemonstratorTest {

    @Test
    void shouldDemonstrateAllOperationsForListAndMatrix(@TempDir Path tempDir) {
        GraphApiDemonstrator demonstrator = new GraphApiDemonstrator(System.out);

        ByteArrayOutputStream listOutput = new ByteArrayOutputStream();
        demonstrator = new GraphApiDemonstrator(new PrintStream(listOutput));
        demonstrator.demonstrate(
                AdjacencyListGraph::new,
                "AdjacencyListGraph",
                tempDir.resolve("list.gexf"));
        assertDemonstrationOutput(listOutput.toString(StandardCharsets.UTF_8));
        assertTrue(Files.exists(tempDir.resolve("list.gexf")));

        ByteArrayOutputStream matrixOutput = new ByteArrayOutputStream();
        demonstrator = new GraphApiDemonstrator(new PrintStream(matrixOutput));
        demonstrator.demonstrate(
                AdjacencyMatrixGraph::new,
                "AdjacencyMatrixGraph",
                tempDir.resolve("matrix.gexf"));
        assertDemonstrationOutput(matrixOutput.toString(StandardCharsets.UTF_8));
        assertTrue(Files.exists(tempDir.resolve("matrix.gexf")));
    }

    private static void assertDemonstrationOutput(String text) {
        assertTrue(text.contains("getVertexCount()"));
        assertTrue(text.contains("isEmptyGraph()"));
        assertTrue(text.contains("isDivergent"));
        assertTrue(text.contains("isConvergent"));
        assertTrue(text.contains("isIncident"));
        assertTrue(text.contains("isConnected()"));
        assertTrue(text.contains("isCompleteGraph()"));
        assertTrue(text.contains("exportToGEPHI"));
        assertTrue(text.contains("LoopNotAllowedException"));
    }
}
