package br.pucminas.tgc.githubgraph.export;

import br.pucminas.tgc.githubgraph.app.DemoDataFactory;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.model.GitHubUser;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;
import br.pucminas.tgc.githubgraph.service.UserIndexMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GephiPresentationExporterTest {

    @Test
    void shouldExportPresentationGexfWithLabelsLayoutAndWeightAttributes(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        Path output = tempDir.resolve("integrated-graph-gephi.gexf");

        GephiPresentationExporter.exportIntegratedPresentationGraph(result, output);

        assertTrue(Files.exists(output));
        String content = Files.readString(output);

        assertTrue(content.contains("xmlns:viz=\"http://www.gexf.net/1.2draft/viz\""));
        assertTrue(content.contains("<viz:position"));
        assertTrue(content.contains("<viz:size"));
        assertTrue(content.contains("title=\"realWeight\""));
        assertTrue(content.contains("title=\"visualWeight\""));
        assertTrue(content.contains("label=\"alice\""));
        assertTrue(content.contains("label=\"bob\""));
        assertFalse(content.contains("label=\"v0\""));
        assertExportedNodeSizesWithinBounds(content);
    }

    @Test
    void visualWeightShouldBeControlledAndLessThanHighRealWeight() {
        double realHigh = 1000.0;
        double visualHigh = GephiPresentationExporter.computeVisualWeight(realHigh);

        assertEquals(1.2, visualHigh, 1e-9);
        assertTrue(visualHigh < realHigh);
        assertTrue(visualHigh <= GephiPresentationExporter.MAX_EDGE_VISUAL_WEIGHT);

        assertEquals(0.45, GephiPresentationExporter.computeVisualWeight(1.0), 1e-9);
        assertEquals(0.7, GephiPresentationExporter.computeVisualWeight(10.0), 1e-9);
        assertEquals(0.95, GephiPresentationExporter.computeVisualWeight(100.0), 1e-9);
    }

    @Test
    void visualWeightShouldClampToConfiguredMaximum() {
        double visualExtreme = GephiPresentationExporter.computeVisualWeight(1_000_000.0);
        assertEquals(GephiPresentationExporter.MAX_EDGE_VISUAL_WEIGHT, visualExtreme, 1e-9);
        assertTrue(visualExtreme >= GephiPresentationExporter.MIN_EDGE_VISUAL_WEIGHT);
    }

    @Test
    void shouldUseBotColorForBotLogins(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        Path output = tempDir.resolve("presentation.gexf");

        GephiPresentationExporter.exportIntegratedPresentationGraph(result, output);

        String content = Files.readString(output);
        assertTrue(content.contains("<viz:color"));
        assertTrue(content.contains("r=\"65\""));
    }

    @Test
    void nodeSizeShouldStayWithinBounds() {
        assertEquals(GephiPresentationExporter.MIN_NODE_SIZE,
                GephiPresentationExporter.computeNodeSize(0), 1e-9);
        assertTrue(GephiPresentationExporter.computeNodeSize(10_000)
                <= GephiPresentationExporter.MAX_NODE_SIZE);
        assertTrue(GephiPresentationExporter.computeNodeSize(10_000)
                >= GephiPresentationExporter.MIN_NODE_SIZE);
    }

    @Test
    void shouldPutRealWeightOnNativeEdgeAttributeAndPreserveVisualWeight(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = buildHighWeightPresentationGraph();
        Path output = tempDir.resolve("presentation.gexf");

        GephiPresentationExporter.exportIntegratedPresentationGraph(result, output);

        String content = Files.readString(output);
        double realWeight = 1000.0;
        double visualWeight = GephiPresentationExporter.computeVisualWeight(realWeight);
        String formattedVisualWeight = GexfFormat.formatWeight(visualWeight);
        String formattedRealWeight = GexfFormat.formatWeight(realWeight);

        Pattern edgePattern = Pattern.compile(
                "<edge id=\"\\d+\" source=\"0\" target=\"1\" weight=\"([^\"]+)\" label=\"([^\"]+)\">");
        Matcher matcher = edgePattern.matcher(content);
        assertTrue(matcher.find(), "Aresta com peso real esperada no atributo nativo weight");
        assertEquals(formattedRealWeight, matcher.group(1));
        assertEquals(formattedRealWeight, matcher.group(2));

        assertTrue(content.contains("<attvalue for=\"0\" value=\"" + formattedRealWeight + "\""));
        assertTrue(content.contains("<attvalue for=\"1\" value=\"" + formattedVisualWeight + "\""));
        assertTrue(content.contains("label=\"alice\""));
        assertTrue(content.contains("label=\"bob\""));
        assertTrue(content.contains("<viz:position"));
        assertTrue(content.contains("<viz:size"));
        assertExportedNodeSizesWithinBounds(content);
    }

    @Test
    void originalGexfExporterShouldRemainUnchangedForPresentationGraph(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = buildHighWeightPresentationGraph();
        Path original = tempDir.resolve("integrated-graph.gexf");
        Path presentation = tempDir.resolve("integrated-graph-gephi.gexf");

        GexfExporter.export(result.getIntegratedGraph(), original.toString());
        GephiPresentationExporter.exportIntegratedPresentationGraph(result, presentation);

        String originalContent = Files.readString(original);
        String presentationContent = Files.readString(presentation);

        assertFalse(originalContent.contains("xmlns:viz"));
        assertTrue(originalContent.contains("label=\"v0\""));
        assertTrue(originalContent.contains("title=\"weight\""));
        assertTrue(originalContent.contains("value=\"" + GexfFormat.formatWeight(1000.0) + "\""));
        assertTrue(originalContent.contains("weight=\"" + GexfFormat.formatWeight(1000.0) + "\""));
        assertFalse(originalContent.contains("title=\"realWeight\""));

        assertTrue(presentationContent.contains("xmlns:viz"));
        assertTrue(presentationContent.contains("label=\"alice\""));
        assertTrue(presentationContent.contains("title=\"realWeight\""));
    }

    private static void assertExportedNodeSizesWithinBounds(String content) {
        Pattern sizePattern = Pattern.compile("<viz:size value=\"([^\"]+)\"/>");
        Matcher matcher = sizePattern.matcher(content);
        assertTrue(matcher.find(), "viz:size esperado no GEXF de apresentacao");
        do {
            double size = Double.parseDouble(matcher.group(1));
            assertTrue(size >= GephiPresentationExporter.MIN_NODE_SIZE,
                    "viz:size abaixo do minimo: " + size);
            assertTrue(size <= GephiPresentationExporter.MAX_NODE_SIZE,
                    "viz:size acima do maximo: " + size);
        } while (matcher.find());
    }

    private static GraphBuildResult buildHighWeightPresentationGraph() {
        AdjacencyListGraph graph = new AdjacencyListGraph(2);
        graph.addEdge(0, 1);
        graph.setEdgeWeight(0, 1, 1000.0);
        UserIndexMapper mapper = new UserIndexMapper(List.of(
                new GitHubUser("alice"),
                new GitHubUser("bob")));
        return new GraphBuildResult(
                mapper,
                graph,
                graph,
                graph,
                graph);
    }
}
