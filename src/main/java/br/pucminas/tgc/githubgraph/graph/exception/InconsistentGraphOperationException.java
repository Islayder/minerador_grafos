package br.pucminas.tgc.githubgraph.graph.exception;

/**
 * Lançada quando uma operação não pode ser aplicada ao estado atual do grafo.
 */
public class InconsistentGraphOperationException extends RuntimeException {

    public InconsistentGraphOperationException(String message) {
        super(message);
    }
}
