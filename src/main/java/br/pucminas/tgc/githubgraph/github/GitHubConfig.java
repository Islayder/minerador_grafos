package br.pucminas.tgc.githubgraph.github;

/**
 * Configuracoes da coleta de dados do GitHub.
 */
public final class GitHubConfig {

    private final String owner;
    private final String repository;
    private final boolean fullRepository;
    private final int maxIssues;
    private final int maxPullRequests;
    private final int maxCommentsPerItem;
    private final int maxReviewsPerPullRequest;
    private final String token;

    private GitHubConfig(
            String owner,
            String repository,
            boolean fullRepository,
            int maxIssues,
            int maxPullRequests,
            int maxCommentsPerItem,
            int maxReviewsPerPullRequest,
            String token) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("O owner nao pode ser nulo ou vazio.");
        }
        if (repository == null || repository.isBlank()) {
            throw new IllegalArgumentException("O repository nao pode ser nulo ou vazio.");
        }
        if (!fullRepository) {
            validatePositive(maxIssues, "maxIssues");
            validatePositive(maxPullRequests, "maxPullRequests");
            validatePositive(maxCommentsPerItem, "maxCommentsPerItem");
            validatePositive(maxReviewsPerPullRequest, "maxReviewsPerPullRequest");
        }

        this.owner = owner.trim();
        this.repository = repository.trim();
        this.fullRepository = fullRepository;
        this.maxIssues = maxIssues;
        this.maxPullRequests = maxPullRequests;
        this.maxCommentsPerItem = maxCommentsPerItem;
        this.maxReviewsPerPullRequest = maxReviewsPerPullRequest;
        this.token = normalizeToken(token);
    }

    public static GitHubConfig fullRepository(String owner, String repository, String token) {
        return new GitHubConfig(owner, repository, true, 0, 0, 0, 0, token);
    }

    public static GitHubConfig sampleLimited(
            String owner,
            String repository,
            int maxIssues,
            int maxPullRequests,
            int maxCommentsPerItem,
            int maxReviewsPerPullRequest,
            String token) {
        return new GitHubConfig(
                owner,
                repository,
                false,
                maxIssues,
                maxPullRequests,
                maxCommentsPerItem,
                maxReviewsPerPullRequest,
                token);
    }

    public static GitHubConfig defaultForConfiguredRepository() {
        String token = EnvironmentLoader.loadGitHubToken();
        return fullRepository("giscus", "giscus", token);
    }

    public static GitHubConfig fromProfile(CollectionProfile profile) {
        String token = EnvironmentLoader.loadGitHubToken();
        if (profile.isFullRepository()) {
            return fullRepository(profile.getOwner(), profile.getRepository(), token);
        }
        return sampleLimited(
                profile.getOwner(),
                profile.getRepository(),
                profile.getMaxIssues(),
                profile.getMaxPullRequests(),
                profile.getMaxCommentsPerItem(),
                profile.getMaxReviewsPerPullRequest(),
                token);
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public boolean isFullRepository() {
        return fullRepository;
    }

    public int getMaxIssues() {
        return maxIssues;
    }

    public int getMaxPullRequests() {
        return maxPullRequests;
    }

    public int getMaxCommentsPerItem() {
        return maxCommentsPerItem;
    }

    public int getMaxReviewsPerPullRequest() {
        return maxReviewsPerPullRequest;
    }

    public String getToken() {
        return token;
    }

    public boolean hasToken() {
        return token != null;
    }

    private static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser positivo.");
        }
    }

    private static String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return token.trim();
    }
}
