package net.itsthesky.upfall.content.world.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NeedleTipFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MAX_STEPS = 14;
    private static final int MIN_TIP_Y = -24;

    public NeedleTipFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int start = topOf(level, origin.getX(), origin.getY() - 1, origin.getZ());
        if (start == Integer.MIN_VALUE) {
            return false;
        }
        BlockPos.MutableBlockPos top = new BlockPos.MutableBlockPos(origin.getX(), start, origin.getZ());
        LongSet visited = new LongOpenHashSet();
        visited.add(McChunks.key(top.getX(), top.getZ()));
        for (int step = 0; step < MAX_STEPS; step++) {
            BlockPos best = null;
            int bestY = top.getY();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int x = top.getX() + dx;
                    int z = top.getZ() + dz;
                    if ((dx == 0 && dz == 0) || visited.contains(McChunks.key(x, z))
                            || Math.abs(x - origin.getX()) > MAX_STEPS || Math.abs(z - origin.getZ()) > MAX_STEPS) continue;
                    int y = topOf(level, x, top.getY(), z);
                    if (y > bestY || y == bestY && best == null && y == top.getY()) {
                        bestY = y;
                        best = new BlockPos(x, y, z);
                    }
                }
            }
            if (best == null) break;
            top.set(best);
            visited.add(McChunks.key(top.getX(), top.getZ()));
        }
        if (top.getY() < MIN_TIP_Y || !isNeedle(level.getBlockState(top)) || level.getBlockState(top.above()).is(ModBlocks.MISTGLOW_CRYSTAL.get())) {
            return false;
        }
        BlockState crystal = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
        level.setBlock(top, crystal, Block.UPDATE_CLIENTS);
        level.setBlock(top.above(), crystal, Block.UPDATE_CLIENTS);
        return true;
    }

    private static int topOf(WorldGenLevel level, int x, int fromY, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, fromY, z);
        if (isNeedle(level.getBlockState(pos))) {
            for (int i = 0; i < 48 && isNeedle(level.getBlockState(pos.move(Direction.UP))); i++) {
            }
            return pos.getY() - 1;
        }
        for (int y = fromY - 1; y > fromY - 16; y--) {
            pos.setY(y);
            if (isNeedle(level.getBlockState(pos))) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isNeedle(BlockState state) {
        return state.is(ModBlocks.DUSKSLATE.get()) || state.is(ModBlocks.PEARLSTONE.get());
    }
}
