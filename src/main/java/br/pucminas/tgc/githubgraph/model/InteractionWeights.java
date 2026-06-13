package br.pucminas.tgc.githubgraph.model;

/**
 * Pesos fixos das interações entre colaboradores no grafo integrado.
 * Valores alinhados a {@link br.pucminas.tgc.githubgraph.service.InteractionWeightResolver}.
 */
public final class InteractionWeights {

    public static final double COMMENT_ON_ISSUE_OR_PR = 2.0;
    public static final double ISSUE_OPENED_WITH_COMMENT = 3.0;
    public static final double ISSUE_CLOSED = 3.0;
    public static final double PR_REVIEW_OR_APPROVAL = 4.0;
    public static final double PR_MERGE = 5.0;

    private InteractionWeights() {
    }
}
