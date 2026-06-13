package br.pucminas.tgc.githubgraph.app;

/**
 * Ponto de entrada da aplicação CLI.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        new ConsoleMenu().run();
    }
}
