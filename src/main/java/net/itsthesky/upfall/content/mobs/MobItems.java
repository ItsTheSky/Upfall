package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.itsthesky.upfall.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class MobItems {
    private MobItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredItem<Item> DUSKWING_PLUME = simple("duskwing_plume", new Item.Properties());
    public static final DeferredItem<Item> CRAGMAW_FANG = simple("cragmaw_fang", new Item.Properties());
    public static final DeferredItem<Item> MIST_GEL = simple("mist_gel", new Item.Properties());
    public static final DeferredItem<Item> DOWN_TUFT = simple("down_tuft", new Item.Properties());
    public static final DeferredItem<Item> RAW_SKYGRAZER_MEAT = simple("raw_skygrazer_meat", new Item.Properties().food(
            new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build()));
    public static final DeferredItem<Item> COOKED_SKYGRAZER_MEAT = simple("cooked_skygrazer_meat", new Item.Properties().food(
            new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));
    public static final DeferredItem<Item> CLOUDBERRY_JELLY = simple("cloudberry_jelly", new Item.Properties().stacksTo(16)
            .craftRemainder(Items.BOWL)
            //? if >=26.1 {
            /*.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build(), net.minecraft.world.item.component.Consumable.builder()
                    .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, 240), 1.0F)).build()));
            *///?} else {
            .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F)
                    .effect(() -> new MobEffectInstance(ModEffects.WEIGHTLESS, 240), 1.0F).build()));
            //?}

    public static final DeferredItem<Item> DUSKWING_SPAWN_EGG = egg("duskwing", MobEntities.DUSKWING, 0x4E5A78, 0xE2B85A);
    public static final DeferredItem<Item> CRAGMAW_SPAWN_EGG = egg("cragmaw", MobEntities.CRAGMAW, 0x7C7A9E, 0xE8B4C0);
    public static final DeferredItem<Item> MISTJELLY_SPAWN_EGG = egg("mistjelly", MobEntities.MISTJELLY, 0xE8B4C0, 0x9FD4C4);
    public static final DeferredItem<Item> SKYGRAZER_SPAWN_EGG = egg("skygrazer", MobEntities.SKYGRAZER, 0xF2EEF5, 0xE2B85A);

    private static DeferredItem<Item> simple(String name, Item.Properties props) {
        DeferredItem<Item> item = McRegistration.simpleItem(ITEMS, name, props);
        ModCreativeTabs.EXTRA.add(item);
        return item;
    }

    private static DeferredItem<Item> egg(String name, Supplier<? extends EntityType<? extends Mob>> type, int primary, int secondary) {
        DeferredItem<Item> item = McRegistration.spawnEgg(ITEMS, name + "_spawn_egg", type, primary, secondary);
        ModCreativeTabs.EXTRA.add(item);
        return item;
    }
}
