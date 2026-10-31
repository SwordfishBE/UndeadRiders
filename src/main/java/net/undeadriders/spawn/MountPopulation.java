package net.undeadriders.spawn;

import java.util.EnumMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Frostbite;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.undeadriders.UndeadRiders;

public final class MountPopulation {
    public enum Type {
        ZOMBIE, HUSK, FROSTBITE, SKELETON, PARCHED, BOGGED, STRAY, PIGLIN, NETHER_SKELETON;

        private final String tag = "undeadriders.mount." + name().toLowerCase(java.util.Locale.ROOT);
        public String tag() { return tag; }
    }

    private static final Type[] TYPES = Type.values();
    private static final Type[] OVERWORLD_SKELETON_TYPES = {Type.SKELETON, Type.PARCHED, Type.BOGGED, Type.STRAY};
    private static final Type[] NETHER_SKELETON_TYPES = {Type.NETHER_SKELETON};

    private MountPopulation() { }

    public static EnumMap<Type, Long> count(Iterable<Entity> entities, boolean nether) {
        EnumMap<Type, Long> counts = new EnumMap<>(Type.class);
        for (Type type : TYPES) counts.put(type, 0L);
        for (Entity mount : entities) {
            var types = typesFor(mount, nether);
            // Remember legacy origins even while named pets are exempt from the cap.
            if (mount.hasCustomName()) continue;
            for (Type type : types) counts.merge(type, 1L, Long::sum);
        }
        return counts;
    }

    /** Cap categories, including conservative legacy matches and named pets. */
    public static java.util.List<Type> typesFor(Entity mount, boolean nether) {
        if (!mount.isAlive() || mount.isRemoved()) return java.util.List.of();
        if (mount instanceof SkeletonHorse horse
            && (horse.isTrap() || horse.entityTags().contains(UndeadRiders.TRAP_HORSE_TAG))) return java.util.List.of();
        if (!(mount instanceof ZombieHorse || mount instanceof SkeletonHorse || mount instanceof Zoglin)) return java.util.List.of();
        Type origin = origin(mount, nether);
        if (origin != null) return java.util.List.of(origin);
        if (mount instanceof ZombieHorse) return java.util.List.of(Type.ZOMBIE, Type.HUSK, Type.FROSTBITE);
        if (mount instanceof SkeletonHorse) return java.util.List.of(nether ? NETHER_SKELETON_TYPES : OVERWORLD_SKELETON_TYPES);
        return java.util.List.of();
    }

    private static Type origin(Entity mount, boolean nether) {
        for (Type type : TYPES) {
            if (mount.entityTags().contains(type.tag())) {
                // A portal transfer still consumes the corresponding skeleton cap in the destination.
                if (mount instanceof SkeletonHorse && nether) return Type.NETHER_SKELETON;
                if (type == Type.NETHER_SKELETON && !nether) return Type.SKELETON;
                return type;
            }
        }
        Entity rider = mount.getFirstPassenger();
        Type inferred = null;
        if (mount instanceof ZombieHorse) {
            if (rider instanceof Frostbite) inferred = Type.FROSTBITE;
            else if (rider instanceof Husk) inferred = Type.HUSK;
            else if (rider instanceof Zombie) inferred = Type.ZOMBIE;
        } else if (mount instanceof SkeletonHorse) {
            if (rider instanceof Bogged) inferred = Type.BOGGED;
            else if (rider instanceof Parched) inferred = Type.PARCHED;
            else if (rider instanceof Stray) inferred = Type.STRAY;
            else if (rider instanceof Skeleton) inferred = nether ? Type.NETHER_SKELETON : Type.SKELETON;
        } else if (mount instanceof Zoglin && mount.entityTags().contains(UndeadRiders.ZOGLIN_RIDER_TAG)) {
            inferred = Type.PIGLIN;
        }
        if (inferred != null) {
            mount.addTag(inferred.tag());
            if (mount instanceof SkeletonHorse && nether) return Type.NETHER_SKELETON;
        }
        return inferred;
    }
}
