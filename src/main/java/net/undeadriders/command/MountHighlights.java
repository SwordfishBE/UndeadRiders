package net.undeadriders.command;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.undeadriders.util.CommandHighlighted;

/** Temporary command outlines, independent of potion effects. Accessed on the server thread. */
public final class MountHighlights {
    private static final String SAVED_MARKER = "undeadriders.command_highlight";
    private final WeakHashMap<LivingEntity, Mark> marks = new WeakHashMap<>();

    private static final class Mark {
        private final Set<UUID> admins = new HashSet<>();
        private int ticks = 600;
    }

    public void add(LivingEntity mount, UUID admin) {
        if (mount.hasCustomName()) return;
        var mark = marks.get(mount);
        // Leave pre-existing outlines and Glowing effects alone.
        if (mark == null) {
            if (mount.isCurrentlyGlowing()) return;
            mark = new Mark();
            marks.put(mount, mark);
            ((CommandHighlighted) mount).undeadriders$setCommandHighlighted(true);
        }
        mark.admins.add(admin);
        mark.ticks = 600;
    }

    public void remove(UUID admin) {
        var iterator = marks.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.getValue().admins.remove(admin);
            if (entry.getValue().admins.isEmpty()) {
                clearMark(entry.getKey());
                iterator.remove();
            }
        }
    }

    public void remove(LivingEntity mount) {
        if (marks.remove(mount) != null) clearMark(mount);
    }

    /** Clear transient outlines that were captured by an autosave. */
    public static void onLoad(LivingEntity mount) {
        // Migration only: older builds persisted our outline using the vanilla flag.
        if (mount.entityTags().contains(SAVED_MARKER)) {
            mount.setGlowingTag(false);
            mount.removeTag(SAVED_MARKER);
        }
    }

    private static void clearMark(LivingEntity mount) {
        ((CommandHighlighted) mount).undeadriders$setCommandHighlighted(false);
    }

    public void tick() {
        var iterator = marks.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var mount = entry.getKey();
            if (--entry.getValue().ticks <= 0 || mount.hasCustomName() || !mount.isAlive() || mount.isRemoved()) {
                clearMark(mount);
                iterator.remove();
            }
        }
    }

    public void clear() {
        marks.keySet().forEach(MountHighlights::clearMark);
        marks.clear();
    }
}
