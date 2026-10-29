package net.itsthesky.upfall.content.hamlet.item;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
//? if <26.1 {
import net.minecraft.world.InteractionResultHolder;
//?}

public class ZiplineHandleItem extends Item {
    public ZiplineHandleItem(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(latch(level, player, hand), level);
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(latch(level, player, hand), level);
    }
    //?}

    private UseResults.Held latch(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isPassenger()) {
            return UseResults.Held.pass(stack);
        }
        if (!level.isClientSide()) {
            ZiplinePulley.Result result = ZiplinePulley.latchNearest(player);
            if (result != ZiplinePulley.Result.STARTED) {
                McPlayers.overlay(player, Component.translatable(result.message()));
                return UseResults.Held.fail(stack);
            }
        }
        return UseResults.Held.sidedSuccess(stack);
    }

    //? if >=26.1 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        hoverText(tooltip);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(tooltip::add);
    }
    //?}

    private static void hoverText(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("item.upfall.zipline_handle.desc").withStyle(ChatFormatting.GRAY));
    }
}
