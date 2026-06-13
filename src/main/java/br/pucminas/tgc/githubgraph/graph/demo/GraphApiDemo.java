package br.pucminas.tgc.githubgraph.graph.demo;

import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyMatrixGraph;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Aplicação separada da ferramenta principal: demonstra todas as operações da API
 * {@link br.pucminas.tgc.githubgraph.graph.AbstractGraph} nas implementações por
 * lista e por matriz de adjacência (requisito da Etapa 2 do trabalho).
 * <p>
 * Não depende de GitHub, internet nem da CLI {@code Main}.
 */
public final class GraphApiDemo {

    public static final String OUTPUT_DIRECTORY = "output";
    public static final String LIST_GEXF_FILE = "graph-api-demo-list.gexf";
    public static final String MATRIX_GEXF_FILE = "graph-api-demo-matrix.gexf";

    private GraphApiDemo() {
    }

    public static void main(String[] args) {
        Path outputDir = Path.of(OUTPUT_DIRECTORY);
        try {
            Files.createDirectories(outputDir);
        } catch (Exception exception) {
            System.err.println("Não foi possível criar " + outputDir + ": " + exception.getMessage());
            System.exit(1);
        }

        GraphApiDemonstrator demonstrator = new GraphApiDemonstrator(System.out);
        System.out.println("=== Demonstração da API AbstractGraph ===");
        System.out.println("Grafos simples, direcionados, sem bibliotecas externas de grafos.");
        System.out.println();

        demonstrator.demonstrate(
                AdjacencyListGraph::new,
                "AdjacencyListGraph",
                outputDir.resolve(LIST_GEXF_FILE));
        demonstrator.demonstrate(
                AdjacencyMatrixGraph::new,
                "AdjacencyMatrixGraph",
                outputDir.resolve(MATRIX_GEXF_FILE));

        System.out.println("Demonstração concluída. Arquivos GEXF em " + outputDir.toAbsolutePath().normalize());
    }
}
