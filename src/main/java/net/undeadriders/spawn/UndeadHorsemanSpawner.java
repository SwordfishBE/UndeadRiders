package net.undeadriders.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.undeadriders.UndeadRiders;
import net.undeadriders.config.UndeadRidersConfig;

import java.util.List;
import java.util.Random;

/**
 * Handles custom spawn logic for all Undead Rider types.
 *
 * <h3>Types and biomes</h3>
 * <ul>
 *   <li><b>Zombie Horseman</b>   — ZombieHorse + Zombie. All overworld biomes, night only.</li>
 *   <li><b>Skeleton Horseman</b> — SkeletonHorse + Skeleton. All overworld biomes, night only.</li>
 *   <li><b>Parched Horseman</b>  — SkeletonHorse + Parched. Desert only. Day &amp; night.</li>
 *   <li><b>Bogged Horseman</b>   — SkeletonHorse + Bogged. Swamp &amp; Mangrove Swamp only. Night only.</li>
 *   <li><b>Stray Horseman</b>    — SkeletonHorse + Stray. Ice Spikes, Snowy Plains, Jagged/Frozen Peaks, Snowy Slopes. Night only.</li>
 * </ul>
 *
 * <h3>Mounting safety</h3>
 * {@code startRiding()} return value is checked — if mounting fails both entities
 * are discarded immediately so no riderless horse is left in the world.
 *
 * <h3>ZombieHorse finalizeSpawn</h3>
 * {@code finalizeSpawn} is intentionally NOT called on horses. Vanilla's own
 * {@code ZombieHorse.finalizeSpawn} injects a zombie rider automatically; calling
 * it would cause a duplicate rider on every server reload.
 */
public class UndeadHorsemanSpawner {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // Tick entry point
    // ─────────────────────────────────────────────────────────────────────────

    public static void onWorldTick(ServerLevel world) {
        if (!world.dimension().equals(Level.OVERWORLD)) return;
        if (world.getDifficulty() == Difficulty.PEACEFUL) return;

        tickCounter++;
        UndeadRidersConfig cfg = UndeadRiders.CONFIG;

        boolean anyEnabled = cfg.zombieHorsemanEnabled || cfg.skeletonHorsemanEnabled
            || cfg.parchedHorsemanEnabled || cfg.boggedHorsemanEnabled || cfg.strayHorsemanEnabled;
        if (!anyEnabled) return;
        if (tickCounter % cfg.spawnCheckIntervalTicks != 0) return;

        List<ServerPlayer> players = world.players();
        if (players.isEmpty()) return;

        long existingZombie   = cfg.zombieHorsemanEnabled   ? countHorsemen(world, EntityType.ZOMBIE_HORSE)   : 0;
        long existingSkeleton = cfg.skeletonHorsemanEnabled ? countHorsemen(world, EntityType.SKELETON_HORSE) : 0;
        long existingParched  = cfg.parchedHorsemanEnabled  ? countEntities(world, EntityType.PARCHED)        : 0;
        long existingBogged   = cfg.boggedHorsemanEnabled   ? countEntities(world, EntityType.BOGGED)         : 0;
        long existingStray    = cfg.strayHorsemanEnabled    ? countEntities(world, EntityType.STRAY)          : 0;

        long cap = (long) players.size() * cfg.maxHorsemenPerPlayer;

        for (ServerPlayer player : players) {
            for (int attempt = 0; attempt < cfg.spawnAttemptsPerPlayer; attempt++) {

                if (cfg.zombieHorsemanEnabled && existingZombie < cap
                        && RANDOM.nextFloat() < cfg.zombieHorsemanSpawnRate) {
                    BlockPos pos = findSurfacePos(world, player, cfg, BiomeFilter.ANY);
                    if (pos != null) { spawnZombieHorseman(world, pos); existingZombie++; }
                }

                if (cfg.skeletonHorsemanEnabled && existingSkeleton < cap
                        && RANDOM.nextFloat() < cfg.skeletonHorsemanSpawnRate) {
                    BlockPos pos = findSurfacePos(world, player, cfg, BiomeFilter.SKELETON);
                    if (pos != null) { spawnSkeletonHorseman(world, pos); existingSkeleton++; }
                }

                if (cfg.parchedHorsemanEnabled && existingParched < cap
                        && RANDOM.nextFloat() < cfg.parchedHorsemanSpawnRate) {
                    BlockPos pos = findSurfacePos(world, player, cfg, BiomeFilter.DESERT);
                    if (pos != null) { spawnParchedHorseman(world, pos); existingParched++; }
                }

                if (cfg.boggedHorsemanEnabled && existingBogged < cap
                        && RANDOM.nextFloat() < cfg.boggedHorsemanSpawnRate) {
                    BlockPos pos = findSurfacePos(world, player, cfg, BiomeFilter.SWAMP);
                    if (pos != null) { spawnBoggedHorseman(world, pos); existingBogged++; }
                }

                if (cfg.strayHorsemanEnabled && existingStray < cap
                        && RANDOM.nextFloat() < cfg.strayHorsemanSpawnRate) {
                    BlockPos pos = findSurfacePos(world, player, cfg, BiomeFilter.FROZEN);
                    if (pos != null) { spawnStrayHorseman(world, pos); existingStray++; }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Biome filter enum
    // ─────────────────────────────────────────────────────────────────────────

    private enum BiomeFilter { ANY, SKELETON, DESERT, SWAMP, FROZEN }

    private static boolean matchesBiome(ServerLevel world, BlockPos pos, BiomeFilter filter) {
        if (filter == BiomeFilter.ANY) return true;
        Holder<Biome> biome = world.getBiome(pos);
        return switch (filter) {
            case SKELETON -> !biome.is(Biomes.DESERT)
                         && !biome.is(Biomes.SWAMP)
                         && !biome.is(Biomes.MANGROVE_SWAMP)
                         && !biome.is(Biomes.ICE_SPIKES)
                         && !biome.is(Biomes.SNOWY_PLAINS)
                         && !biome.is(Biomes.JAGGED_PEAKS)
                         && !biome.is(Biomes.FROZEN_PEAKS)
                         && !biome.is(Biomes.SNOWY_SLOPES);
            case DESERT -> biome.is(Biomes.DESERT);
            case SWAMP  -> biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
            case FROZEN -> biome.is(Biomes.ICE_SPIKES)
                        || biome.is(Biomes.SNOWY_PLAINS)
                        || biome.is(Biomes.JAGGED_PEAKS)
                        || biome.is(Biomes.FROZEN_PEAKS)
                        || biome.is(Biomes.SNOWY_SLOPES);
            default -> true;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spawn position finding
    // ─────────────────────────────────────────────────────────────────────────

    private static BlockPos findSurfacePos(ServerLevel world, ServerPlayer player,
                                            UndeadRidersConfig cfg, BiomeFilter filter) {
        int minDist = cfg.minSpawnDistance;
        int range   = cfg.maxSpawnDistance - minDist;

        // Parched can spawn day or night; all others respect nightOnly
        boolean skipNightCheck = (filter == BiomeFilter.DESERT);

        // Biome-restricted types get more attempts since their target biomes
        // may be rare near the player
        int maxAttempts = (filter == BiomeFilter.ANY || filter == BiomeFilter.SKELETON) ? 12 : 24;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int offsetX = (RANDOM.nextInt(range) + minDist) * (RANDOM.nextBoolean() ? 1 : -1);
            int offsetZ = (RANDOM.nextInt(range) + minDist) * (RANDOM.nextBoolean() ? 1 : -1);

            int x = (int) player.getX() + offsetX;
            int z = (int) player.getZ() + offsetZ;
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);

            if (!matchesBiome(world, candidate, filter)) continue;
            if (!world.canSeeSky(candidate)) continue;
            if (!skipNightCheck && cfg.nightOnly && !isNightOrThunder(world)) continue;
            if (world.getMaxLocalRawBrightness(candidate) > 7) continue;
            if (!world.getBlockState(candidate.below()).isSolid()) continue;
            if (!world.getBlockState(candidate).isAir()) continue;
            if (!world.getBlockState(candidate.above()).isAir()) continue;
            if (!world.getBlockState(candidate.above(2)).isAir()) continue;
            if (!world.getFluidState(candidate).isEmpty()) continue;
            if (!world.getFluidState(candidate.below()).isEmpty()) continue;

            return candidate;
        }
        return null;
    }

    private static boolean isNightOrThunder(ServerLevel world) {
        return world.getDayTime() % 24000L >= 12000L || world.isThundering();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spawn methods
    // ─────────────────────────────────────────────────────────────────────────

    private static void spawnZombieHorseman(ServerLevel world, BlockPos pos) {
        DifficultyInstance localDiff = world.getCurrentDifficultyAt(pos);

        ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(world, EntitySpawnReason.NATURAL);
        if (horse == null) return;
        placeHorse(horse, pos);
        horse.setTamed(false);
        applyZombieHorseEquipment(horse, world.getDifficulty());

        Zombie zombie = EntityType.ZOMBIE.create(world, EntitySpawnReason.NATURAL);
        if (zombie == null) { horse.discard(); return; }
        // Do NOT call finalizeSpawn on the zombie rider — vanilla's finalizeSpawn
        // has a chance to convert it into a ZombieVillager. We handle all
        // equipment ourselves so finalizeSpawn is not needed here.
        zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        applyZombieWeapon(zombie, world.getDifficulty());

        world.addFreshEntity(horse);
        world.addFreshEntity(zombie);
        if (!zombie.startRiding(horse, true, true)) {
            horse.discard();
            zombie.discard();
            return;
        }
        UndeadRiders.LOGGER.debug("[UndeadRiders] Spawned Zombie Horseman at {}", pos);
    }

    private static void spawnSkeletonHorseman(ServerLevel world, BlockPos pos) {
        DifficultyInstance localDiff = world.getCurrentDifficultyAt(pos);

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(world, EntitySpawnReason.NATURAL);
        if (horse == null) return;
        placeHorse(horse, pos);
        horse.setTamed(true);

        Skeleton skeleton = EntityType.SKELETON.create(world, EntitySpawnReason.NATURAL);
        if (skeleton == null) { horse.discard(); return; }
        placeRider(skeleton, world, pos, localDiff);

        world.addFreshEntity(horse);
        world.addFreshEntity(skeleton);
        if (!skeleton.startRiding(horse, true, true)) {
            horse.discard();
            skeleton.discard();
            return;
        }
        UndeadRiders.LOGGER.debug("[UndeadRiders] Spawned Skeleton Horseman at {}", pos);
    }

    private static void spawnParchedHorseman(ServerLevel world, BlockPos pos) {
        DifficultyInstance localDiff = world.getCurrentDifficultyAt(pos);

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(world, EntitySpawnReason.NATURAL);
        if (horse == null) return;
        placeHorse(horse, pos);
        horse.setTamed(true);

        Parched parched = EntityType.PARCHED.create(world, EntitySpawnReason.NATURAL);
        if (parched == null) { horse.discard(); return; }
        placeRider(parched, world, pos, localDiff);

        world.addFreshEntity(horse);
        world.addFreshEntity(parched);
        if (!parched.startRiding(horse, true, true)) {
            horse.discard();
            parched.discard();
            return;
        }
        UndeadRiders.LOGGER.debug("[UndeadRiders] Spawned Parched Horseman at {}", pos);
    }

    private static void spawnBoggedHorseman(ServerLevel world, BlockPos pos) {
        DifficultyInstance localDiff = world.getCurrentDifficultyAt(pos);

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(world, EntitySpawnReason.NATURAL);
        if (horse == null) return;
        placeHorse(horse, pos);
        horse.setTamed(true);

        Bogged bogged = EntityType.BOGGED.create(world, EntitySpawnReason.NATURAL);
        if (bogged == null) { horse.discard(); return; }
        placeRider(bogged, world, pos, localDiff);

        world.addFreshEntity(horse);
        world.addFreshEntity(bogged);
        if (!bogged.startRiding(horse, true, true)) {
            horse.discard();
            bogged.discard();
            return;
        }
        UndeadRiders.LOGGER.debug("[UndeadRiders] Spawned Bogged Horseman at {}", pos);
    }

    private static void spawnStrayHorseman(ServerLevel world, BlockPos pos) {
        DifficultyInstance localDiff = world.getCurrentDifficultyAt(pos);

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(world, EntitySpawnReason.NATURAL);
        if (horse == null) return;
        placeHorse(horse, pos);
        horse.setTamed(true);

        Stray stray = EntityType.STRAY.create(world, EntitySpawnReason.NATURAL);
        if (stray == null) { horse.discard(); return; }
        placeRider(stray, world, pos, localDiff);

        world.addFreshEntity(horse);
        world.addFreshEntity(stray);
        if (!stray.startRiding(horse, true, true)) {
            horse.discard();
            stray.discard();
            return;
        }
        UndeadRiders.LOGGER.debug("[UndeadRiders] Spawned Stray Horseman at {}", pos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Positions a horse without calling finalizeSpawn.
     * Skipping finalizeSpawn on ZombieHorse is intentional: vanilla's own
     * finalizeSpawn injects a zombie rider automatically. If we called it,
     * a second zombie would appear on every server reload (NBT restores the
     * first one, vanilla adds another).
     */
    private static <T extends net.minecraft.world.entity.Mob> void placeHorse(
            T horse, BlockPos pos) {
        horse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        horse.setYRot(RANDOM.nextFloat() * 360f);
    }

    /** Positions a rider and lets vanilla finalizeSpawn handle equipment/enchants. */
    private static <T extends net.minecraft.world.entity.Mob> void placeRider(
            T rider, ServerLevel world, BlockPos pos, DifficultyInstance localDiff) {
        rider.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rider.finalizeSpawn(world, localDiff, EntitySpawnReason.NATURAL, null);
    }

    private static void applyZombieHorseEquipment(ZombieHorse horse, Difficulty difficulty) {
        int roll = RANDOM.nextInt(3);
        ItemStack armor = switch (difficulty) {
            case EASY   -> switch (roll) {
                case 0  -> new ItemStack(Items.LEATHER_HORSE_ARMOR);
                case 1  -> new ItemStack(Items.COPPER_HORSE_ARMOR);
                default -> new ItemStack(Items.IRON_HORSE_ARMOR);
            };
            case NORMAL -> switch (roll) {
                case 0  -> new ItemStack(Items.COPPER_HORSE_ARMOR);
                case 1  -> new ItemStack(Items.IRON_HORSE_ARMOR);
                default -> new ItemStack(Items.GOLDEN_HORSE_ARMOR);
            };
            case HARD   -> switch (roll) {
                case 0  -> new ItemStack(Items.IRON_HORSE_ARMOR);
                case 1  -> new ItemStack(Items.GOLDEN_HORSE_ARMOR);
                default -> new ItemStack(Items.DIAMOND_HORSE_ARMOR);
            };
            default -> ItemStack.EMPTY;
        };
        if (!armor.isEmpty()) {
            horse.setItemSlot(EquipmentSlot.BODY, armor);
            horse.setDropChance(EquipmentSlot.BODY, 0.0f);
        }
    }

    private static void applyZombieWeapon(Zombie zombie, Difficulty difficulty) {
        int roll = RANDOM.nextInt(3);
        ItemStack weapon = switch (difficulty) {
            case EASY   -> switch (roll) {
                case 0  -> new ItemStack(Items.STONE_SWORD);
                case 1  -> new ItemStack(Items.WOODEN_SPEAR);
                default -> new ItemStack(Items.WOODEN_AXE);
            };
            case NORMAL -> switch (roll) {
                case 0  -> new ItemStack(Items.IRON_SWORD);
                case 1  -> new ItemStack(Items.STONE_SPEAR);
                default -> new ItemStack(Items.IRON_AXE);
            };
            case HARD   -> switch (roll) {
                case 0  -> new ItemStack(Items.DIAMOND_SWORD);
                case 1  -> new ItemStack(Items.IRON_SPEAR);
                default -> new ItemStack(Items.DIAMOND_SPEAR);
            };
            default -> ItemStack.EMPTY;
        };
        if (!weapon.isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            zombie.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
        }
    }

    @SuppressWarnings("unchecked")
    private static long countHorsemen(ServerLevel world, EntityType<?> type) {
        return world.getEntities((EntityType<? extends Entity>) type, e -> true).size();
    }

    @SuppressWarnings("unchecked")
    private static long countEntities(ServerLevel world, EntityType<?> type) {
        return world.getEntities((EntityType<? extends Entity>) type, e -> true).size();
    }
}
