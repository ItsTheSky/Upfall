package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.block.LeakingPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LeakingPipeFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!region.get(origin.below()).isSolid()) return false;
        int height = 3 + random.nextInt(4);
        int length = 2 + random.nextInt(3);
        int pipeY = origin.getY() + height;

        Direction best = null;
        int bestFloor = Integer.MAX_VALUE;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int floor = cauldronY(region, origin.relative(dir, length).atY(pipeY), length, dir);
            if (floor != Integer.MIN_VALUE && (floor < bestFloor || floor == bestFloor && random.nextBoolean())) {
                best = dir;
                bestFloor = floor;
            }
        }
        if (best == null) return false;
        for (int y = 0; y <= height; y++) {
            if (!region.isOpen(origin.above(y))) return false;
        }

        for (int y = 0; y < height; y++) {
            region.set(origin.above(y), pipe(Direction.Axis.Y));
        }
        region.set(origin.below(), RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState());
        BlockPos joint = origin.atY(pipeY);
        region.set(joint, plating());
        for (int i = 1; i < length; i++) {
            region.set(joint.relative(best, i), pipe(best.getAxis()));
        }
        BlockPos leak = joint.relative(best, length);
        region.set(leak, leakingPipe(best.getAxis()));
        region.set(leak.atY(bestFloor), Blocks.CAULDRON.defaultBlockState());

        if (random.nextBoolean()) {
            Direction face = best.getClockWise();
            if (random.nextBoolean()) face = face.getOpposite();
            region.setIfOpen(origin.above(1 + random.nextInt(height - 1)).relative(face), valve(face));
        }
        if (random.nextInt(3) == 0) {
            Direction toss = best.getOpposite();
            BlockPos debris = origin.relative(toss, 2 + random.nextInt(2)).relative(best.getClockWise(), random.nextInt(3) - 1);
            int ground = region.surface(debris.getX(), debris.getZ());
            if (Math.abs(ground - origin.getY()) <= 2) {
                Direction.Axis axis = best.getClockWise().getAxis();
                for (int i = 0; i < 2; i++) {
                    BlockPos piece = debris.relative(axis, i).atY(ground);
                    if (region.get(piece.below()).isSolid()) region.setIfOpen(piece, pipe(axis));
                }
            }
        }
        return true;
    }

    private static int cauldronY(FeatureRegion region, BlockPos leak, int length, Direction dir) {
        for (int i = 1; i <= length; i++) {
            if (!region.isOpen(leak.relative(dir, i - length))) return Integer.MIN_VALUE;
        }
        for (int d = 1; d <= LeakingPipeBlock.MAX_DRIP_DISTANCE; d++) {
            BlockPos pos = leak.below(d);
            BlockState state = region.get(pos);
            if (state.isAir()) continue;
            BlockPos cauldron = state.canBeReplaced() ? pos : pos.above();
            if (leak.getY() - cauldron.getY() < 2) return Integer.MIN_VALUE;
            BlockPos floor = cauldron.below();
            return region.get(floor).isFaceSturdy(region.level(), floor, Direction.UP) ? cauldron.getY() : Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }
}
