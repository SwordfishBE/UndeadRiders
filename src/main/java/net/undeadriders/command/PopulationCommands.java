package net.undeadriders.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.undeadriders.spawn.MountPopulation;
import net.undeadriders.spawn.MountPopulation.Type;
import static net.minecraft.commands.Commands.literal;

public final class PopulationCommands {
    private static final Set<UUID> HIGHLIGHT = new HashSet<>();

    private PopulationCommands() { }

    public static void registerLifecycle() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HIGHLIGHT.remove(handler.player.getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> HIGHLIGHT.clear());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> countCommand() {
        var command = literal("count").executes(ctx -> count(ctx.getSource(), null));
        for (Type type : Type.values()) {
            command.then(literal(type.name().toLowerCase(Locale.ROOT))
                .executes(ctx -> count(ctx.getSource(), type)));
        }
        return command;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> highlightCommand() {
        return literal("highlight")
            .then(literal("on").executes(ctx -> toggle(ctx.getSource(), true)))
            .then(literal("off").executes(ctx -> toggle(ctx.getSource(), false)));
    }

    private static int toggle(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
        UUID player = source.getPlayerOrException().getUUID();
        if (enabled) HIGHLIGHT.add(player);
        else HIGHLIGHT.remove(player);
        source.sendSuccess(() -> Component.literal("[UndeadRiders] Count highlighting "
            + (enabled ? "ON: matching mounts will glow for 30 seconds, visible to nearby players."
                : "OFF. Existing effects expire naturally.")), false);
        return 1;
    }

    private static int count(CommandSourceStack source, Type filter) {
        var level = source.getLevel();
        boolean nether = level.dimension().equals(Level.NETHER);
        boolean highlight = source.getPlayer() != null && HIGHLIGHT.contains(source.getPlayer().getUUID());
        var active = new EnumMap<Type, Integer>(Type.class);
        var pets = new EnumMap<Type, Integer>(Type.class);
        int total = 0;
        int legacy = 0;
        for (var mount : level.getAllEntities()) {
            var types = MountPopulation.typesFor(mount, nether);
            if (types.isEmpty() || (filter != null && !types.contains(filter))) continue;
            total++;
            if (types.size() > 1) legacy++;
            var counts = mount.hasCustomName() ? pets : active;
            for (Type type : types) {
                if (filter == null || filter == type) counts.merge(type, 1, Integer::sum);
            }
            if (highlight && mount instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
            }
        }
        var message = new StringBuilder("[UndeadRiders] Loaded mounts in ")
            .append(level.dimension().identifier()).append(": ").append(total).append(" unique mounts");
        for (Type type : Type.values()) {
            if (filter == null || filter == type) {
                message.append("\n").append(type.name().toLowerCase(Locale.ROOT))
                    .append(": ").append(active.getOrDefault(type, 0)).append(" counting toward cap, ")
                    .append(pets.getOrDefault(type, 0)).append(" named pets (exempt)");
            }
        }
        if (legacy > 0) message.append("\nLegacy mounts with unknown subtype: ").append(legacy)
            .append(". These match multiple compatible types; rows may overlap.");
        if (highlight) message.append("\nMatching mounts highlighted for 30 seconds.");
        source.sendSuccess(() -> Component.literal(message.toString()), false);
        return total;
    }
}
