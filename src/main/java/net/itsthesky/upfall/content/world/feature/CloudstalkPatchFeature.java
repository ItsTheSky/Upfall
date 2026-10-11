package net.itsthesky.upfall.content.world.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.content.world.block.CloudstalkBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CloudstalkPatchFeature extends Feature<NoneFeatureConfiguration> {
    private static final int TRIES = 7;
    private static final int SPREAD = 4;

    public CloudstalkPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int placed = 0;
        for (int i = 0; i < TRIES; i++) {
            BlockPos start = i == 0 ? origin : origin.offset(random.nextInt(SPREAD * 2 + 1) - SPREAD, 2, random.nextInt(SPREAD * 2 + 1) - SPREAD);
            if (grow(level, random, start)) placed++;
        }
        return placed > 0;
    }

    public static boolean grow(WorldGenLevel level, RandomSource random, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        int mist = 0;
        for (int i = 0; i < 12; i++) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.MIST.get())) {
                mist++;
            } else if (!state.isAir()) {
                break;
            }
            pos.move(0, -1, 0);
        }
        if (mist == 0 || !CloudstalkBlock.isCloudSoil(level.getBlockState(pos))) {
            return false;
        }
        int height = mist + 1 + random.nextInt(4);
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int y = 1; y <= height + 1; y++) {
            BlockState state = level.getBlockState(cursor.setY(pos.getY() + y));
            if (!state.isAir() && !state.is(ModBlocks.MIST.get())) return false;
        }
        BlockState stalk = WorldBlocks.CLOUDSTALK.get().defaultBlockState();
        for (int y = 1; y < height; y++) {
            level.setBlock(cursor.setY(pos.getY() + y), stalk, Block.UPDATE_CLIENTS);
        }
        level.setBlock(cursor.setY(pos.getY() + height), WorldBlocks.CLOUDBLOOM.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }
}
