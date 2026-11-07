package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.ReliquaryEntities;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.datagen.UpfallRecipes;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
//? if >=26.1 {
/*import net.neoforged.neoforge.common.conditions.NeverCondition;
*///?} else {
import net.neoforged.neoforge.common.conditions.FalseCondition;
//?}

final class SkyboundData implements DataContribution {
    static final TagKey<EntityType<?>> UNSCALED = TagKey.create(Registries.ENTITY_TYPE, Upfall.id("skybound_unscaled"));

    private static final String PRESET_LANG = "generator.upfall.the_upfall";

    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(PRESET_LANG, "The Upfall");
        lang.add("pack.upfall.skybound", "Upfall: Skybound Recipes");
        lang.add("pack.upfall.skybound.description", "Upfall recipes without Nether or End requirements");
        lang.add("message.upfall.key_silent_upfall", "The key stays silent... you are already in the sky.");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add(PRESET_LANG, "The Upfall");
        lang.add("pack.upfall.skybound", "Upfall : recettes célestes");
        lang.add("pack.upfall.skybound.description", "Recettes d'Upfall sans prérequis du Nether ni de l'End");
        lang.add("message.upfall.key_silent_upfall", "La clé reste muette... vous êtes déjà dans le ciel.");
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.<WorldPreset>tags(Registries.WORLD_PRESET, "Upfall World Preset Tags",
                tags -> tags.tag(WorldPresetTags.NORMAL).add(SkyboundPreset.KEY));
        ctx.<EntityType<?>>tags(Registries.ENTITY_TYPE, "Upfall Skybound Entity Tags",
                tags -> tags.tag(UNSCALED).add(ReliquaryEntities.FOLD_WARDEN.getKey(), ReliquaryEntities.FOLD_SATELLITE.getKey()));

        PackOutput pack = ctx.builtinPack(SkyboundPack.NAME);
        ctx.packMetadata(pack, "Upfall Skybound Pack Metadata", Component.translatable("pack.upfall.skybound.description"));
        ctx.recipes(pack, "Upfall Skybound Recipes", SkyboundData::packRecipes);
        ctx.<Block>tags(pack, Registries.BLOCK, "Upfall Skybound Block Tags", tags -> {
            tags.tag(ModTags.Blocks.NEEDS_NETHERITE_TOOL).replace();
            tags.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.NIMBUSITE_ORE.getKey(), ModBlocks.DUSKSLATE_NIMBUSITE_ORE.getKey(),
                    ModBlocks.RAW_NIMBUSITE_BLOCK.getKey(), ModBlocks.NIMBUSITE_BLOCK.getKey());
        });
    }

    private static void packRecipes(RecipeContext r) {
        Item[][] upgrades = {
                {Items.DIAMOND_SWORD, ModItems.NIMBUSITE_SWORD.get()},
                {Items.DIAMOND_PICKAXE, ModItems.NIMBUSITE_PICKAXE.get()},
                {Items.DIAMOND_AXE, ModItems.NIMBUSITE_AXE.get()},
                {Items.DIAMOND_SHOVEL, ModItems.NIMBUSITE_SHOVEL.get()},
                {Items.DIAMOND_HOE, ModItems.NIMBUSITE_HOE.get()},
                {Items.DIAMOND_HELMET, ModItems.NIMBUSITE_HELMET.get()},
                {Items.DIAMOND_CHESTPLATE, ModItems.NIMBUSITE_CHESTPLATE.get()},
                {Items.DIAMOND_LEGGINGS, ModItems.NIMBUSITE_LEGGINGS.get()},
                {Items.DIAMOND_BOOTS, ModItems.NIMBUSITE_BOOTS.get()},
        };
        for (Item[] row : upgrades) {
            UpfallRecipes.upgrade(r, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), row[0], ModItems.NIMBUSITE_INGOT.get(), row[1]);
        }

        //? if >=26.1 {
        /*var disabled = r.output().withConditions(NeverCondition.INSTANCE);
        *///?} else {
        var disabled = r.output().withConditions(FalseCondition.INSTANCE);
        //?}
        r.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HALO_FRAME.get())
                .requires(Items.NETHERITE_INGOT)
                .unlockedBy(RecipeContext.hasName(Items.NETHERITE_INGOT), r.has(Items.NETHERITE_INGOT))
                .save(disabled);
        r.shapeless(RecipeCategory.TOOLS, ModItems.SKYWARD_KEY.get())
                .requires(Items.NETHER_STAR)
                .unlockedBy(RecipeContext.hasName(Items.NETHER_STAR), r.has(Items.NETHER_STAR))
                .save(disabled);
    }
}
