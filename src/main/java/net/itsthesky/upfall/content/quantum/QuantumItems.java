package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.quantum.gas.BottledVeilGasItem;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
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

public final class QuantumItems {
    private QuantumItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VEIL_MASK_MATERIAL = ARMOR_MATERIALS.register("veil_mask", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }), 9, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(Items.IRON_INGOT),
            List.of(new ArmorMaterial.Layer(Upfall.id("veil_mask"))), 0.0F, 0.0F));

    public static final DeferredItem<ArmorItem> VEIL_MASK = add(ITEMS.registerItem("veil_mask",
            p -> new ArmorItem(VEIL_MASK_MATERIAL, ArmorItem.Type.HELMET, p), new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15))));
    public static final DeferredItem<SmithingTemplateItem> VEIL_MASK_BLUEPRINT = blueprint("veil_mask");
    public static final DeferredItem<Item> FLICKER_SHARD = add(ITEMS.registerSimpleItem("flicker_shard", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> QUANTUM_STONE = add(ITEMS.registerSimpleItem("quantum_stone", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<BottledVeilGasItem> BOTTLED_VEIL_GAS = add(ITEMS.registerItem("bottled_veil_gas", BottledVeilGasItem::new,
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    public static final DeferredItem<SmithingTemplateItem> QUANTUM_PEDESTAL_BLUEPRINT = blueprint("quantum_pedestal");

    public static List<DeferredItem<SmithingTemplateItem>> blueprints() {
        return List.of(VEIL_MASK_BLUEPRINT, QUANTUM_PEDESTAL_BLUEPRINT);
    }

    private static DeferredItem<SmithingTemplateItem> blueprint(String target) {
        String name = target + "_blueprint";
        return add(ITEMS.register(name, UpfallSmithingTemplates.create(name, List.of(), MachinesItems.GEAR_SLOT_ICONS)));
    }

    private static <I extends Item> DeferredItem<I> add(DeferredItem<I> item) {
        CREATIVE_ORDER.add(item);
        return item;
    }
}
