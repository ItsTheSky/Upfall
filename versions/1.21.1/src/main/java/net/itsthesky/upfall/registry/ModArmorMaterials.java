package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Upfall.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIMBUSITE = ARMOR_MATERIALS.register("nimbusite", () -> new ArmorMaterial(
            defense(ModGearStats.NIMBUSITE_DEFENSE), ModGearStats.NIMBUSITE_ARMOR_ENCHANTABILITY, ModSounds.ARMOR_EQUIP_NIMBUSITE,
            () -> Ingredient.of(ModItems.NIMBUSITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(Upfall.id("nimbusite"))), ModGearStats.NIMBUSITE_TOUGHNESS, ModGearStats.NIMBUSITE_KNOCKBACK_RESISTANCE));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LEVITITE = ARMOR_MATERIALS.register("levitite", () -> new ArmorMaterial(
            defense(ModGearStats.LEVITITE_DEFENSE), ModGearStats.LEVITITE_ARMOR_ENCHANTABILITY, ModSounds.ARMOR_EQUIP_LEVITITE,
            () -> Ingredient.of(ModItems.LEVITITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(Upfall.id("levitite"))), ModGearStats.LEVITITE_TOUGHNESS, ModGearStats.LEVITITE_KNOCKBACK_RESISTANCE));

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }

    private static EnumMap<ArmorItem.Type, Integer> defense(List<Integer> values) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, values.get(0));
            map.put(ArmorItem.Type.LEGGINGS, values.get(1));
            map.put(ArmorItem.Type.CHESTPLATE, values.get(2));
            map.put(ArmorItem.Type.HELMET, values.get(3));
            map.put(ArmorItem.Type.BODY, values.get(4));
        });
    }
}
