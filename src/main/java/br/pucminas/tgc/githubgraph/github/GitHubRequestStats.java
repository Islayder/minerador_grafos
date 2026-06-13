package br.pucminas.tgc.githubgraph.github;

/**
 * Contadores de requisicoes HTTP versus cache durante a coleta.
 */
public final class GitHubRequestStats {

    private int apiRequests;
    private int cacheHits;

    public void recordApiRequest() {
        apiRequests++;
    }

    public void recordCacheHit() {
        cacheHits++;
    }

    public int getApiRequests() {
        return apiRequests;
    }

    public int getCacheHits() {
        return cacheHits;
    }
}
