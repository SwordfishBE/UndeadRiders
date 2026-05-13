package net.undeadriders.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.undeadriders.UndeadRiders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityZoglinRiderMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void undeadriders$ignoreZoglinRiderSuffocation(
            ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (!(self instanceof ZombifiedPiglin piglin)) {
            return;
        }
        if (!source.is(DamageTypes.IN_WALL) || !piglin.entityTags().contains(UndeadRiders.ZOGLIN_RIDER_TAG)) {
            return;
        }

        Entity vehicle = piglin.getVehicle();
        if (vehicle instanceof Zoglin && vehicle.entityTags().contains(UndeadRiders.ZOGLIN_RIDER_TAG)) {
            cir.setReturnValue(false);
        }
    }
}
