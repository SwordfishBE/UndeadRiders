package net.undeadriders.mixin;

import net.minecraft.world.entity.Entity;
import net.undeadriders.util.SunlightTracked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityFireTrackingMixin {
    @Inject(method = "igniteForTicks", at = @At("HEAD"))
    private void undeadriders$trackExternalIgnition(int ticks, CallbackInfo ci) {
        if ((Object) this instanceof SunlightTracked tracked) {
            tracked.undeadriders$fireState().onExternalIgnition(ticks);
        }
    }

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"))
    private void undeadriders$trackFireTimer(int ticks, CallbackInfo ci) {
        if ((Object) this instanceof SunlightTracked tracked) {
            tracked.undeadriders$fireState().onTimerChange(((Entity) (Object) this).getRemainingFireTicks(), ticks);
        }
    }
}
