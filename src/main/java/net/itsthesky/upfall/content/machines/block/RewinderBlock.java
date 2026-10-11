package net.itsthesky.upfall.content.machines.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.machines.KairiteFuel;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RewinderBlock extends BaseEntityBlock {
    public static final MapCodec<RewinderBlock> CODEC = simpleCodec(RewinderBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(box(2, 0, 2, 14, 3, 14), box(5, 3, 5, 11, 11, 11), box(3, 11, 3, 13, 14, 13));

    public RewinderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RewinderBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, MachineBlockEntities.REWINDER.get(), RewinderBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RewinderBlockEntity rewinder)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (KairiteFuel.units(held) > 0) {
            if (!level.isClientSide) {
                int used = rewinder.accept(held);
                if (used > 0) {
                    if (!player.getAbilities().instabuild) held.shrink(used);
                    level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.7F, 1.4F);
                } else {
                    showFuel(player, rewinder);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (rewinder.canHold(held)) {
            if (!level.isClientSide) {
                rewinder.place(held);
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RewinderBlockEntity rewinder) {
            if (!level.isClientSide) {
                if (rewinder.stack().isEmpty()) {
                    showFuel(player, rewinder);
                } else {
                    player.getInventory().placeItemBackInInventory(rewinder.take());
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.9F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static void showFuel(Player player, RewinderBlockEntity rewinder) {
        player.displayClientMessage(Component.translatable("message.upfall.rewinder.fuel",
                rewinder.fuel() * 100 / RewinderBlockEntity.FUEL_CAPACITY), true);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RewinderBlockEntity rewinder) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rewinder.stack());
            int dust = rewinder.fuel() / (KairiteFuel.DUST_UNITS * RewinderBlockEntity.POINTS_PER_UNIT);
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
        return level.getBlockEntity(pos) instanceof RewinderBlockEntity rewinder ? rewinder.signal() : 0;
    }
}
