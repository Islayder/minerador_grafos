package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.analysis.DensityAnalyzer;
import br.pucminas.tgc.githubgraph.model.RepositoryData;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;
import br.pucminas.tgc.githubgraph.service.PerformanceTimer;

/**
 * Executa o modo stress offline: mede geracao de dados, construcao de grafos e analise leve.
 */
public final class StressApplicationService {

    private final GraphBuilderService graphBuilderService = new GraphBuilderService();
    private final DensityAnalyzer densityAnalyzer = new DensityAnalyzer();

    public StressExecutionResult run(StressProfile profile) {
        PerformanceTimer timer = new PerformanceTimer();

        RepositoryData repositoryData = StressDataFactory.create(profile);
        long dataGenerationMillis = timer.markMillis();

        GraphBuildResult graphBuildResult = graphBuilderService.build(repositoryData);
        long graphBuildMillis = timer.markMillis();

        boolean heavySkipped = !profile.runsHeavyMetricsAutomatically();
        if (!heavySkipped) {
            densityAnalyzer.density(graphBuildResult.getIntegratedGraph());
        }
        long lightAnalysisMillis = timer.markMillis();

        return new StressExecutionResult(
                profile,
                graphBuildResult,
                repositoryData.getInteractions().size(),
                dataGenerationMillis,
                graphBuildMillis,
                lightAnalysisMillis,
                timer.elapsedMillis(),
                PerformanceTimer.usedMemoryBytes(),
                heavySkipped);
    }

    public static String formatReport(StressExecutionResult result) {
        String metricsNote = result.isHeavyMetricsSkipped()
                ? "Metricas pesadas (closeness/betweenness) nao executadas automaticamente em EXTREME."
                : "Analise leve: densidade do grafo integrado calculada.";

        return """
                === Modo stress offline ===
                Mede limite interno (CPU/memoria). A coleta online mede tambem API/rede.
                Perfil: %s (%d usuarios, %d interacoes alvo)
                Tempo geracao dados: %d ms
                Tempo construcao grafos: %d ms
                Tempo analise leve: %d ms
                Tempo total: %d ms
                Usuarios (vertices): %d
                Interacoes: %d
                Arestas no grafo integrado: %d
                Memoria aproximada: %s
                %s
                """.formatted(
                result.getProfile(),
                result.getProfile().getUserCount(),
                result.getProfile().getInteractionCount(),
                result.getDataGenerationMillis(),
                result.getGraphBuildMillis(),
                result.getLightAnalysisMillis(),
                result.getTotalMillis(),
                result.getUserCount(),
                result.getInteractionCount(),
                result.getIntegratedEdgeCount(),
                PerformanceTimer.formatMemory(result.getMemoryBytes()),
                metricsNote);
    }
}
