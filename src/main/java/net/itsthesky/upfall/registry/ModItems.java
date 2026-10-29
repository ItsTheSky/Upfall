package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McGear;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.item.SkywardKeyItem;
import net.itsthesky.upfall.item.UpdraftCompassItem;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
//? if >=26.1 {
/*import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
*///?}
//? if <26.1 {
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
//?}

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredItem<Item> RAW_NIMBUSITE = simple("raw_nimbusite", new Item.Properties().fireResistant());
    public static final DeferredItem<Item> NIMBUSITE_INGOT = simple("nimbusite_ingot", new Item.Properties().fireResistant());
    public static final DeferredItem<Item> LEVITITE_SHARD = simple("levitite_shard", new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LEVITITE_INGOT = simple("levitite_ingot", new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<SmithingTemplateItem> NIMBUSITE_UPGRADE_SMITHING_TEMPLATE = register(
            "nimbusite_upgrade_smithing_template", UpfallSmithingTemplates.factory("nimbusite_upgrade"));
    public static final DeferredItem<SmithingTemplateItem> LEVITITE_UPGRADE_SMITHING_TEMPLATE = register(
            "levitite_upgrade_smithing_template", UpfallSmithingTemplates.factory("levitite_upgrade"));

    public static final DeferredItem<Item> NIMBUSITE_SWORD = sword("nimbusite_sword", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_PICKAXE = pickaxe("nimbusite_pickaxe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_AXE = axe("nimbusite_axe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_SHOVEL = shovel("nimbusite_shovel", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_HOE = hoe("nimbusite_hoe", ModTiers.NIMBUSITE, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_HELMET = armor("nimbusite_helmet", ModArmorMaterials.NIMBUSITE, McGear.Piece.HELMET, ModGearStats.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_CHESTPLATE = armor("nimbusite_chestplate", ModArmorMaterials.NIMBUSITE, McGear.Piece.CHESTPLATE, ModGearStats.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_LEGGINGS = armor("nimbusite_leggings", ModArmorMaterials.NIMBUSITE, McGear.Piece.LEGGINGS, ModGearStats.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);
    public static final DeferredItem<Item> NIMBUSITE_BOOTS = armor("nimbusite_boots", ModArmorMaterials.NIMBUSITE, McGear.Piece.BOOTS, ModGearStats.NIMBUSITE_DURABILITY_FACTOR, Rarity.COMMON);

    public static final DeferredItem<Item> LEVITITE_SWORD = sword("levitite_sword", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_PICKAXE = pickaxe("levitite_pickaxe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_AXE = axe("levitite_axe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_SHOVEL = shovel("levitite_shovel", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_HOE = hoe("levitite_hoe", ModTiers.LEVITITE, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_HELMET = armor("levitite_helmet", ModArmorMaterials.LEVITITE, McGear.Piece.HELMET, ModGearStats.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_CHESTPLATE = armor("levitite_chestplate", ModArmorMaterials.LEVITITE, McGear.Piece.CHESTPLATE, ModGearStats.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_LEGGINGS = armor("levitite_leggings", ModArmorMaterials.LEVITITE, McGear.Piece.LEGGINGS, ModGearStats.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);
    public static final DeferredItem<Item> LEVITITE_BOOTS = armor("levitite_boots", ModArmorMaterials.LEVITITE, McGear.Piece.BOOTS, ModGearStats.LEVITITE_DURABILITY_FACTOR, Rarity.UNCOMMON);

    public static final DeferredItem<Item> GILDFRUIT = simple("gildfruit", new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F)
            //? if >=26.1 {
            /*.build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, 160), 0.35F)).build()));
            *///?} else {
            .effect(() -> new MobEffectInstance(ModEffects.WEIGHTLESS, 160), 0.35F).build()));
            //?}
    public static final DeferredItem<Item> CLOUDBERRY = simple("cloudberry", new Item.Properties().food(new FoodProperties.Builder()
            //? if >=26.1 {
            /*.nutrition(2).saturationModifier(0.3F).build(), Consumables.defaultFood().consumeSeconds(0.8F).build()));
            *///?} else {
            .nutrition(2).saturationModifier(0.3F).fast().build()));
            //?}
    public static final DeferredItem<Item> CLOUDBERRY_TART = simple("cloudberry_tart", new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.7F).build()));

    public static final DeferredItem<SkywardKeyItem> SKYWARD_KEY = register("skyward_key", SkywardKeyItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<UpdraftCompassItem> UPDRAFT_COMPASS = register("updraft_compass", UpdraftCompassItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BucketItem> SKYWATER_BUCKET = register("skywater_bucket",
            props -> new BucketItem(ModFluids.SKYWATER.get(), props), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    public static final DeferredItem<Item> SKYRAY_SPAWN_EGG = spawnEgg("skyray_spawn_egg", ModEntities.SKYRAY, 0xA9C8E8, 0xE2B85A);

    private static DeferredItem<Item> simple(String name, Item.Properties props) {
        DeferredItem<Item> item = McRegistration.simpleItem(ITEMS, name, props);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, I> factory) {
        return register(name, factory, new Item.Properties());
    }

    private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, I> factory, Item.Properties props) {
        DeferredItem<I> item = McRegistration.item(ITEMS, name, factory, props);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static DeferredItem<Item> spawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight) {
        DeferredItem<Item> item = McRegistration.spawnEgg(ITEMS, name, type, background, highlight);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static Item.Properties gear(Rarity rarity) {
        return new Item.Properties().fireResistant().rarity(rarity);
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> sword(String name, net.minecraft.world.item.ToolMaterial tier, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> sword(String name, Tier tier, Rarity rarity) {
    //?}
        return register(name, p -> McGear.sword(tier, 3, -2.4F, p), gear(rarity));
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> pickaxe(String name, net.minecraft.world.item.ToolMaterial tier, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> pickaxe(String name, Tier tier, Rarity rarity) {
    //?}
        return register(name, p -> McGear.pickaxe(tier, 1.0F, -2.8F, p), gear(rarity));
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> axe(String name, net.minecraft.world.item.ToolMaterial tier, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> axe(String name, Tier tier, Rarity rarity) {
    //?}
        return register(name, p -> McGear.axe(tier, 5.0F, -3.0F, p), gear(rarity));
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> shovel(String name, net.minecraft.world.item.ToolMaterial tier, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> shovel(String name, Tier tier, Rarity rarity) {
    //?}
        return register(name, p -> McGear.shovel(tier, 1.5F, -3.0F, p), gear(rarity));
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> hoe(String name, net.minecraft.world.item.ToolMaterial tier, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> hoe(String name, Tier tier, Rarity rarity) {
    //?}
        float bonus = McGear.attackBonus(tier);
        return register(name, p -> McGear.hoe(tier, -bonus, 0.0F, p), gear(rarity));
    }

    //? if >=26.1 {
    /*private static DeferredItem<Item> armor(String name, net.minecraft.world.item.equipment.ArmorMaterial material, McGear.Piece piece, int durabilityFactor, Rarity rarity) {
    *///?} else {
    private static DeferredItem<Item> armor(String name, Holder<ArmorMaterial> material, McGear.Piece piece, int durabilityFactor, Rarity rarity) {
    //?}
        return register(name, p -> McGear.armor(material, piece, durabilityFactor, p), gear(rarity));
    }
}
