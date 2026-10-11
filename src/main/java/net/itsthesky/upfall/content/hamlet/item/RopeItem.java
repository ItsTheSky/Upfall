package net.itsthesky.upfall.content.hamlet.item;

import net.itsthesky.upfall.content.hamlet.HamletItems;
import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RopeItem extends BlockItem {
    private static final double BLOCKS_PER_ROPE = 6.0;

    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof AbstractRopeAnchorBlock)) {
            return super.useOn(context);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos start = stack.get(HamletItems.ROPE_LINK_START.get());
        if (start == null || !(level.getBlockState(start).getBlock() instanceof AbstractRopeAnchorBlock)) {
            stack.set(HamletItems.ROPE_LINK_START.get(), pos.immutable());
            message(player, Component.translatable("message.upfall.rope.started"));
            level.playSound(null, pos, HamletSounds.ROPE_TIE.get(), SoundSource.BLOCKS, 0.6F, 1.4F);
            return InteractionResult.CONSUME;
        }
        if (start.equals(pos)) {
            stack.remove(HamletItems.ROPE_LINK_START.get());
            message(player, Component.translatable("message.upfall.rope.cancelled"));
            return InteractionResult.CONSUME;
        }
        int cost = Math.max(1, (int) Math.ceil(RopeLines.attachPoint(level, start).distanceTo(RopeLines.attachPoint(level, pos)) / BLOCKS_PER_ROPE));
        boolean creative = player != null && player.getAbilities().instabuild;
        if (!creative && stack.getCount() < cost) {
            message(player, Component.translatable("message.upfall.rope.not_enough", cost));
            return InteractionResult.FAIL;
        }
        RopeLines.LinkResult result = RopeLines.link(level, start, pos);
        if (result == RopeLines.LinkResult.LINKED) {
            stack.remove(HamletItems.ROPE_LINK_START.get());
            stack.consume(cost, player);
            level.playSound(null, pos, HamletSounds.ROPE_TIE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        message(player, Component.translatable(result.message()));
        return InteractionResult.CONSUME;
    }

    private static void message(Player player, Component text) {
        if (player != null) {
            player.displayClientMessage(text, true);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(HamletItems.ROPE_LINK_START.get()) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.rope.desc").withStyle(ChatFormatting.GRAY));
        BlockPos start = stack.get(HamletItems.ROPE_LINK_START.get());
        if (start != null) {
            tooltip.add(Component.translatable("item.upfall.rope.tied", start.getX(), start.getY(), start.getZ()).withStyle(ChatFormatting.GOLD));
        }
    }
}
