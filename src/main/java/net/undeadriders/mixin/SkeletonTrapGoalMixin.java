package net.undeadriders.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.undeadriders.UndeadRiders;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonTrapGoal.class)
abstract class SkeletonTrapGoalMixin {
    @Shadow @Final private SkeletonHorse horse;

    @Inject(method = "tick", at = @At("HEAD"))
    private void undeadriders$markOriginalTrapHorse(CallbackInfo ci) {
        horse.addTag(UndeadRiders.TRAP_HORSE_TAG);
    }

    @Inject(method = "createHorse", at = @At("RETURN"))
    private void undeadriders$markAdditionalTrapHorse(DifficultyInstance difficulty,
            CallbackInfoReturnable<AbstractHorse> cir) {
        if (cir.getReturnValue() != null) cir.getReturnValue().addTag(UndeadRiders.TRAP_HORSE_TAG);
    }
}
