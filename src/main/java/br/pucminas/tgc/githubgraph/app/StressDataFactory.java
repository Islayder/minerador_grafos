package br.pucminas.tgc.githubgraph.app;

import br.pucminas.tgc.githubgraph.model.GitHubInteraction;
import br.pucminas.tgc.githubgraph.model.GitHubUser;
import br.pucminas.tgc.githubgraph.model.InteractionType;
import br.pucminas.tgc.githubgraph.model.RepositoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gera {@link RepositoryData} artificial e deterministico para medir o motor interno de grafos.
 */
public final class StressDataFactory {

    private static final long SEED = 42L;
    private static final InteractionType[] TYPES = InteractionType.values();

    private StressDataFactory() {
    }

    public static RepositoryData create(StressProfile profile) {
        return create(profile.getUserCount(), profile.getInteractionCount());
    }

    public static RepositoryData create(int userCount, int interactionCount) {
        if (userCount < 2) {
            throw new IllegalArgumentException("Stress precisa de pelo menos 2 usuarios.");
        }
        if (interactionCount <= 0) {
            throw new IllegalArgumentException("Stress precisa de interacoes positivas.");
        }

        List<GitHubUser> users = new ArrayList<>(userCount);
        for (int index = 0; index < userCount; index++) {
            users.add(new GitHubUser("stress-user-" + index));
        }

        Random random = new Random(SEED);
        List<GitHubInteraction> interactions = new ArrayList<>(interactionCount);
        for (int index = 0; index < interactionCount; index++) {
            int sourceIndex = random.nextInt(userCount);
            int targetIndex = random.nextInt(userCount - 1);
            if (targetIndex >= sourceIndex) {
                targetIndex++;
            }
            InteractionType type = TYPES[random.nextInt(TYPES.length)];
            interactions.add(new GitHubInteraction(users.get(sourceIndex), users.get(targetIndex), type));
        }

        return new RepositoryData("giscus", "giscus", interactions);
    }
}
