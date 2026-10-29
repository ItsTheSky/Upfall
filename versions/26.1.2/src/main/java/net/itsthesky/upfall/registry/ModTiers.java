package net.itsthesky.upfall.registry;

import net.minecraft.world.item.ToolMaterial;

public final class ModTiers {
    private ModTiers() {}

    public static final ToolMaterial NIMBUSITE = new ToolMaterial(ModTags.Blocks.INCORRECT_FOR_NIMBUSITE_TOOL,
            ModGearStats.NIMBUSITE_TOOL_DURABILITY, ModGearStats.NIMBUSITE_TOOL_SPEED, ModGearStats.NIMBUSITE_TOOL_DAMAGE,
            ModGearStats.NIMBUSITE_TOOL_ENCHANTABILITY, ModTags.Items.REPAIRS_NIMBUSITE);
    public static final ToolMaterial LEVITITE = new ToolMaterial(ModTags.Blocks.INCORRECT_FOR_LEVITITE_TOOL,
            ModGearStats.LEVITITE_TOOL_DURABILITY, ModGearStats.LEVITITE_TOOL_SPEED, ModGearStats.LEVITITE_TOOL_DAMAGE,
            ModGearStats.LEVITITE_TOOL_ENCHANTABILITY, ModTags.Items.REPAIRS_LEVITITE);
}
