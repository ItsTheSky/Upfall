package net.itsthesky.upfall.content.mobs.entity;

import net.itsthesky.upfall.compat.mc.UpfallFlyingMob;
import net.itsthesky.upfall.content.mobs.MobSounds;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class Mistjelly extends UpfallFlyingMob {
    public static final int MIN_Y = 36;
    public static final int MAX_Y = 72;

    private int stingCooldown;

    public Mistjelly(EntityType<? extends Mistjelly> type, Level level) {
        super(type, level);
        this.moveControl = new AirSteering.SteerMoveControl(this, 0.05, 0.4);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    public static boolean checkSpawnRules(EntityType<Mistjelly> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getLevel().dimension() != ModDimensions.UPFALL || pos.getY() < MIN_Y || pos.getY() > MAX_Y) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.is(ModBlocks.MIST.get())) return false;
        for (int depth = 1; depth <= 24; depth++) {
            BlockState below = level.getBlockState(pos.below(depth));
            if (below.is(ModBlocks.MIST.get()) || below.is(ModBlocks.DENSE_CLOUD.get())) return true;
        }
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new StingGoal());
        this.goalSelector.addGoal(2, new AirSteering.DriftGoal(this, MIN_Y, MAX_Y, 14, 5, 0.6, 120));
    }

    //? if >=26.1 {
    /*@Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
        return rallyNearby(super.hurtServer(level, source, amount), source);
    }
    *///?} else {
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return rallyNearby(super.hurt(source, amount), source);
    }
    //?}

    private boolean rallyNearby(boolean hurt, DamageSource source) {
        if (hurt && !level().isClientSide() && source.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
            setTarget(attacker);
            for (Mistjelly other : level().getEntitiesOfClass(Mistjelly.class, getBoundingBox().inflate(12.0))) {
                if (other.getTarget() == null) other.setTarget(attacker);
            }
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = getTarget();
        if (!level().isClientSide() && target != null && (!target.isAlive() || distanceToSqr(target) > 30 * 30
                || tickCount - getLastHurtByMobTimestamp() > 600)) {
            setTarget(null);
        }
        if (stingCooldown > 0) stingCooldown--;
        if (!level().isClientSide()) setAggressive(getTarget() != null);
        AirSteering.faceMotion(this, 3.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MobSounds.MISTJELLY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MobSounds.MISTJELLY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MobSounds.MISTJELLY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    private class StingGoal extends Goal {
        StingGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            getMoveControl().setWantedPosition(target.getX(), target.getY(0.6), target.getZ(), 1.3);
            if (stingCooldown == 0 && getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())) {
                stingCooldown = 24;
                //? if >=26.1 {
                /*if (doHurtTarget(getServerLevel(level()), target)) {
                *///?} else {
                if (doHurtTarget(target)) {
                //?}
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), Mistjelly.this);
                }
            }
        }
    }
}
