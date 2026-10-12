package net.itsthesky.upfall.content.quantum.cave;

import it.unimi.dsi.fastutil.longs.LongList;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

public class GasCaveCarver extends WorldCarver<CarverConfiguration> {
    public GasCaveCarver() {
        super(CarverConfiguration.CODEC.codec());
    }

    @Override
    public boolean isStartChunk(CarverConfiguration config, RandomSource random) {
        return true;
    }

    @Override
    public boolean carve(CarvingContext context, CarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomes,
                         RandomSource random, Aquifer aquifer, ChunkPos source, CarvingMask mask) {
        GasCaveNetwork network = GasCaveNetworks.get(context.randomState(), source);
        if (network.isEmpty()) return false;
        LongList cells = network.gasIn(chunk.getPos());
        if (cells.isEmpty()) return false;
        BlockState gas = QuantumBlocks.VEIL_GAS.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean carved = false;
        for (int i = 0; i < cells.size(); i++) {
            pos.set(cells.getLong(i));
            if (pos.getY() <= chunk.getMinBuildHeight() || pos.getY() >= chunk.getMaxBuildHeight()) continue;
            BlockState current = chunk.getBlockState(pos);
            if (current.isAir() || !canReplaceBlock(config, current)) continue;
            chunk.setBlockState(pos, gas, false);
            mask.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            carved = true;
        }
        return carved;
    }
}
