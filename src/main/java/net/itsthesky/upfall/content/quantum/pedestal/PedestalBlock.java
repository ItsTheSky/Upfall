package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.TooltipSource;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
//? if <26.1 {
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import java.util.List;
//?}

public class PedestalBlock extends BaseEntityBlock implements TooltipSource {
    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(3, 3, 3, 13, 13, 13), Block.box(1, 13, 1, 15, 16, 15));

    public PedestalBlock(Properties properties) {
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
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public void hoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }

    //? if <26.1 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip::add, flag);
    }
    //?}

    protected boolean locked(Level level, BlockPos pos) {
        return false;
    }

    //? if >=26.1 {
    /*@Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(place(stack, level, pos, player), level);
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(place(stack, level, pos, player), level);
    }
    //?}

    private UseResults.Outcome place(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
            return UseResults.Outcome.PASS_TO_DEFAULT;
        }
        if (locked(level, pos)) return UseResults.Outcome.CONSUME;
        if (!pedestal.item().isEmpty()) return UseResults.Outcome.SKIP_DEFAULT;
        if (!level.isClientSide()) {
            pedestal.setItem(stack.copyWithCount(1));
            stack.consume(1, player);
            level.playSound(null, pos, QuantumSounds.PEDESTAL_PLACE.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return UseResults.Outcome.SIDED_SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) || pedestal.item().isEmpty()) return InteractionResult.PASS;
        if (locked(level, pos)) return InteractionResult.CONSUME;
        if (!level.isClientSide()) {
            ItemStack taken = pedestal.takeItem();
            if (!player.addItem(taken)) player.drop(taken, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.2F);
        }
        return UseResults.sided(level);
    }

    //? if <26.1 {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal && !pedestal.item().isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, pedestal.item());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    //?}
}
