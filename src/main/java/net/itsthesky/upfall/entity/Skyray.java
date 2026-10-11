package net.itsthesky.upfall.entity;

import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Skyray extends FlyingMob {
    private static final int MIN_GLIDE_Y = 72;
    private static final int MAX_GLIDE_Y = 240;

    public Skyray(EntityType<? extends Skyray> type, Level level) {
        super(type, level);
        this.moveControl = new GlideMoveControl(this);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    public static boolean checkSpawnRules(EntityType<Skyray> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getLevel().dimension() == ModDimensions.UPFALL && pos.getY() > MIN_GLIDE_Y && pos.getY() < MAX_GLIDE_Y
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isAir() && level.getBlockState(pos.above()).isAir();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GlideGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F;
            setYRot(Mth.approachDegrees(getYRot(), yaw, 4.0F));
            yBodyRot = getYRot();
            yHeadRot = getYRot();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SKYRAY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SKYRAY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SKYRAY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    static class GlideMoveControl extends MoveControl {
        private final Skyray skyray;

        GlideMoveControl(Skyray skyray) {
            super(skyray);
            this.skyray = skyray;
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) return;
            Vec3 toTarget = new Vec3(wantedX - skyray.getX(), wantedY - skyray.getY(), wantedZ - skyray.getZ());
            double distance = toTarget.length();
            if (distance < 2.0 || skyray.horizontalCollision) {
                operation = Operation.WAIT;
                return;
            }
            double speed = skyray.getAttributeValue(Attributes.FLYING_SPEED) * speedModifier * 0.4;
            Vec3 desired = toTarget.scale(speed / distance);
            double bob = Math.sin(skyray.tickCount * 0.08) * 0.015;
            Vec3 motion = skyray.getDeltaMovement();
            skyray.setDeltaMovement(motion.add(desired.subtract(motion).scale(0.04)).add(0.0, bob, 0.0));
        }
    }

    static class GlideGoal extends Goal {
        private final Skyray skyray;

        GlideGoal(Skyray skyray) {
            this.skyray = skyray;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl control = skyray.getMoveControl();
            return !control.hasWanted() || skyray.getRandom().nextInt(200) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = skyray.getRandom();
            for (int attempt = 0; attempt < 8; attempt++) {
                double x = skyray.getX() + (random.nextDouble() * 2 - 1) * 28;
                double y = Mth.clamp(skyray.getY() + (random.nextDouble() * 2 - 1) * 8, MIN_GLIDE_Y, MAX_GLIDE_Y);
                double z = skyray.getZ() + (random.nextDouble() * 2 - 1) * 28;
                if (isOpenAir(x, y, z)) {
                    skyray.getMoveControl().setWantedPosition(x, y, z, 1.0);
                    return;
                }
            }
        }

        private boolean isOpenAir(double x, double y, double z) {
            BlockPos target = BlockPos.containing(x, y, z);
            BlockPos middle = BlockPos.containing((x + skyray.getX()) / 2, (y + skyray.getY()) / 2, (z + skyray.getZ()) / 2);
            for (BlockPos pos : new BlockPos[]{target, target.above(), target.below(), middle}) {
                if (!skyray.level().getBlockState(pos).isAir()) return false;
            }
            return true;
        }
    }
}
