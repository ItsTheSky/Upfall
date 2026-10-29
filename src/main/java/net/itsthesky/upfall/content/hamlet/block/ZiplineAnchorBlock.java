package net.itsthesky.upfall.content.hamlet.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.hamlet.HamletItems;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
//? if <26.1 {
import net.minecraft.world.ItemInteractionResult;
//?}

public class ZiplineAnchorBlock extends AbstractRopeAnchorBlock {
    public static final MapCodec<ZiplineAnchorBlock> CODEC = simpleCodec(ZiplineAnchorBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(5.0, 0.0, 5.0, 11.0, 9.0, 11.0), Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0));

    public ZiplineAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        return SHAPE;
    }

    @Override
    public Vec3 attachPoint(BlockPos pos, BlockState state) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.8125, pos.getZ() + 0.5);
    }

    @Override
    public int maxLinks() {
        return 1;
    }

    @Override
    public boolean isZipline() {
        return true;
    }

    @Override
    public double maxLength() {
        return 64.0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    //? if >=26.1 {
    /*@Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        return shapeAfterUpdate(state, direction, level, pos);
    }
    *///?} else {
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return shapeAfterUpdate(state, direction, level, pos);
    }
    //?}

    private static BlockState shapeAfterUpdate(BlockState state, Direction direction, LevelReader level, BlockPos pos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    //? if >=26.1 {
    /*@Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(interact(stack, level, pos, player), level);
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(interact(stack, level, pos, player), level);
    }
    //?}

    private UseResults.Outcome interact(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.is(HamletItems.ZIPLINE_HANDLE.get())) {
            return stack.isEmpty() ? UseResults.Outcome.PASS_TO_DEFAULT : UseResults.Outcome.SKIP_DEFAULT;
        }
        if (level.isClientSide()) {
            return UseResults.Outcome.SUCCESS;
        }
        ZiplinePulley.Result result = ZiplinePulley.rideFromAnchor(player, pos);
        if (result != ZiplinePulley.Result.STARTED) {
            McPlayers.overlay(player, Component.translatable(result.message()));
        }
        return UseResults.Outcome.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            McPlayers.overlay(player, Component.translatable("message.upfall.zipline.needs_handle"));
        }
        return UseResults.sided(level);
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
