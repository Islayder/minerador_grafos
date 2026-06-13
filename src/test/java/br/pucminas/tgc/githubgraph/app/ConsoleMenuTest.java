package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.github.CollectionPropertiesLoader;
import br.pucminas.tgc.githubgraph.service.GraphExportApplicationService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleMenuTest {

    @Test
    void exportAllGraphsWithoutLoadedDataDoesNotInvokeDemo() {
        AtomicInteger demoInvocations = new AtomicInteger();
        DemoApplicationService demo = new DemoApplicationService() {
            @Override
            public br.pucminas.tgc.githubgraph.service.GraphBuildResult runDemo() {
                demoInvocations.incrementAndGet();
                return super.runDemo();
            }
        };

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ConsoleMenu menu = new ConsoleMenu(
                new Scanner(""),
                new PrintStream(buffer, true, StandardCharsets.UTF_8),
                demo,
                new RealCollectionApplicationService(),
                new br.pucminas.tgc.githubgraph.service.GraphSummaryService(),
                new GraphExportApplicationService(),
                new br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService(),
                new br.pucminas.tgc.githubgraph.service.OutputReportService(),
                new CollectionPropertiesLoader(),
                new br.pucminas.tgc.githubgraph.app.StressApplicationService());

        menu.handleMenuOption("6");

        assertEquals(0, demoInvocations.get());
        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains(ConsoleMenu.NO_GRAPH_LOADED_MESSAGE));
    }

    @Test
    void showSummaryAfterDemoUsesLoadedResult() {
        AtomicInteger demoInvocations = new AtomicInteger();
        DemoApplicationService demo = new DemoApplicationService() {
            @Override
            public br.pucminas.tgc.githubgraph.service.GraphBuildResult runDemo() {
                demoInvocations.incrementAndGet();
                return super.runDemo();
            }
        };

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ConsoleMenu menu = new ConsoleMenu(
                new Scanner(""),
                new PrintStream(buffer, true, StandardCharsets.UTF_8),
                demo,
                new RealCollectionApplicationService(),
                new br.pucminas.tgc.githubgraph.service.GraphSummaryService(),
                new GraphExportApplicationService(),
                new br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService(),
                new br.pucminas.tgc.githubgraph.service.OutputReportService(),
                new CollectionPropertiesLoader(),
                new br.pucminas.tgc.githubgraph.app.StressApplicationService());

        menu.handleMenuOption("3");
        menu.handleMenuOption("4");

        assertEquals(1, demoInvocations.get());
        String output = buffer.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains(ConsoleMenu.NO_GRAPH_LOADED_MESSAGE));
        assertTrue(output.contains("Grafo integrado ponderado"));
    }
}
