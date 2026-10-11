package net.itsthesky.upfall.content.hamlet.entity;

import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

final class LoftkinGoals {
    private LoftkinGoals() {}

    static class PlatformStroll extends Goal {
        private static final int RADIUS = 10;
        private final Loftkin mob;
        private final double speed;

        PlatformStroll(Loftkin mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (mob.isTrading() || mob.isPassenger() || !mob.onGround() || !mob.getNavigation().isDone()
                    || mob.getRandom().nextInt(reducedTickDelay(90)) != 0) {
                return false;
            }
            BlockPos target = findTarget();
            if (target == null) {
                return false;
            }
            return mob.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
        }

        @Override
        public boolean canContinueToUse() {
            return !mob.getNavigation().isDone() && !mob.isTrading();
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
        }

        @Nullable
        private BlockPos findTarget() {
            BlockPos center = mob.getHome() != null ? mob.getHome() : mob.blockPosition();
            for (int attempt = 0; attempt < 12; attempt++) {
                BlockPos pos = center.offset(mob.getRandom().nextInt(RADIUS * 2 + 1) - RADIUS, mob.getRandom().nextInt(7) - 3,
                        mob.getRandom().nextInt(RADIUS * 2 + 1) - RADIUS);
                if (isSafeSpot(mob.level(), pos)) {
                    return pos;
                }
            }
            return null;
        }
    }

    static class GreetVisitor extends Goal {
        private static final TargetingConditions VISITOR = TargetingConditions.forNonCombat().range(6.0);
        private final Loftkin mob;
        @Nullable
        private Player visitor;
        private int timer;

        GreetVisitor(Loftkin mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mob.getGreetCooldown() > 0 || mob.isTrading() || mob.getRandom().nextInt(10) != 0) {
                return false;
            }
            visitor = mob.level().getNearestPlayer(VISITOR, mob);
            return visitor != null && !visitor.isSpectator() && mob.hasLineOfSight(visitor);
        }

        @Override
        public void start() {
            timer = 40;
            mob.getNavigation().stop();
            mob.level().broadcastEntityEvent(mob, Loftkin.EVENT_WAVE);
            mob.playSound(HamletSounds.LOFTKIN_GREET.get(), 0.9F, mob.getVoicePitch());
            mob.setGreetCooldown(900 + mob.getRandom().nextInt(600));
        }

        @Override
        public boolean canContinueToUse() {
            return timer > 0 && visitor != null && visitor.isAlive() && !mob.isTrading();
        }

        @Override
        public void tick() {
            timer--;
            if (visitor != null) {
                mob.getLookControl().setLookAt(visitor, 30.0F, 30.0F);
            }
        }

        @Override
        public void stop() {
            visitor = null;
        }
    }

    static boolean isSafeSpot(BlockGetter level, BlockPos pos) {
        if (!isStandable(level, pos)) {
            return false;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos next = pos.relative(side);
            boolean wall = !level.getBlockState(next).getCollisionShape(level, next).isEmpty();
            if (!wall && !hasFloor(level, next) && !hasFloor(level, next.below())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStandable(BlockGetter level, BlockPos pos) {
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        VoxelShape here = level.getBlockState(pos).getCollisionShape(level, pos);
        return here.isEmpty() ? hasFloor(level, pos) : here.max(Direction.Axis.Y) <= 0.5;
    }

    private static boolean hasFloor(BlockGetter level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState state = level.getBlockState(below);
        VoxelShape shape = state.getCollisionShape(level, below);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) <= 1.0;
    }
}
