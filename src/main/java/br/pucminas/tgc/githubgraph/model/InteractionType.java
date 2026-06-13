package br.pucminas.tgc.githubgraph.model;

/**
 * Tipos de interação entre colaboradores considerados no grafo de colaboração.
 */
public enum InteractionType {
    COMMENT,
    ISSUE_COMMENTED_BY_OTHER_USER,
    ISSUE_CLOSED,
    PR_REVIEW,
    PR_APPROVAL,
    PR_MERGE
}
