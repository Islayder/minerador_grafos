package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.RepositoryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StressDataFactoryTest {

    @Test
    void shouldGenerateExpectedCountsForSmallProfile() {
        RepositoryData data = StressDataFactory.create(StressProfile.SMALL);

        assertEquals(100, data.getAllUsers().size());
        assertEquals(500, data.getInteractions().size());
    }

    @Test
    void shouldNotGenerateSelfInteractions() {
        RepositoryData data = StressDataFactory.create(StressProfile.SMALL);
        for (GitHubInteraction interaction : data.getInteractions()) {
            assertNotEquals(
                    interaction.getSourceUser().getLogin(),
                    interaction.getTargetUser().getLogin());
        }
    }
}
