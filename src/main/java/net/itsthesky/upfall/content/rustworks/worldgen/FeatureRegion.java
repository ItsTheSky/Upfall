package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class FeatureRegion {
    static final int SIZE = 48;

    private final WorldGenLevel level;
    final int minX;
    final int minZ;

    // Features may only touch the 3x3 chunks around the decorated chunk.
    FeatureRegion(WorldGenLevel level, BlockPos origin) {
        this.level = level;
        this.minX = SectionPos.blockToSectionCoord(origin.getX()) * 16 - 16;
        this.minZ = SectionPos.blockToSectionCoord(origin.getZ()) * 16 - 16;
    }

    boolean contains(int x, int z) {
        return x >= minX && x < minX + SIZE && z >= minZ && z < minZ + SIZE;
    }

    boolean contains(BlockPos pos) {
        return contains(pos.getX(), pos.getZ()) && !level.isOutsideBuildHeight(pos);
    }

    int surface(int x, int z) {
        return contains(x, z) ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) : Integer.MIN_VALUE;
    }

    BlockState get(BlockPos pos) {
        return contains(pos) ? level.getBlockState(pos) : Blocks.VOID_AIR.defaultBlockState();
    }

    boolean isOpen(BlockPos pos) {
        if (!contains(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(ModBlocks.MIST.get());
    }

    void set(BlockPos pos, BlockState state) {
        if (contains(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    void setIfOpen(BlockPos pos, BlockState state) {
        if (isOpen(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    WorldGenLevel level() {
        return level;
    }
}
