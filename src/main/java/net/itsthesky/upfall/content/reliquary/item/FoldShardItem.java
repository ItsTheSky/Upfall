package net.itsthesky.upfall.content.reliquary.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public class FoldShardItem extends Item {
    public FoldShardItem(Properties properties) {
        super(properties);
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
        tooltip.accept(Component.translatable("item.upfall.fold_shard.desc").withStyle(ChatFormatting.GRAY));
    }
}
