package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class UpdraftColumnFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_RISE = 14;
    private static final int MAX_RISE = 170;
    private static final int[] DISTANCES = {3, 4, 5, 6};

    public UpdraftColumnFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos base = context.origin().below();
        while (level.getBlockState(base).is(ModBlocks.MIST.get()) && base.getY() > McLevel.minY(level)) {
            base = base.below();
        }
        if (!level.getBlockState(base).isSolid()) {
            return false;
        }
        BlockPos vent = base.above();
        int target = findTargetTop(level, vent, random);
        if (target < 0) {
            return false;
        }
        for (int y = vent.getY() + 1; y <= target; y++) {
            BlockState state = level.getBlockState(new BlockPos(vent.getX(), y, vent.getZ()));
            if (!state.isAir() && !state.is(ModBlocks.MIST.get())) return false;
        }
        level.setBlock(vent, ModBlocks.UPDRAFT_VENT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        BlockState mist = ModBlocks.UPDRAFT_MIST.get().defaultBlockState();
        for (int y = vent.getY() + 1; y <= target; y++) {
            level.setBlock(new BlockPos(vent.getX(), y, vent.getZ()), mist, Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static int findTargetTop(WorldGenLevel level, BlockPos vent, RandomSource random) {
        int best = -1;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int distance : DISTANCES) {
                BlockPos side = vent.relative(dir, distance).relative(dir.getClockWise(), random.nextInt(3) - 1);
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, side.getX(), side.getZ());
                int rise = top - vent.getY();
                if (rise >= MIN_RISE && rise <= MAX_RISE && (best < 0 || top < best)) {
                    best = top;
                }
            }
        }
        return best < 0 ? -1 : Math.min(McLevel.maxYExclusive(level) - 1, best + 3);
    }
}
