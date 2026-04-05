package net.undeadriders.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.undeadriders.UndeadRiders;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ModrinthUpdateChecker {

    private static final String PROJECT_ID = "JVLPDceE";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private static final AtomicBoolean CHECK_STARTED = new AtomicBoolean(false);

    private ModrinthUpdateChecker() {
    }

    public static void checkOnceAsync() {
        if (!CHECK_STARTED.compareAndSet(false, true)) {
            return;
        }

        Thread thread = new Thread(ModrinthUpdateChecker::checkForUpdate, "undeadriders-modrinth-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void checkForUpdate() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version"))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", "UndeadRiders/" + currentVersion())
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                UndeadRiders.LOGGER.debug("[UndeadRiders] Update check returned HTTP {}.", response.statusCode());
                return;
            }

            Optional<String> latestVersion = extractLatestVersion(response.body());
            if (latestVersion.isEmpty()) {
                UndeadRiders.LOGGER.debug("[UndeadRiders] Update check returned no usable versions.");
                return;
            }

            String currentVersion = currentVersion();
            String newestVersion = latestVersion.get();
            if (isNewerVersion(newestVersion, currentVersion)) {
                UndeadRiders.LOGGER.info("[UndeadRiders] New version available: {} (current: {})",
                        newestVersion, currentVersion);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            UndeadRiders.LOGGER.debug("[UndeadRiders] Update check failed.", e);
        }
    }

    private static Optional<String> extractLatestVersion(String responseBody) {
        JsonElement root = JsonParser.parseString(responseBody);
        if (!root.isJsonArray()) {
            return Optional.empty();
        }

        JsonArray versions = root.getAsJsonArray();
        String currentMinecraftVersion = currentMinecraftVersion();
        VersionCandidate bestMatch = null;
        VersionCandidate bestRelease = null;

        for (JsonElement versionElement : versions) {
            if (!versionElement.isJsonObject()) {
                continue;
            }

            JsonObject versionObject = versionElement.getAsJsonObject();
            String versionNumber = getString(versionObject, "version_number");
            if (versionNumber == null || versionNumber.isBlank()) {
                continue;
            }

            String versionType = getString(versionObject, "version_type");
            Instant publishedAt = getPublishedAt(versionObject);
            if (publishedAt == null) {
                continue;
            }

            VersionCandidate candidate = new VersionCandidate(versionNumber, publishedAt);
            if ("release".equalsIgnoreCase(versionType)) {
                if (bestRelease == null || candidate.isNewerThan(bestRelease)) {
                    bestRelease = candidate;
                }

                if (matchesCurrentEnvironment(versionObject, currentMinecraftVersion)
                        && (bestMatch == null || candidate.isNewerThan(bestMatch))) {
                    bestMatch = candidate;
                }
            }
        }

        if (bestMatch != null) {
            return Optional.of(bestMatch.versionNumber());
        }

        return Optional.ofNullable(bestRelease != null ? bestRelease.versionNumber() : null);
    }

    private static String getString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }

        return value.getAsString();
    }

    private static String currentVersion() {
        return FabricLoader.getInstance()
                .getModContainer(UndeadRiders.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static String currentMinecraftVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static boolean isNewerVersion(String candidate, String current) {
        try {
            Version candidateVersion = Version.parse(candidate);
            Version currentVersion = Version.parse(current);
            return candidateVersion.compareTo(currentVersion) > 0;
        } catch (VersionParsingException e) {
            UndeadRiders.LOGGER.debug("[UndeadRiders] Could not compare versions: candidate='{}', current='{}'.",
                    candidate, current, e);
            return false;
        }
    }

    private static boolean matchesCurrentEnvironment(JsonObject versionObject, String currentMinecraftVersion) {
        return jsonArrayContains(versionObject, "loaders", "fabric")
                && jsonArrayContains(versionObject, "game_versions", currentMinecraftVersion);
    }

    private static boolean jsonArrayContains(JsonObject object, String key, String expected) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonArray()) {
            return false;
        }

        for (JsonElement element : value.getAsJsonArray()) {
            if (element.isJsonPrimitive() && expected.equalsIgnoreCase(element.getAsString())) {
                return true;
            }
        }

        return false;
    }

    private static Instant getPublishedAt(JsonObject versionObject) {
        String datePublished = getString(versionObject, "date_published");
        if (datePublished == null || datePublished.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(datePublished);
        } catch (DateTimeParseException e) {
            UndeadRiders.LOGGER.debug("[UndeadRiders] Could not parse Modrinth publication date '{}'.",
                    datePublished, e);
            return null;
        }
    }

    private record VersionCandidate(String versionNumber, Instant publishedAt) {
        private boolean isNewerThan(VersionCandidate other) {
            return publishedAt.isAfter(other.publishedAt);
        }
    }
}
