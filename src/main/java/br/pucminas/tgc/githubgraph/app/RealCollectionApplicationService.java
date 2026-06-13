package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.github.CachingGitHubRawDataClient;
import br.pucminas.tgc.githubgraph.github.CollectionExecutionResult;
import br.pucminas.tgc.githubgraph.github.CollectionProfile;
import br.pucminas.tgc.githubgraph.github.CollectionStatistics;
import br.pucminas.tgc.githubgraph.github.RepositoryCollectionResult;
import br.pucminas.tgc.githubgraph.github.CollectionProgressListener;
import br.pucminas.tgc.githubgraph.github.GitHubApiClient;
import br.pucminas.tgc.githubgraph.github.GitHubApiException;
import br.pucminas.tgc.githubgraph.github.GitHubConfig;
import br.pucminas.tgc.githubgraph.github.GitHubDataCollector;
import br.pucminas.tgc.githubgraph.github.GitHubInteractionMapper;
import br.pucminas.tgc.githubgraph.github.GitHubJsonParser;
import br.pucminas.tgc.githubgraph.github.GitHubRequestStats;
import br.pucminas.tgc.githubgraph.github.GitHubResponseCache;
import br.pucminas.tgc.githubgraph.model.RepositoryData;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;
import br.pucminas.tgc.githubgraph.service.PerformanceTimer;

/**
 * Orquestra mineracao real do repositorio configurado e construcao dos grafos.
 */
public final class RealCollectionApplicationService {

    private final GraphBuilderService graphBuilderService;

    public RealCollectionApplicationService() {
        this(new GraphBuilderService());
    }

    public RealCollectionApplicationService(GraphBuilderService graphBuilderService) {
        this.graphBuilderService = graphBuilderService;
    }

    public CollectionExecutionResult collectAndBuild(CollectionProfile profile) {
        return collectAndBuild(profile, CollectionProgressListener.noop());
    }

    public CollectionExecutionResult collectAndBuild(
            CollectionProfile profile,
            CollectionProgressListener progress) {
        GitHubConfig config = GitHubConfig.fromProfile(profile);
        PerformanceTimer timer = new PerformanceTimer();

        GitHubRequestStats stats = new GitHubRequestStats();
        GitHubResponseCache cache = new GitHubResponseCache(profile.isCacheEnabled());
        CachingGitHubRawDataClient client = new CachingGitHubRawDataClient(
                new GitHubApiClient(),
                cache,
                stats);

        GitHubDataCollector collector = new GitHubDataCollector(
                client,
                new GitHubJsonParser(),
                new GitHubInteractionMapper());

        CollectionStatistics collectionStatistics = new CollectionStatistics();
        RepositoryCollectionResult collectionResult = collector.collect(
                config,
                profile,
                progress,
                collectionStatistics);
        RepositoryData repositoryData = collectionResult.repositoryData();
        long collectionMillis = timer.markMillis();

        GraphBuildResult graphBuildResult = graphBuilderService.build(repositoryData);
        long buildMillis = timer.markMillis();
        long totalMillis = timer.elapsedMillis();

        return new CollectionExecutionResult(
                graphBuildResult,
                profile,
                totalMillis,
                collectionMillis,
                buildMillis,
                repositoryData.getInteractions().size(),
                graphBuildResult.getUserIndexMapper().getUserCount(),
                graphBuildResult.getIntegratedGraph().getEdgeCount(),
                stats.getApiRequests(),
                stats.getCacheHits(),
                cache.isEnabled(),
                collectionStatistics);
    }

    public static String friendlyErrorMessage(Exception exception) {
        if (exception instanceof GitHubApiException apiException) {
            if (apiException.getStatusCode() == 401 || apiException.getStatusCode() == 403) {
                return "Falha de autenticacao, permissao ou rate limit na API GitHub. "
                        + "Verifique o GITHUB_TOKEN, aguarde o limite ou tente mais tarde.";
            }
            if (apiException.getStatusCode() == 404) {
                return "Repositorio nao encontrado. Confirme owner/repository em config/collection.properties.";
            }
            return "Erro na API GitHub (HTTP " + apiException.getStatusCode() + "): "
                    + apiException.getMessage();
        }
        return "Nao foi possivel concluir a mineracao: " + exception.getMessage()
                + ". Verifique sua conexao ou use a demonstracao offline.";
    }
}
