package br.pucminas.tgc.githubgraph.export;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GexfExporterTest {

    @Test
    void shouldExportEdgeWithNativeWeightAttribute(@TempDir Path tempDir) throws Exception {
        AdjacencyListGraph graph = new AdjacencyListGraph(3);
        graph.addEdge(1, 2);
        graph.setEdgeWeight(1, 2, 7.5);

        Path output = tempDir.resolve("graph.gexf");
        GexfExporter.export(graph, output.toString());

        String content = Files.readString(output);
        assertTrue(content.contains("<?xml version=\"1.0\""));
        assertTrue(content.contains("<gexf"));
        assertTrue(content.contains("<nodes>"));
        assertTrue(content.contains("<edges>"));
        assertTrue(content.contains("source=\"1\""));
        assertTrue(content.contains("target=\"2\""));
        assertTrue(content.contains("weight=\"7.5\""));
        assertTrue(content.contains("title=\"weight\""));
        assertTrue(content.contains("<attvalue for=\"0\" value=\"7.5\""));
    }
}
