package br.pucminas.tgc.githubgraph.export;

import br.pucminas.tgc.githubgraph.analysis.DegreeMetrics;
import br.pucminas.tgc.githubgraph.graph.AbstractGraph;
import br.pucminas.tgc.githubgraph.graph.impl.AdjacencyListGraph;
import br.pucminas.tgc.githubgraph.service.GraphBuildResult;
import br.pucminas.tgc.githubgraph.service.UserIndexMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Exporta o grafo integrado com labels reais, layout inicial e pesos para o Gephi.
 * O atributo nativo {@code weight} de cada aresta carrega o peso real acumulado do grafo;
 * {@code visualWeight} permanece opcional para espessura suavizada no painel Appearance.
 * Nao altera o grafo em memoria nem os pesos usados na analise.
 */
public final class GephiPresentationExporter {

    static final double MIN_NODE_SIZE = 2.0;
    static final double MAX_NODE_SIZE = 8.0;
    static final double NODE_SIZE_BASE = 2.0;
    static final double NODE_SIZE_LOG_FACTOR = 1.2;

    static final double MIN_EDGE_VISUAL_WEIGHT = 0.2;
    static final double MAX_EDGE_VISUAL_WEIGHT = 1.5;
    static final double EDGE_VISUAL_BASE = 0.2;
    static final double EDGE_VISUAL_LOG_FACTOR = 0.25;

    private static final double[] RING_RADIUS = {80.0, 180.0, 320.0, 480.0};
    private static final double JITTER_SCALE = 12.0;

    private GephiPresentationExporter() {
    }

    public static void exportIntegratedPresentationGraph(GraphBuildResult result, Path outputPath)
            throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("GraphBuildResult nao pode ser nulo.");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("Caminho de saida nao pode ser nulo.");
        }

        AbstractGraph graph = result.getIntegratedGraph();
        UserIndexMapper mapper = result.getUserIndexMapper();
        int vertexCount = graph.getVertexCount();

        DegreeMetrics degreeMetrics = new DegreeMetrics();
        int[] totalDegrees = new int[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            totalDegrees[vertex] = degreeMetrics.totalDegree(graph, vertex);
        }

        double[] positionsX = new double[vertexCount];
        double[] positionsY = new double[vertexCount];
        assignRadialPositions(vertexCount, totalDegrees, positionsX, positionsY);

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<gexf xmlns=\"http://www.gexf.net/1.2draft\" ");
        xml.append("xmlns:viz=\"http://www.gexf.net/1.2draft/viz\" version=\"1.2\">\n");
        xml.append("  <graph defaultedgetype=\"directed\" mode=\"static\">\n");
        appendEdgeAttributeDeclarations(xml);
        xml.append("    <nodes>\n");

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            String label = resolveLabel(mapper, vertex);
            double nodeSize = computeNodeSize(totalDegrees[vertex]);
            appendNode(xml, vertex, label, nodeSize, positionsX[vertex], positionsY[vertex]);
        }

        xml.append("    </nodes>\n");
        xml.append("    <edges>\n");

        int edgeId = 0;
        if (graph instanceof AdjacencyListGraph listGraph) {
            for (int source = 0; source < vertexCount; source++) {
                for (int target : listGraph.getOutgoingTargets(source)) {
                    appendEdge(xml, edgeId++, source, target, graph.getEdgeWeight(source, target));
                }
            }
        } else {
            for (int source = 0; source < vertexCount; source++) {
                for (int target = 0; target < vertexCount; target++) {
                    if (source != target && graph.hasEdge(source, target)) {
                        appendEdge(xml, edgeId++, source, target, graph.getEdgeWeight(source, target));
                    }
                }
            }
        }

        xml.append("    </edges>\n");
        xml.append("  </graph>\n");
        xml.append("</gexf>\n");

        Files.writeString(outputPath, xml.toString(), StandardCharsets.UTF_8);
    }

    static double computeVisualWeight(double realWeight) {
        if (realWeight <= 0.0) {
            return MIN_EDGE_VISUAL_WEIGHT;
        }
        double rawVisualWeight = 1.0 + Math.log10(realWeight);
        double visualWeight = EDGE_VISUAL_BASE + (rawVisualWeight * EDGE_VISUAL_LOG_FACTOR);
        return Math.min(MAX_EDGE_VISUAL_WEIGHT, Math.max(MIN_EDGE_VISUAL_WEIGHT, visualWeight));
    }

    static double computeNodeSize(int totalDegree) {
        double size = NODE_SIZE_BASE + Math.log1p(totalDegree) * NODE_SIZE_LOG_FACTOR;
        return Math.min(MAX_NODE_SIZE, Math.max(MIN_NODE_SIZE, size));
    }

    private static void appendEdgeAttributeDeclarations(StringBuilder xml) {
        xml.append("    <attributes class=\"edge\" mode=\"static\">\n");
        xml.append("      <attribute id=\"0\" title=\"realWeight\" type=\"double\"/>\n");
        xml.append("      <attribute id=\"1\" title=\"visualWeight\" type=\"double\"/>\n");
        xml.append("    </attributes>\n");
    }

    private static void appendNode(
            StringBuilder xml,
            int vertex,
            String label,
            double nodeSize,
            double x,
            double y) {
        xml.append("      <node id=\"")
                .append(vertex)
                .append("\" label=\"")
                .append(GexfFormat.escapeXml(label))
                .append("\">\n");
        if (isBotLogin(label)) {
            xml.append("        <viz:color r=\"170\" g=\"170\" b=\"220\" a=\"1.0\"/>\n");
        } else {
            xml.append("        <viz:color r=\"65\" g=\"105\" b=\"225\" a=\"1.0\"/>\n");
        }
        xml.append("        <viz:size value=\"")
                .append(formatNumber(nodeSize))
                .append("\"/>\n");
        xml.append("        <viz:position x=\"")
                .append(formatNumber(x))
                .append("\" y=\"")
                .append(formatNumber(y))
                .append("\" z=\"0.0\"/>\n");
        xml.append("      </node>\n");
    }

    private static void appendEdge(StringBuilder xml, int edgeId, int source, int target, double realWeight) {
        double visualWeight = computeVisualWeight(realWeight);
        String formattedRealWeight = GexfFormat.formatWeight(realWeight);
        String formattedVisualWeight = GexfFormat.formatWeight(visualWeight);
        xml.append("      <edge id=\"")
                .append(edgeId)
                .append("\" source=\"")
                .append(source)
                .append("\" target=\"")
                .append(target)
                .append("\" weight=\"")
                .append(formattedRealWeight)
                .append("\" label=\"")
                .append(GexfFormat.escapeXml(formattedRealWeight))
                .append("\">\n");
        xml.append("        <attvalues>\n");
        xml.append("          <attvalue for=\"0\" value=\"")
                .append(formattedRealWeight)
                .append("\"/>\n");
        xml.append("          <attvalue for=\"1\" value=\"")
                .append(formattedVisualWeight)
                .append("\"/>\n");
        xml.append("        </attvalues>\n");
        xml.append("      </edge>\n");
    }

    private static String resolveLabel(UserIndexMapper mapper, int vertex) {
        if (mapper == null || vertex < 0 || vertex >= mapper.getUserCount()) {
            return "v" + vertex;
        }
        try {
            return mapper.getUser(vertex).getLogin();
        } catch (IllegalArgumentException exception) {
            return "v" + vertex;
        }
    }

    private static boolean isBotLogin(String login) {
        return login != null && login.toLowerCase().contains("[bot]");
    }

    private static void assignRadialPositions(
            int vertexCount,
            int[] totalDegrees,
            double[] positionsX,
            double[] positionsY) {
        if (vertexCount == 0) {
            return;
        }

        List<Integer> verticesByDegree = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            verticesByDegree.add(vertex);
        }
        verticesByDegree.sort(Comparator.comparingInt((Integer vertex) -> totalDegrees[vertex]).reversed());

        int topCount = Math.max(1, (int) Math.ceil(vertexCount * 0.05));
        int ring20Count = (int) Math.ceil(vertexCount * 0.20);
        int ring35Count = (int) Math.ceil(vertexCount * 0.35);

        int ring0End = Math.min(vertexCount, topCount);
        int ring1End = Math.min(vertexCount, ring0End + ring20Count);
        int ring2End = Math.min(vertexCount, ring1End + ring35Count);

        placeRing(verticesByDegree, 0, ring0End, 0, totalDegrees, positionsX, positionsY);
        placeRing(verticesByDegree, ring0End, ring1End, 1, totalDegrees, positionsX, positionsY);
        placeRing(verticesByDegree, ring1End, ring2End, 2, totalDegrees, positionsX, positionsY);
        placeRing(verticesByDegree, ring2End, vertexCount, 3, totalDegrees, positionsX, positionsY);
    }

    private static void placeRing(
            List<Integer> verticesByDegree,
            int startInclusive,
            int endExclusive,
            int ringIndex,
            int[] totalDegrees,
            double[] positionsX,
            double[] positionsY) {
        int count = endExclusive - startInclusive;
        if (count <= 0) {
            return;
        }

        double radius = RING_RADIUS[Math.min(ringIndex, RING_RADIUS.length - 1)];
        double angleStep = (2.0 * Math.PI) / count;

        for (int offset = 0; offset < count; offset++) {
            int vertex = verticesByDegree.get(startInclusive + offset);
            double angle = offset * angleStep + deterministicJitter(vertex, ringIndex, 1) * 0.08;
            double jitterX = deterministicJitter(vertex, ringIndex, 2) * JITTER_SCALE;
            double jitterY = deterministicJitter(vertex, ringIndex, 3) * JITTER_SCALE;
            positionsX[vertex] = radius * Math.cos(angle) + jitterX;
            positionsY[vertex] = radius * Math.sin(angle) + jitterY;
        }
    }

    private static double deterministicJitter(int vertex, int ring, int salt) {
        long seed = vertex * 31L + ring * 17L + salt * 13L;
        seed = (seed * 1_103_515_245L + 12_345L) & 0x7fff_ffffL;
        return ((seed % 2001) / 1000.0) - 1.0;
    }

    private static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.format(java.util.Locale.US, "%.4f", value);
    }

}
