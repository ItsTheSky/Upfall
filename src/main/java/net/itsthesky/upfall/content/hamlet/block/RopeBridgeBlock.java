package net.itsthesky.upfall.content.hamlet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RopeBridgeBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final int THICKNESS = 3;
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (int i = 0; i < 4; i++) {
            SHAPES[i] = Block.box(0.0, i * 4, 0.0, 16.0, i * 4 + THICKNESS, 16.0);
        }
    }

    public RopeBridgeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LEVEL)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        double inBlock = context.getClickLocation().y - context.getClickedPos().getY();
        int level = context.getClickedFace() == Direction.UP ? 0 : Mth.clamp((int) (inBlock * 4.0), 0, 3);
        return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()).setValue(LEVEL, level);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
        }
        return state;
    }

    public BlockState deckAt(Direction.Axis axis, double surfaceY, int blockY) {
        int quarter = (int) Math.round((surfaceY - THICKNESS / 16.0 - blockY) * 4.0);
        return defaultBlockState().setValue(AXIS, axis).setValue(LEVEL, Mth.clamp(quarter, 0, 3));
    }

    public static int blockYFor(double surfaceY) {
        int quarter = (int) Math.round((surfaceY - THICKNESS / 16.0) * 4.0);
        return Math.floorDiv(quarter, 4);
    }
}
