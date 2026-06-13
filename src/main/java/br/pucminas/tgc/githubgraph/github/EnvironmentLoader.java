package br.pucminas.tgc.githubgraph.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Carrega variáveis de ambiente locais, com suporte opcional ao arquivo {@code .env} na raiz do projeto.
 */
public final class EnvironmentLoader {

    static final String GITHUB_TOKEN_KEY = "GITHUB_TOKEN";
    private static final String ENV_FILE_NAME = ".env";

    private EnvironmentLoader() {
    }

    /**
     * Obtém {@code GITHUB_TOKEN} da variável de ambiente ou, se ausente, do arquivo {@code .env}.
     *
     * @return token trimado, ou {@code null} se não configurado
     */
    public static String loadGitHubToken() {
        return loadGitHubToken(defaultEnvFilePath());
    }

    static String loadGitHubToken(Path envFilePath) {
        return resolveGitHubToken(System.getenv(GITHUB_TOKEN_KEY), envFilePath);
    }

    static String resolveGitHubToken(String environmentValue, Path envFilePath) {
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        return readKeyFromEnvFile(envFilePath, GITHUB_TOKEN_KEY);
    }

    static Path defaultEnvFilePath() {
        return Path.of(System.getProperty("user.dir")).resolve(ENV_FILE_NAME);
    }

    static String readKeyFromEnvFile(Path envFilePath, String key) {
        if (envFilePath == null || key == null || key.isBlank() || !Files.isRegularFile(envFilePath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(envFilePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String parsed = parseValue(line, key);
                if (parsed != null) {
                    return parsed;
                }
            }
        } catch (IOException ignored) {
            return null;
        }
        return null;
    }

    static String parseValue(String line, String key) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return null;
        }

        String entryKey = trimmed.substring(0, separator).trim();
        if (!key.equals(entryKey)) {
            return null;
        }

        String value = trimmed.substring(separator + 1).trim();
        if (value.isEmpty()) {
            return null;
        }
        return stripOptionalQuotes(value);
    }

    private static String stripOptionalQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
