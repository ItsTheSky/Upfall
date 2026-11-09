package net.itsthesky.upfall.content.quantum.cave;

import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GasCaveFeature extends Feature<NoneFeatureConfiguration> {
    public GasCaveFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!(level.getLevel().getChunkSource() instanceof ServerChunkCache cache)) return false;
        RandomState state = cache.randomState();
        ChunkPos chunk = McChunks.of(context.origin());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        int range = GasCaveNetworks.range();
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                GasCaveNetwork network = GasCaveNetworks.get(state, new ChunkPos(McChunks.x(chunk) + dx, McChunks.z(chunk) + dz));
                if (network.isEmpty()) continue;
                for (GasCaveNetwork.Prop prop : network.propsIn(chunk)) {
                    pos.set(prop.pos());
                    BlockState current = level.getBlockState(pos);
                    boolean gas = current.is(QuantumBlocks.VEIL_GAS.get());
                    if (prop.solidOnly() ? gas || current.isAir() || !solidRender(current, level, pos) : !gas) continue;
                    if (prop.host() != null && !prop.host().test(current)) continue;
                    BlockState target = prop.isOre() ? GasCaveOres.pick(current, RandomSource.create(prop.oreSeed())) : prop.state();
                    if (target == null || !target.canSurvive(level, pos)) continue;
                    level.setBlock(pos, target, Block.UPDATE_CLIENTS);
                    if (prop.loot() != null) {
                        RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(prop.pos() ^ level.getSeed()), pos, prop.loot());
                    }
                    placed = true;
                }
            }
        }
        GasCaveWalls walls = new GasCaveWalls(level.getSeed());
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                GasCaveNetwork network = GasCaveNetworks.get(state, new ChunkPos(McChunks.x(chunk) + dx, McChunks.z(chunk) + dz));
                if (!network.isEmpty()) placed |= restyle(level, network, chunk, walls);
            }
        }
        return placed;
    }

    private static boolean restyle(WorldGenLevel level, GasCaveNetwork network, ChunkPos chunk, GasCaveWalls walls) {
        int cx = McChunks.x(chunk), cz = McChunks.z(chunk);
        BlockPos.MutableBlockPos wall = new BlockPos.MutableBlockPos();
        LongOpenHashSet seen = new LongOpenHashSet();
        boolean changed = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LongList cells = network.gasIn(new ChunkPos(cx + dx, cz + dz));
                for (int i = 0; i < cells.size(); i++) {
                    long gas = cells.getLong(i);
                    for (Direction dir : Direction.values()) {
                        int x = BlockPos.getX(gas) + dir.getStepX(), y = BlockPos.getY(gas) + dir.getStepY(), z = BlockPos.getZ(gas) + dir.getStepZ();
                        if (x >> 4 != cx || z >> 4 != cz) continue;
                        long key = BlockPos.asLong(x, y, z);
                        if (network.isGas(key) || !seen.add(key)) continue;
                        wall.set(x, y, z);
                        if (holdsProp(level, network, wall)) continue;
                        boolean floor = network.isGas(BlockPos.asLong(x, y + 1, z));
                        boolean nearSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - y <= 10;
                        BlockState styled = walls.restyle(level.getBlockState(wall), x, y, z, floor, nearSurface);
                        if (styled == null) continue;
                        level.setBlock(wall, styled, Block.UPDATE_CLIENTS);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private static boolean holdsProp(WorldGenLevel level, GasCaveNetwork network, BlockPos wall) {
        for (Direction dir : Direction.values()) {
            BlockPos next = wall.relative(dir);
            if (!network.isGas(next.asLong())) continue;
            BlockState state = level.getBlockState(next);
            if (!state.isAir() && !state.is(QuantumBlocks.VEIL_GAS.get())) return true;
        }
        return false;
    }

    private static boolean solidRender(BlockState state, WorldGenLevel level, BlockPos pos) {
        //? if >=26.1 {
        /*return state.isSolidRender();
        *///?} else {
        return state.isSolidRender(level, pos);
        //?}
    }
}
