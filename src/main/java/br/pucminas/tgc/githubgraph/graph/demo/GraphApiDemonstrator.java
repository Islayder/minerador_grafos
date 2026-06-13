package br.pucminas.tgc.githubgraph.graph.demo;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.exception.InconsistentGraphOperationException;
import br.pucminas.tgc.githubgraph.graph.exception.InvalidVertexException;
import br.pucminas.tgc.githubgraph.graph.exception.LoopNotAllowedException;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.IntFunction;

/**
 * Roteiro fixo que exercita todas as operações públicas de {@link AbstractGraph}.
 * Usado pela aplicação separada {@link GraphApiDemo} (requisito da Etapa 2).
 */
public final class GraphApiDemonstrator {

    private final PrintStream out;

    public GraphApiDemonstrator(PrintStream out) {
        this.out = out;
    }

    public void demonstrate(
            IntFunction<AbstractGraph> graphFactory,
            String implementationName,
            Path gexfOutput) {
        AbstractGraph graph = graphFactory.apply(4);
        out.println("--- " + implementationName + " ---");
        out.println();

        demonstrateCountsAndEmptiness(graph);
        demonstrateAddEdgeAndIdempotence(graph);
        demonstrateAntiparallelEdges(graph);
        demonstrateStructuralQueries(graph);
        demonstrateDegrees(graph);
        demonstrateWeights(graph);
        demonstrateConnectivity(graph);
        demonstrateCompleteGraph(graphFactory);
        demonstrateRemoveEdge(graph);
        demonstrateValidationExceptions(graph);
        demonstrateExport(graph, gexfOutput);

        out.println();
    }

    private void demonstrateCountsAndEmptiness(AbstractGraph graph) {
        section("Contagem e grafo vazio");
        println("getVertexCount() = " + graph.getVertexCount());
        println("getEdgeCount() = " + graph.getEdgeCount());
        println("isEmptyGraph() = " + graph.isEmptyGraph());
    }

    private void demonstrateAddEdgeAndIdempotence(AbstractGraph graph) {
        section("addEdge / hasEdge / idempotência");
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        println("addEdge(0,1) duas vezes -> getEdgeCount() = " + graph.getEdgeCount());
        println("hasEdge(0,1) = " + graph.hasEdge(0, 1));
        println("hasEdge(1,0) = " + graph.hasEdge(1, 0));
    }

    private void demonstrateAntiparallelEdges(AbstractGraph graph) {
        section("Arestas anti-paralelas");
        graph.addEdge(1, 0);
        println("addEdge(1,0) com 0->1 existente -> getEdgeCount() = " + graph.getEdgeCount());
    }

    private void demonstrateStructuralQueries(AbstractGraph graph) {
        section("Relações estruturais");
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);

        println("isSucessor(0,1) = " + graph.isSucessor(0, 1));
        println("isPredessor(0,1) = " + graph.isPredessor(0, 1));
        println("isDivergent(0,1, 0,2) = " + graph.isDivergent(0, 1, 0, 2));
        println("isConvergent(0,2, 1,2) = " + graph.isConvergent(0, 2, 1, 2));
        println("isIncident(0,1, 0) = " + graph.isIncident(0, 1, 0));
        println("isIncident(0,1, 2) = " + graph.isIncident(0, 1, 2));
    }

    private void demonstrateDegrees(AbstractGraph graph) {
        section("Graus");
        println("getVertexInDegree(1) = " + graph.getVertexInDegree(1));
        println("getVertexOutDegree(1) = " + graph.getVertexOutDegree(1));
    }

    private void demonstrateWeights(AbstractGraph graph) {
        section("Pesos de vértices e arestas");
        graph.setVertexWeight(0, 3.5);
        println("setVertexWeight(0, 3.5) -> getVertexWeight(0) = " + graph.getVertexWeight(0));
        println("getEdgeWeight(0,1) (padrão) = " + graph.getEdgeWeight(0, 1));
        graph.setEdgeWeight(0, 1, 2.0);
        println("setEdgeWeight(0,1, 2.0) -> getEdgeWeight(0,1) = " + graph.getEdgeWeight(0, 1));

        try {
            graph.setEdgeWeight(2, 0, 9.0);
            println("setEdgeWeight em aresta inexistente: inesperado");
        } catch (InconsistentGraphOperationException exception) {
            println("setEdgeWeight(2,0) sem aresta -> " + exception.getClass().getSimpleName());
        }
    }

    private void demonstrateConnectivity(AbstractGraph graph) {
        section("Conectividade fraca");
        println("isConnected() = " + graph.isConnected());
    }

    private void demonstrateCompleteGraph(IntFunction<AbstractGraph> graphFactory) {
        section("isCompleteGraph (grafo auxiliar K3 direcionado)");
        AbstractGraph complete = graphFactory.apply(3);
        for (int u = 0; u < 3; u++) {
            for (int v = 0; v < 3; v++) {
                if (u != v) {
                    complete.addEdge(u, v);
                }
            }
        }
        println("K3 direcionado simples -> isCompleteGraph() = " + complete.isCompleteGraph());
    }

    private void demonstrateRemoveEdge(AbstractGraph graph) {
        section("removeEdge");
        graph.removeEdge(1, 0);
        println("removeEdge(1,0) -> hasEdge(1,0) = " + graph.hasEdge(1, 0));
        println("getEdgeCount() = " + graph.getEdgeCount());
    }

    private void demonstrateValidationExceptions(AbstractGraph graph) {
        section("Validações e exceções");
        expectException("addEdge(1,1) laço", LoopNotAllowedException.class,
                () -> graph.addEdge(1, 1));
        expectException("addEdge(-1,0) índice inválido", InvalidVertexException.class,
                () -> graph.addEdge(-1, 0));
    }

    private void demonstrateExport(AbstractGraph graph, Path gexfOutput) {
        section("exportToGEPHI");
        graph.exportToGEPHI(gexfOutput.toString());
        boolean exists = Files.exists(gexfOutput);
        println("exportToGEPHI(\"" + gexfOutput + "\") -> arquivo existe: " + exists);
    }

    private void section(String title) {
        out.println("[" + title + "]");
    }

    private void println(String line) {
        out.println("  " + line);
    }

    private void expectException(String action, Class<? extends Exception> type, Runnable runnable) {
        try {
            runnable.run();
            println(action + ": exceção esperada não lançada");
        } catch (Exception exception) {
            boolean matches = type.isInstance(exception);
            println(action + " -> " + exception.getClass().getSimpleName()
                    + (matches ? "" : " (esperado " + type.getSimpleName() + ")"));
        }
    }
}
