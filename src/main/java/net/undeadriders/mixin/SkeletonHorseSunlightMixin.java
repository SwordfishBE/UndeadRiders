package net.undeadriders.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.level.Level;
import net.undeadriders.UndeadRiders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonHorse.class)
abstract class SkeletonHorseSunlightMixin extends AbstractHorse {
    private SkeletonHorseSunlightMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void undeadriders$burnInSunlight(CallbackInfo ci) {
        if (UndeadRiders.CONFIG == null || !UndeadRiders.CONFIG.skeletonHorseSunlightBurnEnabled) {
            return;
        }
        if (undeadriders$isVanillaSkeletonTrapHorseman()) {
            if (undeadriders$isSunBurnTick()) {
                extinguishFire();
            }
            return;
        }
        if (UndeadRiders.CONFIG.saddledSkeletonHorsesAvoidSunlightBurn
            && !getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
            if (undeadriders$isSunBurnTick()) {
                extinguishFire();
            }
            return;
        }
        if (undeadriders$isSunBurnTick()) {
            igniteForSeconds(8.0f);
        }
    }

    private boolean undeadriders$isSunBurnTick() {
        if (level().isClientSide()) {
            return false;
        }
        boolean monstersBurn = level().environmentAttributes()
            .getValue(EnvironmentAttributes.MONSTERS_BURN, position());
        if (!monstersBurn) {
            return false;
        }

        float brightness = getLightLevelDependentMagicValue();
        BlockPos eyePos = BlockPos.containing(getX(), getEyeY(), getZ());
        boolean protectedByWaterOrSnow = isInWaterOrRain() || isInPowderSnow || wasInPowderSnow;

        return brightness > 0.5f
            && getRandom().nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f
            && !protectedByWaterOrSnow
            && level().canSeeSky(eyePos);
    }

    private boolean undeadriders$isVanillaSkeletonTrapHorseman() {
        if (((SkeletonHorse) (Object) this).isTrap()) {
            return true;
        }

        Entity passenger = getFirstPassenger();
        return passenger instanceof Skeleton skeleton && skeleton.isPersistenceRequired();
    }
}
