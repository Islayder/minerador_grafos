package br.pucminas.tgc.githubgraph.app.desktop;

/**
 * Origem dos dados do último grafo carregado na interface desktop.
 */
public enum DataSourceStatus {
    NONE("Nenhum grafo carregado"),
    REAL("Mineracao completa (repositorio configurado)"),
    OFFLINE("Demonstração offline (contingência)");

    private final String description;

    DataSourceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
