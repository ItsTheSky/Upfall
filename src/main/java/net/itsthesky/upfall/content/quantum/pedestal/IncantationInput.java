package net.itsthesky.upfall.content.quantum.pedestal;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record IncantationInput(ItemStack center, List<ItemStack> sides, List<ItemStack> corners) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index == 0) return center;
        return index <= sides.size() ? sides.get(index - 1) : corners.get(index - 1 - sides.size());
    }

    @Override
    public int size() {
        return 1 + sides.size() + corners.size();
    }
}
