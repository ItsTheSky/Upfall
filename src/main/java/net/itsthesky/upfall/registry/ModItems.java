package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.item.SkywardKeyItem;
import net.itsthesky.upfall.item.UpdraftCompassItem;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredItem<Item> RAW_NIMBUSITE = simple("raw_nimbusite", new Item.Properties().fireResistant());
    public static final DeferredItem<Item> NIMBUSITE_INGOT = simple("nimbusite_ingot", new Item.Properties().fireResistant());
    public static final DeferredItem<Item> LEVITITE_SHARD = simple("levitite_shard", new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LEVITITE_INGOT = simple("levitite_ingot", new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<SmithingTemplateItem> NIMBUSITE_UPGRADE_SMITHING_TEMPLATE = register(
            "nimbusite_upgrade_smithing_template", UpfallSmithingTemplates.create("nimbusite_upgrade"));
    public static final DeferredItem<SmithingTemplateItem> LEVITITE_UPGRADE_SMITHING_TEMPLATE = register(
            "levitite_upgrade_smithing_template", UpfallSmithingTemplates.create("levitite_upgrade"));

    public static final DeferredItem<SwordItem> NIMBUSITE_SWORD = sword("nimbusite_sword", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<PickaxeItem> NIMBUSITE_PICKAXE = pickaxe("nimbusite_pickaxe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<AxeItem> NIMBUSITE_AXE = axe("nimbusite_axe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<ShovelItem> NIMBUSITE_SHOVEL = shovel("nimbusite_shovel", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<HoeItem> NIMBUSITE_HOE = hoe("nimbusite_hoe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<ArmorItem> NIMBUSITE_HELMET = armor("nimbusite_helmet", ModArmorMaterials.NIMBUSITE, ArmorItem.Type.HELMET, ModArmorMaterials.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<ArmorItem> NIMBUSITE_CHESTPLATE = armor("nimbusite_chestplate", ModArmorMaterials.NIMBUSITE, ArmorItem.Type.CHESTPLATE, ModArmorMaterials.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<ArmorItem> NIMBUSITE_LEGGINGS = armor("nimbusite_leggings", ModArmorMaterials.NIMBUSITE, ArmorItem.Type.LEGGINGS, ModArmorMaterials.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<ArmorItem> NIMBUSITE_BOOTS = armor("nimbusite_boots", ModArmorMaterials.NIMBUSITE, ArmorItem.Type.BOOTS, ModArmorMaterials.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);

    public static final DeferredItem<SwordItem> LEVITITE_SWORD = sword("levitite_sword", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<PickaxeItem> LEVITITE_PICKAXE = pickaxe("levitite_pickaxe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<AxeItem> LEVITITE_AXE = axe("levitite_axe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<ShovelItem> LEVITITE_SHOVEL = shovel("levitite_shovel", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<HoeItem> LEVITITE_HOE = hoe("levitite_hoe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<ArmorItem> LEVITITE_HELMET = armor("levitite_helmet", ModArmorMaterials.LEVITITE, ArmorItem.Type.HELMET, ModArmorMaterials.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<ArmorItem> LEVITITE_CHESTPLATE = armor("levitite_chestplate", ModArmorMaterials.LEVITITE, ArmorItem.Type.CHESTPLATE, ModArmorMaterials.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<ArmorItem> LEVITITE_LEGGINGS = armor("levitite_leggings", ModArmorMaterials.LEVITITE, ArmorItem.Type.LEGGINGS, ModArmorMaterials.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<ArmorItem> LEVITITE_BOOTS = armor("levitite_boots", ModArmorMaterials.LEVITITE, ArmorItem.Type.BOOTS, ModArmorMaterials.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);

    public static final DeferredItem<Item> GILDFRUIT = simple("gildfruit", new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F)
            .effect(() -> new MobEffectInstance(ModEffects.WEIGHTLESS, 160), 0.35F).build()));
    public static final DeferredItem<Item> CLOUDBERRY = simple("cloudberry", new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).fast().build()));
    public static final DeferredItem<Item> CLOUDBERRY_TART = simple("cloudberry_tart", new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.7F).build()));

    public static final DeferredItem<SkywardKeyItem> SKYWARD_KEY = register("skyward_key", SkywardKeyItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<UpdraftCompassItem> UPDRAFT_COMPASS = register("updraft_compass", UpdraftCompassItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BucketItem> SKYWATER_BUCKET = register("skywater_bucket",
            props -> new BucketItem(ModFluids.SKYWATER.get(), props), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    public static final DeferredItem<DeferredSpawnEggItem> SKYRAY_SPAWN_EGG = register("skyray_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.SKYRAY, 0xA9C8E8, 0xE2B85A, props), new Item.Properties());

    private static DeferredItem<Item> simple(String name, Item.Properties props) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name, props);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static <I extends Item> DeferredItem<I> register(String name, java.util.function.Supplier<I> factory) {
        DeferredItem<I> item = ITEMS.register(name, factory);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static <I extends Item> DeferredItem<I> register(String name, java.util.function.Function<Item.Properties, I> factory, Item.Properties props) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory, props);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static Item.Properties gear(Rarity rarity) {
        return new Item.Properties().fireResistant().rarity(rarity);
    }

    private static DeferredItem<SwordItem> sword(String name, Tier tier, Rarity rarity) {
        return register(name, p -> new SwordItem(tier, p.attributes(SwordItem.createAttributes(tier, 3, -2.4F))), gear(rarity));
    }

    private static DeferredItem<PickaxeItem> pickaxe(String name, Tier tier, Rarity rarity) {
        return register(name, p -> new PickaxeItem(tier, p.attributes(PickaxeItem.createAttributes(tier, 1.0F, -2.8F))), gear(rarity));
    }

    private static DeferredItem<AxeItem> axe(String name, Tier tier, Rarity rarity) {
        return register(name, p -> new AxeItem(tier, p.attributes(AxeItem.createAttributes(tier, 5.0F, -3.0F))), gear(rarity));
    }

    private static DeferredItem<ShovelItem> shovel(String name, Tier tier, Rarity rarity) {
        return register(name, p -> new ShovelItem(tier, p.attributes(ShovelItem.createAttributes(tier, 1.5F, -3.0F))), gear(rarity));
    }

    private static DeferredItem<HoeItem> hoe(String name, Tier tier, Rarity rarity) {
        float bonus = tier.getAttackDamageBonus();
        return register(name, p -> new HoeItem(tier, p.attributes(HoeItem.createAttributes(tier, -bonus, 0.0F))), gear(rarity));
    }

    private static DeferredItem<ArmorItem> armor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor, Rarity rarity) {
        return register(name, p -> new ArmorItem(material, type, p.durability(type.getDurability(durabilityFactor))), gear(rarity));
    }
}
