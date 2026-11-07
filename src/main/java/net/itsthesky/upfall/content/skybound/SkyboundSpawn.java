package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.Nullable;

final class SkyboundSpawn {
    private SkyboundSpawn() {}

    private static final int SEARCH_RADIUS = 1024;
    private static final int STEP = 32;
    private static final int MIN_ISLAND_Y = UpfallTerrain.CLOUD_BAND_MAX + 40;
    private static final int MAX_CHUNK_CHECKS = 48;

    @Nullable
    static BlockPos find(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState random = level.getChunkSource().randomState();
        int checks = 0;
        for (int r = 0; r <= SEARCH_RADIUS; r += STEP) {
            for (int dx = -r; dx <= r; dx += STEP) {
                for (int dz = -r; dz <= r; dz += STEP) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int top = generator.getBaseHeight(dx, dz, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                    if (top < MIN_ISLAND_Y) continue;
                    if (!generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(dx), QuartPos.fromBlock(top),
                            QuartPos.fromBlock(dz), random.sampler()).is(ModBiomes.DRIFTING_ARCHIPELAGO)) continue;
                    BlockPos spawn = standOn(level, dx, dz);
                    if (spawn != null) return spawn;
                    if (++checks >= MAX_CHUNK_CHECKS) return null;
                }
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos standOn(ServerLevel level, int x, int z) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4);
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
        BlockPos ground = new BlockPos(x, y, z);
        if (y < MIN_ISLAND_Y || !level.getBlockState(ground).is(ModBlocks.SKYGRASS_BLOCK.get())) return null;
        BlockPos feet = ground.above();
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty() ? feet : null;
    }
}
