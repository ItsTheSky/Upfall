package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McGear;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.quantum.gas.BottledVeilGasItem;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
//? if <26.1 {
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
//?}

public final class QuantumItems {
    private QuantumItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    //? if <26.1 {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Upfall.MODID);
    //?}
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    //? if >=26.1 {
    /*public static final net.minecraft.world.item.equipment.ArmorMaterial VEIL_MASK_MATERIAL = new net.minecraft.world.item.equipment.ArmorMaterial(
            15, Util.make(new EnumMap<>(net.minecraft.world.item.equipment.ArmorType.class), map -> {
                map.put(net.minecraft.world.item.equipment.ArmorType.BOOTS, 1);
                map.put(net.minecraft.world.item.equipment.ArmorType.LEGGINGS, 4);
                map.put(net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, 5);
                map.put(net.minecraft.world.item.equipment.ArmorType.HELMET, 2);
                map.put(net.minecraft.world.item.equipment.ArmorType.BODY, 5);
            }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR,
            net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, Upfall.id("veil_mask")));
    *///?} else {
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VEIL_MASK_MATERIAL = ARMOR_MATERIALS.register("veil_mask", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }), 9, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(Items.IRON_INGOT),
            List.of(new ArmorMaterial.Layer(Upfall.id("veil_mask"))), 0.0F, 0.0F));
    //?}

    public static final DeferredItem<Item> VEIL_MASK = add(McRegistration.item(ITEMS, "veil_mask",
            p -> McGear.armor(VEIL_MASK_MATERIAL, McGear.Piece.HELMET, 15, p), new Item.Properties()));
    public static final DeferredItem<SmithingTemplateItem> VEIL_MASK_BLUEPRINT = blueprint("veil_mask");
    public static final DeferredItem<Item> FLICKER_SHARD = add(McRegistration.simpleItem(ITEMS, "flicker_shard", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> QUANTUM_STONE = add(McRegistration.simpleItem(ITEMS, "quantum_stone", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<BottledVeilGasItem> BOTTLED_VEIL_GAS = add(McRegistration.item(ITEMS, "bottled_veil_gas", BottledVeilGasItem::new,
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    public static final DeferredItem<SmithingTemplateItem> QUANTUM_PEDESTAL_BLUEPRINT = blueprint("quantum_pedestal");

    public static List<DeferredItem<SmithingTemplateItem>> blueprints() {
        return List.of(VEIL_MASK_BLUEPRINT, QUANTUM_PEDESTAL_BLUEPRINT);
    }

    private static DeferredItem<SmithingTemplateItem> blueprint(String target) {
        String name = target + "_blueprint";
        return add(McRegistration.item(ITEMS, name, UpfallSmithingTemplates.factory(name, List.of(), MachinesItems.GEAR_SLOT_ICONS), new Item.Properties()));
    }

    private static <I extends Item> DeferredItem<I> add(DeferredItem<I> item) {
        CREATIVE_ORDER.add(item);
        return item;
    }
}
