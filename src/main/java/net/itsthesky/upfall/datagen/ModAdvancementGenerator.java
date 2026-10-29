package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.advancement.PortalLitTrigger;
import net.itsthesky.upfall.content.enchanting.EnchantingModule;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.grotto.GrottoStructures;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;

//? if >=26.1 {
/*import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
*///?} else {
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
//?}

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        var biomes = registries.lookupOrThrow(Registries.BIOME);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModBlocks.SKYGRASS_BLOCK.get(), title("root"), description("root"),
                        rootBackground(), AdvancementType.TASK, false, false, false)
                .addCriterion("has_halo_frame", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.HALO_FRAME.get()))
                .addCriterion("has_skyward_key", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SKYWARD_KEY.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, Upfall.id("upfall/root").toString());

        AdvancementHolder key = simple(saver, root, "skyward_key", ModItems.SKYWARD_KEY.get(), AdvancementType.TASK,
                "has_key", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SKYWARD_KEY.get()));
        AdvancementHolder portal = Advancement.Builder.advancement().parent(key)
                .display(ModBlocks.HALO_FRAME.get(), title("light_portal"), description("light_portal"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("lit", PortalLitTrigger.Instance.lit())
                .save(saver, Upfall.id("upfall/light_portal").toString());
        AdvancementHolder enter = Advancement.Builder.advancement().parent(portal)
                .display(ModBlocks.DENSE_CLOUD.get(), title("enter"), description("enter"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("entered", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(ModDimensions.UPFALL))
                .save(saver, Upfall.id("upfall/enter").toString());

        AdvancementHolder updraft = Advancement.Builder.advancement().parent(enter)
                .display(ModBlocks.UPDRAFT_VENT.get(), title("updraft"), description("updraft"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("updraft", EnterBlockTrigger.TriggerInstance.entersBlock(ModBlocks.UPDRAFT_MIST.get()))
                .save(saver, Upfall.id("upfall/updraft").toString());
        simple(saver, updraft, "updraft_compass", ModItems.UPDRAFT_COMPASS.get(), AdvancementType.TASK,
                "has_compass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.UPDRAFT_COMPASS.get()));

        AdvancementHolder roots = biome(saver, enter, "hanging_roots", ModBlocks.GILDROOT.sapling().get(), biomes.getOrThrow(ModBiomes.HANGING_ROOTS));
        Advancement.Builder.advancement().parent(roots)
                .display(ModItems.GILDFRUIT.get(), title("weightless"), description("weightless"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("weightless", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(ModEffects.WEIGHTLESS)))
                .save(saver, Upfall.id("upfall/weightless").toString());

        Advancement.Builder.advancement().parent(enter)
                .display(GrottoBlocks.AZURE.cluster().get(), title("crystal_grotto"), description("crystal_grotto"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("visited", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(GrottoStructures.CRYSTAL_GROTTO))))
                .save(saver, Upfall.id("upfall/crystal_grotto").toString());

        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        List<EnchantmentPredicate> sky = List.of(new EnchantmentPredicate(HolderSet.direct(
                enchantments.getOrThrow(EnchantingModule.SKYWALKER), enchantments.getOrThrow(EnchantingModule.UPLIFTED),
                enchantments.getOrThrow(EnchantingModule.CLOUDSIGHT), enchantments.getOrThrow(EnchantingModule.JUMPSTART)), MinMaxBounds.Ints.ANY));
        Advancement.Builder.advancement().parent(enter)
                .display(Items.ENCHANTED_BOOK, title("sky_enchantment"), description("sky_enchantment"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("enchanted_item", enchanted(withEnchantments(ItemPredicate.Builder.item(), sky, false)))
                .addCriterion("enchanted_book", enchanted(withEnchantments(ItemPredicate.Builder.item(), sky, true)))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, Upfall.id("upfall/sky_enchantment").toString());

        AdvancementHolder nimbusite = simple(saver, enter, "mine_nimbusite", ModItems.RAW_NIMBUSITE.get(), AdvancementType.TASK,
                "has_raw_nimbusite", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RAW_NIMBUSITE.get()));
        AdvancementHolder template = simple(saver, nimbusite, "nimbusite_template", ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(),
                AdvancementType.TASK, "has_template", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get()));
        fullSet(registries, saver, template, "nimbusite_armor", ModItems.NIMBUSITE_CHESTPLATE.get(), AdvancementType.GOAL,
                ModItems.NIMBUSITE_HELMET.get(), ModItems.NIMBUSITE_CHESTPLATE.get(), ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.NIMBUSITE_BOOTS.get());

        AdvancementHolder gilded = biome(saver, template, "gilded_ridge", ModBlocks.GILDED_DUSKSLATE.get(), biomes.getOrThrow(ModBiomes.GILDED_RIDGE));
        AdvancementHolder levitite = simple(saver, gilded, "mine_levitite", ModItems.LEVITITE_SHARD.get(), AdvancementType.TASK,
                "has_shard", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LEVITITE_SHARD.get()));
        AdvancementHolder observatory = simple(saver, levitite, "levitite_template", ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(),
                AdvancementType.GOAL, "has_template", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get()));
        fullSet(registries, saver, observatory, "levitite_armor", ModItems.LEVITITE_CHESTPLATE.get(), AdvancementType.CHALLENGE,
                ModItems.LEVITITE_HELMET.get(), ModItems.LEVITITE_CHESTPLATE.get(), ModItems.LEVITITE_LEGGINGS.get(), ModItems.LEVITITE_BOOTS.get());
    }

    private static ResourceLocation rootBackground() {
        //? if >=26.1 {
        /*return Upfall.id("block/pearlstone");
        *///?} else {
        return Upfall.id("textures/block/pearlstone.png");
        //?}
    }

    private static ItemPredicate.Builder itemOf(HolderLookup.Provider registries, Item item) {
        //? if >=26.1 {
        /*return ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), item);
        *///?} else {
        return ItemPredicate.Builder.item().of(item);
        //?}
    }

    private static ItemPredicate.Builder withEnchantments(ItemPredicate.Builder item, List<EnchantmentPredicate> enchantments, boolean stored) {
        //? if >=26.1 {
        /*DataComponentMatchers.Builder components = DataComponentMatchers.Builder.components();
        if (stored) {
            components.partial(DataComponentPredicates.STORED_ENCHANTMENTS, EnchantmentsPredicate.storedEnchantments(enchantments));
        } else {
            components.partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(enchantments));
        }
        return item.withComponents(components.build());
        *///?} else {
        if (stored) {
            return item.withSubPredicate(ItemSubPredicates.STORED_ENCHANTMENTS, ItemEnchantmentsPredicate.storedEnchantments(enchantments));
        }
        return item.withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(enchantments));
        //?}
    }

    private static net.minecraft.advancements.Criterion<EnchantedItemTrigger.TriggerInstance> enchanted(ItemPredicate.Builder item) {
        return CriteriaTriggers.ENCHANTED_ITEM.createCriterion(new EnchantedItemTrigger.TriggerInstance(Optional.empty(), Optional.of(item.build()), MinMaxBounds.Ints.ANY));
    }

    private static Component title(String name) {
        return Component.translatable("advancements.upfall." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.upfall." + name + ".description");
    }

    private static AdvancementHolder simple(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name,
                                            ItemLike icon, AdvancementType type, String criterion,
                                            net.minecraft.advancements.Criterion<?> trigger) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion(criterion, trigger)
                .save(saver, Upfall.id("upfall/" + name).toString());
    }

    private static AdvancementHolder biome(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name,
                                           ItemLike icon, net.minecraft.core.Holder<Biome> biome) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, AdvancementType.TASK, true, true, false)
                .addCriterion("in_biome", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(biome)))
                .save(saver, Upfall.id("upfall/" + name).toString());
    }

    private static AdvancementHolder fullSet(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name,
                                             ItemLike icon, AdvancementType type, Item head, Item chest, Item legs, Item feet) {
        EntityPredicate.Builder wearer = EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                .head(itemOf(registries, head))
                .chest(itemOf(registries, chest))
                .legs(itemOf(registries, legs))
                .feet(itemOf(registries, feet)));
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("full_set", PlayerTrigger.TriggerInstance.located(wearer))
                .save(saver, Upfall.id("upfall/" + name).toString());
    }
}
