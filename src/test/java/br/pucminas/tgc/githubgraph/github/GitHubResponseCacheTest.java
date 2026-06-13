package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubResponseCacheTest {

    @Test
    void shouldStoreAndReadResponseByUrl(@TempDir Path tempDir) throws IOException {
        GitHubResponseCache cache = new GitHubResponseCache(true, tempDir.resolve("github"));
        String url = "https://api.github.com/repos/giscus/giscus/issues?page=1";

        cache.put(url, "{\"items\":[]}");
        String cached = cache.get(url);

        assertEquals("{\"items\":[]}", cached);
        assertTrue(Files.exists(cache.resolveCacheFile(url)));
    }

    @Test
    void cacheFileNameMustNotContainToken(@TempDir Path tempDir) throws IOException {
        GitHubResponseCache cache = new GitHubResponseCache(true, tempDir);
        String url = "https://api.github.com/repos/giscus/giscus/issues";
        cache.put(url, "[]");

        Path file = cache.resolveCacheFile(url);
        String fileName = file.getFileName().toString();
        assertFalse(fileName.toLowerCase().contains("token"));
        assertFalse(fileName.toLowerCase().contains("github_pat"));
        assertFalse(Files.readString(file).contains("ghp_"));
    }

    @Test
    void disabledCacheReturnsNullWithoutCreatingDirectory(@TempDir Path tempDir) throws IOException {
        Path cacheRoot = tempDir.resolve("disabled-cache-root");
        GitHubResponseCache cache = new GitHubResponseCache(false, cacheRoot);
        assertNull(cache.get("https://api.github.com/repos/giscus/giscus/issues"));
        assertFalse(Files.exists(cacheRoot));
    }

    @Test
    void missingCacheEntryReturnsNull(@TempDir Path tempDir) throws IOException {
        GitHubResponseCache cache = new GitHubResponseCache(true, tempDir);
        assertNull(cache.get("https://api.github.com/repos/giscus/giscus/pulls?page=99"));
    }
}
