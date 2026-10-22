package net.undeadriders;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinLoadingTest {
    @Test
    void minecraftBootstrapLoadsMixinTargets() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        assertTrue(java.util.Arrays.stream(SkeletonHorse.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("undeadriders$burnInSunlight")));
        assertTrue(java.util.Arrays.stream(LivingEntity.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("undeadriders$includeCommandHighlight")));
        assertTrue(java.util.Arrays.stream(LivingEntity.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("undeadriders$ignoreZoglinRiderSuffocation")));
        assertTrue(java.util.Arrays.stream(Entity.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("undeadriders$trackExternalIgnition")));
        assertTrue(java.util.Arrays.stream(SkeletonTrapGoal.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("undeadriders$markAdditionalTrapHorse")));
    }
}
