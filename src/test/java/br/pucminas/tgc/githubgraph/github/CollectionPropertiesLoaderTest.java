package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionPropertiesLoaderTest {

    @Test
    void shouldLoadFullRepositoryFromFile(@TempDir Path tempDir) throws IOException {
        Path file = writeFullRepositoryProperties(tempDir);
        CollectionPropertiesLoader loader = new CollectionPropertiesLoader(file);

        CollectionProfile profile = loader.loadConfiguredProfile();

        assertEquals(GitHubCollectionMode.FULL_REPOSITORY, profile.getMode());
        assertEquals("giscus", profile.getOwner());
        assertEquals("giscus", profile.getRepository());
        assertEquals(100, profile.getPerPage());
        assertEquals(4, profile.getConcurrency());
        assertTrue(profile.isCacheEnabled());
        assertTrue(profile.getDescription().contains("repositorio inteiro"));
    }

    @Test
    void shouldUseFallbackWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("missing.properties");
        CollectionPropertiesLoader loader = new CollectionPropertiesLoader(missing);

        CollectionProfile profile = loader.loadConfiguredProfile();

        assertEquals("giscus", profile.getOwner());
        assertEquals("giscus", profile.getRepository());
        assertEquals(GitHubCollectionMode.FULL_REPOSITORY, profile.getMode());
        assertTrue(profile.isCacheEnabled());
    }

    @Test
    void shouldRespectCacheDisabledInPropertiesFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("no-cache.properties");
        Files.writeString(file, """
                github.owner=giscus
                github.repository=giscus
                github.collection.mode=FULL_REPOSITORY
                github.collection.perPage=100
                github.collection.concurrency=4
                github.collection.cacheEnabled=false
                github.collection.description=teste
                """);
        CollectionProfile profile = new CollectionPropertiesLoader(file).loadConfiguredProfile();
        assertFalse(profile.isCacheEnabled());
    }

    @Test
    void shouldRejectNonPositivePerPage(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bad.properties");
        Files.writeString(file, """
                github.owner=giscus
                github.repository=giscus
                github.collection.mode=FULL_REPOSITORY
                github.collection.perPage=0
                github.collection.concurrency=4
                github.collection.cacheEnabled=false
                """);

        CollectionPropertiesLoader loader = new CollectionPropertiesLoader(file);

        assertThrows(IllegalArgumentException.class, loader::loadConfiguredProfile);
    }

    @Test
    void shouldRejectUnsupportedMode(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bad-mode.properties");
        Files.writeString(file, """
                github.owner=giscus
                github.repository=giscus
                github.collection.mode=SAMPLE_LIMITED
                github.collection.perPage=100
                github.collection.concurrency=4
                github.collection.cacheEnabled=false
                """);

        CollectionPropertiesLoader loader = new CollectionPropertiesLoader(file);

        assertThrows(IllegalArgumentException.class, loader::loadConfiguredProfile);
    }

    private static Path writeFullRepositoryProperties(Path tempDir) throws IOException {
        Path file = tempDir.resolve("collection.properties");
        Files.writeString(file, """
                github.owner=giscus
                github.repository=giscus
                github.collection.mode=FULL_REPOSITORY
                github.collection.perPage=100
                github.collection.concurrency=4
                github.collection.cacheEnabled=true
                github.collection.description=Minera o repositorio inteiro com paginacao ate o fim.
                """);
        return file;
    }
}
