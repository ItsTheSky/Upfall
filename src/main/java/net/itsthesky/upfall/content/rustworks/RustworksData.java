package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.content.rustworks.block.PipeValveBlock;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksStructures;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksWorldgen;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class RustworksData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        RustworksSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        RustworksFloraData.blockStates(states);
        ModelFile turf = states.models().cubeBottomTop("oxide_turf", states.blockTexture("oxide_turf_side"),
                states.blockTexture("rustsoil"), states.blockTexture("oxide_turf_top"));
        states.simpleBlock(RustworksBlocks.OXIDE_TURF.get(), ConfiguredModel.allYRotations(turf, 0, false));
        states.blockItem(RustworksBlocks.OXIDE_TURF.get());

        for (DeferredBlock<?> block : List.of(RustworksBlocks.RUSTSOIL, RustworksBlocks.RUSTY_PLATING, RustworksBlocks.COG_PLATING,
                RustworksBlocks.HEARTGEAR)) {
            states.simpleBlockWithItem(block.get(), states.cubeAll(block.get()));
        }

        kairiteOre(states);

        states.stone(RustworksBlocks.RUSTSLATE, RustworksBlocks.RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.COBBLED_RUSTSLATE, RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.POLISHED_RUSTSLATE, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.RUSTSLATE_BRICKS, RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);

        pipe(states, RustworksBlocks.RUSTY_PIPE.get(), "rusty_pipe_side");
        pipe(states, RustworksBlocks.LEAKING_PIPE.get(), "leaking_pipe_side");
        valve(states);
        RustworksVentData.blockStates(states);

        states.axisBlock(RustworksBlocks.RUSTY_COG.get(), states.blockTexture("rusty_cog_side"), states.blockTexture("rusty_cog_end"));
        states.blockItem(RustworksBlocks.RUSTY_COG.get());

        ResourceLocation content = states.blockTexture("stillflow_still");
        ModelFile[] levels = {
                states.models().withExistingParent("stillflow_cauldron_level1", states.mcLoc("block/template_cauldron_level1")).texture("content", content),
                states.models().withExistingParent("stillflow_cauldron_level2", states.mcLoc("block/template_cauldron_level2")).texture("content", content),
                states.models().withExistingParent("stillflow_cauldron_full", states.mcLoc("block/template_cauldron_full")).texture("content", content)};
        states.getVariantBuilder(RustworksBlocks.STILLFLOW_CAULDRON.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(levels[state.getValue(LayeredCauldronBlock.LEVEL) - 1]).build());
    }

    private static void kairiteOre(ModBlockStateProvider states) {
        Block ore = RustworksBlocks.KAIRITE_ORE.get();
        String[] names = {"kairite_ore", "kairite_ore_warm", "kairite_ore_peak"};
        ModelFile[] models = new ModelFile[3];
        for (int i = 0; i < 3; i++) {
            models[i] = states.models().cubeAll(names[i], states.blockTexture(names[i]));
        }
        states.getVariantBuilder(ore).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(models[state.getValue(KairiteOreBlock.GLOW)]).build());
        states.blockItem(ore);
    }

    private static void pipe(ModBlockStateProvider states, RotatedPillarBlock block, String side) {
        String name = ModBlockStateProvider.name(block);
        BlockModelBuilder model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("side", states.blockTexture(side)).texture("end", states.blockTexture("rusty_pipe_end"))
                .texture("particle", states.blockTexture(side)).renderType("cutout");
        model.element().from(2, 0, 2).to(14, 16, 14).allFaces((dir, face) -> {
            if (dir.getAxis() == Direction.Axis.Y) {
                face.texture("#end").uvs(2, 2, 14, 14).cullface(dir);
            } else {
                face.texture("#side").uvs(2, 0, 14, 16);
            }
        }).end();
        states.getVariantBuilder(block).forAllStates(state -> switch (state.getValue(RotatedPillarBlock.AXIS)) {
            case Y -> ConfiguredModel.builder().modelFile(model).build();
            case Z -> ConfiguredModel.builder().modelFile(model).rotationX(90).build();
            case X -> ConfiguredModel.builder().modelFile(model).rotationX(90).rotationY(90).build();
        });
        states.blockItem(block);
    }

    private static void valve(ModBlockStateProvider states) {
        BlockModelBuilder model = states.models().withExistingParent("pipe_valve", states.mcLoc("block/block"))
                .texture("wheel", states.blockTexture("pipe_valve")).texture("stem", states.blockTexture("rusty_pipe_side"))
                .texture("particle", states.blockTexture("pipe_valve")).renderType("cutout");
        model.element().from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture("#stem")
                .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)).end();
        model.element().from(2, 6, 2).to(14, 8, 14).allFaces((dir, face) -> {
            face.texture("#wheel");
            if (dir.getAxis() == Direction.Axis.Y) {
                face.uvs(2, 2, 14, 14);
            } else {
                face.uvs(2, 2, 14, 4);
            }
        }).end();
        states.getVariantBuilder(RustworksBlocks.PIPE_VALVE.get()).forAllStates(state -> {
            Direction facing = state.getValue(PipeValveBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
        states.blockItem(RustworksBlocks.PIPE_VALVE.get());
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(RustworksItems.KAIRITE_DUST.get());
        models.basicItem(RustworksItems.KAIRITE_GEAR.get());
        models.basicItem(RustworksItems.RUSTY_PLATE.get());
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        RustworksFloraData.blockLoot(loot);
        loot.add(RustworksBlocks.OXIDE_TURF.get(), loot.createSingleItemTableWithSilkTouch(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get()));
        loot.add(RustworksBlocks.RUSTSLATE.get(), loot.createSingleItemTableWithSilkTouch(RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE.get()));
        for (ModBlocks.StoneVariants variants : RustworksBlocks.allVariants()) {
            loot.add(variants.slab().get(), loot.createSlabItemTable(variants.slab().get()));
        }
        kairiteOreLoot(loot);
        loot.dropOther(RustworksBlocks.STILLFLOW_CAULDRON.get(), Items.CAULDRON);
    }

    private static void kairiteOreLoot(ModBlockLootProvider loot) {
        Block ore = RustworksBlocks.KAIRITE_ORE.get();
        LootItemBlockStatePropertyCondition.Builder peak = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ore)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(KairiteOreBlock.GLOW, 2));
        loot.add(ore, loot.createSilkTouchDispatchTable(ore, AlternativesEntry.alternatives(
                LootItem.lootTableItem(RustworksItems.KAIRITE_GEAR.get()).when(peak),
                (LootPoolEntryContainer.Builder<?>) loot.explosionDecay(ore, LootItem.lootTableItem(RustworksItems.KAIRITE_DUST.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(loot.fortuneEnchantment()))))));
    }

    @Override
    public void recipes(RecipeOutput output) {
        RustworksFloraData.recipes(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, RustworksItems.KAIRITE_GEAR.get())
                .requires(RustworksItems.KAIRITE_DUST.get(), 9)
                .unlockedBy(ModRecipeProvider.hasName(RustworksItems.KAIRITE_DUST.get()), ModRecipeProvider.hasItem(RustworksItems.KAIRITE_DUST.get()))
                .save(output, Upfall.id("kairite_gear_from_dust"));

        metalRecipes(output);
        RustworksVentData.recipes(output);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(RustworksBlocks.COBBLED_RUSTSLATE.get()), RecipeCategory.BUILDING_BLOCKS,
                        RustworksBlocks.RUSTSLATE.get(), 0.1F, 200)
                .unlockedBy(ModRecipeProvider.hasName(RustworksBlocks.COBBLED_RUSTSLATE.get()), ModRecipeProvider.hasItem(RustworksBlocks.COBBLED_RUSTSLATE.get()))
                .save(output, Upfall.id("rustslate_from_smelting"));
        square(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE.get());
        square(output, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.POLISHED_RUSTSLATE.get());

        family(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_VARIANTS);
        family(output, RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
        family(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        family(output, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);

        cutAll(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_VARIANTS);
        cutAll(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        cutAll(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cut(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE.get(), 1);
        cut(output, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICKS.get(), 1);
        cutAll(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        cutAll(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cut(output, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICKS.get(), 1);
        cutAll(output, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cutAll(output, RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
    }

    private static void metalRecipes(RecipeOutput output) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        Item dust = RustworksItems.KAIRITE_DUST.get();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, plate, 4).requires(Items.COPPER_INGOT).requires(dust)
                .unlockedBy(ModRecipeProvider.hasName(dust), ModRecipeProvider.hasItem(dust))
                .save(output, Upfall.id("rusty_plate"));
        Block plating = RustworksBlocks.RUSTY_PLATING.get();
        Block pipe = RustworksBlocks.RUSTY_PIPE.get();
        Block valve = RustworksBlocks.PIPE_VALVE.get();
        metal(plating, 4).define('#', plate).pattern("##").pattern("##").save(output);
        metal(RustworksBlocks.COG_PLATING.get(), 4).define('#', plate).define('P', plating).pattern("#P").pattern("P#").save(output);
        metal(pipe, 6).define('#', plate).pattern("# #").pattern("# #").pattern("# #").save(output);
        metal(valve, 1).define('#', plate).define('P', pipe).pattern(" # ").pattern("###").pattern(" P ").save(output);
        metal(RustworksBlocks.RUSTY_COG.get(), 1).define('#', plate).define('P', plating).pattern(" # ").pattern("#P#").pattern(" # ").save(output);
        metal(RustworksBlocks.LEAKING_PIPE.get(), 1).define('P', pipe).define('D', dust).pattern("P").pattern("D")
                .unlockedBy(ModRecipeProvider.hasName(dust), ModRecipeProvider.hasItem(dust)).save(output);
    }

    private static ShapedRecipeBuilder metal(Block result, int count) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(ModRecipeProvider.hasName(plate), ModRecipeProvider.hasItem(plate));
    }

    private static void square(RecipeOutput output, Block result, Block input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4).define('#', input).pattern("##").pattern("##")
                .unlockedBy(ModRecipeProvider.hasName(input), ModRecipeProvider.hasItem(input)).save(output);
    }

    private static void family(RecipeOutput output, Block base, ModBlocks.StoneVariants variants) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, variants.stairs().get(), 4).define('#', base)
                .pattern("#  ").pattern("## ").pattern("###")
                .unlockedBy(ModRecipeProvider.hasName(base), ModRecipeProvider.hasItem(base)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, variants.slab().get(), 6).define('#', base).pattern("###")
                .unlockedBy(ModRecipeProvider.hasName(base), ModRecipeProvider.hasItem(base)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, variants.wall().get(), 6).define('#', base).pattern("###").pattern("###")
                .unlockedBy(ModRecipeProvider.hasName(base), ModRecipeProvider.hasItem(base)).save(output);
    }

    private static void cutAll(RecipeOutput output, Block base, ModBlocks.StoneVariants variants) {
        cut(output, base, variants.stairs().get(), 1);
        cut(output, base, variants.slab().get(), 2);
        cut(output, base, variants.wall().get(), 1);
    }

    private static void cut(RecipeOutput output, Block input, Block result, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(ModRecipeProvider.hasName(input), ModRecipeProvider.hasItem(input))
                .save(output, Upfall.id(ModRecipeProvider.itemName(result) + "_from_" + ModRecipeProvider.itemName(input) + "_stonecutting"));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        RustworksFloraData.blockTags(tags);
        var pickaxe = tags.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        pickaxe.add(RustworksBlocks.EXHAUST_VENT.get());
        pickaxe.add(RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE.get(),
                RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.KAIRITE_ORE.get(), RustworksBlocks.RUSTY_PLATING.get(), RustworksBlocks.COG_PLATING.get(),
                RustworksBlocks.RUSTY_PIPE.get(), RustworksBlocks.LEAKING_PIPE.get(), RustworksBlocks.PIPE_VALVE.get(),
                RustworksBlocks.RUSTY_COG.get(), RustworksBlocks.HEARTGEAR.get(), RustworksBlocks.STILLFLOW_CAULDRON.get());
        for (ModBlocks.StoneVariants v : RustworksBlocks.allVariants()) {
            pickaxe.add(v.stairs().get(), v.slab().get(), v.wall().get());
            tags.tag(BlockTags.STAIRS).add(v.stairs().get());
            tags.tag(BlockTags.SLABS).add(v.slab().get());
            tags.tag(BlockTags.WALLS).add(v.wall().get());
        }
        tags.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get());
        tags.tag(BlockTags.DIRT).add(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get());
        tags.tag(BlockTags.NEEDS_IRON_TOOL).add(RustworksBlocks.KAIRITE_ORE.get());
        tags.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(RustworksBlocks.HEARTGEAR.get());
        tags.tag(BlockTags.CAULDRONS).add(RustworksBlocks.STILLFLOW_CAULDRON.get());
    }

    @Override
    public void particles(net.itsthesky.upfall.datagen.ModParticleProvider particles) {
        RustworksFloraData.particles(particles);
    }

    @Override
    public void dataMaps(net.itsthesky.upfall.datagen.ModDataMapProvider dataMaps) {
        RustworksFloraData.dataMaps(dataMaps);
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        RustworksVentData.sounds(sounds);
        sounds.addSound(RustworksSounds.AMBIENT_MOOD, ModSoundProvider.def(RustworksSounds.AMBIENT_MOOD)
                .with(ModSoundProvider.event("ambient.basalt_deltas.mood").pitch(0.7).volume(0.5)));
        sounds.addSound(RustworksSounds.AMBIENT_ADDITIONS, ModSoundProvider.def(RustworksSounds.AMBIENT_ADDITIONS)
                .with(ModSoundProvider.event("block.chain.step").pitch(0.5).volume(0.4).weight(3),
                        ModSoundProvider.event("block.anvil.land").pitch(0.5).volume(0.08),
                        ModSoundProvider.event("block.iron_door.close").pitch(0.5).volume(0.2).weight(2),
                        ModSoundProvider.event("block.pointed_dripstone.drip_water").pitch(0.8).volume(0.5).weight(3),
                        ModSoundProvider.event("block.pointed_dripstone.drip_water_into_cauldron").pitch(0.7).volume(0.4).weight(2),
                        ModSoundProvider.event("entity.breeze.inhale").pitch(0.5).volume(0.25).weight(2),
                        ModSoundProvider.event("block.lava.extinguish").pitch(0.6).volume(0.1)));
    }

    @Override
    public void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RustworksWorldgen.configuredFeatures(context);
    }

    @Override
    public void placedFeatures(BootstrapContext<PlacedFeature> context) {
        RustworksWorldgen.placedFeatures(context);
    }

    @Override
    public void biomeFeatures(BiomeFeatureSink sink) {
        RustworksWorldgen.biomeFeatures(sink);
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        RustworksStructures.structures(context);
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        RustworksStructures.structureSets(context);
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), RustworksLoot.provider(output, registries));
        generator.addProvider(event.includeServer(), new BiomeTagsProvider(output, registries, Upfall.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(RustworksStructures.HAS_PIPE_BRIDGE).add(ModBiomes.RUSTWORKS);
            }

            @Override
            public String getName() {
                return "Pipe Bridge Biome Tags";
            }
        });
    }
}
