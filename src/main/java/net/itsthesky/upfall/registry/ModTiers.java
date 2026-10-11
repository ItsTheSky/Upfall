package net.itsthesky.upfall.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class ModTiers {
    private ModTiers() {}

    public static final Tier NIMBUSITE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_NIMBUSITE_TOOL,
            2400, 10.0F, 5.0F, 16, () -> Ingredient.of(ModItems.NIMBUSITE_INGOT.get()));
    public static final Tier LEVITITE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_LEVITITE_TOOL,
            3000, 11.0F, 6.0F, 18, () -> Ingredient.of(ModItems.LEVITITE_INGOT.get()));
}
