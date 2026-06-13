package br.pucminas.tgc.githubgraph.app.desktop;

import java.util.function.Consumer;

/**
 * Tarefa executada em segundo plano pela interface desktop, com relato de progresso.
 */
@FunctionalInterface
public interface DesktopTask {

    String run(Consumer<String> progress);
}
