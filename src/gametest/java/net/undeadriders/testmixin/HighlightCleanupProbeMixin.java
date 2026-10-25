package net.undeadriders.testmixin;

import net.minecraft.world.entity.LivingEntity;
import net.undeadriders.CompatibilityTests;
import net.undeadriders.command.MountHighlights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MountHighlights.class, remap = false)
abstract class HighlightCleanupProbeMixin {
    @Inject(method = "remove(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private void undeadriders$verifyCleanupThread(LivingEntity entity, CallbackInfo ci) {
        CompatibilityTests.onHighlightRemoval(entity);
    }
}
