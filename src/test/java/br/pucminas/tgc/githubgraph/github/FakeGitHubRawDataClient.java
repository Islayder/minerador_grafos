package br.pucminas.tgc.githubgraph.github;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class FakeGitHubRawDataClient implements GitHubRawDataClient {

    private final Map<String, String> responses = new HashMap<>();
    private final AtomicInteger issuesPageCalls = new AtomicInteger();
    private final AtomicInteger pullRequestsPageCalls = new AtomicInteger();
    private final Map<Integer, AtomicInteger> issueCommentPageCallsByItem = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> reviewPageCallsByPullRequest = new ConcurrentHashMap<>();

    void registerIssuesPage(int page, String json) {
        responses.put(issuesPageKey(page), json);
    }

    void registerPullRequestsPage(int page, String json) {
        responses.put(pullRequestsPageKey(page), json);
    }

    void registerIssueComments(int issueNumber, String json) {
        registerIssueCommentsPage(issueNumber, 1, json);
    }

    void registerIssueCommentsPage(int issueNumber, int page, String json) {
        responses.put(commentsKey(issueNumber, page), json);
    }

    void registerPullRequestReviews(int pullRequestNumber, String json) {
        registerPullRequestReviewsPage(pullRequestNumber, 1, json);
    }

    void registerPullRequestReviewsPage(int pullRequestNumber, int page, String json) {
        responses.put(reviewsKey(pullRequestNumber, page), json);
    }

    int getIssuesPageCalls() {
        return issuesPageCalls.get();
    }

    int getPullRequestsPageCalls() {
        return pullRequestsPageCalls.get();
    }

    int getIssueCommentsPageCalls(int itemNumber) {
        return issueCommentPageCallsByItem
                .getOrDefault(itemNumber, new AtomicInteger())
                .get();
    }

    int getPullRequestReviewsPageCalls(int pullRequestNumber) {
        return reviewPageCallsByPullRequest
                .getOrDefault(pullRequestNumber, new AtomicInteger())
                .get();
    }

    int getTotalIssueCommentsPageCalls() {
        return issueCommentPageCallsByItem.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    int getTotalReviewPageCalls() {
        return reviewPageCallsByPullRequest.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    @Override
    public String get(String url) {
        return responses.getOrDefault(url, "[]");
    }

    @Override
    public String getIssuesPage(GitHubConfig config, int page, int perPage) {
        issuesPageCalls.incrementAndGet();
        return responses.getOrDefault(issuesPageKey(page), "[]");
    }

    @Override
    public String getPullRequestsPage(GitHubConfig config, int page, int perPage) {
        pullRequestsPageCalls.incrementAndGet();
        return responses.getOrDefault(pullRequestsPageKey(page), "[]");
    }

    @Override
    public String getIssueCommentsPage(GitHubConfig config, int issueNumber, int page, int perPage) {
        issueCommentPageCallsByItem
                .computeIfAbsent(issueNumber, ignored -> new AtomicInteger())
                .incrementAndGet();
        return responses.getOrDefault(commentsKey(issueNumber, page), "[]");
    }

    @Override
    public String getPullRequestReviewsPage(GitHubConfig config, int pullRequestNumber, int page, int perPage) {
        reviewPageCallsByPullRequest
                .computeIfAbsent(pullRequestNumber, ignored -> new AtomicInteger())
                .incrementAndGet();
        return responses.getOrDefault(reviewsKey(pullRequestNumber, page), "[]");
    }

    private static String issuesPageKey(int page) {
        return "issues-page-" + page;
    }

    private static String pullRequestsPageKey(int page) {
        return "pulls-page-" + page;
    }

    private static String commentsKey(int itemNumber, int page) {
        return "comments-" + itemNumber + "-page-" + page;
    }

    private static String reviewsKey(int pullRequestNumber, int page) {
        return "reviews-" + pullRequestNumber + "-page-" + page;
    }
}
