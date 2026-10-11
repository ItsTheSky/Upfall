package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.advancement.PortalLitTrigger;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class ModAdvancementGenerator implements AdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        var biomes = registries.lookupOrThrow(Registries.BIOME);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModBlocks.SKYGRASS_BLOCK.get(), title("root"), description("root"),
                        Upfall.id("textures/block/pearlstone.png"), AdvancementType.TASK, false, false, false)
                .addCriterion("has_halo_frame", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.HALO_FRAME.get()))
                .addCriterion("has_skyward_key", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SKYWARD_KEY.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, Upfall.id("upfall/root"), helper);

        AdvancementHolder key = simple(saver, helper, root, "skyward_key", ModItems.SKYWARD_KEY.get(), AdvancementType.TASK,
                "has_key", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SKYWARD_KEY.get()));
        AdvancementHolder portal = Advancement.Builder.advancement().parent(key)
                .display(ModBlocks.HALO_FRAME.get(), title("light_portal"), description("light_portal"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("lit", PortalLitTrigger.Instance.lit())
                .save(saver, Upfall.id("upfall/light_portal"), helper);
        AdvancementHolder enter = Advancement.Builder.advancement().parent(portal)
                .display(ModBlocks.DENSE_CLOUD.get(), title("enter"), description("enter"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("entered", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(ModDimensions.UPFALL))
                .save(saver, Upfall.id("upfall/enter"), helper);

        AdvancementHolder updraft = Advancement.Builder.advancement().parent(enter)
                .display(ModBlocks.UPDRAFT_VENT.get(), title("updraft"), description("updraft"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("updraft", EnterBlockTrigger.TriggerInstance.entersBlock(ModBlocks.UPDRAFT_MIST.get()))
                .save(saver, Upfall.id("upfall/updraft"), helper);
        simple(saver, helper, updraft, "updraft_compass", ModItems.UPDRAFT_COMPASS.get(), AdvancementType.TASK,
                "has_compass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.UPDRAFT_COMPASS.get()));

        AdvancementHolder roots = biome(saver, helper, enter, "hanging_roots", ModBlocks.GILDROOT.sapling().get(), biomes.getOrThrow(ModBiomes.HANGING_ROOTS));
        Advancement.Builder.advancement().parent(roots)
                .display(ModItems.GILDFRUIT.get(), title("weightless"), description("weightless"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("weightless", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(ModEffects.WEIGHTLESS)))
                .save(saver, Upfall.id("upfall/weightless"), helper);

        AdvancementHolder nimbusite = simple(saver, helper, enter, "mine_nimbusite", ModItems.RAW_NIMBUSITE.get(), AdvancementType.TASK,
                "has_raw_nimbusite", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RAW_NIMBUSITE.get()));
        AdvancementHolder template = simple(saver, helper, nimbusite, "nimbusite_template", ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(),
                AdvancementType.TASK, "has_template", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get()));
        AdvancementHolder nimbusiteSet = fullSet(saver, helper, template, "nimbusite_armor", ModItems.NIMBUSITE_CHESTPLATE.get(), AdvancementType.GOAL,
                ModItems.NIMBUSITE_HELMET.get(), ModItems.NIMBUSITE_CHESTPLATE.get(), ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.NIMBUSITE_BOOTS.get());

        AdvancementHolder gilded = biome(saver, helper, nimbusiteSet, "gilded_ridge", ModBlocks.GILDED_DUSKSLATE.get(), biomes.getOrThrow(ModBiomes.GILDED_RIDGE));
        AdvancementHolder levitite = simple(saver, helper, gilded, "mine_levitite", ModItems.LEVITITE_SHARD.get(), AdvancementType.TASK,
                "has_shard", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LEVITITE_SHARD.get()));
        AdvancementHolder observatory = simple(saver, helper, levitite, "levitite_template", ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(),
                AdvancementType.GOAL, "has_template", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get()));
        fullSet(saver, helper, observatory, "levitite_armor", ModItems.LEVITITE_CHESTPLATE.get(), AdvancementType.CHALLENGE,
                ModItems.LEVITITE_HELMET.get(), ModItems.LEVITITE_CHESTPLATE.get(), ModItems.LEVITITE_LEGGINGS.get(), ModItems.LEVITITE_BOOTS.get());
    }

    private static Component title(String name) {
        return Component.translatable("advancements.upfall." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.upfall." + name + ".description");
    }

    private static AdvancementHolder simple(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String name,
                                            ItemLike icon, AdvancementType type, String criterion,
                                            net.minecraft.advancements.Criterion<?> trigger) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion(criterion, trigger)
                .save(saver, Upfall.id("upfall/" + name), helper);
    }

    private static AdvancementHolder biome(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String name,
                                           ItemLike icon, net.minecraft.core.Holder<Biome> biome) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, AdvancementType.TASK, true, true, false)
                .addCriterion("in_biome", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(biome)))
                .save(saver, Upfall.id("upfall/" + name), helper);
    }

    private static AdvancementHolder fullSet(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String name,
                                             ItemLike icon, AdvancementType type, Item head, Item chest, Item legs, Item feet) {
        EntityPredicate.Builder wearer = EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().of(head))
                .chest(ItemPredicate.Builder.item().of(chest))
                .legs(ItemPredicate.Builder.item().of(legs))
                .feet(ItemPredicate.Builder.item().of(feet)));
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("full_set", PlayerTrigger.TriggerInstance.located(wearer))
                .save(saver, Upfall.id("upfall/" + name), helper);
    }
}
