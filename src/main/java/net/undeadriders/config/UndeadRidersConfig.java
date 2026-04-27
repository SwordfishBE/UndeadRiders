package net.undeadriders.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
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
    /** Frozen biomes only. */
    public boolean strayHorsemanEnabled    = true;

    // ── Spawn rates (0.0 – 1.0) ───────────────────────────────────────────────
    public float zombieHorsemanSpawnRate   = 0.15f;
    public float huskHorsemanSpawnRate     = 0.15f;
    public float skeletonHorsemanSpawnRate = 0.1f;
    public float parchedHorsemanSpawnRate  = 0.1f;
    public float boggedHorsemanSpawnRate   = 0.15f;
    public float strayHorsemanSpawnRate    = 0.1f;

    // ── Saddle spawn chances ──────────────────────────────────────────────────
    /**
     * Chance (0.0–1.0) that a ZombieHorse spawns with a saddle equipped.
     * The saddle still has a drop chance — players must hunt at night for saddled horses.
     * Set to 0.0 to never spawn saddles on Zombie Horses.
     * Default: 0.15 (15%)
     */
    public float zombieHorseSaddleChance   = 0.15f;

    /**
     * Chance (0.0–1.0) that a SkeletonHorse spawns with a saddle equipped.
     * Set to 0.0 to never spawn saddles on Skeleton Horses.
     * Default: 0.10 (10%)
     */
    public float skeletonHorseSaddleChance = 0.3f;

    // ── Horse armor chance ────────────────────────────────────────────────────
    /**
     * Chance (0.0–1.0) that a ZombieHorse spawns wearing horse armor.
     * The armor tier is still difficulty-scaled from the existing tables.
     * Set to 0.0 to never spawn armor on Zombie Horses.
     * Default: 0.60 (60%)
     */
    public float zombieHorseArmorChance    = 0.3f;

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

    // ── Mod compat ────────────────────────────────────────────────────────────
    /**
     * If true and Open Parties and Claims is installed, Undead Riders will not
     * naturally spawn horsemen inside claims that disable hostile natural spawns.
     */
    public boolean openPartiesAndClaimsSupport = true;

    // ── Load / Save ───────────────────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("undeadriders.json");

    public static UndeadRidersConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (JsonReader reader = new JsonReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                reader.setLenient(true);
                UndeadRidersConfig config = GSON.fromJson(reader, UndeadRidersConfig.class);
                if (config != null) {
                    config.validate();
                    config.save();
                    return config;
                }
            } catch (IOException | RuntimeException e) {
                UndeadRiders.LOGGER.error("[UndeadRiders] Failed to read config — using defaults.", e);
            }
        }
        UndeadRidersConfig defaults = new UndeadRidersConfig();
        defaults.save();
        UndeadRiders.LOGGER.debug("[UndeadRiders] Created default config at {}", CONFIG_PATH);
        return defaults;
    }

    public void save() {
        try {
            File file = CONFIG_PATH.toFile();
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(toCommentedJson());
            }
        } catch (IOException e) {
            UndeadRiders.LOGGER.error("[UndeadRiders] Failed to save config.", e);
        }
    }

    public void validateForEditing() {
        validate();
    }

    private String toCommentedJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        appendComment(sb, "Enable or disable each horseman type independently.");
        appendBoolean(sb, "zombieHorsemanEnabled", zombieHorsemanEnabled, "Enable Zombie Horseman.");
        appendBoolean(sb, "huskHorsemanEnabled", huskHorsemanEnabled, "Enable Husk Horseman. Desert biomes only.");
        appendBoolean(sb, "skeletonHorsemanEnabled", skeletonHorsemanEnabled, "Enable Skeleton Horseman.");
        appendBoolean(sb, "parchedHorsemanEnabled", parchedHorsemanEnabled, "Enable Parched Horseman. Desert biomes only.");
        appendBoolean(sb, "boggedHorsemanEnabled", boggedHorsemanEnabled, "Enable Bogged Horseman. Swamp and Mangrove Swamp only.");
        appendBoolean(sb, "strayHorsemanEnabled", strayHorsemanEnabled, "Enable Stray Horseman. Frozen biomes only.");
        sb.append('\n');

        appendComment(sb, "Spawn chance per attempt. Range: 0.0 to 1.0.");
        appendFloat(sb, "zombieHorsemanSpawnRate", zombieHorsemanSpawnRate, "Zombie Horseman spawn chance per attempt.");
        appendFloat(sb, "huskHorsemanSpawnRate", huskHorsemanSpawnRate, "Husk Horseman spawn chance per attempt.");
        appendFloat(sb, "skeletonHorsemanSpawnRate", skeletonHorsemanSpawnRate, "Skeleton Horseman spawn chance per attempt.");
        appendFloat(sb, "parchedHorsemanSpawnRate", parchedHorsemanSpawnRate, "Parched Horseman spawn chance per attempt.");
        appendFloat(sb, "boggedHorsemanSpawnRate", boggedHorsemanSpawnRate, "Bogged Horseman spawn chance per attempt.");
        appendFloat(sb, "strayHorsemanSpawnRate", strayHorsemanSpawnRate, "Stray Horseman spawn chance per attempt.");
        sb.append('\n');

        appendComment(sb, "Saddle spawn chances. Range: 0.0 to 1.0.");
        appendFloat(sb, "zombieHorseSaddleChance", zombieHorseSaddleChance, "Chance a Zombie Horse spawns with a saddle. Saddles still use their normal drop logic.");
        appendFloat(sb, "skeletonHorseSaddleChance", skeletonHorseSaddleChance, "Chance a Skeleton Horse spawns with a saddle.");
        sb.append('\n');

        appendComment(sb, "Horse armor spawn chance. Range: 0.0 to 1.0.");
        appendFloat(sb, "zombieHorseArmorChance", zombieHorseArmorChance, "Chance a Zombie Horse spawns wearing horse armor. Armor tier still scales by difficulty.");
        sb.append('\n');

        appendComment(sb, "Spawn timing and attempt count.");
        appendInt(sb, "spawnCheckIntervalTicks", spawnCheckIntervalTicks, "Ticks between spawn rounds. 20 ticks = 1 second.");
        appendInt(sb, "spawnAttemptsPerPlayer", spawnAttemptsPerPlayer, "Spawn attempts per player during each spawn round.");
        sb.append('\n');

        appendComment(sb, "Valid spawn distance from the player, in blocks.");
        appendInt(sb, "minSpawnDistance", minSpawnDistance, "Minimum spawn distance from the player.");
        appendInt(sb, "maxSpawnDistance", maxSpawnDistance, "Maximum spawn distance from the player.");
        sb.append('\n');

        appendComment(sb, "General spawn conditions and caps.");
        appendBoolean(sb, "nightOnly", nightOnly, "If true, most horsemen only spawn at night or during thunderstorms. Parched ignores this and can spawn day and night.");
        appendInt(sb, "maxHorsemenPerPlayer", maxHorsemenPerPlayer, "Maximum number of horsemen of each type per online player.");
        sb.append('\n');

        appendComment(sb, "Optional mod compatibility.");
        appendBoolean(sb, "openPartiesAndClaimsSupport", openPartiesAndClaimsSupport, "Respect Open Parties and Claims hostile natural spawn protection when OPAC is installed.", false);

        sb.append("}\n");
        return sb.toString();
    }

    private static void appendComment(StringBuilder sb, String comment) {
        sb.append("  // ").append(comment).append('\n');
    }

    private static void appendBoolean(StringBuilder sb, String key, boolean value, String comment) {
        appendBoolean(sb, key, value, comment, true);
    }

    private static void appendBoolean(StringBuilder sb, String key, boolean value, String comment, boolean comma) {
        appendEntry(sb, key, Boolean.toString(value), comment, comma);
    }

    private static void appendFloat(StringBuilder sb, String key, float value, String comment) {
        appendEntry(sb, key, Float.toString(value), comment, true);
    }

    private static void appendInt(StringBuilder sb, String key, int value, String comment) {
        appendInt(sb, key, value, comment, true);
    }

    private static void appendInt(StringBuilder sb, String key, int value, String comment, boolean comma) {
        appendEntry(sb, key, Integer.toString(value), comment, comma);
    }

    private static void appendEntry(StringBuilder sb, String key, String value, String comment, boolean comma) {
        sb.append("  \"").append(key).append("\": ").append(value);
        if (comma) {
            sb.append(',');
        }
        sb.append(" // ").append(comment).append('\n');
    }

    private void validate() {
        zombieHorsemanSpawnRate    = clamp(zombieHorsemanSpawnRate,    0f, 1f);
        huskHorsemanSpawnRate      = clamp(huskHorsemanSpawnRate,      0f, 1f);
        skeletonHorsemanSpawnRate  = clamp(skeletonHorsemanSpawnRate,  0f, 1f);
        parchedHorsemanSpawnRate   = clamp(parchedHorsemanSpawnRate,   0f, 1f);
        boggedHorsemanSpawnRate    = clamp(boggedHorsemanSpawnRate,    0f, 1f);
        strayHorsemanSpawnRate     = clamp(strayHorsemanSpawnRate,     0f, 1f);
        zombieHorseSaddleChance    = clamp(zombieHorseSaddleChance,    0f, 1f);
        skeletonHorseSaddleChance  = clamp(skeletonHorseSaddleChance,  0f, 1f);
        zombieHorseArmorChance     = clamp(zombieHorseArmorChance,     0f, 1f);
        spawnCheckIntervalTicks    = Math.max(20, spawnCheckIntervalTicks);
        spawnAttemptsPerPlayer     = Math.max(1,  spawnAttemptsPerPlayer);
        minSpawnDistance           = Math.max(8,  minSpawnDistance);
        maxSpawnDistance           = Math.max(minSpawnDistance + 8, maxSpawnDistance);
        maxHorsemenPerPlayer       = Math.max(0,  maxHorsemenPerPlayer);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
