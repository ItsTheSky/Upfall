package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class PhaseLanternBlock extends PhaseTrackingBlock {
    public PhaseLanternBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PHASE, ChronoPhase.DAWN));
    }

    @Override
    public BlockState stateAt(BlockState state, long time) {
        return state.setValue(PHASE, ChronoClock.phaseAt(time));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }
}
