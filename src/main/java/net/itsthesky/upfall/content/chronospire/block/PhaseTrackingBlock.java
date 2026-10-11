package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public abstract class PhaseTrackingBlock extends Block {
    public static final EnumProperty<ChronoPhase> PHASE = EnumProperty.create("phase", ChronoPhase.class);
    public static final int MAX_CHECK_INTERVAL = 60;

    protected PhaseTrackingBlock(Properties properties) {
        super(properties);
    }

    public abstract BlockState stateAt(BlockState state, long time);

    protected int ticksUntilChange(BlockState state, long time) {
        return ChronoClock.ticksUntilNextPhase(time);
    }

    public int nextCheckDelay(BlockState state, long time) {
        return Mth.clamp(ticksUntilChange(state, time), 1, MAX_CHECK_INTERVAL);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateAt(defaultBlockState(), ChronoClock.time(context.getLevel()));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long time = ChronoClock.time(level);
        BlockState target = stateAt(state, time);
        if (target != state) {
            level.setBlock(pos, target, Block.UPDATE_CLIENTS);
            onPhaseChange(target, level, pos);
        }
        level.scheduleTick(pos, this, nextCheckDelay(target, time));
    }

    protected void onPhaseChange(BlockState state, ServerLevel level, BlockPos pos) {
    }
}
