package br.pucminas.tgc.githubgraph.github;

/**
 * Callback opcional para exibir progresso da mineracao (CLI, desktop ou testes).
 */
@FunctionalInterface
public interface CollectionProgressListener {

    void onProgress(String message);

    static CollectionProgressListener noop() {
        return message -> {
        };
    }
}
