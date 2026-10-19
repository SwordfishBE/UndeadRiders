package net.undeadriders.command;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;

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
            mount.addTag(SAVED_MARKER);
            mount.setGlowingTag(true);
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
        if (mount.entityTags().contains(SAVED_MARKER)) clearMark(mount);
    }

    private static void clearMark(LivingEntity mount) {
        mount.setGlowingTag(false);
        mount.removeTag(SAVED_MARKER);
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
