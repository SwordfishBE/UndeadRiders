package net.undeadriders;

import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.network.chat.Component;
import net.undeadriders.config.UndeadRidersConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.level.Level;
import net.undeadriders.spawn.MountPopulation;
import net.undeadriders.spawn.MountPopulation.Type;
import net.undeadriders.util.SunlightTracked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MountLifecycleTest {
    private static ClassLoader originalContextLoader;
    @BeforeAll
    static void bootstrap() {
        originalContextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MountLifecycleTest.class.getClassLoader());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterAll
    static void restoreContextLoader() {
        Thread.currentThread().setContextClassLoader(originalContextLoader);
    }

    private Level level() { return mock(Level.class, RETURNS_DEEP_STUBS); }

    private UndeadRidersConfig previousConfig;

    @BeforeEach
    void configure() {
        previousConfig = UndeadRiders.CONFIG;
        UndeadRiders.CONFIG = new UndeadRidersConfig();
        UndeadRiders.CONFIG.skeletonHorseSunlightBurnEnabled = true;
        UndeadRiders.CONFIG.saddledSkeletonHorsesAvoidSunlightBurn = true;
    }

    @AfterEach
    void restoreConfig() { UndeadRiders.CONFIG = previousConfig; }

    private void sunlight(SkeletonHorse horse) {
        var fire = ((SunlightTracked) horse).undeadriders$fireState();
        fire.beginSunlight(horse.getRemainingFireTicks());
        horse.igniteForTicks(160);
        fire.endSunlight();
    }

    private void protectionTick(SkeletonHorse horse) throws Exception {
        var handler = java.util.Arrays.stream(SkeletonHorse.class.getDeclaredMethods())
            .filter(method -> method.getName().contains("undeadriders$burnInSunlight"))
            .findFirst().orElseThrow();
        handler.setAccessible(true);
        handler.invoke(horse, new CallbackInfo("aiStep", false));
    }

    @Test
    void parchedRiderProtectsOnlyModMountWhileAliveAndMounted() throws Exception {
        var level = level();
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        var rider = new net.minecraft.world.entity.monster.skeleton.Parched(EntityTypes.PARCHED, level);
        UndeadRiders.CONFIG.saddledSkeletonHorsesAvoidSunlightBurn = false;
        assertTrue(rider.startRiding(horse, true, true));
        var predicate = java.util.Arrays.stream(SkeletonHorse.class.getDeclaredMethods())
            .filter(method -> method.getName().contains("undeadriders$hasParchedRiderProtection"))
            .findFirst().orElseThrow();
        predicate.setAccessible(true);
        assertEquals(true, predicate.invoke(horse));
        assertTrue(horse.entityTags().contains(Type.PARCHED.tag()));
        sunlight(horse);
        protectionTick(horse);
        assertEquals(0, horse.getRemainingFireTicks());
        assertTrue(horse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty());
        rider.setHealth(0);
        assertEquals(false, predicate.invoke(horse));
        rider.setHealth(20);
        assertEquals(true, predicate.invoke(horse));
        rider.stopRiding();
        assertEquals(false, predicate.invoke(horse));
        horse.removeTag(Type.PARCHED.tag());
        horse.addTag(Type.STRAY.tag());
        assertTrue(rider.startRiding(horse, true, true));
        assertEquals(false, predicate.invoke(horse));
    }

    @Test
    void parchedProtectionPreservesExternalFire() throws Exception {
        var level = level();
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        horse.addTag(Type.PARCHED.tag());
        var rider = new net.minecraft.world.entity.monster.skeleton.Parched(EntityTypes.PARCHED, level);
        assertTrue(rider.startRiding(horse, true, true));
        sunlight(horse);
        horse.igniteForTicks(40);
        protectionTick(horse);
        assertEquals(40, horse.getRemainingFireTicks());
        assertFalse(((SunlightTracked) horse).undeadriders$fireState().active());
        horse.igniteForTicks(300);
        protectionTick(horse);
        assertEquals(300, horse.getRemainingFireTicks());
    }

    @Test
    void saddleInShadeExtinguishesOnlySunlightFire() throws Exception {
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level());
        sunlight(horse);
        horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Holder.direct(Items.SADDLE, DataComponentMap.EMPTY)));
        protectionTick(horse);
        assertEquals(0, horse.getRemainingFireTicks());
        assertFalse(((SunlightTracked) horse).undeadriders$fireState().active());
    }

    @Test
    void saddleKeepsOverlappingExternalFireButRemovesSunlightExtension() throws Exception {
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level());
        sunlight(horse);
        horse.igniteForTicks(40);
        horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Holder.direct(Items.SADDLE, DataComponentMap.EMPTY)));
        protectionTick(horse);
        assertEquals(40, horse.getRemainingFireTicks());
        assertFalse(((SunlightTracked) horse).undeadriders$fireState().active());
    }

    @Test
    void protectedHorseStillBurnsFromExternalFire() throws Exception {
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level());
        horse.setTrap(true);
        horse.setTrap(false);
        horse.igniteForTicks(300);
        protectionTick(horse);
        assertEquals(300, horse.getRemainingFireTicks());
    }

    @Test
    void namedSkeletonIsNotMistakenForATrap() throws Exception {
        var level = level();
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        var rider = new Skeleton(EntityTypes.SKELETON, level);
        rider.setCustomName(Component.literal("Named rider"));
        rider.setPersistenceRequired();
        assertTrue(rider.startRiding(horse, true, true));
        var predicate = java.util.Arrays.stream(SkeletonHorse.class.getDeclaredMethods())
            .filter(method -> method.getName().contains("undeadriders$isVanillaSkeletonTrapHorseman"))
            .findFirst().orElseThrow();
        predicate.setAccessible(true);
        assertEquals(false, predicate.invoke(horse));
    }

    @Test
    void entitySaveReloadPreservesOriginTrapAndFireOwnership() {
        var level = level();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        when(level.registryAccess()).thenReturn(registries);
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        horse.addTag(Type.STRAY.tag());
        horse.setTrap(true);
        horse.setTrap(false);
        sunlight(horse);
        horse.igniteForTicks(40);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        horse.saveWithoutId(output);

        var loaded = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertTrue(loaded.entityTags().contains(Type.STRAY.tag()));
        assertTrue(loaded.entityTags().contains(UndeadRiders.TRAP_HORSE_TAG));
        var fire = ((SunlightTracked) loaded).undeadriders$fireState();
        assertTrue(fire.active());
        assertEquals(40, fire.externalTicks());
        assertEquals(160, loaded.getRemainingFireTicks());
    }

    @Test
    void riderlessMountsKeepTheirOriginalCaps() {
        var level = level();
        var horse = new ZombieHorse(EntityTypes.ZOMBIE_HORSE, level);
        horse.addTag(Type.HUSK.tag());
        var zoglin = new Zoglin(EntityTypes.ZOGLIN, level);
        zoglin.addTag(Type.PIGLIN.tag());
        assertEquals(1L, MountPopulation.count(List.of(horse), false).get(Type.HUSK));
        assertEquals(0L, MountPopulation.count(List.of(horse), false).get(Type.ZOMBIE));
        assertEquals(1L, MountPopulation.count(List.of(zoglin), true).get(Type.PIGLIN));
    }

    @Test
    void countCommandsFilterMountsAndCountLegacyOnlyOnceInTotal() throws Exception {
        var level = mock(ServerLevel.class, RETURNS_DEEP_STUBS);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        // This isolated bootstrap has no bound effect-immunity tags.
        var skeleton = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level) {
            @Override
            public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) { return true; }
        };
        skeleton.setId(1001);
        skeleton.addTag(Type.SKELETON.tag());
        skeleton.setCustomName(Component.literal("Pet"));
        var legacy = new ZombieHorse(EntityTypes.ZOMBIE_HORSE, level);
        var trap = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        trap.setTrap(true);
        when(level.getAllEntities()).thenReturn(List.of(skeleton, legacy, trap));
        var source = mock(net.minecraft.commands.CommandSourceStack.class);
        when(source.getLevel()).thenReturn(level);
        var dispatcher = new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();
        dispatcher.register(net.undeadriders.command.PopulationCommands.countCommand());
        assertEquals(2, dispatcher.execute("count", source));
        assertEquals(1, dispatcher.execute("count skeleton", source));
        assertEquals(1, dispatcher.execute("count husk", source));
        assertEquals(0, dispatcher.execute("count piglin", source));
        assertFalse(skeleton.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
        assertThrows(com.mojang.brigadier.exceptions.CommandSyntaxException.class,
            () -> dispatcher.execute("count invalid", source));
        var player = mock(net.minecraft.server.level.ServerPlayer.class);
        when(player.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(source.getPlayer()).thenReturn(player);
        when(source.getPlayerOrException()).thenReturn(player);
        dispatcher.register(net.undeadriders.command.PopulationCommands.highlightCommand());
        try {
            dispatcher.execute("highlight on", source);
            dispatcher.execute("count skeleton", source);
            assertFalse(skeleton.isCurrentlyGlowing());
            skeleton.setCustomName(null);
            dispatcher.execute("count skeleton", source);
            assertTrue(skeleton.isCurrentlyGlowing());
            assertFalse(legacy.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
            dispatcher.execute("highlight off", source);
            assertFalse(skeleton.isCurrentlyGlowing());
            dispatcher.execute("count husk", source);
            assertFalse(legacy.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
        } finally {
            dispatcher.execute("highlight off", source);
        }
    }

    @Test
    void highlightsExpireProtectPetsAndPreserveOtherSources() {
        var level = level();
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level) {
            @Override
            public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) { return true; }
        };
        var highlights = new net.undeadriders.command.MountHighlights();
        horse.setId(1002);
        var admin = java.util.UUID.randomUUID();
        var other = java.util.UUID.randomUUID();
        highlights.add(horse, admin);
        for (int i = 0; i < 599; i++) highlights.tick();
        assertTrue(horse.isCurrentlyGlowing());
        highlights.tick();
        assertFalse(horse.isCurrentlyGlowing());
        highlights.add(horse, admin);
        highlights.add(horse, other);
        highlights.remove(admin);
        assertTrue(horse.isCurrentlyGlowing());
        highlights.remove(other);
        assertFalse(horse.isCurrentlyGlowing());
        highlights.add(horse, admin);
        highlights.remove(horse);
        assertFalse(horse.isCurrentlyGlowing());
        highlights.add(horse, admin);
        horse.setCustomName(Component.literal("Pet"));
        highlights.tick();
        assertFalse(horse.isCurrentlyGlowing());
        horse.setCustomName(null);
        highlights.add(horse, admin);
        horse.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 1200));
        highlights.remove(admin);
        assertTrue(horse.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
        assertEquals(1200, horse.getEffect(net.minecraft.world.effect.MobEffects.GLOWING).getDuration());
        horse.removeEffect(net.minecraft.world.effect.MobEffects.GLOWING);
        highlights.add(horse, admin);
        // Another source sets the vanilla flag after our highlight has started.
        horse.setGlowingTag(true);
        highlights.remove(admin);
        assertTrue(horse.isCurrentlyGlowing());
        horse.setGlowingTag(false);
        assertFalse(horse.isCurrentlyGlowing());
        horse.setGlowingTag(true);
        highlights.add(horse, admin);
        highlights.remove(admin);
        assertTrue(horse.isCurrentlyGlowing());
        horse.addTag("undeadriders.command_highlight");
        net.undeadriders.command.MountHighlights.onLoad(horse);
        assertFalse(horse.isCurrentlyGlowing());
        assertFalse(horse.entityTags().contains("undeadriders.command_highlight"));
    }

    @Test
    void commandHighlightsAreNotSavedWithTheHorse() {
        var level = level();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        when(level.registryAccess()).thenReturn(registries);
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        horse.setId(1003);
        var highlights = new net.undeadriders.command.MountHighlights();
        highlights.add(horse, java.util.UUID.randomUUID());
        assertTrue(horse.isCurrentlyGlowing());
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        horse.saveWithoutId(output);
        var loaded = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertFalse(loaded.isCurrentlyGlowing());
        assertFalse(loaded.entityTags().contains("undeadriders.command_highlight"));
        highlights.clear();
        assertFalse(horse.isCurrentlyGlowing());
    }

    @Test
    void namedMountsAreExemptUntilTheirNameIsRemoved() {
        var level = level();
        var skeleton = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        skeleton.addTag(Type.STRAY.tag());
        var zombie = new ZombieHorse(EntityTypes.ZOMBIE_HORSE, level);
        zombie.addTag(Type.HUSK.tag());
        var zoglin = new Zoglin(EntityTypes.ZOGLIN, level);
        zoglin.addTag(Type.PIGLIN.tag());
        var legacy = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        for (var mount : List.of(skeleton, zombie, zoglin, legacy)) {
            mount.setCustomName(Component.literal("Pet"));
            assertTrue(MountPopulation.count(List.of(mount), false).values().stream().allMatch(count -> count == 0));
            assertTrue(MountPopulation.count(List.of(mount), true).values().stream().allMatch(count -> count == 0));
            mount.setCustomName(null);
        }
        assertEquals(1L, MountPopulation.count(List.of(skeleton), false).get(Type.STRAY));
        assertEquals(1L, MountPopulation.count(List.of(zombie), false).get(Type.HUSK));
        assertEquals(1L, MountPopulation.count(List.of(zoglin), true).get(Type.PIGLIN));
        assertEquals(1L, MountPopulation.count(List.of(legacy), false).get(Type.SKELETON));
    }

    @Test
    void namedRiderDoesNotExemptAnUnnamedMount() {
        var level = level();
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        var rider = new Skeleton(EntityTypes.SKELETON, level);
        rider.setCustomName(Component.literal("Rider"));
        assertTrue(rider.startRiding(horse, true, true));
        assertEquals(1L, MountPopulation.count(List.of(horse), false).get(Type.SKELETON));
    }

    @Test
    void unclassifiedOldHorsesCountConservatively() {
        var horse = new ZombieHorse(EntityTypes.ZOMBIE_HORSE, level());
        var population = MountPopulation.count(List.of(horse), false);
        assertEquals(1L, population.get(Type.ZOMBIE));
        assertEquals(1L, population.get(Type.HUSK));
    }

    @Test
    void legacyRiderSubtypeIsRememberedAfterRiderRemoval() {
        var level = level();
        var horse = new ZombieHorse(EntityTypes.ZOMBIE_HORSE, level);
        var rider = new Husk(EntityTypes.HUSK, level);
        assertTrue(rider.startRiding(horse, true, true));
        assertEquals(1L, MountPopulation.count(List.of(horse), false).get(Type.HUSK));
        rider.discard();
        assertNull(horse.getFirstPassenger());
        assertTrue(horse.entityTags().contains(Type.HUSK.tag()));
        assertEquals(1L, MountPopulation.count(List.of(horse), false).get(Type.HUSK));
    }

    @Test
    void eachAdditionalVanillaTrapHorseIsMarkedAtCreation() throws Exception {
        var level = mock(ServerLevel.class, RETURNS_DEEP_STUBS);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        var original = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level);
        var goal = new SkeletonTrapGoal(original);
        var createHorse = SkeletonTrapGoal.class.getDeclaredMethod("createHorse", DifficultyInstance.class);
        createHorse.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            var horse = (AbstractHorse) createHorse.invoke(goal, new DifficultyInstance(Difficulty.NORMAL, 0L, 0L, 0f));
            assertNotNull(horse);
            assertTrue(horse.entityTags().contains(UndeadRiders.TRAP_HORSE_TAG));
        }
    }

    @Test
    void trapProtectionSurvivesTriggerAndDoesNotConsumeModCaps() {
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level());
        horse.setTrap(true);
        horse.setTrap(false);
        assertTrue(horse.entityTags().contains(UndeadRiders.TRAP_HORSE_TAG));
        assertEquals(0L, MountPopulation.count(List.of(horse), false).get(Type.SKELETON));
    }

    @Test
    void realFireHooksCaptureShorterExternalIgnition() {
        var horse = new SkeletonHorse(EntityTypes.SKELETON_HORSE, level());
        var fire = ((SunlightTracked) horse).undeadriders$fireState();
        fire.beginSunlight(0);
        horse.igniteForTicks(160);
        fire.endSunlight();
        horse.igniteForTicks(40);
        assertEquals(160, horse.getRemainingFireTicks());
        assertEquals(40, fire.externalTicks());
        horse.setRemainingFireTicks(159);
        assertEquals(39, fire.externalTicks());
    }
}
