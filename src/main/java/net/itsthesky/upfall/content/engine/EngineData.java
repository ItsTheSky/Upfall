package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.content.engine.worldgen.StalledEngineStructure;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class EngineData implements DataContribution {
    public static final TagKey<Biome> HAS_STALLED_ENGINE = TagKey.create(Registries.BIOME, Upfall.id("has_structure/stalled_engine"));

    @Override
    public void lang(ModLanguageProvider lang) {
        EngineSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
        for (EngineBooks.Book book : EngineBooks.all()) {
            for (int i = 0; i < book.pages().size(); i++) {
                lang.add(book.pageKey(i), book.pages().get(i));
            }
        }
        lang.add("message.upfall.drain_regulator.status", "Drain valves open: %s / %s");
        lang.add("message.upfall.drain_regulator.drained", "The vault has been drained");
        lang.add("advancements.upfall.stalled_engine.title", "Stalled, Not Dead");
        lang.add("advancements.upfall.stalled_engine.description", "Find a Stalled Engine, the ruined pump hall of the Rustworks, whose chests hold machine blueprints");
        lang.add("advancements.upfall.kairite_gear.title", "Perfect Timing");
        lang.add("advancements.upfall.kairite_gear.description", "Obtain a Kairite Gear by mining Rustworks Kairite ore as it flares, or from nine Kairite Dust");
        lang.add("advancements.upfall.machine.title", "Clockwork");
        lang.add("advancements.upfall.machine.description", "Smith a machine from a Stalled Engine blueprint, its base and a Kairite Gear");
        lang.add("advancements.upfall.veil_mask.title", "Breathe Easy");
        lang.add("advancements.upfall.veil_mask.description", "Smith a Veil Mask from its blueprint, an iron helmet and a Kairite Gear to brave the Veil Gas");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        states.simpleBlock(EngineBlocks.STAGNANT_MIST.get(), states.models().cubeAll("stagnant_mist", states.blockTexture("stagnant_mist"))
                .renderType("translucent"));
        states.simpleBlockWithItem(EngineBlocks.ENGRAVED_RUSTSLATE.get(), states.cubeAll(EngineBlocks.ENGRAVED_RUSTSLATE.get()));
        valve(states);
        regulator(states);
    }

    private static void valve(ModBlockStateProvider states) {
        ModelFile closed = valveModel(states, "drain_valve", 0);
        ModelFile open = valveModel(states, "drain_valve_open", 45);
        states.getVariantBuilder(EngineBlocks.DRAIN_VALVE.get()).forAllStates(state -> {
            Direction facing = state.getValue(DrainValveBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(state.getValue(DrainValveBlock.OPEN) ? open : closed).rotationX(x).rotationY(y).build();
        });
        states.blockItem(EngineBlocks.DRAIN_VALVE.get());
    }

    private static ModelFile valveModel(ModBlockStateProvider states, String name, float angle) {
        BlockModelBuilder model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("wheel", states.blockTexture("drain_valve")).texture("stem", states.blockTexture("rusty_pipe_side"))
                .texture("particle", states.blockTexture("drain_valve")).renderType("cutout");
        model.element().from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture("#stem")
                .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)).end();
        var wheel = model.element().from(2, 6, 2).to(14, 8, 14);
        if (angle != 0) {
            wheel.rotation().angle(angle).axis(Direction.Axis.Y).origin(8, 7, 8).end();
        }
        wheel.allFaces((dir, face) -> {
                    face.texture("#wheel");
                    if (dir.getAxis() == Direction.Axis.Y) {
                        face.uvs(2, 2, 14, 14);
                    } else {
                        face.uvs(2, 2, 14, 4);
                    }
                }).end();
        return model;
    }

    private static void regulator(ModBlockStateProvider states) {
        ModelFile sealed = states.models().orientable("drain_regulator", states.blockTexture("rusty_plating"),
                states.blockTexture("drain_regulator_front"), states.blockTexture("rusty_plating"));
        ModelFile drained = states.models().orientable("drain_regulator_drained", states.blockTexture("rusty_plating"),
                states.blockTexture("drain_regulator_front_drained"), states.blockTexture("rusty_plating"));
        states.getVariantBuilder(EngineBlocks.DRAIN_REGULATOR.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DrainRegulatorBlock.DRAINED) ? drained : sealed)
                .rotationY(((int) state.getValue(DrainRegulatorBlock.FACING).toYRot() + 180) % 360).build());
    }

    @Override
    public void recipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, EngineBlocks.DRAIN_VALVE.get())
                .define('V', RustworksBlocks.PIPE_VALVE.get()).define('D', RustworksItems.KAIRITE_DUST.get()).pattern("V").pattern("D")
                .unlockedBy(ModRecipeProvider.hasName(RustworksItems.KAIRITE_DUST.get()), ModRecipeProvider.hasItem(RustworksItems.KAIRITE_DUST.get()))
                .save(output, Upfall.id("drain_valve"));
        stonecut(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), EngineBlocks.ENGRAVED_RUSTSLATE.get());
        stonecut(output, RustworksBlocks.RUSTSLATE.get(), EngineBlocks.ENGRAVED_RUSTSLATE.get());
    }

    private static void stonecut(RecipeOutput output, ItemLike input, ItemLike result) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result)
                .unlockedBy(ModRecipeProvider.hasName(input), ModRecipeProvider.hasItem(input))
                .save(output, Upfall.id(ModRecipeProvider.itemName(result) + "_from_" + ModRecipeProvider.itemName(input) + "_stonecutting"));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(EngineBlocks.DRAIN_VALVE.get(), EngineBlocks.DRAIN_REGULATOR.get(),
                EngineBlocks.ENGRAVED_RUSTSLATE.get());
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        sounds.addSound(EngineSounds.VALVE_TURN, ModSoundProvider.def(EngineSounds.VALVE_TURN)
                .with(ModSoundProvider.event("block.iron_trapdoor.open").pitch(0.6).volume(0.8),
                        ModSoundProvider.event("block.chain.place").pitch(0.7)));
        sounds.addSound(EngineSounds.REGULATOR_CLANK, ModSoundProvider.def(EngineSounds.REGULATOR_CLANK)
                .with(ModSoundProvider.event("block.anvil.land").pitch(0.6).volume(0.25),
                        ModSoundProvider.event("block.iron_door.close").pitch(0.5)));
        sounds.addSound(EngineSounds.REGULATOR_DRAIN, ModSoundProvider.def(EngineSounds.REGULATOR_DRAIN)
                .with(ModSoundProvider.event("block.bubble_column.whirlpool_inside").pitch(0.6).volume(0.7),
                        ModSoundProvider.event("block.fire.extinguish").pitch(0.5).volume(0.4)));
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        context.register(EngineStructures.STALLED_ENGINE, new StalledEngineStructure(
                new Structure.StructureSettings.Builder(context.lookup(Registries.BIOME).getOrThrow(HAS_STALLED_ENGINE))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        context.register(EngineStructures.STALLED_ENGINE_SET, new StructureSet(context.lookup(Registries.STRUCTURE).getOrThrow(EngineStructures.STALLED_ENGINE),
                new RandomSpreadStructurePlacement(12, 5, RandomSpreadType.LINEAR, 1912202611)));
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new Named("Stalled Engine loot tables", new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(EngineLoot::new, LootContextParamSets.CHEST)), registries)));
        generator.addProvider(event.includeServer(), new Named("Stalled Engine advancements",
                new AdvancementProvider(output, registries, helper, List.of(EngineData::advancements))));
        generator.addProvider(event.includeServer(), new BiomeTagsProvider(output, registries, Upfall.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(HAS_STALLED_ENGINE).add(ModBiomes.RUSTWORKS);
            }

            @Override
            public String getName() {
                return "Stalled Engine Biome Tags";
            }
        });
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        AdvancementHolder engine = Advancement.Builder.advancement().parent(enter)
                .display(RustworksBlocks.HEARTGEAR.get(), Component.translatable("advancements.upfall.stalled_engine.title"),
                        Component.translatable("advancements.upfall.stalled_engine.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("visited", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(EngineStructures.STALLED_ENGINE))))
                .save(saver, Upfall.id("upfall/stalled_engine").toString());
        AdvancementHolder gear = obtain(saver, engine, "kairite_gear", RustworksItems.KAIRITE_GEAR.get(), RustworksItems.KAIRITE_GEAR.get());
        obtain(saver, gear, "machine", MachinesBlocks.HASTENING_GEAR.get(), MachinesBlocks.GEARLIFT.get(), MachinesBlocks.PNEUMATIC_PIPE.get(),
                MachinesBlocks.HASTENING_GEAR.get(), MachinesBlocks.REWINDER.get(), MachinesItems.PHASE_WATCH.get(), MachinesBlocks.VOID_TETHER.get());
        obtain(saver, gear, "veil_mask", QuantumItems.VEIL_MASK.get(), QuantumItems.VEIL_MASK.get());
    }

    private static AdvancementHolder obtain(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon, ItemLike... items) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, Component.translatable("advancements.upfall." + name + ".title"),
                        Component.translatable("advancements.upfall." + name + ".description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items)))
                .save(saver, Upfall.id("upfall/" + name).toString());
    }

    private record Named(String name, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
