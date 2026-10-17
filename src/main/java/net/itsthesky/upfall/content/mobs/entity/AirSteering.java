package net.itsthesky.upfall.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public final class AirSteering {
    private AirSteering() {}

    public static class SteerMoveControl extends MoveControl {
        private final double inertia;
        private final double speedScale;

        public SteerMoveControl(Mob mob, double inertia, double speedScale) {
            super(mob);
            this.inertia = inertia;
            this.speedScale = speedScale;
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) return;
            Vec3 toTarget = new Vec3(wantedX - mob.getX(), wantedY - mob.getY(), wantedZ - mob.getZ());
            double distance = toTarget.length();
            if (distance < 1.0 || mob.horizontalCollision && distance < 3.0) {
                operation = Operation.WAIT;
                return;
            }
            double speed = mob.getAttributeValue(Attributes.FLYING_SPEED) * speedModifier * speedScale;
            Vec3 desired = toTarget.scale(speed / distance);
            Vec3 motion = mob.getDeltaMovement();
            mob.setDeltaMovement(motion.add(desired.subtract(motion).scale(inertia)));
        }
    }

    public static void faceMotion(Mob mob, float maxTurn) {
        Vec3 motion = mob.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F;
            mob.setYRot(Mth.approachDegrees(mob.getYRot(), yaw, maxTurn));
            mob.yBodyRot = mob.getYRot();
            mob.yHeadRot = mob.getYRot();
        }
    }

    static boolean freeOfRock(Mob mob, double x, double y, double z) {
        BlockPos target = BlockPos.containing(x, y, z);
        BlockPos middle = BlockPos.containing((x + mob.getX()) / 2, (y + mob.getY()) / 2, (z + mob.getZ()) / 2);
        for (BlockPos pos : new BlockPos[]{target, target.above(), target.below(), middle}) {
            if (!mob.level().getBlockState(pos).getCollisionShape(mob.level(), pos).isEmpty()) return false;
        }
        return true;
    }

    static class DriftGoal extends Goal {
        private final Mob mob;
        private final int minY;
        private final int maxY;
        private final double range;
        private final double verticalRange;
        private final double speed;
        private final int restlessness;

        DriftGoal(Mob mob, int minY, int maxY, double range, double verticalRange, double speed, int restlessness) {
            this.mob = mob;
            this.minY = minY;
            this.maxY = maxY;
            this.range = range;
            this.verticalRange = verticalRange;
            this.speed = speed;
            this.restlessness = restlessness;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !mob.getMoveControl().hasWanted() || mob.getRandom().nextInt(restlessness) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = mob.getRandom();
            for (int attempt = 0; attempt < 8; attempt++) {
                double x = mob.getX() + (random.nextDouble() * 2 - 1) * range;
                double y = Mth.clamp(mob.getY() + (random.nextDouble() * 2 - 1) * verticalRange, minY, maxY);
                double z = mob.getZ() + (random.nextDouble() * 2 - 1) * range;
                if (freeOfRock(mob, x, y, z)) {
                    mob.getMoveControl().setWantedPosition(x, y, z, speed);
                    return;
                }
            }
        }
    }
}
