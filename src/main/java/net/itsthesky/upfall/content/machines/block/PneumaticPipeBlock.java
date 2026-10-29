package net.itsthesky.upfall.content.machines.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.machines.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class PneumaticPipeBlock extends PipeBlock {
    public static final MapCodec<PneumaticPipeBlock> CODEC = simpleCodec(PneumaticPipeBlock::new);

    public PneumaticPipeBlock(Properties properties) {
        // 26.1.2 PipeBlock takes the full width in pixels, 1.21.1 the half-width in blocks
        //? if >=26.1 {
        /*super(12.0F, properties);
        *///?} else {
        super(0.375F, properties);
        //?}
        BlockState state = stateDefinition.any();
        for (Direction dir : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends PipeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    //? if >=26.1 {
    /*@Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks,
                                     BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighbor, net.minecraft.util.RandomSource random) {
        return withConnection(state, dir, neighbor);
    }
    *///?} else {
    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return withConnection(state, dir, neighbor);
    }
    //?}

    private static BlockState withConnection(BlockState state, Direction dir, BlockState neighbor) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), PipeNetwork.connected(state, neighbor, dir));
    }

    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), PipeNetwork.connected(state, level.getBlockState(pos.relative(dir)), dir));
        }
        return state;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    //? if >=26.1 {
    /*@Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
    *///?} else {
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    //?}
}
