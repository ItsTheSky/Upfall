package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.aerie.worldgen.PearlAerieStructure;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.Consumer;

final class AerieData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get(), "Weathered Pearlstone Bricks");
        lang.add(AerieItems.GRAVITY_SCEPTRE.get(), "Gravity Sceptre");
        lang.add("item.upfall.gravity_sceptre.desc", "Use to become Weightless and walk on the ceiling above you. Use again to let go.");
        lang.add("advancements.upfall.pearl_aerie.title", "City in the Clouds");
        lang.add("advancements.upfall.pearl_aerie.description", "Find a Pearl Aerie, the abandoned city of the sky builders in the Drifting Archipelago");
        lang.add("advancements.upfall.gravity_sceptre.title", "Sceptre of the Builders");
        lang.add("advancements.upfall.gravity_sceptre.description", "Recover the Gravity Sceptre from the keep of a Pearl Aerie");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("block.upfall.weathered_pearlstone_bricks", "Briques de pearlstone usées");
        lang.add("item.upfall.gravity_sceptre", "Sceptre de gravité");
        lang.add("item.upfall.gravity_sceptre.desc", "Utilisez-le pour passer en apesanteur et marcher au plafond au-dessus de vous. Réutilisez-le pour lâcher prise.");
        lang.add("advancements.upfall.pearl_aerie.title", "La cité dans les nuages");
        lang.add("advancements.upfall.pearl_aerie.description", "Trouvez une Aire de pearlstone, la cité abandonnée des bâtisseurs du ciel, dans l'Archipel à la dérive");
        lang.add("advancements.upfall.gravity_sceptre.title", "Le sceptre des bâtisseurs");
        lang.add("advancements.upfall.gravity_sceptre.description", "Récupérez le Sceptre de gravité dans le donjon d'une Aire de pearlstone");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get(), states.cubeAll(AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get()));
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.handheldItem(AerieItems.GRAVITY_SCEPTRE.get());
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        loot.add(AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get(), loot.createSingleItemTableWithSilkTouch(
                AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get(), ModBlocks.CRACKED_PEARLSTONE_BRICKS.get()));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get());
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        context.register(AerieStructures.PEARL_AERIE, new PearlAerieStructure(
                new Structure.StructureSettings.Builder(context.lookup(Registries.BIOME).getOrThrow(AerieStructures.HAS_PEARL_AERIE))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        context.register(AerieStructures.PEARL_AERIE_SET, new StructureSet(context.lookup(Registries.STRUCTURE).getOrThrow(AerieStructures.PEARL_AERIE),
                new RandomSpreadStructurePlacement(96, 40, RandomSpreadType.LINEAR, 1012202614)));
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.lootTables("Pearl Aerie loot tables",
                List.of(new LootTableProvider.SubProviderEntry(AerieLoot::new, LootContextParamSets.CHEST))));
        ctx.add(ctx.advancements("Pearl Aerie advancements", List.of(AerieData::advancements)));
        ctx.<Biome>tags(Registries.BIOME, "Pearl Aerie Biome Tags",
                tags -> tags.tag(AerieStructures.HAS_PEARL_AERIE).add(ModBiomes.DRIFTING_ARCHIPELAGO));
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        AdvancementHolder visited = Advancement.Builder.advancement().parent(enter)
                .display(ModBlocks.CHISELED_PEARLSTONE_BRICKS.get(), Component.translatable("advancements.upfall.pearl_aerie.title"),
                        Component.translatable("advancements.upfall.pearl_aerie.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("visited", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(AerieStructures.PEARL_AERIE))))
                .save(saver, Upfall.id("upfall/pearl_aerie").toString());
        Advancement.Builder.advancement().parent(visited)
                .display(AerieItems.GRAVITY_SCEPTRE.get(), Component.translatable("advancements.upfall.gravity_sceptre.title"),
                        Component.translatable("advancements.upfall.gravity_sceptre.description"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("sceptre", InventoryChangeTrigger.TriggerInstance.hasItems(AerieItems.GRAVITY_SCEPTRE.get()))
                .save(saver, Upfall.id("upfall/gravity_sceptre").toString());
    }
}
