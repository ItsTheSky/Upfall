package net.itsthesky.upfall.content.rustworks.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CogClusterFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int count = 1 + random.nextInt(3);
        int placed = 0;
        for (int i = 0; i < count; i++) {
            int x = origin.getX() + (i == 0 ? 0 : random.nextInt(13) - 6);
            int z = origin.getZ() + (i == 0 ? 0 : random.nextInt(13) - 6);
            int ground = region.surface(x, z);
            if (ground < MIN_LAND || Math.abs(ground - origin.getY()) > 4) continue;
            int radius = i == 0 ? 3 + random.nextInt(2) : 2 + random.nextInt(2);
            Direction.Axis axis = random.nextInt(4) == 0 ? Direction.Axis.Y : random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
            int sink = axis == Direction.Axis.Y ? 1 : 1 + random.nextInt(radius / 2 + 1);
            gear(region, random, new BlockPos(x, ground - sink, z), axis, radius);
            placed++;
        }
        return placed > 0;
    }

    private static void gear(FeatureRegion region, RandomSource random, BlockPos center, Direction.Axis axis, int radius) {
        Direction.Axis first = axis == Direction.Axis.Y ? Direction.Axis.X : lateral(axis);
        Direction.Axis second = axis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y;
        int teeth = Math.max(6, radius * 3) & ~1;
        float phase = random.nextFloat() * Mth.TWO_PI;
        double outer = outer(radius);
        double tip = (radius + 1.5) * (radius + 1.5);
        for (int a = -radius - 1; a <= radius + 1; a++) {
            for (int b = -radius - 1; b <= radius + 1; b++) {
                int d2 = a * a + b * b;
                BlockPos pos = center.relative(first, a).relative(second, b);
                if (d2 == 0) {
                    for (int n = -1; n <= 1; n++) {
                        region.set(pos.relative(axis, n), pipe(axis));
                    }
                } else if (d2 <= outer) {
                    if (radius >= 4 && Math.abs(a) == 2 && Math.abs(b) == 2) continue;
                    region.set(pos, cogPlating());
                } else if (d2 <= tip) {
                    float angle = Mth.positiveModulo((float) Mth.atan2(b, a) - phase, Mth.TWO_PI);
                    if ((int) (angle / Mth.TWO_PI * teeth) % 2 == 0) {
                        region.set(pos, cogPlating());
                    }
                }
            }
        }
    }
}
