package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.GitHubUser;
import br.pucminas.tgc.githubgraph.model.InteractionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte dados intermediários da API em {@link GitHubInteraction}.
 */
public final class GitHubInteractionMapper {

    public List<GitHubInteraction> mapComment(String commentAuthorLogin, String itemAuthorLogin, boolean issueItem) {
        if (!isValidPair(commentAuthorLogin, itemAuthorLogin)) {
            return List.of();
        }

        GitHubUser source = new GitHubUser(commentAuthorLogin);
        GitHubUser target = new GitHubUser(itemAuthorLogin);
        List<GitHubInteraction> interactions = new ArrayList<>();
        interactions.add(new GitHubInteraction(source, target, InteractionType.COMMENT));

        if (issueItem) {
            interactions.add(new GitHubInteraction(source, target, InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));
        }
        return interactions;
    }

    public List<GitHubInteraction> mapIssueClosed(String closedByLogin, String issueAuthorLogin) {
        if (!isValidPair(closedByLogin, issueAuthorLogin)) {
            return List.of();
        }
        return List.of(new GitHubInteraction(
                new GitHubUser(closedByLogin),
                new GitHubUser(issueAuthorLogin),
                InteractionType.ISSUE_CLOSED));
    }

    public List<GitHubInteraction> mapReview(String reviewerLogin, String pullRequestAuthorLogin, String reviewState) {
        if (!isValidPair(reviewerLogin, pullRequestAuthorLogin) || reviewState == null) {
            return List.of();
        }

        InteractionType type = "APPROVED".equalsIgnoreCase(reviewState)
                ? InteractionType.PR_APPROVAL
                : InteractionType.PR_REVIEW;

        return List.of(new GitHubInteraction(
                new GitHubUser(reviewerLogin),
                new GitHubUser(pullRequestAuthorLogin),
                type));
    }

    public List<GitHubInteraction> mapMerge(String mergedByLogin, String pullRequestAuthorLogin) {
        if (!isValidPair(mergedByLogin, pullRequestAuthorLogin)) {
            return List.of();
        }
        return List.of(new GitHubInteraction(
                new GitHubUser(mergedByLogin),
                new GitHubUser(pullRequestAuthorLogin),
                InteractionType.PR_MERGE));
    }

    private boolean isValidPair(String sourceLogin, String targetLogin) {
        if (sourceLogin == null || sourceLogin.isBlank() || targetLogin == null || targetLogin.isBlank()) {
            return false;
        }
        return !sourceLogin.equalsIgnoreCase(targetLogin);
    }
}
