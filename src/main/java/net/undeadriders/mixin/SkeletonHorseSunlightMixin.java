package net.undeadriders.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.undeadriders.UndeadRiders;
import net.undeadriders.spawn.MountPopulation.Type;
import net.undeadriders.util.SunlightFireState;
import net.undeadriders.util.SunlightTracked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonHorse.class)
abstract class SkeletonHorseSunlightMixin extends AbstractHorse implements SunlightTracked {
    @Unique private SunlightFireState undeadriders$fire;

    @Override
    public SunlightFireState undeadriders$fireState() {
        if (undeadriders$fire == null) undeadriders$fire = new SunlightFireState();
        return undeadriders$fire;
    }

    private SkeletonHorseSunlightMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void undeadriders$burnInSunlight(CallbackInfo ci) {
        if (level().isClientSide() || UndeadRiders.CONFIG == null) return;
        var config = UndeadRiders.CONFIG;
        SunlightFireState fire = undeadriders$fireState();
        if (!config.skeletonHorseSunlightBurnEnabled || undeadriders$isVanillaSkeletonTrapHorseman()
            || undeadriders$hasParchedRiderProtection()
            || (config.saddledSkeletonHorsesAvoidSunlightBurn
                && !getItemBySlot(EquipmentSlot.SADDLE).isEmpty())) {
            if (fire.active()) {
                int remaining = fire.externalTicks();
                fire.clear();
                if (remaining == 0) extinguishFire();
                else setRemainingFireTicks(remaining);
            }
            return;
        }
        if (undeadriders$isSunBurnTick()
            && getRandom().nextFloat() * 30.0f < (getLightLevelDependentMagicValue() - 0.4f) * 2.0f) {
            fire.beginSunlight(getRemainingFireTicks());
            try {
                igniteForSeconds(8.0f);
            } finally {
                fire.endSunlight();
            }
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
            && !protectedByWaterOrSnow
            && level().canSeeSky(eyePos);
    }

    private boolean undeadriders$isVanillaSkeletonTrapHorseman() {
        return ((SkeletonHorse) (Object) this).isTrap() || entityTags().contains(UndeadRiders.TRAP_HORSE_TAG);
    }

    @Unique
    private boolean undeadriders$hasParchedRiderProtection() {
        if (!(getFirstPassenger() instanceof Parched parched) || !parched.isAlive()) return false;
        if (entityTags().contains(Type.PARCHED.tag())) return true;
        // Older occupied mounts predate origin tags. Infer them here, before sunlight ignition,
        // rather than waiting for a population scan that may be disabled.
        for (Type type : Type.values()) {
            if (entityTags().contains(type.tag())) return false;
        }
        addTag(Type.PARCHED.tag());
        return true;
    }

    @Inject(method = "setTrap", at = @At("HEAD"))
    private void undeadriders$rememberTrap(boolean trap, CallbackInfo ci) {
        if (trap) addTag(UndeadRiders.TRAP_HORSE_TAG);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void undeadriders$saveFireSource(ValueOutput output, CallbackInfo ci) {
        SunlightFireState fire = undeadriders$fireState();
        output.putBoolean("undeadriders:sunlight_fire", fire.active());
        output.putInt("undeadriders:external_fire_ticks", fire.externalTicks());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void undeadriders$loadFireSource(ValueInput input, CallbackInfo ci) {
        undeadriders$fireState().restore(input.getBooleanOr("undeadriders:sunlight_fire", false),
            input.getIntOr("undeadriders:external_fire_ticks", 0), getRemainingFireTicks());
    }
}
