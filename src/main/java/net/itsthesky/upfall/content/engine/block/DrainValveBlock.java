package net.itsthesky.upfall.content.engine.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.engine.EngineSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class DrainValveBlock extends DirectionalBlock {
    public static final MapCodec<DrainValveBlock> CODEC = simpleCodec(DrainValveBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.UP, Shapes.or(Block.box(6, 0, 6, 10, 6, 10), Block.box(2, 6, 2, 14, 8, 14)));
        SHAPES.put(Direction.DOWN, Shapes.or(Block.box(6, 10, 6, 10, 16, 10), Block.box(2, 8, 2, 14, 10, 14)));
        SHAPES.put(Direction.NORTH, Shapes.or(Block.box(6, 6, 10, 10, 10, 16), Block.box(2, 2, 8, 14, 14, 10)));
        SHAPES.put(Direction.SOUTH, Shapes.or(Block.box(6, 6, 0, 10, 10, 6), Block.box(2, 2, 6, 14, 14, 8)));
        SHAPES.put(Direction.WEST, Shapes.or(Block.box(10, 6, 6, 16, 10, 10), Block.box(8, 2, 2, 10, 14, 14)));
        SHAPES.put(Direction.EAST, Shapes.or(Block.box(0, 6, 6, 6, 10, 10), Block.box(6, 2, 2, 8, 14, 14)));
    }

    public DrainValveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            turn(level, pos, state, player);
        }
        return UseResults.sided(level);
    }

    public static void turn(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        boolean open = !state.getValue(OPEN);
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        level.playSound(null, pos, EngineSounds.VALVE_TURN.get(), SoundSource.BLOCKS, 1.0F, open ? 0.9F : 0.7F);
        level.gameEvent(player, open ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    public static boolean isOpen(BlockState state) {
        return state.getBlock() instanceof DrainValveBlock && state.getValue(OPEN);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
