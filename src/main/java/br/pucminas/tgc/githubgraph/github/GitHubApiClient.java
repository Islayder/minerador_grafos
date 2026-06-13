package br.pucminas.tgc.githubgraph.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP para a GitHub REST API. Retorna apenas JSON bruto.
 */
public final class GitHubApiClient implements GitHubRawDataClient {

    private static final String API_BASE = "https://api.github.com";
    private static final String ACCEPT_HEADER = "application/vnd.github+json";

    private final HttpClient httpClient;

    public GitHubApiClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build());
    }

    public GitHubApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String get(String url) {
        return executeRequest(url, null);
    }

    @Override
    public String getIssuesPage(GitHubConfig config, int page, int perPage) {
        String url = API_BASE + "/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/issues?state=all&page=" + page + "&per_page=" + perPage;
        return executeRequest(url, config.getToken());
    }

    @Override
    public String getPullRequestsPage(GitHubConfig config, int page, int perPage) {
        String url = API_BASE + "/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/pulls?state=all&page=" + page + "&per_page=" + perPage;
        return executeRequest(url, config.getToken());
    }

    @Override
    public String getIssueCommentsPage(GitHubConfig config, int issueNumber, int page, int perPage) {
        String url = API_BASE + "/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/issues/" + issueNumber + "/comments?page=" + page + "&per_page=" + perPage;
        return executeRequest(url, config.getToken());
    }

    @Override
    public String getPullRequestReviewsPage(GitHubConfig config, int pullRequestNumber, int page, int perPage) {
        String url = API_BASE + "/repos/" + config.getOwner() + "/" + config.getRepository()
                + "/pulls/" + pullRequestNumber + "/reviews?page=" + page + "&per_page=" + perPage;
        return executeRequest(url, config.getToken());
    }

    private String executeRequest(String url, String token) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", ACCEPT_HEADER)
                    .GET();

            if (token != null && !token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 403) {
                throw new GitHubApiException(
                        status,
                        "Rate limit ou permissao insuficiente na GitHub API (HTTP 403). "
                                + "Aguarde, use GITHUB_TOKEN ou tente mais tarde.");
            }
            if (status < 200 || status >= 300) {
                throw new GitHubApiException(
                        status,
                        "Requisicao GitHub falhou com status " + status + " para URL: " + url);
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GitHubApiException(-1, "Requisicao interrompida: " + exception.getMessage());
        } catch (IOException exception) {
            throw new GitHubApiException(-1, "Falha de comunicacao com a GitHub API: " + exception.getMessage());
        }
    }
}
