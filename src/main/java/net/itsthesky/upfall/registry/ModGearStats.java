package net.itsthesky.upfall.registry;

import java.util.List;

public final class ModGearStats {
    private ModGearStats() {}

    public static final int NIMBUSITE_TOOL_DURABILITY = 2400;
    public static final float NIMBUSITE_TOOL_SPEED = 10.0F;
    public static final float NIMBUSITE_TOOL_DAMAGE = 5.0F;
    public static final int NIMBUSITE_TOOL_ENCHANTABILITY = 16;

    public static final int LEVITITE_TOOL_DURABILITY = 3000;
    public static final float LEVITITE_TOOL_SPEED = 11.0F;
    public static final float LEVITITE_TOOL_DAMAGE = 6.0F;
    public static final int LEVITITE_TOOL_ENCHANTABILITY = 18;

    public static final int NIMBUSITE_DURABILITY_FACTOR = 44;
    public static final List<Integer> NIMBUSITE_DEFENSE = List.of(3, 7, 8, 3, 11);
    public static final int NIMBUSITE_ARMOR_ENCHANTABILITY = 16;
    public static final float NIMBUSITE_TOUGHNESS = 3.5F;
    public static final float NIMBUSITE_KNOCKBACK_RESISTANCE = 0.125F;

    public static final int LEVITITE_DURABILITY_FACTOR = 55;
    public static final List<Integer> LEVITITE_DEFENSE = List.of(4, 7, 9, 4, 13);
    public static final int LEVITITE_ARMOR_ENCHANTABILITY = 18;
    public static final float LEVITITE_TOUGHNESS = 4.0F;
    public static final float LEVITITE_KNOCKBACK_RESISTANCE = 0.15F;
}
