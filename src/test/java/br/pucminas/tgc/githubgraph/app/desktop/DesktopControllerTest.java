package br.pucminas.tgc.githubgraph.app.desktop;

import br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService;
import br.pucminas.tgc.githubgraph.app.DemoApplicationService;
import br.pucminas.tgc.githubgraph.app.RealCollectionApplicationService;
import br.pucminas.tgc.githubgraph.app.StressApplicationService;
import br.pucminas.tgc.githubgraph.app.StressProfile;
import br.pucminas.tgc.githubgraph.github.CollectionPropertiesLoader;
import br.pucminas.tgc.githubgraph.service.GraphExportApplicationService;
import br.pucminas.tgc.githubgraph.service.GraphSummaryService;
import br.pucminas.tgc.githubgraph.service.OutputReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesktopControllerTest {

    @Test
    void initialStateShouldBeNone() {
        DesktopController controller = new DesktopController();

        assertEquals(DataSourceStatus.NONE, controller.getDataSourceStatus());
        assertFalse(controller.hasLoadedGraph());
    }

    @Test
    void summaryWithoutGraphShouldReturnFriendlyMessage() {
        DesktopController controller = new DesktopController();

        assertEquals(DesktopController.NO_GRAPH_LOADED_MESSAGE, controller.showSummary());
    }

    @Test
    void offlineModeShouldLoadGraphAndUpdateStatus() {
        DesktopController controller = new DesktopController();

        String output = controller.runOfflineMode();

        assertEquals(DataSourceStatus.OFFLINE, controller.getDataSourceStatus());
        assertTrue(controller.hasLoadedGraph());
        assertTrue(output.contains("Modo offline"));
        assertTrue(output.contains("Grafo integrado ponderado"));
    }

    @Test
    void miningConfigurationShouldDescribeFullRepository() {
        DesktopController controller = new DesktopController();

        String description = controller.describeMiningConfiguration();

        assertTrue(description.contains("FULL_REPOSITORY"));
        assertTrue(description.contains("giscus"));
        assertTrue(description.contains("Cache local: ativado"));
    }

    @Test
    void statusLineShouldMentionFullRepositoryMode() {
        DesktopController controller = new DesktopController();

        assertTrue(controller.getStatusLine().contains("FULL_REPOSITORY"));
        assertTrue(controller.getStatusLine().contains("giscus/giscus"));
    }

    @Test
    void stressModeShouldLoadGraphWithoutInternet() {
        DesktopController controller = new DesktopController();
        controller.setSelectedStressProfile(StressProfile.SMALL);

        String output = controller.runStressOfflineMode();

        assertTrue(controller.hasLoadedGraph());
        assertTrue(output.contains("Modo stress offline"));
        assertTrue(output.contains("SMALL"));
    }

    @Test
    void exportWithoutGraphShouldReturnFriendlyMessage() {
        DesktopController controller = new DesktopController();

        assertEquals(DesktopController.NO_GRAPH_LOADED_MESSAGE, controller.exportGexfFiles());
    }

    @Test
    void configuredRepositoryShouldBeGiscus() {
        DesktopController controller = new DesktopController();

        assertEquals("giscus/giscus", controller.getConfiguredRepositorySlug());
    }

    @Test
    void offlineModeShouldReportProgressMessages() {
        DesktopController controller = new DesktopController();
        List<String> progress = new ArrayList<>();

        controller.runOfflineMode(progress::add);

        assertTrue(progress.stream().anyMatch(line -> line.contains("modo offline")));
        assertTrue(progress.stream().anyMatch(line -> line.contains("concluido")));
    }

    @Test
    void exportAfterOfflineShouldMentionGephiPresentationFile() {
        DesktopController controller = new DesktopController();
        controller.runOfflineMode();

        String output = controller.exportGexfFiles();

        assertTrue(output.contains("Arquivos GEXF atualizados em output/"));
        assertTrue(output.contains("integrated-graph-gephi.gexf"));
        assertTrue(output.contains("integrated-graph.gexf"));
    }

    @Test
    void exportAfterOfflineShouldWriteGexfFilesWithoutLosingGraph(@TempDir Path tempDir) throws Exception {
        DesktopController controller = new DesktopController();
        controller.setExportOutputDirectoryForTests(tempDir.toString());
        controller.runOfflineMode();
        assertTrue(controller.hasLoadedGraph());

        String exportOutput = controller.exportGexfFiles();
        assertTrue(exportOutput.contains("Arquivos GEXF atualizados em output/"));
        assertTrue(controller.hasLoadedGraph());
        assertFalse(controller.showSummary().contains(DesktopController.NO_GRAPH_LOADED_MESSAGE));

        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.INTEGRATED_GRAPH_GEPHI_FILE)));
        assertTrue(Files.exists(tempDir.resolve(GraphExportApplicationService.COMMENTS_GRAPH_FILE)));
    }

}
