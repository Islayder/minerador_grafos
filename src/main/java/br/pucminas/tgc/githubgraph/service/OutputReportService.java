package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera arquivos textuais de resumo e análise no diretório de saída.
 */
public final class OutputReportService {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "output";
    public static final String GRAPH_SUMMARY_FILE = "graph-summary.txt";
    public static final String INTEGRATED_ANALYSIS_FILE = "integrated-analysis.txt";
    public static final String COMPARATIVE_ANALYSIS_FILE = "comparative-analysis.txt";

    private final GraphSummaryService graphSummaryService;
    private final GraphAnalysisService graphAnalysisService;
    private final ComparativeGraphAnalysisService comparativeGraphAnalysisService;

    public OutputReportService() {
        this(new GraphSummaryService(), new GraphAnalysisService(), new ComparativeGraphAnalysisService());
    }

    public OutputReportService(
            GraphSummaryService graphSummaryService,
            GraphAnalysisService graphAnalysisService,
            ComparativeGraphAnalysisService comparativeGraphAnalysisService) {
        this.graphSummaryService = graphSummaryService;
        this.graphAnalysisService = graphAnalysisService;
        this.comparativeGraphAnalysisService = comparativeGraphAnalysisService;
    }

    public Path writeGraphSummary(GraphBuildResult result, String outputDirectory) {
        String content = graphSummaryService.summarize(result);
        return writeFile(outputDirectory, GRAPH_SUMMARY_FILE, content);
    }

    public Path writeIntegratedAnalysis(GraphBuildResult result, String outputDirectory) {
        var report = graphAnalysisService.analyze(result);
        String content = graphAnalysisService.formatReport(report, result.getUserIndexMapper());
        return writeFile(outputDirectory, INTEGRATED_ANALYSIS_FILE, content);
    }

    public Path writeComparativeAnalysis(GraphBuildResult result, String outputDirectory) {
        String content = comparativeGraphAnalysisService.compare(result);
        return writeFile(outputDirectory, COMPARATIVE_ANALYSIS_FILE, content);
    }

    public List<Path> writeAllReports(GraphBuildResult result, String outputDirectory) {
        List<Path> written = new ArrayList<>();
        written.add(writeGraphSummary(result, outputDirectory));
        written.add(writeIntegratedAnalysis(result, outputDirectory));
        written.add(writeComparativeAnalysis(result, outputDirectory));
        return List.copyOf(written);
    }

    public List<Path> writeAllReports(GraphBuildResult result) {
        return writeAllReports(result, DEFAULT_OUTPUT_DIRECTORY);
    }

    private Path writeFile(String outputDirectory, String fileName, String content) {
        Path directory = Path.of(outputDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            Path filePath = directory.resolve(fileName);
            Files.writeString(filePath, content);
            return filePath;
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao escrever arquivo de relatório: " + fileName, exception);
        }
    }
}
