package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.model.RepositoryData;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.GraphBuilderService;

/**
 * Orquestra a demonstração offline com dados simulados.
 */
public class DemoApplicationService {

    private final GraphBuilderService graphBuilderService;

    public DemoApplicationService() {
        this(new GraphBuilderService());
    }

    public DemoApplicationService(GraphBuilderService graphBuilderService) {
        this.graphBuilderService = graphBuilderService;
    }

    public GraphBuildResult runDemo() {
        RepositoryData repositoryData = DemoDataFactory.createGiscusDemo();
        return graphBuilderService.build(repositoryData);
    }
}
