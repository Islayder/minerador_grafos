package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.service.GraphBuildResult;

/**
 * Resultado do modo stress offline com tempos por fase.
 */
public final class StressExecutionResult {

    private final StressProfile profile;
    private final GraphBuildResult graphBuildResult;
    private final int interactionCount;
    private final long dataGenerationMillis;
    private final long graphBuildMillis;
    private final long lightAnalysisMillis;
    private final long totalMillis;
    private final long memoryBytes;
    private final boolean heavyMetricsSkipped;

    public StressExecutionResult(
            StressProfile profile,
            GraphBuildResult graphBuildResult,
            int interactionCount,
            long dataGenerationMillis,
            long graphBuildMillis,
            long lightAnalysisMillis,
            long totalMillis,
            long memoryBytes,
            boolean heavyMetricsSkipped) {
        this.profile = profile;
        this.graphBuildResult = graphBuildResult;
        this.interactionCount = interactionCount;
        this.dataGenerationMillis = dataGenerationMillis;
        this.graphBuildMillis = graphBuildMillis;
        this.lightAnalysisMillis = lightAnalysisMillis;
        this.totalMillis = totalMillis;
        this.memoryBytes = memoryBytes;
        this.heavyMetricsSkipped = heavyMetricsSkipped;
    }

    public StressProfile getProfile() {
        return profile;
    }

    public GraphBuildResult getGraphBuildResult() {
        return graphBuildResult;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public long getDataGenerationMillis() {
        return dataGenerationMillis;
    }

    public long getGraphBuildMillis() {
        return graphBuildMillis;
    }

    public long getLightAnalysisMillis() {
        return lightAnalysisMillis;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public long getMemoryBytes() {
        return memoryBytes;
    }

    public boolean isHeavyMetricsSkipped() {
        return heavyMetricsSkipped;
    }

    public int getUserCount() {
        return graphBuildResult.getUserIndexMapper().getUserCount();
    }

    public int getIntegratedEdgeCount() {
        return graphBuildResult.getIntegratedGraph().getEdgeCount();
    }
}
