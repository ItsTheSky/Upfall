package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final ArmorMaterial NIMBUSITE = new ArmorMaterial(ModGearStats.NIMBUSITE_DURABILITY_FACTOR,
            defense(ModGearStats.NIMBUSITE_DEFENSE), ModGearStats.NIMBUSITE_ARMOR_ENCHANTABILITY, ModSounds.ARMOR_EQUIP_NIMBUSITE,
            ModGearStats.NIMBUSITE_TOUGHNESS, ModGearStats.NIMBUSITE_KNOCKBACK_RESISTANCE,
            ModTags.Items.REPAIRS_NIMBUSITE, asset("nimbusite"));

    public static final ArmorMaterial LEVITITE = new ArmorMaterial(ModGearStats.LEVITITE_DURABILITY_FACTOR,
            defense(ModGearStats.LEVITITE_DEFENSE), ModGearStats.LEVITITE_ARMOR_ENCHANTABILITY, ModSounds.ARMOR_EQUIP_LEVITITE,
            ModGearStats.LEVITITE_TOUGHNESS, ModGearStats.LEVITITE_KNOCKBACK_RESISTANCE,
            ModTags.Items.REPAIRS_LEVITITE, asset("levitite"));

    public static void register(IEventBus bus) {}

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Upfall.id(name));
    }

    private static EnumMap<ArmorType, Integer> defense(List<Integer> values) {
        return Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.BOOTS, values.get(0));
            map.put(ArmorType.LEGGINGS, values.get(1));
            map.put(ArmorType.CHESTPLATE, values.get(2));
            map.put(ArmorType.HELMET, values.get(3));
            map.put(ArmorType.BODY, values.get(4));
        });
    }
}
