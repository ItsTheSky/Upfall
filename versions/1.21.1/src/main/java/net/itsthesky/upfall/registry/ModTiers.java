package net.itsthesky.upfall.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class ModTiers {
    private ModTiers() {}

    public static final Tier NIMBUSITE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_NIMBUSITE_TOOL,
            ModGearStats.NIMBUSITE_TOOL_DURABILITY, ModGearStats.NIMBUSITE_TOOL_SPEED, ModGearStats.NIMBUSITE_TOOL_DAMAGE,
            ModGearStats.NIMBUSITE_TOOL_ENCHANTABILITY, () -> Ingredient.of(ModItems.NIMBUSITE_INGOT.get()));
    public static final Tier LEVITITE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_LEVITITE_TOOL,
            ModGearStats.LEVITITE_TOOL_DURABILITY, ModGearStats.LEVITITE_TOOL_SPEED, ModGearStats.LEVITITE_TOOL_DAMAGE,
            ModGearStats.LEVITITE_TOOL_ENCHANTABILITY, () -> Ingredient.of(ModItems.LEVITITE_INGOT.get()));
}
