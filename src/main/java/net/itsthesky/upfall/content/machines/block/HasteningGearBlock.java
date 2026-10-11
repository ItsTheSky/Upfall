package net.itsthesky.upfall.content.machines.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.machines.KairiteFuel;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HasteningGearBlock extends BaseEntityBlock {
    public static final MapCodec<HasteningGearBlock> CODEC = simpleCodec(HasteningGearBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        SHAPES[Direction.UP.ordinal()] = box(0, 0, 0, 16, 6, 16);
        SHAPES[Direction.DOWN.ordinal()] = box(0, 10, 0, 16, 16, 16);
        SHAPES[Direction.NORTH.ordinal()] = box(0, 0, 10, 16, 16, 16);
        SHAPES[Direction.SOUTH.ordinal()] = box(0, 0, 0, 16, 16, 6);
        SHAPES[Direction.WEST.ordinal()] = box(10, 0, 0, 16, 16, 16);
        SHAPES[Direction.EAST.ordinal()] = box(0, 0, 0, 6, 16, 16);
    }

    public HasteningGearBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()];
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HasteningGearBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, MachineBlockEntities.HASTENING_GEAR.get(), HasteningGearBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (KairiteFuel.units(held) > 0 && level.getBlockEntity(pos) instanceof HasteningGearBlockEntity gear) {
            if (!level.isClientSide) {
                int used = gear.accept(held);
                if (used > 0) {
                    if (!player.getAbilities().instabuild) held.shrink(used);
                    level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.7F, 1.4F);
                } else {
                    showFuel(player, gear);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof HasteningGearBlockEntity gear) {
            if (!level.isClientSide) {
                showFuel(player, gear);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static void showFuel(Player player, HasteningGearBlockEntity gear) {
        player.displayClientMessage(Component.translatable("message.upfall.hastening_gear.fuel",
                gear.fuel() * 100 / HasteningGearBlockEntity.FUEL_CAPACITY), true);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HasteningGearBlockEntity gear) {
            int dust = gear.fuel() / (KairiteFuel.DUST_UNITS * HasteningGearBlockEntity.UNIT_TICKS);
            if (dust > 0) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(RustworksItems.KAIRITE_DUST.get(), dust));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof HasteningGearBlockEntity gear ? gear.signal() : 0;
    }
}
