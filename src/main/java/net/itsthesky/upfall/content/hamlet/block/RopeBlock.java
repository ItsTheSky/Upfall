package net.itsthesky.upfall.content.hamlet.block;

import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
//? if <26.1 {
import net.minecraft.world.ItemInteractionResult;
//?}

public class RopeBlock extends Block {
    public static final BooleanProperty END = BooleanProperty.create("end");
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape END_SHAPE = Block.box(5.5, 2.0, 5.5, 10.5, 16.0, 10.5);
    private static final int MAX_EXTEND = 64;

    public RopeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(END, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(END);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(END) ? END_SHAPE : SHAPE;
    }

    public static boolean isSupported(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() instanceof RopeBlock || aboveState.getBlock() instanceof AbstractRopeAnchorBlock
                || Block.canSupportCenter(level, above, Direction.DOWN)) {
            return true;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(side);
            if (level.getBlockState(neighbor).isFaceSturdy(level, neighbor, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(END, !(context.getLevel().getBlockState(context.getClickedPos().below()).getBlock() instanceof RopeBlock));
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    //? if >=26.1 {
    /*@Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        return shapeAfterUpdate(state, direction, neighborState, level, pos);
    }
    *///?} else {
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return shapeAfterUpdate(state, direction, neighborState, level, pos);
    }
    //?}

    private static BlockState shapeAfterUpdate(BlockState state, Direction direction, BlockState neighborState, LevelReader level, BlockPos pos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.DOWN) {
            return state.setValue(END, !(neighborState.getBlock() instanceof RopeBlock));
        }
        return state;
    }

    //? if >=26.1 {
    /*@Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                              InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(interact(stack, state, level, pos, player), level);
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(interact(stack, state, level, pos, player), level);
    }
    //?}

    private UseResults.Outcome interact(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (!stack.is(asItem())) {
            return UseResults.Outcome.PASS_TO_DEFAULT;
        }
        BlockPos.MutableBlockPos bottom = pos.mutable();
        for (int i = 0; i < MAX_EXTEND && level.getBlockState(bottom.below()).getBlock() instanceof RopeBlock; i++) {
            bottom.move(Direction.DOWN);
        }
        BlockPos target = bottom.below();
        if (!level.isInWorldBounds(target) || !level.getBlockState(target).canBeReplaced()) {
            return UseResults.Outcome.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(target, defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, target, getSoundType(state, level, target, player).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, target, HamletSounds.ROPE_TIE.get(), SoundSource.BLOCKS, 0.5F, 1.2F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, target);
            stack.consume(1, player);
        }
        return UseResults.Outcome.SIDED_SUCCESS;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
