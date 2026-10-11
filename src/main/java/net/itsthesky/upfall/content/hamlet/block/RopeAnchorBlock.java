package net.itsthesky.upfall.content.hamlet.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class RopeAnchorBlock extends AbstractRopeAnchorBlock {
    public static final MapCodec<RopeAnchorBlock> CODEC = simpleCodec(RopeAnchorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.UP, Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0));
        SHAPES.put(Direction.DOWN, Block.box(5.0, 6.0, 5.0, 11.0, 16.0, 11.0));
        SHAPES.put(Direction.NORTH, Block.box(5.0, 5.0, 6.0, 11.0, 11.0, 16.0));
        SHAPES.put(Direction.SOUTH, Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 10.0));
        SHAPES.put(Direction.WEST, Block.box(6.0, 5.0, 5.0, 16.0, 11.0, 11.0));
        SHAPES.put(Direction.EAST, Block.box(0.0, 5.0, 5.0, 10.0, 11.0, 11.0));
    }

    public RopeAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public Vec3 attachPoint(BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        return Vec3.atCenterOf(pos).add(facing.getStepX() * 0.25, facing.getStepY() * 0.25, facing.getStepZ() * 0.25);
    }

    @Override
    public int maxLinks() {
        return 4;
    }

    @Override
    public boolean isZipline() {
        return false;
    }

    @Override
    public double maxLength() {
        return 32.0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return Block.canSupportCenter(level, support, facing) || level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
