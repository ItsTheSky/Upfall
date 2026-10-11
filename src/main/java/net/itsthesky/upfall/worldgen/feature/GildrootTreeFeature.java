package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GildrootTreeFeature extends Feature<NoneFeatureConfiguration> {
    public GildrootTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.above()).is(ModTags.Blocks.SKY_PLANTABLE)) {
            return false;
        }
        int length = 5 + random.nextInt(5);
        for (int y = 0; y <= length + 2; y++) {
            if (!TreeBuilder.canReplace(level, origin.below(y))) return false;
        }

        TreeBuilder tree = new TreeBuilder(level);
        for (int y = 0; y < length; y++) {
            tree.log(origin.below(y), Direction.Axis.Y);
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int rootLength = 1 + random.nextInt(3);
            for (int i = 1; i <= rootLength; i++) {
                BlockPos root = origin.relative(dir, i);
                if (!level.getBlockState(root.above()).isSolid()) break;
                tree.log(root, dir.getAxis());
            }
        }

        BlockPos crown = origin.below(length);
        List<BlockPos> crowns = new ArrayList<>();
        crowns.add(crown);
        int branches = 1 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos cursor = origin.below(length / 2 + random.nextInt(2));
            for (int i = 0; i < 3; i++) {
                cursor = cursor.relative(dir).below(i % 2);
                tree.log(cursor, dir.getAxis());
            }
            crowns.add(cursor.below());
        }

        for (int i = 0; i < crowns.size(); i++) {
            BlockPos center = crowns.get(i);
            float rx = i == 0 ? 3.2F + random.nextFloat() : 2.0F + random.nextFloat() * 0.8F;
            float ry = i == 0 ? 2.6F : 1.8F;
            tree.log(center, Direction.Axis.Y);
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        double d = (dx * dx + dz * dz) / (double) (rx * rx) + (dy * dy) / (double) (ry * ry);
                        if (d <= 1.0 - random.nextFloat() * 0.18) {
                            tree.leaf(center.offset(dx, dy, dz));
                        }
                    }
                }
            }
        }
        tree.place(ModBlocks.GILDROOT.log().get(), ModBlocks.GILDROOT.leaves().get());

        for (int i = 0; i < 4; i++) {
            BlockPos start = crown.offset(random.nextInt(5) - 2, -1, random.nextInt(5) - 2);
            while (start.getY() > crown.getY() - 4 && !level.getBlockState(start).isAir()) {
                start = start.below();
            }
            if (level.getBlockState(start).isAir() && level.getBlockState(start.above()).is(ModBlocks.GILDROOT.leaves().get())) {
                hangVine(level, start, 1 + random.nextInt(4), random);
            }
        }
        return true;
    }

    static void hangVine(WorldGenLevel level, BlockPos start, int length, RandomSource random) {
        int n = 0;
        while (n < length && level.getBlockState(start.below(n)).isAir()) {
            n++;
        }
        if (n == 0) return;
        for (int i = 0; i < n - 1; i++) {
            level.setBlock(start.below(i), ModBlocks.GLOWVINE_PLANT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(start.below(n - 1), ModBlocks.GLOWVINE.get().defaultBlockState()
                .setValue(GrowingPlantHeadBlock.AGE, 18 + random.nextInt(7)), Block.UPDATE_CLIENTS);
    }
}
