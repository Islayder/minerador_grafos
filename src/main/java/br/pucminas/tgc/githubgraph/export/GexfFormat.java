package br.pucminas.tgc.githubgraph.export;

import java.util.Locale;

/**
 * Formatacao numerica e escape XML compartilhados pelos exportadores GEXF.
 */
final class GexfFormat {

    private GexfFormat() {
    }

    static String formatWeight(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0.0";
        }
        String formatted = String.format(Locale.US, "%.4f", value);
        if (!formatted.contains(".")) {
            return formatted + ".0";
        }
        formatted = formatted.replaceAll("0+$", "");
        if (formatted.endsWith(".")) {
            formatted = formatted + "0";
        }
        return formatted;
    }

    static String escapeXml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
