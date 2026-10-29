package net.itsthesky.upfall.content.guide;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
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

public class SkyAlmanacItem extends Item {
    public SkyAlmanacItem(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(openGuide(level, player, hand), level);
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(openGuide(level, player, hand), level);
    }
    //?}

    private UseResults.Held openGuide(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (GuideModule.isGuideMeLoaded()) {
                GuideCompat.open(player);
            } else {
                McPlayers.overlay(player, Component.translatable("message.upfall.sky_almanac.missing_guideme"));
            }
        }
        return UseResults.Held.sidedSuccess(player.getItemInHand(hand));
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
        tooltip.accept(Component.translatable("item.upfall.sky_almanac.desc").withStyle(ChatFormatting.GRAY));
    }
}
