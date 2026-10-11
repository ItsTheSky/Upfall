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

public class MistglowCrystalFeature extends Feature<NoneFeatureConfiguration> {
    public MistglowCrystalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Direction grow;
        if (level.getBlockState(origin.above()).isSolid()) {
            grow = Direction.DOWN;
        } else if (level.getBlockState(origin.below()).isSolid()) {
            grow = Direction.UP;
        } else {
            return false;
        }
        BlockState crystal = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
        int length = 1 + random.nextInt(grow == Direction.DOWN ? 4 : 2);
        for (int i = 0; i < length; i++) {
            BlockPos pos = origin.relative(grow, i);
            if (!level.getBlockState(pos).isAir()) break;
            level.setBlock(pos, crystal, Block.UPDATE_CLIENTS);
            if (i == 0) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos sidePos = pos.relative(side);
                    if (random.nextInt(3) == 0 && level.getBlockState(sidePos).isAir()
                            && level.getBlockState(sidePos.relative(grow.getOpposite())).isSolid()) {
                        level.setBlock(sidePos, crystal, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return true;
    }
}
