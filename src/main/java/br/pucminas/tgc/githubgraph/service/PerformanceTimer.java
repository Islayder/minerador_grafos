package br.pucminas.tgc.githubgraph.service;

/**
 * Cronometro simples para medicao de fases do pipeline.
 */
public final class PerformanceTimer {

    private final long startNanos = System.nanoTime();
    private long lastMarkNanos = startNanos;

    public long elapsedMillis() {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    public long markMillis() {
        long now = System.nanoTime();
        long delta = (now - lastMarkNanos) / 1_000_000L;
        lastMarkNanos = now;
        return delta;
    }

    public static long usedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
