package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.export.GephiPresentationExporter;
import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporta grafos para arquivos GEXF compatíveis com o Gephi.
 */
public final class GraphExportApplicationService {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "output";
    public static final String INTEGRATED_GRAPH_FILE = "integrated-graph.gexf";
    public static final String INTEGRATED_GRAPH_GEPHI_FILE = "integrated-graph-gephi.gexf";
    public static final String COMMENTS_GRAPH_FILE = "comments-graph.gexf";
    public static final String ISSUE_CLOSURE_GRAPH_FILE = "issue-closure-graph.gexf";
    public static final String PULL_REQUEST_GRAPH_FILE = "pull-request-graph.gexf";
    public static final String DEFAULT_INTEGRATED_OUTPUT_PATH =
            DEFAULT_OUTPUT_DIRECTORY + "/" + INTEGRATED_GRAPH_FILE;

    public List<Path> exportAllGraphs(GraphBuildResult result, String outputDirectory) {
        Path directory = ensureDirectory(outputDirectory);
        List<Path> exported = new ArrayList<>();
        exported.add(exportGraph(result.getCommentsGraph(), directory.resolve(COMMENTS_GRAPH_FILE)));
        exported.add(exportGraph(result.getIssueClosureGraph(), directory.resolve(ISSUE_CLOSURE_GRAPH_FILE)));
        exported.add(exportGraph(result.getPullRequestGraph(), directory.resolve(PULL_REQUEST_GRAPH_FILE)));
        exported.add(exportGraph(result.getIntegratedGraph(), directory.resolve(INTEGRATED_GRAPH_FILE)));
        exported.add(exportIntegratedPresentationGraph(result, directory.resolve(INTEGRATED_GRAPH_GEPHI_FILE)));
        return List.copyOf(exported);
    }

    public Path exportIntegratedPresentationGraph(GraphBuildResult result, Path outputPath) {
        Path path = outputPath.toAbsolutePath().normalize();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            GephiPresentationExporter.exportIntegratedPresentationGraph(result, path);
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao exportar grafo de apresentacao para " + path, exception);
        }
    }

    public Path exportIntegratedPresentationGraph(GraphBuildResult result) {
        return exportIntegratedPresentationGraph(
                result,
                Path.of(DEFAULT_OUTPUT_DIRECTORY, INTEGRATED_GRAPH_GEPHI_FILE));
    }

    public static Path resolveDefaultOutputDirectory() {
        return Path.of(System.getProperty("user.dir", "."))
                .resolve(DEFAULT_OUTPUT_DIRECTORY)
                .toAbsolutePath()
                .normalize();
    }

    public List<Path> exportAllGraphs(GraphBuildResult result) {
        return exportAllGraphs(result, resolveDefaultOutputDirectory().toString());
    }

    public static String formatGexfExportSuccessMessage() {
        String lineSeparator = System.lineSeparator();
        return """
                Arquivos GEXF atualizados em output/
                  - output/comments-graph.gexf
                  - output/issue-closure-graph.gexf
                  - output/pull-request-graph.gexf
                  - output/integrated-graph.gexf
                  - output/integrated-graph-gephi.gexf
                Arquivo recomendado para Gephi: output/integrated-graph-gephi.gexf"""
                .stripTrailing();
    }

    public Path exportIntegratedGraph(GraphBuildResult result, String outputPath) {
        return exportGraph(result.getIntegratedGraph(), Path.of(outputPath));
    }

    public Path exportIntegratedGraph(GraphBuildResult result) {
        return exportIntegratedGraph(result, DEFAULT_INTEGRATED_OUTPUT_PATH);
    }

    private Path exportGraph(AbstractGraph graph, Path outputPath) {
        Path path = outputPath.toAbsolutePath().normalize();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            graph.exportToGEPHI(path.toString());
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao exportar grafo para " + path, exception);
        }
    }

    private Path ensureDirectory(String outputDirectory) {
        Path directory = Path.of(outputDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            return directory;
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao criar diretório de saída: " + directory, exception);
        }
    }
}
