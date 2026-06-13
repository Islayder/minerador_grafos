package br.pucminas.tgc.githubgraph.github;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metricas de coleta (chamadas, pulos seguros e tempos por fase).
 */
public final class CollectionStatistics {

    private final AtomicInteger issuesListed = new AtomicInteger();
    private final AtomicInteger pullRequestsListed = new AtomicInteger();
    private final AtomicInteger issueListPageCalls = new AtomicInteger();
    private final AtomicInteger pullRequestListPageCalls = new AtomicInteger();

    private final AtomicInteger issueCommentApiCalls = new AtomicInteger();
    private final AtomicInteger issueCommentItemsSkipped = new AtomicInteger();
    private final AtomicInteger pullRequestCommentApiCalls = new AtomicInteger();
    private final AtomicInteger pullRequestCommentItemsSkipped = new AtomicInteger();
    private final AtomicInteger reviewApiCalls = new AtomicInteger();

    private final AtomicLong issueListingNanos = new AtomicLong();
    private final AtomicLong pullRequestListingNanos = new AtomicLong();
    private final AtomicLong issueCommentsNanos = new AtomicLong();
    private final AtomicLong pullRequestCommentsNanos = new AtomicLong();
    private final AtomicLong reviewsNanos = new AtomicLong();

    public void recordIssuesListed(int count) {
        issuesListed.addAndGet(count);
    }

    public void recordPullRequestsListed(int count) {
        pullRequestsListed.addAndGet(count);
    }

    public void recordIssueListPageCall() {
        issueListPageCalls.incrementAndGet();
    }

    public void recordPullRequestListPageCall() {
        pullRequestListPageCalls.incrementAndGet();
    }

    public void addIssueListingNanos(long nanos) {
        issueListingNanos.addAndGet(nanos);
    }

    public void addPullRequestListingNanos(long nanos) {
        pullRequestListingNanos.addAndGet(nanos);
    }

    public void recordIssueCommentApiCall() {
        issueCommentApiCalls.incrementAndGet();
    }

    public void recordIssueCommentSkipped() {
        issueCommentItemsSkipped.incrementAndGet();
    }

    public void addIssueCommentsNanos(long nanos) {
        issueCommentsNanos.addAndGet(nanos);
    }

    public void recordPullRequestCommentApiCall() {
        pullRequestCommentApiCalls.incrementAndGet();
    }

    public void recordPullRequestCommentSkipped() {
        pullRequestCommentItemsSkipped.incrementAndGet();
    }

    public void addPullRequestCommentsNanos(long nanos) {
        pullRequestCommentsNanos.addAndGet(nanos);
    }

    public void recordReviewApiCall() {
        reviewApiCalls.incrementAndGet();
    }

    public void addReviewsNanos(long nanos) {
        reviewsNanos.addAndGet(nanos);
    }

    public int getIssuesListed() {
        return issuesListed.get();
    }

    public int getPullRequestsListed() {
        return pullRequestsListed.get();
    }

    public int getIssueListPageCalls() {
        return issueListPageCalls.get();
    }

    public int getPullRequestListPageCalls() {
        return pullRequestListPageCalls.get();
    }

    public int getIssueCommentApiCalls() {
        return issueCommentApiCalls.get();
    }

    public int getIssueCommentItemsSkipped() {
        return issueCommentItemsSkipped.get();
    }

    public int getPullRequestCommentApiCalls() {
        return pullRequestCommentApiCalls.get();
    }

    public int getPullRequestCommentItemsSkipped() {
        return pullRequestCommentItemsSkipped.get();
    }

    public int getReviewApiCalls() {
        return reviewApiCalls.get();
    }

    public long getIssueListingMillis() {
        return issueListingNanos.get() / 1_000_000L;
    }

    public long getPullRequestListingMillis() {
        return pullRequestListingNanos.get() / 1_000_000L;
    }

    public long getIssueCommentsMillis() {
        return issueCommentsNanos.get() / 1_000_000L;
    }

    public long getPullRequestCommentsMillis() {
        return pullRequestCommentsNanos.get() / 1_000_000L;
    }

    public long getReviewsMillis() {
        return reviewsNanos.get() / 1_000_000L;
    }

    public int getEstimatedCallsAvoided() {
        return issueCommentItemsSkipped.get() + pullRequestCommentItemsSkipped.get();
    }

    public int getMeasuredApiCalls() {
        return issueListPageCalls.get()
                + pullRequestListPageCalls.get()
                + issueCommentApiCalls.get()
                + pullRequestCommentApiCalls.get()
                + reviewApiCalls.get();
    }

    public String formatPerformanceSummary(long buildPhaseMillis, long totalMillis) {
        return """
                Issues listadas: %d
                Pull requests listados: %d
                Comentarios de issues: %d chamadas, %d puladas por comments=0
                Comentarios de PRs: %d chamadas, %d puladas por comments=0
                Reviews de PRs: %d chamadas
                Chamadas totais estimadas evitadas: %d
                Tempo listagem issues: %s
                Tempo listagem PRs: %s
                Tempo comentarios issues: %s
                Tempo comentarios PRs: %s
                Tempo reviews: %s
                Tempo construcao dos grafos: %s
                Tempo total: %s
                """.formatted(
                issuesListed.get(),
                pullRequestsListed.get(),
                issueCommentApiCalls.get(),
                issueCommentItemsSkipped.get(),
                pullRequestCommentApiCalls.get(),
                pullRequestCommentItemsSkipped.get(),
                reviewApiCalls.get(),
                getEstimatedCallsAvoided(),
                formatDuration(getIssueListingMillis()),
                formatDuration(getPullRequestListingMillis()),
                formatDuration(getIssueCommentsMillis()),
                formatDuration(getPullRequestCommentsMillis()),
                formatDuration(getReviewsMillis()),
                formatDuration(buildPhaseMillis),
                formatDuration(totalMillis)).stripTrailing();
    }

    static String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes == 0) {
            return seconds + "s";
        }
        return minutes + "min" + seconds + "s";
    }
}
