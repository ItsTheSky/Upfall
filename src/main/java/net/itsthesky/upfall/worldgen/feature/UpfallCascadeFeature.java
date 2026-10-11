package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class UpfallCascadeFeature extends Feature<NoneFeatureConfiguration> {
    public UpfallCascadeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos top = context.origin();
        if (!level.getBlockState(top.above()).isSolid()) {
            return false;
        }
        BlockState cascade = ModBlocks.UPFALL_CASCADE.get().defaultBlockState();
        int length = 10 + random.nextInt(24);
        Direction widthDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        boolean placed = false;
        for (int strand = 0; strand < 2; strand++) {
            BlockPos start = strand == 0 ? top : top.relative(widthDir);
            if (strand == 1 && (!level.getBlockState(start).isAir() || !level.getBlockState(start.above()).isSolid())) break;
            int strandLength = strand == 0 ? length : length - 3 - random.nextInt(6);
            for (int i = 0; i < strandLength; i++) {
                BlockPos pos = start.below(i);
                if (!level.getBlockState(pos).isAir()) break;
                level.setBlock(pos, cascade, Block.UPDATE_CLIENTS);
                placed = true;
            }
        }
        return placed;
    }
}
