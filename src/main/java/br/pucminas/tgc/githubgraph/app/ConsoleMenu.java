package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.analysis.GraphAnalysisService;
import br.pucminas.tgc.githubgraph.github.CollectionExecutionResult;
import br.pucminas.tgc.githubgraph.github.CollectionProfile;
import br.pucminas.tgc.githubgraph.github.CollectionPropertiesLoader;
import br.pucminas.tgc.githubgraph.github.CollectionReportFormatter;
import br.pucminas.tgc.githubgraph.github.GitHubConfig;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphExportApplicationService;
import br.pucminas.tgc.githubgraph.service.GraphSummaryService;
import br.pucminas.tgc.githubgraph.service.OutputReportService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Menu interativo da aplicação CLI.
 * <p>
 * Fluxo principal: mineracao completa do repositorio configurado (opcao 2).
 * Plano B: demonstração offline com dados simulados (opção 3).
 */
public final class ConsoleMenu {

    static final String NO_GRAPH_LOADED_MESSAGE =
            "Nenhum grafo carregado. Execute primeiro a opcao 2 (mineracao completa) "
                    + "ou a opcao 3 (demonstracao offline).";

    private final DemoApplicationService demoApplicationService;
    private final RealCollectionApplicationService realCollectionApplicationService;
    private final GraphSummaryService graphSummaryService;
    private final GraphExportApplicationService graphExportApplicationService;
    private final GraphAnalysisService graphAnalysisService;
    private final OutputReportService outputReportService;
    private final CollectionPropertiesLoader collectionPropertiesLoader;
    private final StressApplicationService stressApplicationService;
    private final Scanner scanner;
    private final PrintStream out;

    private GraphBuildResult lastBuildResult;

    public ConsoleMenu() {
        this(new Scanner(System.in));
    }

    public ConsoleMenu(Scanner scanner) {
        this(scanner, System.out);
    }

    ConsoleMenu(Scanner scanner, PrintStream out) {
        this(scanner, out,
                new DemoApplicationService(),
                new RealCollectionApplicationService(),
                new GraphSummaryService(),
                new GraphExportApplicationService(),
                new GraphAnalysisService(),
                new OutputReportService(),
                new CollectionPropertiesLoader(),
                new StressApplicationService());
    }

    ConsoleMenu(
            Scanner scanner,
            PrintStream out,
            DemoApplicationService demoApplicationService,
            RealCollectionApplicationService realCollectionApplicationService,
            GraphSummaryService graphSummaryService,
            GraphExportApplicationService graphExportApplicationService,
            GraphAnalysisService graphAnalysisService,
            OutputReportService outputReportService,
            CollectionPropertiesLoader collectionPropertiesLoader,
            StressApplicationService stressApplicationService) {
        this.scanner = scanner;
        this.out = out;
        this.demoApplicationService = demoApplicationService;
        this.realCollectionApplicationService = realCollectionApplicationService;
        this.graphSummaryService = graphSummaryService;
        this.graphExportApplicationService = graphExportApplicationService;
        this.graphAnalysisService = graphAnalysisService;
        this.outputReportService = outputReportService;
        this.collectionPropertiesLoader = collectionPropertiesLoader;
        this.stressApplicationService = stressApplicationService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> showProjectInfo();
                case "2" -> runFullRepositoryMining();
                case "3" -> runDemo();
                case "4" -> showLoadedGraphSummary();
                case "5" -> analyzeIntegratedGraph();
                case "6" -> exportAllGraphs();
                case "7" -> generateOutputReports();
                case "8" -> running = false;
                case "9" -> runStressOffline();
                default -> out.println("Opcao invalida. Escolha um numero de 1 a 9 (8 para sair).");
            }
            out.println();
        }
        out.println("Encerrando github-collaboration-graph. Até logo.");
    }

    void handleMenuOption(String option) {
        switch (option) {
            case "1" -> showProjectInfo();
            case "2" -> runFullRepositoryMining();
            case "3" -> runDemo();
            case "4" -> showLoadedGraphSummary();
            case "5" -> analyzeIntegratedGraph();
            case "6" -> exportAllGraphs();
            case "7" -> generateOutputReports();
            case "9" -> runStressOffline();
            default -> throw new IllegalArgumentException("Opção não suportada em teste: " + option);
        }
    }

    private void printMenu() {
        out.println("=== github-collaboration-graph ===");
        CollectionProfile configured = collectionPropertiesLoader.loadConfiguredProfile();
        out.println("Repositorio configurado: " + configured.getRepositorySlug()
                + " (mineracao completa FULL_REPOSITORY)");
        out.println("1. Mostrar informacoes do projeto");
        out.println("2. Minerar repositorio inteiro configurado (fluxo principal)");
        out.println("3. Executar demonstracao offline (plano B)");
        out.println("4. Exibir resumo dos grafos carregados");
        out.println("5. Analisar grafo integrado");
        out.println("6. Exportar todos os grafos para GEXF");
        out.println("7. Gerar relatórios em output/");
        out.println("8. Sair");
        out.println("9. Modo stress offline (sem API; mede limite interno)");
        out.print("Escolha uma opção: ");
    }

    private void showProjectInfo() {
        CollectionProfile profile = collectionPropertiesLoader.loadConfiguredProfile();
        out.println("""
                Ferramenta academica para analisar colaboracao em repositorios GitHub publicos.
                Repositario padrao: giscus/giscus (viavel para mineracao integral na apresentacao).
                facebook/react e facebook/docusaurus foram evitados por serem grandes demais para mineracao completa ao vivo.

                Grafos simples e direcionados implementados manualmente (sem bibliotecas de grafos).

                Fluxo principal: opcao 2 — mineracao FULL_REPOSITORY (pagina ate o fim, sem amostragem).
                Plano B: opcao 3 — demonstracao offline. Opcao 9 — stress offline (limite interno).

                Configure GITHUB_TOKEN (.env ou variavel de ambiente). Cache local em cache/github/ (config/collection.properties).
                Limite pratico online: API GitHub, internet, token e rate limit.

                Config atual: %s
                """.formatted(profile.toHumanReadableString()));
    }

    private Optional<GraphBuildResult> requireBuildResult() {
        if (lastBuildResult == null) {
            out.println(NO_GRAPH_LOADED_MESSAGE);
            return Optional.empty();
        }
        return Optional.of(lastBuildResult);
    }

    private void runFullRepositoryMining() {
        CollectionProfile profile = collectionPropertiesLoader.loadConfiguredProfile();
        GitHubConfig config = GitHubConfig.fromProfile(profile);

        out.println(CollectionReportFormatter.formatPreCollection(profile, config.hasToken()));

        try {
            CollectionExecutionResult executionResult = realCollectionApplicationService.collectAndBuild(
                    profile,
                    out::println);
            lastBuildResult = executionResult.getGraphBuildResult();
            out.println(CollectionReportFormatter.formatPostCollection(executionResult));
            out.println(graphSummaryService.summarize(lastBuildResult));
        } catch (Exception exception) {
            out.println("A mineracao falhou. Verifique internet, GITHUB_TOKEN e rate limit. "
                    + "Use o modo offline como contingencia.");
            out.println(RealCollectionApplicationService.friendlyErrorMessage(exception));
        }
    }

    private void runDemo() {
        out.println("Executando demonstracao offline (dados simulados; nao substitui mineracao real)...");
        lastBuildResult = demoApplicationService.runDemo();
        out.println(graphSummaryService.summarize(lastBuildResult));
    }

    private void runStressOffline() {
        StressProfile profile = chooseStressProfile();
        out.println("Modo stress offline: gera dados artificiais e constroi grafos sem internet.");
        StressExecutionResult result = stressApplicationService.run(profile);
        lastBuildResult = result.getGraphBuildResult();
        out.println(StressApplicationService.formatReport(result));
        if (profile == StressProfile.SMALL || profile == StressProfile.MEDIUM) {
            out.println(graphSummaryService.summarize(lastBuildResult));
        }
    }

    private StressProfile chooseStressProfile() {
        out.println("Perfis stress offline:");
        out.println("1. SMALL (100 usuarios, 500 interacoes)");
        out.println("2. MEDIUM (1000 usuarios, 10000 interacoes)");
        out.println("3. LARGE (5000 usuarios, 50000 interacoes)");
        out.println("4. EXTREME (10000 usuarios, 100000 interacoes; sem metricas pesadas automaticas)");
        out.print("Escolha o perfil [1-4] (padrao 1): ");
        String choice = scanner.nextLine().trim();
        return switch (choice) {
            case "2" -> StressProfile.MEDIUM;
            case "3" -> StressProfile.LARGE;
            case "4" -> StressProfile.EXTREME;
            case "", "1" -> StressProfile.SMALL;
            default -> {
                out.println("Opcao invalida. Usando SMALL.");
                yield StressProfile.SMALL;
            }
        };
    }

    private void showLoadedGraphSummary() {
        requireBuildResult().ifPresent(result -> out.println(graphSummaryService.summarize(result)));
    }

    private void exportAllGraphs() {
        requireBuildResult().ifPresent(result -> {
            graphExportApplicationService.exportAllGraphs(result);
            out.println(GraphExportApplicationService.formatGexfExportSuccessMessage());
        });
    }

    private void analyzeIntegratedGraph() {
        requireBuildResult().ifPresent(result -> {
            out.println("Analisando grafo integrado...");
            var report = graphAnalysisService.analyze(result);
            out.println(graphAnalysisService.formatReport(report, result.getUserIndexMapper()));
        });
    }

    private void generateOutputReports() {
        requireBuildResult().ifPresent(result -> {
            List<Path> reportPaths = outputReportService.writeAllReports(result);
            out.println("Arquivos gerados em output/:");
            for (Path path : reportPaths) {
                out.println("  - " + path.getFileName());
            }
        });
    }
}
