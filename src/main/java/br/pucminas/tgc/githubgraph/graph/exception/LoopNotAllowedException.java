package br.pucminas.tgc.githubgraph.graph.exception;

/**
 * Lançada quando se tenta criar uma aresta com origem e destino iguais (laço).
 */
public class LoopNotAllowedException extends RuntimeException {

    public LoopNotAllowedException(int vertex) {
        super("Laços não são permitidos. Tentativa de aresta (" + vertex + " -> " + vertex + ").");
    }
}
