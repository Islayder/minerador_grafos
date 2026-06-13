package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.app.DemoDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputReportServiceTest {

    @Test
    void shouldWriteAllReportFiles(@TempDir Path tempDir) throws Exception {
        GraphBuildResult result = new GraphBuilderService().build(DemoDataFactory.createGiscusDemo());
        OutputReportService reportService = new OutputReportService();

        Path summary = reportService.writeGraphSummary(result, tempDir.toString());
        Path integrated = reportService.writeIntegratedAnalysis(result, tempDir.toString());
        Path comparative = reportService.writeComparativeAnalysis(result, tempDir.toString());

        assertTrue(Files.exists(summary));
        assertTrue(Files.exists(integrated));
        assertTrue(Files.exists(comparative));

        String summaryContent = Files.readString(summary);
        assertTrue(summaryContent.contains("Grafo de comentários"));
        assertTrue(summaryContent.contains("Grafo integrado ponderado"));
        assertTrue(summaryContent.contains("peso"));

        String integratedContent = Files.readString(integrated);
        assertTrue(integratedContent.contains("=== 2. Densidade ==="));
        assertTrue(integratedContent.contains("=== 4. Eigenvector Centrality ==="));
        assertTrue(integratedContent.contains("=== 7. Clustering Coefficient ==="));
        assertTrue(integratedContent.contains("=== 11. Aberturas de Pull Requests por usuário ==="));
        assertTrue(integratedContent.contains("alice"));

        String comparativeContent = Files.readString(comparative);
        assertTrue(comparativeContent.contains("Análise comparativa"));
        assertTrue(comparativeContent.contains("issueClosureGraph"));
        assertTrue(comparativeContent.contains("Grafo mais denso"));
    }
}
