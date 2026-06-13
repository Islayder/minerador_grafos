package br.pucminas.tgc.githubgraph.export;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exporta um grafo para o formato GEXF, compatível com o Gephi.
 */
public final class GexfExporter {

    private GexfExporter() {
    }

    public static void export(AbstractGraph graph, String path) throws IOException {
        Path output = Path.of(path);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n");
        xml.append("  <graph defaultedgetype=\"directed\" mode=\"static\">\n");
        appendEdgeWeightAttributeDeclaration(xml);
        xml.append("    <nodes>\n");

        int vertexCount = graph.getVertexCount();
        for (int v = 0; v < vertexCount; v++) {
            xml.append("      <node id=\"")
                    .append(v)
                    .append("\" label=\"v")
                    .append(v)
                    .append("\"/>\n");
        }

        xml.append("    </nodes>\n");
        xml.append("    <edges>\n");

        int edgeId = 0;
        if (graph instanceof AdjacencyListGraph listGraph) {
            for (int u = 0; u < vertexCount; u++) {
                for (int v : listGraph.getOutgoingTargets(u)) {
                    appendEdge(xml, edgeId++, u, v, graph.getEdgeWeight(u, v));
                }
            }
        } else {
            for (int u = 0; u < vertexCount; u++) {
                for (int v = 0; v < vertexCount; v++) {
                    if (u != v && graph.hasEdge(u, v)) {
                        appendEdge(xml, edgeId++, u, v, graph.getEdgeWeight(u, v));
                    }
                }
            }
        }

        xml.append("    </edges>\n");
        xml.append("  </graph>\n");
        xml.append("</gexf>\n");

        Files.writeString(output, xml.toString(), StandardCharsets.UTF_8);
    }

    private static void appendEdgeWeightAttributeDeclaration(StringBuilder xml) {
        xml.append("    <attributes class=\"edge\" mode=\"static\">\n");
        xml.append("      <attribute id=\"0\" title=\"weight\" type=\"float\"/>\n");
        xml.append("    </attributes>\n");
    }

    private static void appendEdge(StringBuilder xml, int edgeId, int source, int target, double weight) {
        String formattedWeight = GexfFormat.formatWeight(weight);
        xml.append("      <edge id=\"")
                .append(edgeId)
                .append("\" source=\"")
                .append(source)
                .append("\" target=\"")
                .append(target)
                .append("\" weight=\"")
                .append(formattedWeight)
                .append("\">\n");
        xml.append("        <attvalues>\n");
        xml.append("          <attvalue for=\"0\" value=\"")
                .append(formattedWeight)
                .append("\"/>\n");
        xml.append("        </attvalues>\n");
        xml.append("      </edge>\n");
    }
}
