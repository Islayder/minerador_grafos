package br.pucminas.tgc.githubgraph.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvironmentLoaderTest {

    @Test
    void shouldNotFailWhenEnvFileDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve(".env");
        assertNull(EnvironmentLoader.resolveGitHubToken(null, missing));
    }

    @Test
    void shouldReadGitHubTokenFromEnvFile(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "GITHUB_TOKEN=abc123\n");

        assertEquals("abc123", EnvironmentLoader.resolveGitHubToken(null, envFile));
    }

    @Test
    void shouldIgnoreEmptyLines(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "\n\nGITHUB_TOKEN=token_ok\n\n");

        assertEquals("token_ok", EnvironmentLoader.resolveGitHubToken(null, envFile));
    }

    @Test
    void shouldIgnoreCommentLines(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # comentário de exemplo
                GITHUB_TOKEN=from_file
                """);

        assertEquals("from_file", EnvironmentLoader.resolveGitHubToken(null, envFile));
    }

    @Test
    void shouldIgnoreOtherKeys(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                OTHER_KEY=ignored
                GITHUB_TOKEN=expected
                ANOTHER=value
                """);

        assertEquals("expected", EnvironmentLoader.resolveGitHubToken(null, envFile));
    }

    @Test
    void environmentVariableShouldTakePrecedenceOverEnvFile(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "GITHUB_TOKEN=from_file\n");

        assertEquals("from_env", EnvironmentLoader.resolveGitHubToken("from_env", envFile));
    }

    @Test
    void parseValueShouldReturnNullForUnrelatedLine() {
        assertNull(EnvironmentLoader.parseValue("FOO=bar", EnvironmentLoader.GITHUB_TOKEN_KEY));
    }
}
