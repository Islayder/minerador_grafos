package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubConfigTest {

    @Test
    void defaultForConfiguredRepositoryShouldUseFacebookAndDocusaurus() {
        GitHubConfig config = GitHubConfig.defaultForConfiguredRepository();

        assertEquals("giscus", config.getOwner());
        assertEquals("giscus", config.getRepository());
        assertTrue(config.isFullRepository());
    }

    @Test
    void fullRepositoryFromProfileShouldNotUseSamplingLimits() {
        CollectionProfile profile = CollectionProfile.fullRepository(
                "giscus", "giscus", 100, 4, false, "teste");

        GitHubConfig config = GitHubConfig.fromProfile(profile);

        assertTrue(config.isFullRepository());
        assertEquals("giscus", config.getOwner());
        assertEquals("giscus", config.getRepository());
    }

    @Test
    void sampleLimitedRequiresPositiveLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> GitHubConfig.sampleLimited("giscus", "giscus", 0, 10, 10, 10, null));
    }

    @Test
    void fromProfileFullRepositoryLoadsCacheFromConfiguredProperties() {
        CollectionPropertiesLoader loader = new CollectionPropertiesLoader();
        CollectionProfile profile = loader.loadConfiguredProfile();

        assertTrue(profile.isFullRepository());
        assertTrue(profile.isCacheEnabled());
        assertEquals(100, profile.getPerPage());
        assertEquals(6, profile.getConcurrency());
    }

    @Test
    void ownerAndRepositoryMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> GitHubConfig.fullRepository(null, "giscus", null));
        assertThrows(IllegalArgumentException.class,
                () -> GitHubConfig.fullRepository("giscus", "  ", null));
    }
}
