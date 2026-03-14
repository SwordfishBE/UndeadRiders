package net.undeadriders.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.undeadriders.UndeadRiders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Configuration for Undead Riders.
 * Stored at: config/undeadriders.json
 * Reload at runtime with: /undeadriders reload
 */
public class UndeadRidersConfig {

    // ── Toggles ───────────────────────────────────────────────────────────────
    public boolean zombieHorsemanEnabled   = true;
    public boolean huskHorsemanEnabled     = true;
    public boolean skeletonHorsemanEnabled = true;
    /** Desert biomes only. */
    public boolean parchedHorsemanEnabled  = true;
    /** Swamp and Mangrove Swamp biomes only. */
    public boolean boggedHorsemanEnabled   = true;
    /** Frozen biomes only (Ice Spikes, Snowy Plains, Jagged/Frozen Peaks, Snowy Slopes). */
    public boolean strayHorsemanEnabled    = true;

    // ── Spawn rates (0.0 – 1.0) ───────────────────────────────────────────────
    public float zombieHorsemanSpawnRate   = 0.15f;
    public float huskHorsemanSpawnRate     = 0.15f;
    public float skeletonHorsemanSpawnRate = 0.15f;
    public float parchedHorsemanSpawnRate  = 0.15f;
    public float boggedHorsemanSpawnRate   = 0.15f;
    public float strayHorsemanSpawnRate    = 0.15f;

    // ── Spawn timing ──────────────────────────────────────────────────────────
    /** How often (ticks) a spawn round is triggered. Default: 400 (20 seconds). */
    public int spawnCheckIntervalTicks = 400;
    /** Spawn attempts per player per round. Higher = more frequent spawns. Default: 6 */
    public int spawnAttemptsPerPlayer  = 6;

    // ── Spawn distance ────────────────────────────────────────────────────────
    public int minSpawnDistance = 24;
    public int maxSpawnDistance = 64;

    // ── Conditions ────────────────────────────────────────────────────────────
    /**
     * Zombie/Husk/Skeleton/Bogged/Stray horsemen only spawn at night or during thunderstorms.
     * Parched horsemen ignore this and can spawn both day and night.
     * Default: true
     */
    public boolean nightOnly = true;
    /** Max horsemen of each type per online player. Default: 5 */
    public int maxHorsemenPerPlayer = 5;

    // ── Load / Save ───────────────────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("undeadriders.json");

    public static UndeadRidersConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                UndeadRidersConfig config = GSON.fromJson(reader, UndeadRidersConfig.class);
                if (config != null) {
                    config.validate();
                    config.save();
                    return config;
                }
            } catch (IOException e) {
                UndeadRiders.LOGGER.error("[UndeadRiders] Failed to read config — using defaults.", e);
            }
        }
        UndeadRidersConfig defaults = new UndeadRidersConfig();
        defaults.save();
        UndeadRiders.LOGGER.info("[UndeadRiders] Created default config at {}", CONFIG_PATH);
        return defaults;
    }

    public void save() {
        try {
            File file = CONFIG_PATH.toFile();
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            UndeadRiders.LOGGER.error("[UndeadRiders] Failed to save config.", e);
        }
    }

    private void validate() {
        zombieHorsemanSpawnRate   = clamp(zombieHorsemanSpawnRate,   0f, 1f);
        huskHorsemanSpawnRate     = clamp(huskHorsemanSpawnRate,     0f, 1f);
        skeletonHorsemanSpawnRate = clamp(skeletonHorsemanSpawnRate, 0f, 1f);
        parchedHorsemanSpawnRate  = clamp(parchedHorsemanSpawnRate,  0f, 1f);
        boggedHorsemanSpawnRate   = clamp(boggedHorsemanSpawnRate,   0f, 1f);
        strayHorsemanSpawnRate    = clamp(strayHorsemanSpawnRate,    0f, 1f);
        spawnCheckIntervalTicks   = Math.max(20, spawnCheckIntervalTicks);
        spawnAttemptsPerPlayer    = Math.max(1,  spawnAttemptsPerPlayer);
        minSpawnDistance          = Math.max(8,  minSpawnDistance);
        maxSpawnDistance          = Math.max(minSpawnDistance + 8, maxSpawnDistance);
        maxHorsemenPerPlayer      = Math.max(0,  maxHorsemenPerPlayer);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
