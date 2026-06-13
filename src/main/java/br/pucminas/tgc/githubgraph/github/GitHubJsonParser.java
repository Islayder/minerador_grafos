package br.pucminas.tgc.githubgraph.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte JSON bruto da GitHub API em estruturas intermediárias simples.
 */
public final class GitHubJsonParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<GitHubIssueData> parseIssues(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<GitHubIssueData> issues = new ArrayList<>();
            for (JsonNode node : root) {
                int number = node.path("number").asInt();
                String authorLogin = extractLogin(node.path("user"));
                String closedByLogin = extractLogin(node.path("closed_by"));
                boolean pullRequest = !node.path("pull_request").isMissingNode()
                        && !node.path("pull_request").isNull();
                int commentsCount = parseCommentsCount(node);
                String commentsUrl = parseTextOrNull(node.path("comments_url"));

                issues.add(new GitHubIssueData(
                        number,
                        authorLogin,
                        closedByLogin,
                        pullRequest,
                        commentsCount,
                        commentsUrl));
            }
            return issues;
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON de issues inválido: " + e.getMessage(), e);
        }
    }

    public List<GitHubPullRequestData> parsePullRequests(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<GitHubPullRequestData> pullRequests = new ArrayList<>();
            for (JsonNode node : root) {
                int number = node.path("number").asInt();
                String authorLogin = extractLogin(node.path("user"));
                String mergedByLogin = extractLogin(node.path("merged_by"));
                boolean merged = !node.path("merged_at").isMissingNode()
                        && !node.path("merged_at").isNull();

                int commentsCount = parseCommentsCount(node);
                String commentsUrl = parseTextOrNull(node.path("comments_url"));

                pullRequests.add(new GitHubPullRequestData(
                        number,
                        authorLogin,
                        mergedByLogin,
                        merged,
                        commentsCount,
                        commentsUrl));
            }
            return pullRequests;
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON de pull requests inválido: " + e.getMessage(), e);
        }
    }

    public List<GitHubCommentData> parseComments(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<GitHubCommentData> comments = new ArrayList<>();
            for (JsonNode node : root) {
                String authorLogin = extractLogin(node.path("user"));
                if (authorLogin != null) {
                    comments.add(new GitHubCommentData(authorLogin));
                }
            }
            return comments;
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON de comentários inválido: " + e.getMessage(), e);
        }
    }

    public List<GitHubReviewData> parseReviews(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<GitHubReviewData> reviews = new ArrayList<>();
            for (JsonNode node : root) {
                String authorLogin = extractLogin(node.path("user"));
                String state = node.path("state").asText(null);
                if (authorLogin != null && state != null) {
                    reviews.add(new GitHubReviewData(authorLogin, state));
                }
            }
            return reviews;
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON de reviews inválido: " + e.getMessage(), e);
        }
    }

    private static int parseCommentsCount(JsonNode node) {
        JsonNode commentsNode = node.path("comments");
        if (commentsNode.isMissingNode() || commentsNode.isNull()) {
            return GitHubIssueData.UNKNOWN_COMMENTS_COUNT;
        }
        return Math.max(0, commentsNode.asInt(0));
    }

    private static String parseTextOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String extractLogin(JsonNode userNode) {
        if (userNode == null || userNode.isMissingNode() || userNode.isNull()) {
            return null;
        }
        String login = userNode.path("login").asText(null);
        if (login == null || login.isBlank()) {
            return null;
        }
        return login.trim();
    }
}
