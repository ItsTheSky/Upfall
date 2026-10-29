package net.itsthesky.upfall.compat.mc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

public interface TooltipSource {
    void hoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> out, TooltipFlag flag);
}
