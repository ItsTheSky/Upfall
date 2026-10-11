package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.Util;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PlungingPipeFeature extends RustFeature {
    private static final int MAX_REACH = 16;
    private static final int DROP = 12;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = random.nextInt(3) == 0 ? 1 : 2;
        List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Util.shuffle(dirs, random);
        for (Direction dir : dirs) {
            int edge = findEdge(region, origin, dir);
            if (edge < 0) continue;
            int reach = edge + radius;
            if (!clearBelow(region, origin.relative(dir, reach), dir, radius, origin.getY())) continue;
            build(region, random, origin, dir, reach, radius);
            return true;
        }
        return false;
    }

    private static int findEdge(FeatureRegion region, BlockPos origin, Direction dir) {
        for (int i = 1; i <= MAX_REACH; i++) {
            BlockPos col = origin.relative(dir, i);
            int top = region.surface(col.getX(), col.getZ());
            if (top == Integer.MIN_VALUE || top > origin.getY() + 2) return -1;
            if (top < origin.getY() - DROP) return i;
        }
        return -1;
    }

    private static boolean clearBelow(FeatureRegion region, BlockPos center, Direction dir, int radius, int y) {
        for (int w = -radius - 1; w <= radius + 1; w++) {
            for (int u = -radius - 1; u <= radius + 1; u++) {
                BlockPos col = offset(center, dir.getAxis(), w, u, 0);
                if (!region.contains(col.getX(), col.getZ())) return false;
                if (w >= -radius + 1 && region.surface(col.getX(), col.getZ()) >= y - DROP) return false;
            }
        }
        return true;
    }

    private static void build(FeatureRegion region, RandomSource random, BlockPos origin, Direction dir, int reach, int radius) {
        Direction.Axis along = dir.getAxis();
        int sign = dir.getAxisDirection().getStep();
        boolean hollow = radius > 1 && random.nextBoolean();
        double outer = outer(radius);
        double inner = inner(radius);
        double flange = (radius + 1.3) * (radius + 1.3);
        int cy = origin.getY() - 1;
        BlockPos column = origin.relative(dir, reach);
        int bottom = bottom(region, column, cy - radius - 1, random);

        List<BlockPos> interior = new ArrayList<>();
        for (int t = -2; t <= reach; t++) {
            BlockPos center = offset(origin, along, t * sign, 0, cy - origin.getY());
            for (int u = -radius; u <= radius; u++) {
                for (int v = -radius; v <= radius; v++) {
                    int d2 = u * u + v * v;
                    if (d2 > outer) continue;
                    BlockPos pos = offset(center, along, 0, u, v);
                    if (d2 <= inner && t > 0) {
                        interior.add(pos);
                    } else {
                        region.set(pos, plating());
                    }
                }
            }
        }
        for (int y = cy + radius; y >= bottom; y--) {
            boolean ring = (cy - y) % 8 == 7;
            for (int w = -radius - 1; w <= radius + 1; w++) {
                for (int u = -radius - 1; u <= radius + 1; u++) {
                    int d2 = u * u + w * w;
                    BlockPos pos = offset(column.atY(y), along, w * sign, u, 0);
                    if (d2 <= inner && y < cy + radius) {
                        interior.add(pos);
                    } else if (d2 <= outer) {
                        region.set(pos, plating());
                    } else if (ring && d2 <= flange) {
                        region.setIfOpen(pos, cog(Direction.Axis.Y));
                    }
                }
            }
        }
        BlockState fill = hollow ? Blocks.AIR.defaultBlockState() : plating();
        for (BlockPos pos : interior) {
            region.set(pos, fill);
        }
    }

    private static int bottom(FeatureRegion region, BlockPos column, int startY, RandomSource random) {
        int floor = UpfallTerrain.CLOUD_BAND_MIN + 6;
        for (int y = startY; y > floor; y--) {
            BlockState state = region.get(column.atY(y));
            if (state.is(ModBlocks.DENSE_CLOUD.get()) || state.is(ModBlocks.MIST.get())) {
                return Math.max(floor, y - 6 - random.nextInt(5));
            }
            if (!state.isAir()) {
                return y - 2;
            }
        }
        return floor;
    }
}
