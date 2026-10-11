package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Upfall.MODID);

    public static final int NIMBUSITE_DURABILITY_FACTOR = 44;
    public static final int LEVITITE_DURABILITY_FACTOR = 55;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIMBUSITE = ARMOR_MATERIALS.register("nimbusite", () -> new ArmorMaterial(
            defense(3, 7, 8, 3, 11), 16, ModSounds.ARMOR_EQUIP_NIMBUSITE,
            () -> Ingredient.of(ModItems.NIMBUSITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(Upfall.id("nimbusite"))), 3.5F, 0.125F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LEVITITE = ARMOR_MATERIALS.register("levitite", () -> new ArmorMaterial(
            defense(4, 7, 9, 4, 13), 18, ModSounds.ARMOR_EQUIP_LEVITITE,
            () -> Ingredient.of(ModItems.LEVITITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(Upfall.id("levitite"))), 4.0F, 0.15F));

    private static EnumMap<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet, int body) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, boots);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.BODY, body);
        });
    }
}
