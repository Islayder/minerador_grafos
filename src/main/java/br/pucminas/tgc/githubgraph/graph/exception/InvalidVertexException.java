package br.pucminas.tgc.githubgraph.graph.exception;

/**
 * Lançada quando um índice de vértice está fora do intervalo válido do grafo.
 */
public class InvalidVertexException extends RuntimeException {

    public InvalidVertexException(int vertex, int vertexCount) {
        super("Vértice inválido: " + vertex + ". Intervalo válido: [0, " + (vertexCount - 1) + "].");
    }

    public InvalidVertexException(String message) {
        super(message);
    }
}
