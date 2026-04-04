package net.undeadriders;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.undeadriders.config.UndeadRidersConfig;
import net.undeadriders.spawn.UndeadHorsemanSpawner;
import net.undeadriders.util.ModrinthUpdateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static net.minecraft.commands.Commands.literal;

/**
 * Undead Riders — Fabric Mod
 *
 * Adds naturally spawning undead horsemen to the overworld surface.
 *
 * <ul>
 *   <li>Zombie Horseman  — ZombieHorse + Zombie. All biomes except Desert.</li>
 *   <li>Husk Horseman    — ZombieHorse + Husk.   Desert only.</li>
 *   <li>Skeleton Horseman— SkeletonHorse + Skeleton. All biomes except Desert/Swamp/Frozen.</li>
 *   <li>Parched Horseman — SkeletonHorse + Parched. Desert only.</li>
 *   <li>Bogged Horseman  — SkeletonHorse + Bogged.  Swamp biomes only.</li>
 *   <li>Stray Horseman   — SkeletonHorse + Stray.   Frozen biomes only.</li>
 * </ul>
 *
 * Config: config/undeadriders.json
 * Commands: /undeadriders reload | info  (requires gamemaster permission)
 */
public class UndeadRiders implements ModInitializer {

    public static final String MOD_ID = "undeadriders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** Globally accessible config instance. Reloaded via /undeadriders reload. */
    public static UndeadRidersConfig CONFIG;

    public static UndeadRidersConfig loadConfigForEditing() {
        return UndeadRidersConfig.load();
    }

    public static void applyEditedConfig(UndeadRidersConfig config) {
        config.validateForEditing();
        config.save();
        CONFIG = config;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[UndeadRiders] Initializing...");

        CONFIG = UndeadRidersConfig.load();

        // Spawn logic
        ServerTickEvents.END_LEVEL_TICK.register(UndeadHorsemanSpawner::onWorldTick);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ModrinthUpdateChecker.checkOnceAsync());

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                literal("undeadriders")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(literal("reload")
                        .executes(ctx -> {
                            CONFIG = UndeadRidersConfig.load();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("[UndeadRiders] Config reloaded."),
                                true
                            );
                            LOGGER.info("[UndeadRiders] Config reloaded via command.");
                            return 1;
                        })
                    )
                    .then(literal("info")
                        .executes(ctx -> {
                            var src = ctx.getSource();
                            var cfg = CONFIG;
                            var level = src.getLevel();
                            long players = level.players().size();
                            long cap = players * cfg.maxHorsemenPerPlayer;

                            src.sendSuccess(() -> Component.literal(
                                "§6=== Undead Riders Info ===\n" +
                                "§eZombie Horseman  §r" + status(cfg.zombieHorsemanEnabled)   + " §7rate=" + cfg.zombieHorsemanSpawnRate   + "\n" +
                                "§eHusk Horseman    §r" + status(cfg.huskHorsemanEnabled)     + " §7rate=" + cfg.huskHorsemanSpawnRate     + "\n" +
                                "§eSkeleton Horseman§r" + status(cfg.skeletonHorsemanEnabled) + " §7rate=" + cfg.skeletonHorsemanSpawnRate + "\n" +
                                "§eParched Horseman §r" + status(cfg.parchedHorsemanEnabled)  + " §7rate=" + cfg.parchedHorsemanSpawnRate  + "\n" +
                                "§eBogged Horseman  §r" + status(cfg.boggedHorsemanEnabled)   + " §7rate=" + cfg.boggedHorsemanSpawnRate   + "\n" +
                                "§eStray Horseman   §r" + status(cfg.strayHorsemanEnabled)    + " §7rate=" + cfg.strayHorsemanSpawnRate    + "\n" +
                                "§7Attempts/player: §f" + cfg.spawnAttemptsPerPlayer +
                                " §7| Interval: §f" + cfg.spawnCheckIntervalTicks + " ticks\n" +
                                "§7Cap: §f" + cap + " §7per type (" + players + " player(s) × " + cfg.maxHorsemenPerPlayer + ")"
                            ), false);
                            return 1;
                        })
                    )
            )
        );

        LOGGER.info("[UndeadRiders] Ready!");
    }

    private static String status(boolean enabled) {
        return enabled ? "§a[ON] " : "§c[OFF]";
    }
}
