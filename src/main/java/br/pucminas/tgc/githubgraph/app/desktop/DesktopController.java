package br.pucminas.tgc.githubgraph.app.desktop;

import br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService;
import br.pucminas.tgc.githubgraph.app.DemoApplicationService;
import br.pucminas.tgc.githubgraph.app.RealCollectionApplicationService;
import br.pucminas.tgc.githubgraph.app.StressApplicationService;
import br.pucminas.tgc.githubgraph.app.StressExecutionResult;
import br.pucminas.tgc.githubgraph.app.StressProfile;
import br.pucminas.tgc.githubgraph.github.CollectionExecutionResult;
import br.pucminas.tgc.githubgraph.github.CollectionProfile;
import br.pucminas.tgc.githubgraph.github.CollectionPropertiesLoader;
import br.pucminas.tgc.githubgraph.github.CollectionReportFormatter;
import br.pucminas.tgc.githubgraph.github.EnvironmentLoader;
import br.pucminas.tgc.githubgraph.github.GitHubConfig;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphExportApplicationService;
import br.pucminas.tgc.githubgraph.service.GraphSummaryService;
import br.pucminas.tgc.githubgraph.service.OutputReportService;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Conecta a interface desktop aos servicos de aplicacao existentes (sem logica de grafo ou Swing).
 */
public final class DesktopController {

    static final String NO_GRAPH_LOADED_MESSAGE =
            "Nenhum grafo carregado. Execute a mineracao completa ou o modo offline.";

    static final String MINING_FAILURE_MESSAGE =
            "A mineracao falhou. Verifique internet, GITHUB_TOKEN e rate limit da API. "
                    + "Use o modo offline como contingencia.";

    private final DemoApplicationService demoApplicationService;
    private final RealCollectionApplicationService realCollectionApplicationService;
    private final GraphSummaryService graphSummaryService;
    private final GraphAnalysisService graphAnalysisService;
    private final GraphExportApplicationService graphExportApplicationService;
    private final OutputReportService outputReportService;
    private final CollectionPropertiesLoader collectionPropertiesLoader;
    private final StressApplicationService stressApplicationService;

    private GraphBuildResult lastBuildResult;
    private DataSourceStatus dataSourceStatus = DataSourceStatus.NONE;
    private StressProfile selectedStressProfile = StressProfile.SMALL;
    private String exportOutputDirectoryOverride;

    public DesktopController() {
        this(
                new DemoApplicationService(),
                new RealCollectionApplicationService(),
                new GraphSummaryService(),
                new GraphAnalysisService(),
                new GraphExportApplicationService(),
                new OutputReportService(),
                new CollectionPropertiesLoader(),
                new StressApplicationService());
    }

    DesktopController(
            DemoApplicationService demoApplicationService,
            RealCollectionApplicationService realCollectionApplicationService,
            GraphSummaryService graphSummaryService,
            GraphAnalysisService graphAnalysisService,
            GraphExportApplicationService graphExportApplicationService,
            OutputReportService outputReportService,
            CollectionPropertiesLoader collectionPropertiesLoader,
            StressApplicationService stressApplicationService) {
        this.demoApplicationService = demoApplicationService;
        this.realCollectionApplicationService = realCollectionApplicationService;
        this.graphSummaryService = graphSummaryService;
        this.graphAnalysisService = graphAnalysisService;
        this.graphExportApplicationService = graphExportApplicationService;
        this.outputReportService = outputReportService;
        this.collectionPropertiesLoader = collectionPropertiesLoader;
        this.stressApplicationService = stressApplicationService;
    }

    public DataSourceStatus getDataSourceStatus() {
        return dataSourceStatus;
    }

    public StressProfile getSelectedStressProfile() {
        return selectedStressProfile;
    }

    public void setSelectedStressProfile(StressProfile profile) {
        this.selectedStressProfile = profile;
    }

    public boolean hasLoadedGraph() {
        return lastBuildResult != null;
    }

    public CollectionProfile getConfiguredProfile() {
        return collectionPropertiesLoader.loadConfiguredProfile();
    }

    public String getConfiguredRepositorySlug() {
        return getConfiguredProfile().getRepositorySlug();
    }

    public String getStatusLine() {
        CollectionProfile profile = getConfiguredProfile();
        return "Origem: " + dataSourceStatus.getDescription()
                + " | Alvo: " + profile.getRepositorySlug()
                + " | Modo: FULL_REPOSITORY";
    }

    public String getTokenStatusLine() {
        String token = EnvironmentLoader.loadGitHubToken();
        if (token != null && !token.isBlank()) {
            return "GITHUB_TOKEN: encontrado (variavel de ambiente ou arquivo .env).";
        }
        return "GITHUB_TOKEN: nao encontrado — mineracao pode falhar por limite da API publica.";
    }

    public String describeMiningConfiguration() {
        CollectionProfile profile = getConfiguredProfile();
        return profile.toHumanReadableString() + System.lineSeparator() + CollectionReportFormatter.API_BOTTLENECK_MESSAGE;
    }

    public String mineFullRepository() {
        return mineFullRepository(ignore -> {
        });
    }

    public String mineFullRepository(Consumer<String> progress) {
        CollectionProfile profile = getConfiguredProfile();
        boolean tokenPresent = GitHubConfig.fromProfile(profile).hasToken();
        String slug = profile.getRepositorySlug();

        progress.accept("Iniciando mineracao completa de " + slug + "...");
        progress.accept("Modo: FULL_REPOSITORY");
        progress.accept(profile.isCacheEnabled()
                ? "Cache: ativado (cache/github/)"
                : "Cache: desligado");
        progress.accept("perPage: " + profile.getPerPage() + " | concurrency: " + profile.getConcurrency());
        progress.accept(CollectionReportFormatter.formatPreCollection(profile, tokenPresent).trim());

        try {
            CollectionExecutionResult executionResult = realCollectionApplicationService.collectAndBuild(
                    profile,
                    progress::accept);
            lastBuildResult = executionResult.getGraphBuildResult();
            dataSourceStatus = DataSourceStatus.REAL;

            progress.accept("Mineracao de " + slug + " concluida com sucesso.");
            progress.accept(CollectionReportFormatter.formatPostCollection(executionResult).trim());
            progress.accept("Arquivo recomendado para Gephi: output/"
                    + GraphExportApplicationService.INTEGRATED_GRAPH_GEPHI_FILE);

            StringBuilder output = new StringBuilder();
            output.append("=== Resultado da mineracao ===").append(System.lineSeparator());
            output.append(graphSummaryService.summarize(lastBuildResult));
            return output.toString();
        } catch (Exception exception) {
            progress.accept(MINING_FAILURE_MESSAGE);
            progress.accept(RealCollectionApplicationService.friendlyErrorMessage(exception));
            return MINING_FAILURE_MESSAGE + System.lineSeparator()
                    + RealCollectionApplicationService.friendlyErrorMessage(exception);
        }
    }

    public String runOfflineMode() {
        return runOfflineMode(ignore -> {
        });
    }

    public String runOfflineMode(Consumer<String> progress) {
        progress.accept("Iniciando modo offline (dados simulados)...");
        lastBuildResult = demoApplicationService.runDemo();
        dataSourceStatus = DataSourceStatus.OFFLINE;
        progress.accept("Modo offline concluido.");
        return """
                Modo offline executado (plano B / contingencia).
                Dados simulados — nao substitui a mineracao real.

                """ + graphSummaryService.summarize(lastBuildResult);
    }

    public String runStressOfflineMode() {
        return runStressOfflineMode(ignore -> {
        });
    }

    public String runStressOfflineMode(Consumer<String> progress) {
        progress.accept("Iniciando stress offline: perfil " + selectedStressProfile + "...");
        StressExecutionResult result = stressApplicationService.run(selectedStressProfile);
        lastBuildResult = result.getGraphBuildResult();
        dataSourceStatus = DataSourceStatus.OFFLINE;
        progress.accept("Stress offline concluido.");
        StringBuilder output = new StringBuilder();
        output.append(StressApplicationService.formatReport(result));
        if (selectedStressProfile == StressProfile.SMALL || selectedStressProfile == StressProfile.MEDIUM) {
            output.append(System.lineSeparator()).append(graphSummaryService.summarize(lastBuildResult));
        }
        return output.toString();
    }

    public String showSummary() {
        return showSummary(ignore -> {
        });
    }

    public String showSummary(Consumer<String> progress) {
        if (lastBuildResult == null) {
            return NO_GRAPH_LOADED_MESSAGE;
        }
        progress.accept("Gerando resumo dos grafos...");
        return graphSummaryService.summarize(lastBuildResult);
    }

    public String analyzeIntegratedGraph() {
        return analyzeIntegratedGraph(ignore -> {
        });
    }

    public String analyzeIntegratedGraph(Consumer<String> progress) {
        if (lastBuildResult == null) {
            return NO_GRAPH_LOADED_MESSAGE;
        }
        progress.accept("Analisando grafo integrado...");
        var report = graphAnalysisService.analyze(lastBuildResult);
        progress.accept("Analise concluida.");
        return """
                Analise do grafo integrado:

                """ + graphAnalysisService.formatReport(report, lastBuildResult.getUserIndexMapper());
    }

    public String exportGexfFiles() {
        return exportGexfFiles(ignore -> {
        });
    }

    void setExportOutputDirectoryForTests(String outputDirectory) {
        this.exportOutputDirectoryOverride = outputDirectory;
    }

    public String exportGexfFiles(Consumer<String> progress) {
        if (lastBuildResult == null) {
            return NO_GRAPH_LOADED_MESSAGE;
        }
        String outputDirectory = resolveExportOutputDirectory();
        progress.accept("Exportando arquivos GEXF em " + outputDirectory + "...");
        List<Path> paths = graphExportApplicationService.exportAllGraphs(lastBuildResult, outputDirectory);
        progress.accept("Exportacao GEXF concluida (" + paths.size() + " arquivos).");
        return GraphExportApplicationService.formatGexfExportSuccessMessage();
    }

    private String resolveExportOutputDirectory() {
        if (exportOutputDirectoryOverride != null) {
            return exportOutputDirectoryOverride;
        }
        return GraphExportApplicationService.resolveDefaultOutputDirectory().toString();
    }

    public String generateTextReports() {
        return generateTextReports(ignore -> {
        });
    }

    public String generateTextReports(Consumer<String> progress) {
        if (lastBuildResult == null) {
            return NO_GRAPH_LOADED_MESSAGE;
        }
        progress.accept("Gerando relatorios em output/...");
        List<Path> paths = outputReportService.writeAllReports(lastBuildResult);
        progress.accept("Relatorios gerados (" + paths.size() + " arquivos).");
        String files = paths.stream()
                .map(path -> "  - " + path.toAbsolutePath().normalize())
                .collect(Collectors.joining(System.lineSeparator()));
        return """
                Relatorios gerados em output/:

                """ + files;
    }
}
