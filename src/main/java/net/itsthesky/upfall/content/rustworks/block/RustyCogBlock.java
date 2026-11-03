package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
//? if >=26.1 {
/*import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
*///?}

/**
 * A rusty cog that spins (animated, glowing core) while it receives a redstone signal, and rests otherwise.
 */
public class RustyCogBlock extends RotatedPillarBlock {
    public static final MapCodec<RustyCogBlock> CODEC = simpleCodec(RustyCogBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RustyCogBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    public static int light(BlockState state) {
        return state.getValue(POWERED) ? 4 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    //? if >=26.1 {
    /*@Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        updatePower(state, level, pos);
    }
    *///?} else {
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        updatePower(state, level, pos);
    }
    //?}

    private static void updatePower(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }
}
