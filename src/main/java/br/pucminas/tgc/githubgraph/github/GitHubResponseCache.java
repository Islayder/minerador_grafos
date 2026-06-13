package br.pucminas.tgc.githubgraph.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cache local de respostas JSON da GitHub API (por URL, sem token no nome do arquivo).
 */
public final class GitHubResponseCache {

    public static final String DEFAULT_CACHE_DIRECTORY = "cache/github";

    private final Path cacheDirectory;
    private final boolean enabled;

    public GitHubResponseCache(boolean enabled) {
        this(enabled, Path.of(DEFAULT_CACHE_DIRECTORY));
    }

    public GitHubResponseCache(boolean enabled, Path cacheDirectory) {
        this.enabled = enabled;
        this.cacheDirectory = cacheDirectory.toAbsolutePath().normalize();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String get(String url) throws IOException {
        if (!enabled) {
            return null;
        }
        Path file = resolveCacheFile(url);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public void put(String url, String body) throws IOException {
        if (!enabled || body == null) {
            return;
        }
        Path file = resolveCacheFile(url);
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, body, StandardCharsets.UTF_8);
    }

    Path resolveCacheFile(String url) {
        String hash = sha256(url);
        return cacheDirectory.resolve(hash.substring(0, 2)).resolve(hash + ".json");
    }

    static String sha256(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }
}
