package net.undeadriders.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.undeadriders.util.CommandHighlighted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityCommandHighlightMixin implements CommandHighlighted {
    @Unique private volatile boolean undeadriders$commandHighlighted;

    @Shadow private void updateGlowingStatus() { throw new AssertionError(); }

    @Override
    public void undeadriders$setCommandHighlighted(boolean highlighted) {
        undeadriders$commandHighlighted = highlighted;
        updateGlowingStatus();
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void undeadriders$includeCommandHighlight(CallbackInfoReturnable<Boolean> cir) {
        if (undeadriders$commandHighlighted && !((LivingEntity) (Object) this).level().isClientSide()) {
            cir.setReturnValue(true);
        }
    }
}
