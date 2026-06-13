package br.pucminas.tgc.githubgraph.github;

import br.pucminas.tgc.githubgraph.model.RepositoryData;

/**
 * Resultado da coleta com metricas associadas.
 */
public record RepositoryCollectionResult(RepositoryData repositoryData, CollectionStatistics statistics) {
}
