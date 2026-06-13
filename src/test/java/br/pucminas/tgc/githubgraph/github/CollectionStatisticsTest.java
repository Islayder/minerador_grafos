package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionStatisticsTest {

    @Test
    void shouldFormatPerformanceSummaryWithCounters() {
        CollectionStatistics statistics = new CollectionStatistics();
        statistics.recordIssuesListed(381);
        statistics.recordPullRequestsListed(1053);
        statistics.recordIssueCommentApiCall();
        statistics.recordIssueCommentSkipped();
        statistics.recordPullRequestCommentApiCall();
        statistics.recordPullRequestCommentSkipped();
        statistics.recordReviewApiCall();

        String summary = statistics.formatPerformanceSummary(12_000, 155_000);

        assertTrue(summary.contains("Issues listadas: 381"));
        assertTrue(summary.contains("Pull requests listados: 1053"));
        assertTrue(summary.contains("Comentarios de issues: 1 chamadas, 1 puladas"));
        assertTrue(summary.contains("Reviews de PRs: 1 chamadas"));
        assertEquals(2, statistics.getEstimatedCallsAvoided());
    }
}
