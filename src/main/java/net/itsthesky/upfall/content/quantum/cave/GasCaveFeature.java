package net.itsthesky.upfall.content.quantum.cave;

import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        for (int dx = -GasCaveNetworks.RANGE; dx <= GasCaveNetworks.RANGE; dx++) {
            for (int dz = -GasCaveNetworks.RANGE; dz <= GasCaveNetworks.RANGE; dz++) {
                GasCaveNetwork network = GasCaveNetworks.get(state, new ChunkPos(McChunks.x(chunk) + dx, McChunks.z(chunk) + dz));
                if (network.isEmpty()) continue;
                for (GasCaveNetwork.Prop prop : network.propsIn(chunk)) {
                    pos.set(prop.pos());
                    BlockState current = level.getBlockState(pos);
                    boolean gas = current.is(QuantumBlocks.VEIL_GAS.get());
                    if (prop.solidOnly() ? gas || current.isAir() || !solidRender(current, level, pos) : !gas) continue;
                    if (!prop.state().canSurvive(level, pos)) continue;
                    level.setBlock(pos, prop.state(), Block.UPDATE_CLIENTS);
                    if (prop.loot() != null) {
                        RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(prop.pos() ^ level.getSeed()), pos, prop.loot());
                    }
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean solidRender(BlockState state, WorldGenLevel level, BlockPos pos) {
        //? if >=26.1 {
        /*return state.isSolidRender();
        *///?} else {
        return state.isSolidRender(level, pos);
        //?}
    }
}
