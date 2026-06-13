package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubJsonParserTest {

    private GitHubJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new GitHubJsonParser();
    }

    @Test
    void shouldParseIssueCommentsCountAndUrl() {
        String json = """
                [
                  {
                    "number": 3,
                    "user": { "login": "bob" },
                    "comments": 0,
                    "comments_url": "https://api.github.com/repos/o/r/issues/3/comments"
                  }
                ]
                """;

        GitHubIssueData issue = parser.parseIssues(json).get(0);

        assertEquals(0, issue.getCommentsCount());
        assertTrue(issue.shouldSkipCommentsFetch());
        assertEquals("https://api.github.com/repos/o/r/issues/3/comments", issue.getCommentsUrl());
    }

    @Test
    void shouldParsePullRequestCommentsCount() {
        String json = """
                [
                  {
                    "number": 7,
                    "user": { "login": "alice" },
                    "comments": 4
                  }
                ]
                """;

        GitHubPullRequestData pullRequest = parser.parsePullRequests(json).get(0);

        assertEquals(4, pullRequest.getCommentsCount());
        assertFalse(pullRequest.shouldSkipCommentsFetch());
    }

    @Test
    void shouldTreatMissingCommentsFieldAsUnknown() {
        String json = """
                [
                  { "number": 1, "user": { "login": "bob" } }
                ]
                """;

        GitHubIssueData issue = parser.parseIssues(json).get(0);

        assertEquals(GitHubIssueData.UNKNOWN_COMMENTS_COUNT, issue.getCommentsCount());
        assertFalse(issue.shouldSkipCommentsFetch());
    }

    @Test
    void shouldParseIssueWithAuthorAndClosedBy() {
        String json = """
                [
                  {
                    "number": 10,
                    "user": { "login": "bob" },
                    "closed_by": { "login": "alice" }
                  }
                ]
                """;

        List<GitHubIssueData> issues = parser.parseIssues(json);

        assertEquals(1, issues.size());
        assertEquals(10, issues.get(0).getNumber());
        assertEquals("bob", issues.get(0).getAuthorLogin());
        assertEquals("alice", issues.get(0).getClosedByLogin());
        assertFalse(issues.get(0).isPullRequest());
    }

    @Test
    void shouldParsePullRequestWithMergeInformation() {
        String json = """
                [
                  {
                    "number": 20,
                    "user": { "login": "carol" },
                    "merged_by": { "login": "bob" },
                    "merged_at": "2024-01-01T00:00:00Z"
                  }
                ]
                """;

        List<GitHubPullRequestData> pullRequests = parser.parsePullRequests(json);

        assertEquals(1, pullRequests.size());
        assertEquals("carol", pullRequests.get(0).getAuthorLogin());
        assertEquals("bob", pullRequests.get(0).getMergedByLogin());
        assertTrue(pullRequests.get(0).isMerged());
    }

    @Test
    void shouldParseComments() {
        String json = """
                [
                  { "user": { "login": "alice" } },
                  { "user": { "login": "bob" } }
                ]
                """;

        List<GitHubCommentData> comments = parser.parseComments(json);

        assertEquals(2, comments.size());
        assertEquals("alice", comments.get(0).getAuthorLogin());
        assertEquals("bob", comments.get(1).getAuthorLogin());
    }

    @Test
    void shouldParseReviewsIncludingApproved() {
        String json = """
                [
                  { "user": { "login": "bob" }, "state": "COMMENTED" },
                  { "user": { "login": "carol" }, "state": "APPROVED" }
                ]
                """;

        List<GitHubReviewData> reviews = parser.parseReviews(json);

        assertEquals(2, reviews.size());
        assertEquals("COMMENTED", reviews.get(0).getState());
        assertEquals("APPROVED", reviews.get(1).getState());
    }
}
