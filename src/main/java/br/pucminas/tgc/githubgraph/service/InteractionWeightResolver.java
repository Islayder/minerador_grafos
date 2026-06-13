package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.model.InteractionType;

/**
 * Resolve o peso fixo associado a cada tipo de interação no grafo integrado.
 */
public final class InteractionWeightResolver {

    public double resolve(InteractionType type) {
        return switch (type) {
            case COMMENT -> 2.0;
            case ISSUE_COMMENTED_BY_OTHER_USER -> 3.0;
            case ISSUE_CLOSED -> 3.0;
            case PR_REVIEW, PR_APPROVAL -> 4.0;
            case PR_MERGE -> 5.0;
        };
    }
}
