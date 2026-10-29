package net.itsthesky.upfall.compat.mc;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ItemSink {
    boolean valid(ItemStack stack);

    int acceptable(ItemStack stack, List<ItemStack> pending);

    void accept(ItemStack stack);
}
