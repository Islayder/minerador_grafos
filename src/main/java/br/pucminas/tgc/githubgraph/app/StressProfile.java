package br.pucminas.tgc.githubgraph.app;

/**
 * Perfis de volume para o modo stress offline (sem API GitHub).
 */
public enum StressProfile {
    SMALL(100, 500),
    MEDIUM(1000, 10_000),
    LARGE(5000, 50_000),
    EXTREME(10_000, 100_000);

    private final int userCount;
    private final int interactionCount;

    StressProfile(int userCount, int interactionCount) {
        this.userCount = userCount;
        this.interactionCount = interactionCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public boolean runsHeavyMetricsAutomatically() {
        return this != EXTREME;
    }
}
