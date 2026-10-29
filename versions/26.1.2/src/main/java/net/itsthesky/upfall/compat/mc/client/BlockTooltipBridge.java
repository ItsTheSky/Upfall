package net.itsthesky.upfall.compat.mc.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.TooltipSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Upfall.MODID, value = Dist.CLIENT)
public final class BlockTooltipBridge {
    private BlockTooltipBridge() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TooltipSource source) {
            List<Component> lines = new ArrayList<>();
            source.hoverText(stack, event.getContext(), lines::add, event.getFlags());
            List<Component> tooltip = event.getToolTip();
            tooltip.addAll(Math.min(1, tooltip.size()), lines);
        }
    }
}
