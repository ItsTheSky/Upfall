package net.itsthesky.upfall.content.mobs.entity;

import net.itsthesky.upfall.content.mobs.MobSounds;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Duskwing extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DIVING = SynchedEntityData.defineId(Duskwing.class, EntityDataSerializers.BOOLEAN);
    private static final int MIN_Y = 70;
    private static final int MAX_Y = 240;

    public Duskwing(EntityType<? extends Duskwing> type, Level level) {
        super(type, level);
        this.moveControl = new AirSteering.SteerMoveControl(this, 0.1, 0.55);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.FLYING_SPEED, 0.9);
    }

    public static boolean checkSpawnRules(EntityType<Duskwing> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.getLevel().dimension() == ModDimensions.UPFALL
                && pos.getY() > MIN_Y && pos.getY() < MAX_Y
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.below()).isAir();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIVING, false);
    }

    public boolean isDiving() {
        return entityData.get(DIVING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DiveAttackGoal());
        this.goalSelector.addGoal(2, new AirSteering.DriftGoal(this, MIN_Y, MAX_Y, 28, 8, 0.8, 160));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }
        AirSteering.faceMotion(this, isDiving() ? 25.0F : 8.0F);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MobSounds.DUSKWING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MobSounds.DUSKWING_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MobSounds.DUSKWING_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    private enum Phase {
        CIRCLE, DIVE, CLIMB
    }

    private class DiveAttackGoal extends Goal {
        private Phase phase = Phase.CIRCLE;
        private int phaseTicks;
        private int circleDuration;
        private double angle;
        private Vec3 retreat = Vec3.ZERO;

        DiveAttackGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            enter(Phase.CIRCLE);
            angle = getRandom().nextDouble() * Math.PI * 2;
        }

        @Override
        public void stop() {
            entityData.set(DIVING, false);
        }

        private void enter(Phase next) {
            phase = next;
            phaseTicks = 0;
            entityData.set(DIVING, next == Phase.DIVE);
            if (next == Phase.CIRCLE) circleDuration = 50 + getRandom().nextInt(50);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            phaseTicks++;
            switch (phase) {
                case CIRCLE -> {
                    angle += 0.09;
                    double radius = 6.5;
                    double height = target.getY() + 7.0 + Math.sin(phaseTicks * 0.15);
                    getMoveControl().setWantedPosition(target.getX() + Math.cos(angle) * radius, height,
                            target.getZ() + Math.sin(angle) * radius, 1.0);
                    boolean above = getY() > target.getY() + 3.0 && distanceToSqr(target.getX(), getY(), target.getZ()) < 18 * 18;
                    if (phaseTicks > circleDuration && above && hasLineOfSight(target)) enter(Phase.DIVE);
                }
                case DIVE -> {
                    getMoveControl().setWantedPosition(target.getX(), target.getY(0.5), target.getZ(), 1.7);
                    getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (getBoundingBox().inflate(0.35).intersects(target.getBoundingBox())) {
                        doHurtTarget(target);
                        Vec3 away = position().subtract(target.position()).multiply(1, 0, 1);
                        retreat = (away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize()).scale(10);
                        enter(Phase.CLIMB);
                    } else if (phaseTicks > 40 || horizontalCollision) {
                        enter(Phase.CIRCLE);
                    }
                }
                case CLIMB -> {
                    getMoveControl().setWantedPosition(target.getX() + retreat.x, target.getY() + 11.0, target.getZ() + retreat.z, 1.2);
                    if (phaseTicks > 25 || horizontalCollision) enter(Phase.CIRCLE);
                }
            }
        }
    }
}
