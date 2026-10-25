package net.undeadriders.testmixin;

import net.minecraft.world.entity.Mob;
import net.undeadriders.CompatibilityTests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class MobTickProbeMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void undeadriders$observeCompatibilityTick(CallbackInfo ci) {
        if (CompatibilityTests.onTick((Mob) (Object) this)) ci.cancel();
    }
}
