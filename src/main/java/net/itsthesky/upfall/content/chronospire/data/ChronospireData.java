package net.itsthesky.upfall.content.chronospire.data;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireStructure;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.Consumer;

public class ChronospireData implements DataContribution {
    public static final ResourceKey<Structure> STRUCTURE = ResourceKey.create(Registries.STRUCTURE, Upfall.id("chronospire"));
    public static final ResourceKey<StructureSet> STRUCTURE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("chronospire"));
    public static final TagKey<Biome> HAS_CHRONOSPIRE = TagKey.create(Registries.BIOME, Upfall.id("has_structure/chronospire"));

    @Override
    public void structures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(STRUCTURE, new ChronospireStructure(new Structure.StructureSettings.Builder(biomes.getOrThrow(HAS_CHRONOSPIRE))
                .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION).terrainAdapation(TerrainAdjustment.NONE).build()));
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        context.register(STRUCTURE_SET, new StructureSet(context.lookup(Registries.STRUCTURE).getOrThrow(STRUCTURE),
                new RandomSpreadStructurePlacement(80, 27, RandomSpreadType.LINEAR, 20261011)));
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.lootTables("Chronospire Loot Tables",
                List.of(new LootTableProvider.SubProviderEntry(ChronospireLootProvider::new, LootContextParamSets.CHEST))));
        ctx.add(ctx.advancements("Chronospire Advancements", List.of(ChronospireData::advancements)));
        ctx.<Biome>tags(Registries.BIOME, "Chronospire Biome Tags",
                tags -> tags.tag(HAS_CHRONOSPIRE).add(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.MISTSEA));
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        AdvancementHolder spire = Advancement.Builder.advancement().parent(enter)
                .display(ChronoBlocks.HOUR_DIAL.get(), Component.translatable("advancements.upfall.chronospire.title"),
                        Component.translatable("advancements.upfall.chronospire.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("in_chronospire", PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inStructure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(STRUCTURE))))
                .save(saver, Upfall.id("upfall/chronospire").toString());
        Advancement.Builder.advancement().parent(spire)
                .display(ChronoRegistries.REWINDING_HOURGLASS.get(), Component.translatable("advancements.upfall.rewinding_hourglass.title"),
                        Component.translatable("advancements.upfall.rewinding_hourglass.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_hourglass", InventoryChangeTrigger.TriggerInstance.hasItems(ChronoRegistries.REWINDING_HOURGLASS.get()))
                .save(saver, Upfall.id("upfall/rewinding_hourglass").toString());
    }

    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add("advancements.upfall.chronospire.title", "Keeper of Hours");
        lang.add("advancements.upfall.chronospire.description", "Find a Chronospire, the floating tower that turns with the days of the Overworld");
        lang.add("advancements.upfall.rewinding_hourglass.title", "Borrowed Time");
        lang.add("advancements.upfall.rewinding_hourglass.description", "Solve the puzzles of a Chronospire and take the Rewinding Hourglass from its vault");
        lang.add("chronospire.upfall.phase.dawn", "Dawn");
        lang.add("chronospire.upfall.phase.noon", "Noon");
        lang.add("chronospire.upfall.phase.dusk", "Dusk");
        lang.add("chronospire.upfall.phase.midnight", "Midnight");
        lang.add("block.upfall.hour_dial.desc", "Shows the hour of the Chronospire. The tower keeps the days of the Overworld: "
                + "Dawn, Noon, Dusk and Midnight, five minutes each.");
        lang.add("block.upfall.phase_stone.desc", "Solid only during its own hour. It flickers before it fades.");
        lang.add("message.upfall.hour_dial", "Hour of the Chronospire: %s. %s until %s.");
        lang.add("message.upfall.glyph.watch", "The glyphs remember an order. Watch them light up, then strike them in the same order.");
        lang.add("message.upfall.glyph.idle", "Use the Glyph Pedestal to awaken the glyphs.");
        lang.add("message.upfall.glyph.progress", "Glyph %s of %s");
        lang.add("message.upfall.glyph.wrong", "Wrong glyph: they fall silent. Use the pedestal to watch them again.");
        lang.add("message.upfall.glyph.reset", "The glyphs fall silent.");
        lang.add("message.upfall.glyph.solved", "The glyphs ring as one. The seal around the lift dissolves.");
        lang.add("message.upfall.glyph.at_rest", "The glyphs are at rest. The lift is free.");
        lang.add("message.upfall.wheel.hint", "Turn the five wheels until all of them show the hour of the Spire: %s. "
                + "Each wheel drags its two neighbours. Sneak to reset.");
        lang.add("message.upfall.wheel.locked", "The wheels are locked in place.");
        lang.add("message.upfall.wheel.scrambled", "The wheels spin back into disorder.");
        lang.add("message.upfall.wheel.solved", "The hours align. The seals of the Chronospire dissolve, up to its crown.");
        lang.add("message.upfall.wheel.at_rest", "The hours are aligned. The way to the crown is open.");
        lang.add("message.upfall.hourglass.empty", "The sand has nothing to remember yet.");
        lang.add("item.upfall.rewinding_hourglass.desc", "Use to return to where you last stood, at least 3 seconds ago. Recharges in 20 seconds.");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        for (var block : List.of(ChronoBlocks.CHRONOSTONE, ChronoBlocks.CHRONOSTONE_BRICKS, ChronoBlocks.CHISELED_CHRONOSTONE,
                ChronoBlocks.GILDED_CHRONOSTONE, ChronoBlocks.WARDED_CHRONOSTONE)) {
            states.simpleBlockWithItem(block.get(), states.cubeAll(block.get()));
        }
        states.translucentCube(ChronoBlocks.CHRONOGLASS, true);
        ChronospireModels.blockStates(states);
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(ChronoRegistries.REWINDING_HOURGLASS.get());
    }

    @Override
    public void recipes(RecipeContext recipes) {
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, ChronoBlocks.CHRONOSTONE_BRICKS.get(), 4)
                .pattern("##").pattern("##").define('#', ChronoBlocks.CHRONOSTONE.get())
                .unlockedBy(RecipeContext.hasName(ChronoBlocks.CHRONOSTONE.get()), recipes.has(ChronoBlocks.CHRONOSTONE.get()))
                .save(recipes.output());
        stonecutting(recipes, ChronoBlocks.CHRONOSTONE.get(), ChronoBlocks.CHRONOSTONE_BRICKS.get());
        stonecutting(recipes, ChronoBlocks.CHRONOSTONE.get(), ChronoBlocks.CHISELED_CHRONOSTONE.get());
        stonecutting(recipes, ChronoBlocks.CHRONOSTONE_BRICKS.get(), ChronoBlocks.CHISELED_CHRONOSTONE.get());
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, ChronoBlocks.GILDED_CHRONOSTONE.get(), 4)
                .pattern("#G").pattern("G#").define('#', ChronoBlocks.CHRONOSTONE_BRICKS.get()).define('G', Items.GOLD_INGOT)
                .unlockedBy(RecipeContext.hasName(ChronoBlocks.CHRONOSTONE_BRICKS.get()), recipes.has(ChronoBlocks.CHRONOSTONE_BRICKS.get()))
                .save(recipes.output());
        recipes.shaped(RecipeCategory.REDSTONE, ChronoBlocks.HOUR_DIAL.get())
                .pattern(" C ").pattern("#G#").pattern("###")
                .define('C', Items.CLOCK).define('G', Items.GOLD_INGOT).define('#', ChronoBlocks.CHRONOSTONE_BRICKS.get())
                .unlockedBy(RecipeContext.hasName(ChronoBlocks.CHRONOSTONE_BRICKS.get()), recipes.has(ChronoBlocks.CHRONOSTONE_BRICKS.get()))
                .save(recipes.output());
        recipes.shaped(RecipeCategory.DECORATIONS, ChronoBlocks.PHASE_LANTERN.get(), 4)
                .pattern("#G#").pattern("GMG").pattern("#G#")
                .define('#', ChronoBlocks.CHRONOSTONE.get()).define('G', Items.GOLD_NUGGET).define('M', ModBlocks.MISTGLOW_CRYSTAL.get())
                .unlockedBy(RecipeContext.hasName(ModBlocks.MISTGLOW_CRYSTAL.get()), recipes.has(ModBlocks.MISTGLOW_CRYSTAL.get()))
                .save(recipes.output());
    }

    private static void stonecutting(RecipeContext recipes, Block input, Block result) {
        recipes.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result)
                .unlockedBy(RecipeContext.hasName(input), recipes.has(input))
                .save(recipes.output(), Upfall.id(RecipeContext.itemName(result) + "_from_" + RecipeContext.itemName(input) + "_stonecutting").toString());
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ChronoBlocks.CHRONOSTONE.get(), ChronoBlocks.CHRONOSTONE_BRICKS.get(),
                ChronoBlocks.CHISELED_CHRONOSTONE.get(), ChronoBlocks.GILDED_CHRONOSTONE.get(), ChronoBlocks.HOUR_DIAL.get(),
                ChronoBlocks.PHASE_LANTERN.get());
        Block[] warded = {ChronoBlocks.WARDED_CHRONOSTONE.get(), ChronoBlocks.CHRONOGLASS.get(), ChronoBlocks.PHASE_STONE.get(),
                ChronoBlocks.CHRONOGLYPH.get(), ChronoBlocks.GLYPH_PEDESTAL.get(), ChronoBlocks.PHASE_WHEEL.get(),
                ChronoBlocks.CHRONO_GNOMON.get(), ChronoBlocks.CHRONO_SEAL.get(), ChronoBlocks.CHRONO_CURRENT.get(), ChronoBlocks.DRIFT_PLATE.get()};
        tags.tag(BlockTags.WITHER_IMMUNE).add(warded);
        tags.tag(BlockTags.DRAGON_IMMUNE).add(warded);
        tags.tag(BlockTags.FEATURES_CANNOT_REPLACE).add(warded);
    }
}
