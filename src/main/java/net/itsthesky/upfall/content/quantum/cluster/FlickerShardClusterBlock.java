package net.itsthesky.upfall.content.quantum.cluster;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FlickerShardClusterBlock extends AmethystClusterBlock implements EntityBlock {
    public static final MapCodec<FlickerShardClusterBlock> CODEC_FLICKER = simpleCodec(FlickerShardClusterBlock::new);

    public FlickerShardClusterBlock(Properties properties) {
        //? if >=26.1 {
        /*super(7.0F, 10.0F, properties);
        *///?} else {
        super(7.0F, 3.0F, properties);
        //?}
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<AmethystClusterBlock> codec() {
        return (MapCodec<AmethystClusterBlock>) (MapCodec<?>) CODEC_FLICKER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlickerShardClusterBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != QuantumBlocks.FLICKER_SHARD_CLUSTER_ENTITY.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<FlickerShardClusterBlockEntity>) FlickerShardClusterBlockEntity::serverTick;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            level.addParticle(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + 0.2 + random.nextDouble() * 0.6, pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.0, 0.0);
        }
    }
}
