package br.pucminas.tgc.githubgraph.github;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Carrega configuracao de coleta de {@code config/collection.properties} (sem token; sem internet).
 */
public final class CollectionPropertiesLoader {

    public static final String DEFAULT_PROPERTIES_PATH = "config/collection.properties";
    private static final String KEY_OWNER = "github.owner";
    private static final String KEY_REPOSITORY = "github.repository";
    private static final String KEY_MODE = "github.collection.mode";
    private static final String KEY_PER_PAGE = "github.collection.perPage";
    private static final String KEY_CONCURRENCY = "github.collection.concurrency";
    private static final String KEY_CACHE_ENABLED = "github.collection.cacheEnabled";
    private static final String KEY_DESCRIPTION = "github.collection.description";

    private final Path propertiesPath;

    public CollectionPropertiesLoader() {
        this(defaultPropertiesPath());
    }

    public CollectionPropertiesLoader(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public static Path defaultPropertiesPath() {
        return Path.of(System.getProperty("user.dir")).resolve(DEFAULT_PROPERTIES_PATH);
    }

    public CollectionProfile loadConfiguredProfile() {
        Properties properties = loadProperties();
        GitHubCollectionMode mode = GitHubCollectionMode.fromString(
                properties.getProperty(KEY_MODE, GitHubCollectionMode.FULL_REPOSITORY.name()));
        if (mode != GitHubCollectionMode.FULL_REPOSITORY) {
            throw new IllegalArgumentException(
                    "Modo de coleta nao suportado na aplicacao principal: " + mode
                            + ". Use FULL_REPOSITORY em " + propertiesPath);
        }
        String owner = readString(properties, KEY_OWNER, "giscus");
        String repository = readString(properties, KEY_REPOSITORY, "giscus");
        int perPage = readPositiveInt(properties, KEY_PER_PAGE, 100);
        int concurrency = readPositiveInt(properties, KEY_CONCURRENCY, 6);
        boolean cacheEnabled = readBoolean(properties, KEY_CACHE_ENABLED, true);
        String description = readString(properties, KEY_DESCRIPTION,
                "Minera o repositorio inteiro giscus/giscus com paginacao ate o fim.");

        return CollectionProfile.fullRepository(
                owner,
                repository,
                perPage,
                concurrency,
                cacheEnabled,
                description);
    }

    Properties loadProperties() {
        if (Files.isRegularFile(propertiesPath)) {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(propertiesPath)) {
                properties.load(input);
                return properties;
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Falha ao ler arquivo de configuracao: " + propertiesPath, exception);
            }
        }
        return defaultProperties();
    }

    private static Properties defaultProperties() {
        Properties properties = new Properties();
        properties.setProperty(KEY_OWNER, "giscus");
        properties.setProperty(KEY_REPOSITORY, "giscus");
        properties.setProperty(KEY_MODE, GitHubCollectionMode.FULL_REPOSITORY.name());
        properties.setProperty(KEY_PER_PAGE, "100");
        properties.setProperty(KEY_CONCURRENCY, "4");
        properties.setProperty(KEY_CACHE_ENABLED, "true");
        properties.setProperty(KEY_DESCRIPTION,
                "Minera o repositorio inteiro giscus/giscus com paginacao ate o fim.");
        return properties;
    }

    private static int readPositiveInt(Properties properties, String key, int fallback) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                throw new IllegalArgumentException(
                        "Valor invalido em " + key + ": deve ser positivo (valor=" + value + ").");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor numerico invalido em " + key + ": " + raw, exception);
        }
    }

    private static String readString(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(raw.trim());
    }
}
