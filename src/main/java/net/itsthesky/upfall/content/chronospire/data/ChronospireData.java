package net.itsthesky.upfall.content.chronospire.data;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.ChronoGnomonBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireStructure;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
                new RandomSpreadStructurePlacement(36, 12, RandomSpreadType.LINEAR, 20261011)));
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        DataProvider loot = new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ChronospireLootProvider::new, LootContextParamSets.CHEST)), registries);
        generator.addProvider(event.includeServer(), new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return loot.run(cache);
            }

            @Override
            public String getName() {
                return "Chronospire Loot Tables";
            }
        });
        DataProvider advancements = new AdvancementProvider(output, registries, helper, List.of(ChronospireData::advancements));
        generator.addProvider(event.includeServer(), new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return advancements.run(cache);
            }

            @Override
            public String getName() {
                return "Chronospire Advancements";
            }
        });
        generator.addProvider(event.includeServer(), new BiomeTagsProvider(output, registries, Upfall.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(HAS_CHRONOSPIRE).add(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.MISTSEA);
            }

            @Override
            public String getName() {
                return "Chronospire Biome Tags";
            }
        });
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        AdvancementHolder spire = Advancement.Builder.advancement().parent(enter)
                .display(ChronoBlocks.HOUR_DIAL.get(), Component.translatable("advancements.upfall.chronospire.title"),
                        Component.translatable("advancements.upfall.chronospire.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("in_chronospire", PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inStructure(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(STRUCTURE))))
                .save(saver, Upfall.id("upfall/chronospire"), helper);
        Advancement.Builder.advancement().parent(spire)
                .display(ChronoRegistries.REWINDING_HOURGLASS.get(), Component.translatable("advancements.upfall.rewinding_hourglass.title"),
                        Component.translatable("advancements.upfall.rewinding_hourglass.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_hourglass", InventoryChangeTrigger.TriggerInstance.hasItems(ChronoRegistries.REWINDING_HOURGLASS.get()))
                .save(saver, Upfall.id("upfall/rewinding_hourglass"), helper);
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
        ModelFile glass = states.models().cubeAll("chronoglass", states.blockTexture("chronoglass")).renderType("translucent");
        states.simpleBlockWithItem(ChronoBlocks.CHRONOGLASS.get(), glass);

        states.getVariantBuilder(ChronoBlocks.PHASE_STONE.get()).forAllStates(state -> {
            String phase = state.getValue(PhaseTrackingBlock.PHASE).getSerializedName();
            boolean slab = state.getValue(PhaseStoneBlock.SLAB);
            PhaseStoneBlock.Mode mode = state.getValue(PhaseStoneBlock.MODE);
            String texture = "phase_stone_" + phase + (mode == PhaseStoneBlock.Mode.SOLID ? "" : "_" + mode.getSerializedName());
            String name = texture + (slab ? "_slab" : "");
            ModelFile model = mode == PhaseStoneBlock.Mode.GHOST
                    ? box(states, name, texture, "translucent", slab, 6)
                    : box(states, name, texture, "solid", slab, 15);
            return ConfiguredModel.builder().modelFile(model).build();
        });
        states.itemModels().withExistingParent("phase_stone", Upfall.id("block/phase_stone_dawn"));

        states.getVariantBuilder(ChronoBlocks.PHASE_LANTERN.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(states.glowingCube("phase_lantern_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName(), "solid")).build());
        states.itemModels().withExistingParent("phase_lantern", Upfall.id("block/phase_lantern_dusk"));

        ModelFile[] dials = new ModelFile[4];
        for (ChronoPhase phase : ChronoPhase.values()) {
            dials[phase.ordinal()] = states.models().cubeBottomTop("hour_dial_" + phase.getSerializedName(), states.blockTexture("hour_dial_side"),
                    states.blockTexture("chronostone_bricks"), states.blockTexture("hour_dial_top_" + phase.getSerializedName()));
        }
        states.getVariantBuilder(ChronoBlocks.HOUR_DIAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(dials[state.getValue(PhaseTrackingBlock.PHASE).ordinal()]).build());
        states.itemModels().withExistingParent("hour_dial", Upfall.id("block/hour_dial_dusk"));

        states.getVariantBuilder(ChronoBlocks.CHRONOGLYPH.get()).forAllStates(state -> {
            String glyph = "chronoglyph_" + state.getValue(ChronoglyphBlock.GLYPH).getSerializedName();
            ModelFile model = switch (state.getValue(ChronoglyphBlock.LIGHT)) {
                case OFF -> states.models().cubeAll(glyph, states.blockTexture(glyph));
                case ON, SOLVED -> states.glowingCube(glyph + "_on", "solid");
                case ERROR -> states.glowingCube(glyph + "_error", "solid");
            };
            return ConfiguredModel.builder().modelFile(model).build();
        });
        states.itemModels().withExistingParent("chronoglyph", Upfall.id("block/chronoglyph_dawn"));

        states.getVariantBuilder(ChronoBlocks.GLYPH_PEDESTAL.get()).forAllStates(state -> {
            String mode = state.getValue(GlyphPedestalBlock.MODE).getSerializedName();
            return ConfiguredModel.builder().modelFile(states.models().cubeBottomTop("glyph_pedestal_" + mode,
                    states.blockTexture("glyph_pedestal_side"), states.blockTexture("warded_chronostone"),
                    states.blockTexture("glyph_pedestal_top_" + mode))).build();
        });
        states.itemModels().withExistingParent("glyph_pedestal", Upfall.id("block/glyph_pedestal_idle"));

        states.getVariantBuilder(ChronoBlocks.PHASE_WHEEL.get()).forAllStates(state -> {
            String name = "phase_wheel_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName();
            return ConfiguredModel.builder().modelFile(states.models().cubeBottomTop(name, states.blockTexture(name),
                    states.blockTexture("warded_chronostone"), states.blockTexture("phase_wheel_top"))).build();
        });
        states.itemModels().withExistingParent("phase_wheel", Upfall.id("block/phase_wheel_dawn"));

        states.getVariantBuilder(ChronoBlocks.CHRONO_GNOMON.get()).forAllStates(state -> {
            String name = "chrono_gnomon_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName()
                    + (state.getValue(ChronoGnomonBlock.SOLVED) ? "_solved" : "");
            return ConfiguredModel.builder().modelFile(box(states, name, name, "solid", false, 15)).build();
        });
        states.itemModels().withExistingParent("chrono_gnomon", Upfall.id("block/chrono_gnomon_dusk"));

        ModelFile sealClosed = box(states, "chrono_seal", "chrono_seal", "translucent", false, 12);
        ModelFile empty = states.models().getBuilder("chrono_seal_open").texture("particle", states.blockTexture("chrono_seal"));
        states.getVariantBuilder(ChronoBlocks.CHRONO_SEAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ChronoSealBlock.OPEN) ? empty : sealClosed).build());
        states.itemModels().withExistingParent("chrono_seal", Upfall.id("block/chrono_seal"));

        states.simpleBlock(ChronoBlocks.CHRONO_CURRENT.get(), states.models().getBuilder("chrono_current")
                .texture("particle", states.blockTexture("chrono_seal")));
        states.simpleBlock(ChronoBlocks.DRIFT_PLATE.get(), states.models().slab("drift_plate", states.blockTexture("drift_plate_side"),
                states.blockTexture("drift_plate_bottom"), states.blockTexture("drift_plate_top")));
    }

    private static ModelFile box(ModBlockStateProvider states, String name, String texture, String renderType, boolean slab, int light) {
        ResourceLocation tex = states.blockTexture(texture);
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("all", tex).texture("particle", tex).renderType(renderType)
                .element().from(0, 0, 0).to(16, slab ? 8 : 16, 16).shade(false).ao(false).emissivity(light, light)
                .allFaces((dir, face) -> {
                    face.texture("#all");
                    if (!slab || dir != Direction.UP) face.cullface(dir);
                    if (slab && dir.getAxis().isHorizontal()) face.uvs(0, 8, 16, 16);
                }).end();
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(ChronoRegistries.REWINDING_HOURGLASS.get());
    }

    @Override
    public void recipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ChronoBlocks.CHRONOSTONE_BRICKS.get(), 4)
                .pattern("##").pattern("##").define('#', ChronoBlocks.CHRONOSTONE.get())
                .unlockedBy(ModRecipeProvider.hasName(ChronoBlocks.CHRONOSTONE.get()), ModRecipeProvider.hasItem(ChronoBlocks.CHRONOSTONE.get()))
                .save(output);
        stonecutting(output, ChronoBlocks.CHRONOSTONE.get(), ChronoBlocks.CHRONOSTONE_BRICKS.get());
        stonecutting(output, ChronoBlocks.CHRONOSTONE.get(), ChronoBlocks.CHISELED_CHRONOSTONE.get());
        stonecutting(output, ChronoBlocks.CHRONOSTONE_BRICKS.get(), ChronoBlocks.CHISELED_CHRONOSTONE.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ChronoBlocks.GILDED_CHRONOSTONE.get(), 4)
                .pattern("#G").pattern("G#").define('#', ChronoBlocks.CHRONOSTONE_BRICKS.get()).define('G', Items.GOLD_INGOT)
                .unlockedBy(ModRecipeProvider.hasName(ChronoBlocks.CHRONOSTONE_BRICKS.get()), ModRecipeProvider.hasItem(ChronoBlocks.CHRONOSTONE_BRICKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ChronoBlocks.HOUR_DIAL.get())
                .pattern(" C ").pattern("#G#").pattern("###")
                .define('C', Items.CLOCK).define('G', Items.GOLD_INGOT).define('#', ChronoBlocks.CHRONOSTONE_BRICKS.get())
                .unlockedBy(ModRecipeProvider.hasName(ChronoBlocks.CHRONOSTONE_BRICKS.get()), ModRecipeProvider.hasItem(ChronoBlocks.CHRONOSTONE_BRICKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ChronoBlocks.PHASE_LANTERN.get(), 4)
                .pattern("#G#").pattern("GMG").pattern("#G#")
                .define('#', ChronoBlocks.CHRONOSTONE.get()).define('G', Items.GOLD_NUGGET).define('M', ModBlocks.MISTGLOW_CRYSTAL.get())
                .unlockedBy(ModRecipeProvider.hasName(ModBlocks.MISTGLOW_CRYSTAL.get()), ModRecipeProvider.hasItem(ModBlocks.MISTGLOW_CRYSTAL.get()))
                .save(output);
    }

    private static void stonecutting(RecipeOutput output, Block input, Block result) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result)
                .unlockedBy(ModRecipeProvider.hasName(input), ModRecipeProvider.hasItem(input))
                .save(output, Upfall.id(ModRecipeProvider.itemName(result) + "_from_" + ModRecipeProvider.itemName(input) + "_stonecutting"));
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
