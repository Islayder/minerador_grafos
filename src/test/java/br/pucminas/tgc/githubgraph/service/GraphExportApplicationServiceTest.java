package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.app.DemoDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphExportApplicationServiceTest {

    @Test
    void shouldExportIntegratedGraphToGexfFile(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        GraphExportApplicationService exportService = new GraphExportApplicationService();

        Path output = exportService.exportIntegratedGraph(result, tempDir.resolve("demo.gexf").toString());

        assertTrue(Files.exists(output));
        String content = Files.readString(output);
        assertTrue(content.contains("<gexf"));
        assertTrue(content.contains("<edge id=\"0\""));
        assertTrue(content.contains("weight=\""));
        assertTrue(content.contains("title=\"weight\""));
    }

    @Test
    void shouldExportAllFourGraphsToDirectory(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        GraphExportApplicationService exportService = new GraphExportApplicationService();

        List<Path> exported = exportService.exportAllGraphs(result, tempDir.toString());

        assertEquals(5, exported.size());
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.COMMENTS_GRAPH_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.ISSUE_CLOSURE_GRAPH_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.PULL_REQUEST_GRAPH_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_GEPHI_FILE)));

        String integrated = Files.readString(
                tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_FILE));
        assertTrue(integrated.contains("weight=\""));
        assertTrue(integrated.contains("title=\"weight\""));

        String presentation = Files.readString(
                tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_GEPHI_FILE));
        assertTrue(presentation.contains("label=\"alice\""));
        assertTrue(presentation.contains("xmlns:viz"));
        assertTrue(presentation.contains("title=\"realWeight\""));
        assertTrue(presentation.contains("title=\"visualWeight\""));

        for (Path path : exported) {
            String content = Files.readString(path);
            assertTrue(content.contains("<gexf"), "Arquivo GEXF esperado: " + path.getFileName());
            assertTrue(content.contains("<nodes>"));
            assertTrue(content.contains("<edges>"));
        }
    }
}
