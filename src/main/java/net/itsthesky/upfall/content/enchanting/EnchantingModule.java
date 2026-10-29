package net.itsthesky.upfall.content.enchanting;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McIds;
import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class EnchantingModule implements UpfallModule {
    public static final ResourceKey<Enchantment> SKYWALKER = key("skywalker");
    public static final ResourceKey<Enchantment> UPLIFTED = key("uplifted");
    public static final ResourceKey<Enchantment> CLOUDSIGHT = key("cloudsight");
    public static final ResourceKey<Enchantment> JUMPSTART = key("jumpstart");

    public static final TagKey<Item> JUMPSTART_ENCHANTABLE = TagKey.create(Registries.ITEM, Upfall.id("enchantable/jumpstart"));

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Upfall.id(name));
    }

    @Override
    public void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(EnchantingEffects::onEntityTick);
        NeoForge.EVENT_BUS.addListener(EnchantingEffects::onJump);
    }

    @Override
    public DataContribution data() {
        return new DataContribution() {
            @Override
            public void lang(ModLanguageProvider lang) {
                lang.add("enchantment.upfall.skywalker", "Skywalker");
                lang.add("enchantment.upfall.uplifted", "Uplifted");
                lang.add("enchantment.upfall.cloudsight", "Cloudsight");
                lang.add("enchantment.upfall.jumpstart", "Jumpstart");
            }

            @Override
            public void itemTags(ModTagProviders.Items tags) {
                tags.tag(JUMPSTART_ENCHANTABLE).add(ModItems.LEVITITE_BOOTS.get());
            }

            @Override
            public void enchantments(BootstrapContext<Enchantment> context) {
                var items = context.lookup(Registries.ITEM);
                HolderSet<Item> boots = items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE);

                context.register(SKYWALKER, Enchantment.enchantment(Enchantment.definition(
                        boots, 5, 3, Enchantment.dynamicCost(8, 9), Enchantment.dynamicCost(28, 9), 4,
                        EquipmentSlotGroup.FEET)).build(McIds.id(SKYWALKER)));

                context.register(UPLIFTED, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE), 5, 3, Enchantment.dynamicCost(8, 9),
                        Enchantment.dynamicCost(28, 9), 4, EquipmentSlotGroup.CHEST)).build(McIds.id(UPLIFTED)));

                context.register(CLOUDSIGHT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE), 3, 1, Enchantment.constantCost(15),
                        Enchantment.constantCost(45), 4, EquipmentSlotGroup.HEAD)).build(McIds.id(CLOUDSIGHT)));

                context.register(JUMPSTART, Enchantment.enchantment(Enchantment.definition(
                                items.getOrThrow(JUMPSTART_ENCHANTABLE), 2, 3, Enchantment.dynamicCost(12, 12),
                                Enchantment.dynamicCost(42, 12), 6, EquipmentSlotGroup.FEET))
                        .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(
                                Upfall.id("enchantment.jumpstart"), Attributes.JUMP_STRENGTH,
                                LevelBasedValue.perLevel(0.06F), AttributeModifier.Operation.ADD_VALUE))
                        .build(McIds.id(JUMPSTART)));
            }

            @Override
            public void addProviders(DataGenContext ctx) {
                ctx.<Enchantment>tags(Registries.ENCHANTMENT, "Upfall Enchantment Tags", tags -> {
                    for (TagKey<Enchantment> tag : List.of(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE,
                            net.minecraft.tags.EnchantmentTags.NON_TREASURE, net.minecraft.tags.EnchantmentTags.TRADEABLE,
                            net.minecraft.tags.EnchantmentTags.ON_RANDOM_LOOT)) {
                        tags.tag(tag).add(SKYWALKER, UPLIFTED, CLOUDSIGHT, JUMPSTART);
                    }
                });
            }
        };
    }
}
