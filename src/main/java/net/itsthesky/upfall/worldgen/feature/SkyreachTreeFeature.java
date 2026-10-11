package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SkyreachTreeFeature extends Feature<NoneFeatureConfiguration> {
    public SkyreachTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.below()).is(ModTags.Blocks.SKY_PLANTABLE) && !level.getBlockState(origin.below()).is(net.minecraft.tags.BlockTags.DIRT)) {
            return false;
        }
        int height = 9 + random.nextInt(6);
        for (int y = 1; y <= height + 1; y++) {
            if (!TreeBuilder.canReplace(level, origin.above(y))) return false;
        }

        TreeBuilder tree = new TreeBuilder(level);
        BlockPos.MutableBlockPos trunk = origin.mutable();
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int leanAt = random.nextInt(3) == 0 ? height / 2 + random.nextInt(2) : -1;
        for (int y = 0; y < height; y++) {
            if (y == leanAt) {
                tree.log(trunk, Direction.Axis.Y);
                trunk.move(lean);
            }
            tree.log(trunk, Direction.Axis.Y);
            if (y < height - 1) trunk.move(Direction.UP);
        }
        BlockPos top = trunk.immutable();

        int tiers = height >= 12 ? 3 : 2;
        for (int t = 0; t < tiers; t++) {
            BlockPos center = top.below(t * 4);
            float radius = 2.2F + t * 1.3F + random.nextFloat() * 0.6F;
            tier(tree, random, center, radius);
        }
        tree.leaf(top.above());
        tree.leaf(top.above(2));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            tree.leaf(top.above().relative(dir));
        }
        tree.place(ModBlocks.SKYREACH.log().get(), ModBlocks.SKYREACH.leaves().get());
        return true;
    }

    private static void tier(TreeBuilder tree, RandomSource random, BlockPos center, float radius) {
        int r = Mth.ceil(radius);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int i = 1; i <= Math.max(1, r / 2); i++) {
                tree.log(center.relative(dir, i), dir.getAxis());
            }
        }
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz) + random.nextFloat() * 0.5;
                if (dist > radius) continue;
                BlockPos pos = center.offset(dx, 0, dz);
                tree.leaf(pos);
                if (dist < radius - 1.6) {
                    tree.leaf(pos.above());
                }
                if (dist > radius - 1.1 && random.nextFloat() < 0.4F) {
                    int length = 1 + random.nextInt(3);
                    for (int i = 1; i <= length; i++) {
                        tree.leaf(pos.below(i));
                    }
                }
            }
        }
    }
}
