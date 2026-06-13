package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.model.InteractionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InteractionWeightResolverTest {

    private final InteractionWeightResolver resolver = new InteractionWeightResolver();

    @Test
    void shouldReturnFixedWeightsForAllInteractionTypes() {
        assertEquals(2.0, resolver.resolve(InteractionType.COMMENT));
        assertEquals(3.0, resolver.resolve(InteractionType.ISSUE_COMMENTED_BY_OTHER_USER));
        assertEquals(3.0, resolver.resolve(InteractionType.ISSUE_CLOSED));
        assertEquals(4.0, resolver.resolve(InteractionType.PR_REVIEW));
        assertEquals(4.0, resolver.resolve(InteractionType.PR_APPROVAL));
        assertEquals(5.0, resolver.resolve(InteractionType.PR_MERGE));
    }
}
