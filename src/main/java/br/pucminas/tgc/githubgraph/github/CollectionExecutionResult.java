package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.service.GraphBuildResult;

/**
 * Resultado de uma coleta real com metricas de tempo e volume.
 */
public final class CollectionExecutionResult {

    private final GraphBuildResult graphBuildResult;
    private final CollectionProfile profile;
    private final long totalDurationMillis;
    private final long collectionPhaseMillis;
    private final long buildPhaseMillis;
    private final int interactionCount;
    private final int userCount;
    private final int integratedEdgeCount;
    private final int apiRequests;
    private final int cacheHits;
    private final boolean cacheEnabled;
    private final CollectionStatistics collectionStatistics;

    public CollectionExecutionResult(
            GraphBuildResult graphBuildResult,
            CollectionProfile profile,
            long totalDurationMillis,
            long collectionPhaseMillis,
            long buildPhaseMillis,
            int interactionCount,
            int userCount,
            int integratedEdgeCount,
            int apiRequests,
            int cacheHits,
            boolean cacheEnabled,
            CollectionStatistics collectionStatistics) {
        this.graphBuildResult = graphBuildResult;
        this.profile = profile;
        this.totalDurationMillis = totalDurationMillis;
        this.collectionPhaseMillis = collectionPhaseMillis;
        this.buildPhaseMillis = buildPhaseMillis;
        this.interactionCount = interactionCount;
        this.userCount = userCount;
        this.integratedEdgeCount = integratedEdgeCount;
        this.apiRequests = apiRequests;
        this.cacheHits = cacheHits;
        this.cacheEnabled = cacheEnabled;
        this.collectionStatistics = collectionStatistics;
    }

    public GraphBuildResult getGraphBuildResult() {
        return graphBuildResult;
    }

    public CollectionProfile getProfile() {
        return profile;
    }

    public long getDurationMillis() {
        return totalDurationMillis;
    }

    public long getCollectionPhaseMillis() {
        return collectionPhaseMillis;
    }

    public long getBuildPhaseMillis() {
        return buildPhaseMillis;
    }

    public double getDurationSeconds() {
        return totalDurationMillis / 1000.0;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getIntegratedEdgeCount() {
        return integratedEdgeCount;
    }

    public int getApiRequests() {
        return apiRequests;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public CollectionStatistics getCollectionStatistics() {
        return collectionStatistics;
    }
}
