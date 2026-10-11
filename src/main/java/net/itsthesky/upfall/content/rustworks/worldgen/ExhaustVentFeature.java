package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ExhaustVentFeature extends RustFeature {
    static final int CLEARANCE = 14;
    private static final int REACH_MIN = 6;
    private static final int REACH_MAX = 22;
    private static final int DROP = 12;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos vent = origin.below();
        if (!footing(region, vent)) return false;
        for (int i = 0; i < CLEARANCE; i++) {
            if (!region.isOpen(origin.above(i))) return false;
        }
        boolean above = islandAbove(region, origin);
        if (!above && !(nearEdge(region, origin) && random.nextInt(3) == 0)) return false;
        region.set(vent, RustworksBlocks.EXHAUST_VENT.get().defaultBlockState());
        context.level().scheduleTick(vent, RustworksBlocks.EXHAUST_VENT.get(), 1 + random.nextInt(40));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextInt(3) != 0) {
                region.set(vent.relative(dir), random.nextBoolean() ? plating() : RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState());
            }
        }
        if (random.nextBoolean()) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos stub = origin.relative(side, 2);
            int height = 1 + random.nextInt(2);
            if (region.isOpen(stub) && !region.isOpen(stub.below())) {
                for (int y = 0; y < height; y++) {
                    region.setIfOpen(stub.above(y), pipe(Direction.Axis.Y));
                }
                region.setIfOpen(stub.above(height), valve(Direction.UP));
            }
        }
        return true;
    }

    private static boolean footing(FeatureRegion region, BlockPos vent) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = vent.offset(dx, 0, dz);
                if (region.isOpen(pos) || !region.get(pos).isSolid() || region.isOpen(pos.below())) return false;
            }
        }
        return true;
    }

    private static boolean islandAbove(FeatureRegion region, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int r = 3; r <= 9; r += 2) {
            for (int a = 0; a < 8; a++) {
                double angle = a * Math.PI / 4;
                int x = origin.getX() + (int) Math.round(Math.cos(angle) * r);
                int z = origin.getZ() + (int) Math.round(Math.sin(angle) * r);
                for (int dy = REACH_MIN; dy <= REACH_MAX; dy++) {
                    cursor.set(x, origin.getY() + dy, z);
                    if (!region.contains(cursor)) break;
                    if (!region.isOpen(cursor) && region.isOpen(cursor.above())) return true;
                }
            }
        }
        return false;
    }

    private static boolean nearEdge(FeatureRegion region, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int r = 2; r <= 6; r++) {
                BlockPos column = origin.relative(dir, r);
                boolean floor = false;
                for (int dy = 0; dy <= DROP && !floor; dy++) {
                    cursor.set(column.getX(), origin.getY() - dy, column.getZ());
                    if (!region.contains(cursor)) {
                        floor = true;
                    } else if (!region.isOpen(cursor)) {
                        floor = true;
                    }
                }
                if (!floor) return true;
            }
        }
        return false;
    }
}
