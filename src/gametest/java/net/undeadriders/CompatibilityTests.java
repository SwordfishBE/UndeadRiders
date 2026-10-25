package net.undeadriders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.undeadriders.config.UndeadRidersConfig;
import net.undeadriders.spawn.MountPopulation;
import net.undeadriders.spawn.MountPopulation.Type;
import net.undeadriders.spawn.UndeadHorsemanSpawner;

/** Dedicated-server probes. No production entities are forced onto the main thread. */
public class CompatibilityTests {
    private static final Set<UUID> WATCHED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> REMOVE = ConcurrentHashMap.newKeySet();
    private static final Set<String> THREADS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger WORKER_REMOVALS = new AtomicInteger();
    private static final AtomicInteger TICKS = new AtomicInteger();
    private static final AtomicInteger OFF_THREAD_CLEANUP = new AtomicInteger();

    public static void onHighlightRemoval(net.minecraft.world.entity.LivingEntity entity) {
        if (WATCHED.contains(entity.getUUID()) && !entity.level().getServer().isSameThread()) {
            OFF_THREAD_CLEANUP.incrementAndGet();
        }
    }

    public static boolean onTick(Mob mob) {
        if (!WATCHED.contains(mob.getUUID())) return false;
        TICKS.incrementAndGet();
        if (!mob.level().getServer().isSameThread()) {
            THREADS.add(Thread.currentThread().getName());
            if (REMOVE.remove(mob.getUUID())) {
                mob.discard();
                WORKER_REMOVALS.incrementAndGet();
                return true;
            }
        }
        return false;
    }

    private static Method method(String name, Class<?>... parameters) throws Exception {
        var method = UndeadHorsemanSpawner.class.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return method;
    }

    private static void command(GameTestHelper h, net.minecraft.commands.CommandSourceStack source, String text) {
        try { h.getLevel().getServer().getCommands().getDispatcher().execute(text, source); }
        catch (Exception e) { throw new RuntimeException(text, e); }
    }

    @GameTest(structure = "undeadriders-test:arena", maxTicks = 1400)
    public void compatibility(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        var original = UndeadRiders.CONFIG;
        var cfg = new UndeadRidersConfig();
        cfg.zombieHorsemanEnabled = cfg.huskHorsemanEnabled = cfg.skeletonHorsemanEnabled = false;
        cfg.parchedHorsemanEnabled = cfg.boggedHorsemanEnabled = cfg.strayHorsemanEnabled = false;
        cfg.zombifiedPiglinRiderEnabled = cfg.netherSkeletonHorsemanEnabled = false;
        cfg.skeletonHorseSunlightBurnEnabled = true;
        cfg.saddledSkeletonHorsesAvoidSunlightBurn = true;
        cfg.skeletonHorseSaddleChance = 0;
        UndeadRiders.CONFIG = cfg;
        WATCHED.clear(); REMOVE.clear(); THREADS.clear(); WORKER_REMOVALS.set(0); TICKS.set(0); OFF_THREAD_CLEANUP.set(0);
        var entities = new ArrayList<Entity>();
        var player = h.makeMockServerPlayerInLevel();
        var source = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
        h.runBeforeTestEnd(() -> {
            command(h, source, "undeadriders highlight off");
            WATCHED.clear(); REMOVE.clear();
            entities.forEach(Entity::discard);
            level.getServer().getPlayerList().remove(player);
            UndeadRiders.CONFIG = original;
        });
        h.setTime(6000);
        for (int x = 0; x < 48; x++) for (int z = 0; z < 48; z++) h.setBlock(x, 0, z, Blocks.STONE);
        // Exercise actual production factories for all eight combinations.
        String[] factories = {"spawnZombieHorseman", "spawnHuskHorseman", "spawnSkeletonHorseman",
            "spawnParchedHorseman", "spawnBoggedHorseman", "spawnStrayHorseman",
            "spawnZombifiedPiglinRider", "spawnNetherSkeletonHorseman"};
        var mounts = new ArrayList<Mob>();
        for (int i = 0; i < factories.length; i++) {
            BlockPos pos = h.absolutePos(new BlockPos(4 + (i % 4) * 9, 1, 4 + (i / 4) * 9));
            method(factories[i], ServerLevel.class, BlockPos.class).invoke(null, level, pos);
            Mob mount = null;
            for (Entity e : level.getAllEntities()) {
                if (e instanceof Mob mob && e.getVehicle() == null && e.getFirstPassenger() != null
                    && e.position().distanceToSqr(Vec3.atBottomCenterOf(pos)) < 1) mount = mob;
            }
            h.assertTrue(mount != null, "Production factory created mounted pair: " + factories[i]);
            mounts.add(mount);
            entities.add(mount);
            entities.add(mount.getFirstPassenger());
            WATCHED.add(mount.getUUID());
            mount.setNoAi(true);
            mount.setPermanentlyInvulnerable(true);
            mount.setPersistenceRequired();
            ((Mob) mount.getFirstPassenger()).setNoAi(true);
            ((Mob) mount.getFirstPassenger()).setPermanentlyInvulnerable(true);
            ((Mob) mount.getFirstPassenger()).setPersistenceRequired();
        }
        SkeletonHorse parched = (SkeletonHorse) mounts.get(3);
        Parched parchedRider = (Parched) parched.getFirstPassenger();
        h.assertTrue(parched.getItemBySlot(EquipmentSlot.SADDLE).isEmpty(), "Parched uses configured zero saddle chance");
        parched.removeTag(Type.PARCHED.tag()); // Legacy combination, with spawning disabled.
        var piglin = (ZombifiedPiglin) mounts.get(6).getFirstPassenger();
        piglin.setPermanentlyInvulnerable(false);
        float health = piglin.getHealth();
        h.assertTrue(!piglin.hurtServer(level, piglin.damageSources().inWall(), 5), "Mounted tagged piglin ignores suffocation");
        h.assertTrue(piglin.getHealth() == health, "Suffocation did not damage piglin");
        piglin.setPermanentlyInvulnerable(true);
        BlockPos floor = h.absolutePos(new BlockPos(40, 1, 40));
        h.setBlock(40, 0, 40, Blocks.BEDROCK);
        h.assertTrue(!(boolean) method("isValidSpawnSpace", ServerLevel.class, BlockPos.class, int.class)
            .invoke(null, level, floor, 4), "Bedrock floor is rejected");
        h.setBlock(40, 0, 40, Blocks.STONE);
        h.assertTrue((boolean) method("isValidSpawnSpace", ServerLevel.class, BlockPos.class, int.class)
            .invoke(null, level, floor, 4), "Clear stone floor is accepted");
        h.setBlock(40, 3, 40, Blocks.STONE);
        h.assertTrue(!(boolean) method("isValidSpawnSpace", ServerLevel.class, BlockPos.class, int.class)
            .invoke(null, level, floor, 4), "Low ceiling is rejected");
        // Add enough entities that Async cannot silently choose its sequential small-batch path.
        for (int i = 0; i < 192; i++) {
            var horse = h.spawn(EntityTypes.SKELETON_HORSE, new Vec3(2 + i % 24 * 1.8, 18, 2 + i / 24 * 1.8));
            horse.setNoAi(true); horse.setNoGravity(true); horse.noPhysics = true;
            horse.setPersistenceRequired();
            horse.setPermanentlyInvulnerable(true);
            horse.addTag(Type.SKELETON.tag());
            WATCHED.add(horse.getUUID());
            entities.add(horse);
        }
        var pet = (SkeletonHorse) entities.getLast();
        pet.setCustomName(Component.literal("Compatibility pet"));
        command(h, source, "undeadriders count");
        h.assertTrue(!pet.isCurrentlyGlowing(), "Highlight defaults to off");
        command(h, source, "undeadriders highlight on");
        command(h, source, "undeadriders count skeleton");
        h.assertTrue(!pet.isCurrentlyGlowing(), "Named pets are not highlighted");
        h.assertTrue(mounts.get(2).isCurrentlyGlowing(), "Count highlights matching skeleton mount");
        mounts.get(2).setGlowingTag(true);
        command(h, source, "undeadriders highlight off");
        h.assertTrue(mounts.get(2).isCurrentlyGlowing(), "Turning off preserves external glow");
        mounts.get(2).setGlowingTag(false);
        h.assertTrue(!mounts.get(2).isCurrentlyGlowing(), "Own glow removed immediately");
        command(h, source, "undeadriders highlight on");
        command(h, source, "undeadriders count skeleton");

        h.runAfterDelay(100, () -> {
            h.assertTrue(parched.entityTags().contains(Type.PARCHED.tag()), "Legacy Parched origin migrated during entity tick");
            h.assertTrue(!parched.isOnFire(), "Parched mount survives daylight without saddle");
            h.assertTrue(TICKS.get() > 200, "Actual entity ticks observed");
            boolean async = FabricLoader.getInstance().isModLoaded("async");
            if (async) {
                h.assertTrue(!THREADS.isEmpty(), "Undead mounts actually ticked on Async workers");
                UndeadRiders.LOGGER.info("Compatibility worker threads: {}", THREADS);
                // Trigger real off-thread unload events for marked mounts.
                for (Entity entity : entities.subList(16, 80)) REMOVE.add(entity.getUUID());
            }
            parchedRider.stopRiding();
            parchedRider.discard();
            ((SkeletonHorse) mounts.get(2)).setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        });
        h.runAfterDelay(200, () -> {
            h.assertTrue(OFF_THREAD_CLEANUP.get() == 0, "Highlight map cleanup stays on the main server thread");
            h.assertTrue(parched.isOnFire(), "Sunlight ignition resumes after Parched dismount");
            h.assertTrue(!mounts.get(2).isOnFire(), "Saddle removes sunlight fire");
            if (FabricLoader.getInstance().isModLoaded("async")) {
                h.assertTrue(WORKER_REMOVALS.get() > 0, "Marked mounts removed during actual Async worker ticks");
            }
            command(h, source, "undeadriders highlight off");
            var counts = MountPopulation.count(mounts.stream().map(m -> (Entity) m).toList(), false);
            h.assertTrue(counts.get(Type.PARCHED) == 1, "Riderless Parched mount still counts toward its cap");
            parched.setCustomName(Component.literal("Saved pet"));
            h.assertTrue(MountPopulation.count(List.of(parched), false).get(Type.PARCHED) == 0, "Named pet is exempt from cap");
            parched.setCustomName(null);
            command(h, source, "undeadriders highlight on");
            command(h, source, "undeadriders count parched");
        });
        h.runAfterDelay(805, () -> {
            h.assertTrue(!parched.isCurrentlyGlowing(), "Highlight expires after 600 actual server ticks");
            UndeadRiders.LOGGER.info("COMPATIBILITY PASS: moonrise={}, async={}, observedTicks={}, workerRemovals={}, offThreadCleanup={}",
                FabricLoader.getInstance().isModLoaded("moonrise"), FabricLoader.getInstance().isModLoaded("async"),
                TICKS.get(), WORKER_REMOVALS.get(), OFF_THREAD_CLEANUP.get());
            h.succeed();
        });
    }
}
