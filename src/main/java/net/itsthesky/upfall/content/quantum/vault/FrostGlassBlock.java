package net.itsthesky.upfall.content.quantum.vault;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.itsthesky.upfall.content.quantum.observe.ObservationBlocker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class FrostGlassBlock extends TransparentBlock implements ObservationBlocker {
    public static final MapCodec<FrostGlassBlock> CODEC = simpleCodec(FrostGlassBlock::new);
    public static final BooleanProperty FROSTED = BooleanProperty.create("frosted");

    public FrostGlassBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FROSTED, false));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FROSTED);
    }

    @Override
    public boolean blocksObservation(BlockState state) {
        return state.getValue(FROSTED);
    }

    public static void frost(ServerLevel level, BlockPos pos, BlockState state, int ticks) {
        if (!state.getValue(FROSTED)) {
            level.setBlock(pos, state.setValue(FROSTED, true), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, QuantumSounds.FROST.get(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        level.scheduleTick(pos, state.getBlock(), ticks);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(FROSTED)) {
            level.setBlock(pos, state.setValue(FROSTED, false), Block.UPDATE_CLIENTS);
        }
    }
}
