package net.itsthesky.upfall.compat.mc;

import net.minecraft.world.item.Item;
//? if <26.1 {
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
//?}

public final class McGear {
    private McGear() {}

    public enum Piece {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }

    //? if >=26.1 {
    /*public static float attackBonus(net.minecraft.world.item.ToolMaterial tier) {
        return tier.attackDamageBonus();
    }

    public static Item sword(net.minecraft.world.item.ToolMaterial tier, int damage, float speed, Item.Properties p) {
        return new Item(p.sword(tier, damage, speed));
    }

    public static Item pickaxe(net.minecraft.world.item.ToolMaterial tier, float damage, float speed, Item.Properties p) {
        return new Item(p.pickaxe(tier, damage, speed));
    }

    public static Item axe(net.minecraft.world.item.ToolMaterial tier, float damage, float speed, Item.Properties p) {
        return new net.minecraft.world.item.AxeItem(tier, damage, speed, p);
    }

    public static Item shovel(net.minecraft.world.item.ToolMaterial tier, float damage, float speed, Item.Properties p) {
        return new net.minecraft.world.item.ShovelItem(tier, damage, speed, p);
    }

    public static Item hoe(net.minecraft.world.item.ToolMaterial tier, float damage, float speed, Item.Properties p) {
        return new net.minecraft.world.item.HoeItem(tier, damage, speed, p);
    }

    public static Item armor(net.minecraft.world.item.equipment.ArmorMaterial material, Piece piece, Item.Properties p) {
        return new Item(p.humanoidArmor(material, armorType(piece)));
    }

    public static Item armor(net.minecraft.world.item.equipment.ArmorMaterial material, Piece piece, int durabilityFactor, Item.Properties p) {
        return armor(material, piece, p);
    }

    public static int armorDurability(Piece piece, int durabilityFactor) {
        return armorType(piece).getDurability(durabilityFactor);
    }

    private static net.minecraft.world.item.equipment.ArmorType armorType(Piece piece) {
        return switch (piece) {
            case HELMET -> net.minecraft.world.item.equipment.ArmorType.HELMET;
            case CHESTPLATE -> net.minecraft.world.item.equipment.ArmorType.CHESTPLATE;
            case LEGGINGS -> net.minecraft.world.item.equipment.ArmorType.LEGGINGS;
            case BOOTS -> net.minecraft.world.item.equipment.ArmorType.BOOTS;
        };
    }
    *///?} else {
    public static float attackBonus(Tier tier) {
        return tier.getAttackDamageBonus();
    }

    public static Item sword(Tier tier, int damage, float speed, Item.Properties p) {
        return new SwordItem(tier, p.attributes(SwordItem.createAttributes(tier, damage, speed)));
    }

    public static Item pickaxe(Tier tier, float damage, float speed, Item.Properties p) {
        return new PickaxeItem(tier, p.attributes(PickaxeItem.createAttributes(tier, damage, speed)));
    }

    public static Item axe(Tier tier, float damage, float speed, Item.Properties p) {
        return new AxeItem(tier, p.attributes(AxeItem.createAttributes(tier, damage, speed)));
    }

    public static Item shovel(Tier tier, float damage, float speed, Item.Properties p) {
        return new ShovelItem(tier, p.attributes(ShovelItem.createAttributes(tier, damage, speed)));
    }

    public static Item hoe(Tier tier, float damage, float speed, Item.Properties p) {
        return new HoeItem(tier, p.attributes(HoeItem.createAttributes(tier, damage, speed)));
    }

    public static Item armor(Holder<ArmorMaterial> material, Piece piece, Item.Properties p) {
        return new ArmorItem(material, armorType(piece), p);
    }

    public static Item armor(Holder<ArmorMaterial> material, Piece piece, int durabilityFactor, Item.Properties p) {
        return armor(material, piece, p.durability(armorType(piece).getDurability(durabilityFactor)));
    }

    public static int armorDurability(Piece piece, int durabilityFactor) {
        return armorType(piece).getDurability(durabilityFactor);
    }

    private static ArmorItem.Type armorType(Piece piece) {
        return switch (piece) {
            case HELMET -> ArmorItem.Type.HELMET;
            case CHESTPLATE -> ArmorItem.Type.CHESTPLATE;
            case LEGGINGS -> ArmorItem.Type.LEGGINGS;
            case BOOTS -> ArmorItem.Type.BOOTS;
        };
    }
    //?}
}
