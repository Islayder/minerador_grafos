package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.RepositoryData;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubDataCollectorTest {

    @Test
    void collectFullRepositoryShouldPaginateUntilEmptyPage() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 1 },
                  { "number": 2, "user": { "login": "carol" }, "comments": 1 }
                ]
                """);
        fakeClient.registerIssuesPage(2, "[]");
        fakeClient.registerIssueCommentsPage(1, 1, """
                [ { "user": { "login": "alice" } } ]
                """);
        fakeClient.registerIssueCommentsPage(2, 1, """
                [ { "user": { "login": "dave" } } ]
                """);

        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 10, "user": { "login": "alice" }, "merged_by": { "login": "bob" },
                    "merged_at": "2024-01-01T00:00:00Z", "comments": 0 },
                  { "number": 11, "user": { "login": "erin" }, "comments": 0 }
                ]
                """);
        fakeClient.registerPullRequestsPage(2, "[]");
        fakeClient.registerPullRequestReviewsPage(10, 1, """
                [ { "user": { "login": "carol" }, "state": "APPROVED" } ]
                """);
        fakeClient.registerPullRequestReviewsPage(11, 1, "[]");

        CollectionProfile profile = CollectionProfile.fullRepository(
                "giscus", "giscus", 2, 1, false, "teste");
        GitHubConfig config = GitHubConfig.fullRepository("giscus", "giscus", null);
        CollectionStatistics statistics = new CollectionStatistics();
        GitHubDataCollector collector = new GitHubDataCollector(
                fakeClient,
                new GitHubJsonParser(),
                new GitHubInteractionMapper());

        List<String> progress = new ArrayList<>();
        RepositoryData data = collector.collect(config, profile, progress::add, statistics).repositoryData();

        assertEquals(2, fakeClient.getIssuesPageCalls());
        assertEquals(2, fakeClient.getPullRequestsPageCalls());
        assertEquals(0, fakeClient.getIssueCommentsPageCalls(10));
        assertEquals(0, fakeClient.getIssueCommentsPageCalls(11));
        assertEquals(2, statistics.getIssueCommentApiCalls());
        assertEquals(0, statistics.getPullRequestCommentApiCalls());
        assertEquals(2, statistics.getPullRequestCommentItemsSkipped());
        assertEquals(2, statistics.getReviewApiCalls());
        assertEquals("giscus", data.getOwner());
        assertFalse(data.getInteractions().isEmpty());
        assertEquals(1, data.getPullRequestsOpenedByUser().get("alice"));
        assertEquals(1, data.getPullRequestsOpenedByUser().get("erin"));
        assertTrue(progress.stream().anyMatch(line -> line.contains("pagina")));
    }

    @Test
    void issueWithZeroCommentsShouldSkipCommentEndpoint() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 0 }
                ]
                """);
        fakeClient.registerPullRequestsPage(1, "[]");

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(0, fakeClient.getIssueCommentsPageCalls(1));
        assertEquals(1, statistics.getIssueCommentItemsSkipped());
        assertEquals(0, statistics.getIssueCommentApiCalls());
    }

    @Test
    void issueWithPositiveCommentsShouldCallCommentEndpoint() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 2 }
                ]
                """);
        fakeClient.registerIssueCommentsPage(1, 1, """
                [ { "user": { "login": "alice" } } ]
                """);
        fakeClient.registerPullRequestsPage(1, "[]");

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(1, fakeClient.getIssueCommentsPageCalls(1));
        assertEquals(1, statistics.getIssueCommentApiCalls());
        assertEquals(0, statistics.getIssueCommentItemsSkipped());
    }

    @Test
    void pullRequestWithZeroCommentsShouldSkipCommentEndpointButFetchReviews() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, "[]");
        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 10, "user": { "login": "alice" }, "comments": 0 }
                ]
                """);
        fakeClient.registerPullRequestReviewsPage(10, 1, """
                [ { "user": { "login": "bob" }, "state": "COMMENTED" } ]
                """);

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(0, fakeClient.getIssueCommentsPageCalls(10));
        assertEquals(1, statistics.getPullRequestCommentItemsSkipped());
        assertEquals(0, statistics.getPullRequestCommentApiCalls());
        assertEquals(1, statistics.getReviewApiCalls());
        assertEquals(1, fakeClient.getPullRequestReviewsPageCalls(10));
    }

    @Test
    void pullRequestWithPositiveCommentsShouldCallCommentEndpoint() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, "[]");
        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 10, "user": { "login": "alice" }, "comments": 1 }
                ]
                """);
        fakeClient.registerIssueCommentsPage(10, 1, """
                [ { "user": { "login": "bob" } } ]
                """);
        fakeClient.registerPullRequestReviewsPage(10, 1, "[]");

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(1, fakeClient.getIssueCommentsPageCalls(10));
        assertEquals(1, statistics.getPullRequestCommentApiCalls());
        assertEquals(0, statistics.getPullRequestCommentItemsSkipped());
    }

    @Test
    void commentsWithMultiplePagesShouldAllBeCollected() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 3 }
                ]
                """);
        fakeClient.registerIssueCommentsPage(1, 1, """
                [ { "user": { "login": "alice" } }, { "user": { "login": "carol" } } ]
                """);
        fakeClient.registerIssueCommentsPage(1, 2, """
                [ { "user": { "login": "dave" } } ]
                """);
        fakeClient.registerPullRequestsPage(1, "[]");

        RepositoryData data = collectRepositoryData(fakeClient);

        assertEquals(2, fakeClient.getIssueCommentsPageCalls(1));
        assertTrue(data.getInteractions().stream()
                .anyMatch(i -> "dave".equals(i.getSourceUser().getLogin())));
    }

    @Test
    void issueEntryRepresentingPullRequestShouldNotDuplicateCommentCollection() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 99, "user": { "login": "pr-author" }, "pull_request": {}, "comments": 5 }
                ]
                """);
        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 99, "user": { "login": "pr-author" }, "comments": 1 }
                ]
                """);
        fakeClient.registerIssueCommentsPage(99, 1, """
                [ { "user": { "login": "reviewer" } } ]
                """);
        fakeClient.registerPullRequestReviewsPage(99, 1, "[]");

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(1, fakeClient.getIssueCommentsPageCalls(99));
        assertEquals(0, statistics.getIssueCommentApiCalls());
        assertEquals(1, statistics.getPullRequestCommentApiCalls());
    }

    private CollectionStatistics collectWithFakeClient(FakeGitHubRawDataClient fakeClient) {
        CollectionStatistics statistics = new CollectionStatistics();
        collectWithFakeClient(fakeClient, statistics);
        return statistics;
    }

    private RepositoryData collectRepositoryData(FakeGitHubRawDataClient fakeClient) {
        CollectionStatistics statistics = new CollectionStatistics();
        return collectWithFakeClient(fakeClient, statistics).repositoryData();
    }

    private RepositoryCollectionResult collectWithFakeClient(
            FakeGitHubRawDataClient fakeClient,
            CollectionStatistics statistics) {
        CollectionProfile profile = CollectionProfile.fullRepository(
                "giscus", "giscus", 2, 1, false, "teste");
        GitHubConfig config = GitHubConfig.fullRepository("giscus", "giscus", null);
        return new GitHubDataCollector(fakeClient, new GitHubJsonParser(), new GitHubInteractionMapper())
                .collect(config, profile, ignored -> {
                }, statistics);
    }

    @Test
    void statisticsCountersShouldReflectSkippedAndPerformedCalls() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 0 },
                  { "number": 2, "user": { "login": "carol" }, "comments": 1 }
                ]
                """);
        fakeClient.registerIssueCommentsPage(2, 1, """
                [ { "user": { "login": "alice" } } ]
                """);
        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 10, "user": { "login": "erin" }, "comments": 0 }
                ]
                """);
        fakeClient.registerPullRequestReviewsPage(10, 1, "[]");

        CollectionStatistics statistics = collectWithFakeClient(fakeClient);

        assertEquals(2, statistics.getIssuesListed());
        assertEquals(1, statistics.getPullRequestsListed());
        assertEquals(1, statistics.getIssueCommentItemsSkipped());
        assertEquals(1, statistics.getIssueCommentApiCalls());
        assertEquals(1, statistics.getPullRequestCommentItemsSkipped());
        assertEquals(1, statistics.getReviewApiCalls());
        assertEquals(2, statistics.getEstimatedCallsAvoided());
        assertTrue(statistics.getMeasuredApiCalls() >= 4);
    }

    @Test
    void collectSampleLimitedShouldRespectConfiguredLimits() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, """
                [
                  { "number": 1, "user": { "login": "bob" }, "comments": 1 },
                  { "number": 2, "user": { "login": "carol" }, "comments": 1 }
                ]
                """);
        fakeClient.registerIssueComments(1, """
                [ { "user": { "login": "alice" } } ]
                """);
        fakeClient.registerPullRequestsPage(1, """
                [
                  { "number": 10, "user": { "login": "alice" }, "comments": 0 },
                  { "number": 11, "user": { "login": "bob" }, "comments": 0 }
                ]
                """);
        fakeClient.registerPullRequestReviews(10, """
                [ { "user": { "login": "carol" }, "state": "COMMENTED" } ]
                """);

        CollectionProfile profile = CollectionProfile.sampleLimitedForTests(
                "giscus", "giscus", 100, 1, 1, 1, 50, 1);
        GitHubConfig config = GitHubConfig.sampleLimited(
                "giscus", "giscus", 1, 1, 50, 1, null);

        RepositoryData data = new GitHubDataCollector(fakeClient, new GitHubJsonParser(), new GitHubInteractionMapper())
                .collect(config, profile);

        assertFalse(data.getInteractions().stream()
                .anyMatch(i -> "dave".equals(i.getSourceUser().getLogin())));
        assertEquals(1, fakeClient.getIssuesPageCalls());
    }

    @Test
    void collectShouldNotBuildGraphsDirectly() {
        FakeGitHubRawDataClient fakeClient = new FakeGitHubRawDataClient();
        fakeClient.registerIssuesPage(1, "[]");
        fakeClient.registerPullRequestsPage(1, "[]");

        CollectionProfile profile = CollectionProfile.fullRepository(
                "giscus", "giscus", 100, 1, false, "teste");
        RepositoryData data = new GitHubDataCollector(fakeClient, new GitHubJsonParser(), new GitHubInteractionMapper())
                .collect(GitHubConfig.fullRepository("giscus", "giscus", null), profile);

        assertInstanceOf(RepositoryData.class, data);
        assertEquals(0, data.getInteractions().size());

        var graphResult = new GraphBuilderService().build(data);
        assertEquals(0, graphResult.getIntegratedGraph().getVertexCount());
    }

}
