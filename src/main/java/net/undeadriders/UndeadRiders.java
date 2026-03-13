package net.undeadriders;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.undeadriders.config.UndeadRidersConfig;
import net.undeadriders.spawn.UndeadHorsemanSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

/**
 * Undead Riders — Fabric Mod
 *
 * Adds naturally spawning Zombie Horsemen, Skeleton Horsemen, and Parched Horsemen
 * to the overworld surface.
 *
 * <ul>
 *   <li>Zombie Horseman  — ZombieHorse with difficulty-scaled armor, ridden by a Zombie.</li>
 *   <li>Skeleton Horseman — wild SkeletonHorse, ridden by a Skeleton.</li>
 *   <li>Parched Horseman  — wild SkeletonHorse, ridden by a Parched. Desert biomes only.</li>
 * </ul>
 *
 * Config: config/undeadriders.json
 * Reload: /undeadriders reload  (requires gamemaster permission)
 */
public class UndeadRiders implements ModInitializer {

    public static final String MOD_ID = "undeadriders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Globally accessible config instance. Reloaded via /undeadriders reload. */
    public static UndeadRidersConfig CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("[UndeadRiders] Initializing...");

        CONFIG = UndeadRidersConfig.load();

        ServerTickEvents.END_WORLD_TICK.register(UndeadHorsemanSpawner::onWorldTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                literal("undeadriders")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(literal("reload")
                        .executes(ctx -> {
                            CONFIG = UndeadRidersConfig.load();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("[UndeadRiders] Config reloaded successfully."),
                                true
                            );
                            LOGGER.info("[UndeadRiders] Config reloaded via command.");
                            return 1;
                        })
                    )
            )
        );

        LOGGER.info("[UndeadRiders] Ready!");
    }
}
