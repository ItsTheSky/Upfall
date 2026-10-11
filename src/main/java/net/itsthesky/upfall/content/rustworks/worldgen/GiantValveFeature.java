package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GiantValveFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = 3 + random.nextInt(2);
        if (!flat(region, origin, radius)) return false;
        if (random.nextInt(3) == 0) {
            toppled(region, random, origin, radius);
        } else {
            standing(region, random, origin, radius);
        }
        return true;
    }

    private static boolean flat(FeatureRegion region, BlockPos origin, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int top = region.surface(origin.getX() + dx, origin.getZ() + dz);
                if (top > origin.getY() + 1) return false;
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && top < origin.getY()) return false;
            }
        }
        return true;
    }

    private static void standing(FeatureRegion region, RandomSource random, BlockPos origin, int radius) {
        int height = 2 + random.nextInt(3);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                region.set(origin.offset(dx, -1, dz), RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState());
            }
        }
        for (int y = 0; y < height; y++) {
            region.set(origin.above(y), pipe(Direction.Axis.Y));
        }
        BlockPos hub = origin.above(height);
        wheel(region, hub, Direction.Axis.Y, radius);
        region.setIfOpen(hub.above(), valve(Direction.UP));
    }

    private static void toppled(FeatureRegion region, RandomSource random, BlockPos origin, int radius) {
        Direction.Axis normal = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        BlockPos hub = origin.below(1 + random.nextInt(2));
        wheel(region, hub, normal, radius);
        Direction face = Direction.get(random.nextBoolean() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE, normal);
        region.setIfOpen(hub.relative(face), valve(face));
        for (int i = 1; i <= 3; i++) {
            region.set(hub.relative(face.getOpposite(), i), pipe(normal));
        }
    }

    private static void wheel(FeatureRegion region, BlockPos hub, Direction.Axis normal, int radius) {
        Direction.Axis first = normal == Direction.Axis.Y ? Direction.Axis.X : lateral(normal);
        Direction.Axis second = normal == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y;
        double outer = outer(radius);
        double inner = inner(radius);
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                int d2 = a * a + b * b;
                BlockPos pos = hub.relative(first, a).relative(second, b);
                if (d2 == 0) {
                    region.set(pos, cog(normal));
                } else if (d2 > inner && d2 <= outer) {
                    region.set(pos, cogPlating());
                } else if (d2 <= inner && (a == 0 || b == 0)) {
                    region.set(pos, pipe(a == 0 ? second : first));
                }
            }
        }
    }
}
