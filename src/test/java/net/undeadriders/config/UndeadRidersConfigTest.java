package net.undeadriders.config;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UndeadRidersConfigTest {
    @TempDir Path directory;

    @Test
    void malformedConfigIsNotOverwritten() throws Exception {
        Path file = directory.resolve("undeadriders.json");
        String original = "{\"zombieHorsemanEnabled\":false,\"spawnAttemptsPerPlayer\":oops}";
        Files.writeString(file, original);
        UndeadRidersConfig.load(file);
        assertEquals(original, Files.readString(file));
    }

    @Test
    void nullConfigIsNotOverwritten() throws Exception {
        Path file = directory.resolve("undeadriders.json");
        Files.writeString(file, "null");
        UndeadRidersConfig.load(file);
        assertEquals("null", Files.readString(file));
    }

    @Test
    void validConfigCanBeReplacedAfterReadingAndLeavesNoTemporaryFiles() throws Exception {
        Path file = directory.resolve("undeadriders.json");
        Files.writeString(file, "{\"zombieHorsemanEnabled\":false,\"spawnCheckIntervalTicks\":0}");
        UndeadRidersConfig config = UndeadRidersConfig.load(file);
        assertFalse(config.zombieHorsemanEnabled);
        assertEquals(20, config.spawnCheckIntervalTicks);
        assertTrue(Files.readString(file).contains("\"spawnCheckIntervalTicks\": 20"));
        assertTrue(Files.readString(file).contains("\"babyUndeadRiderChance\""));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void extremeDistancesCannotOverflowOrProduceAnEmptyRandomRange() {
        UndeadRidersConfig config = new UndeadRidersConfig();
        config.minSpawnDistance = Integer.MAX_VALUE;
        config.maxSpawnDistance = Integer.MIN_VALUE;
        config.spawnAttemptsPerPlayer = Integer.MAX_VALUE;
        config.spawnCheckIntervalTicks = 0;
        config.validateForEditing();

        assertEquals(1016, config.minSpawnDistance);
        assertEquals(1024, config.maxSpawnDistance);
        assertEquals(64, config.spawnAttemptsPerPlayer);
        assertEquals(20, config.spawnCheckIntervalTicks);
    }

    @Test
    void invalidProbabilitiesBecomeFiniteAndBounded() {
        UndeadRidersConfig config = new UndeadRidersConfig();
        config.zombieHorsemanSpawnRate = Float.NaN;
        config.babyUndeadRiderChance = Float.POSITIVE_INFINITY;
        config.zombieHorseArmorChance = Float.NEGATIVE_INFINITY;
        config.validateForEditing();

        assertEquals(0f, config.zombieHorsemanSpawnRate);
        assertEquals(1f, config.babyUndeadRiderChance);
        assertEquals(0f, config.zombieHorseArmorChance);
        assertDoesNotThrow(() -> new Gson().toJson(config));
    }

    @Test
    void partialOldConfigsKeepNewDefaultsAndExistingChoices() {
        UndeadRidersConfig config = new Gson().fromJson(
            "{\"zombieHorsemanEnabled\":false,\"maxHorsemenPerPlayer\":0}", UndeadRidersConfig.class);
        config.validateForEditing();

        assertFalse(config.zombieHorsemanEnabled);
        assertEquals(0, config.maxHorsemenPerPlayer);
        assertTrue(config.zombifiedPiglinRiderEnabled);
        assertEquals(0.3f, config.zombifiedPiglinRiderSpawnRate);
        assertEquals(24, config.minSpawnDistance);
        assertEquals(64, config.maxSpawnDistance);
        assertFalse(config.skeletonHorseSunlightBurnEnabled);
    }
}
