package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModDataMapProvider;
import net.itsthesky.upfall.datagen.ModParticleProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModParticles;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

final class RustworksFloraData {
    private RustworksFloraData() {}

    static void blockStates(ModBlockStateProvider states) {
        Block mossBlock = RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get();
        states.simpleBlockWithItem(mossBlock, states.cubeAll(mossBlock));
        Block carpet = RustworksBlocks.VERDIGRIS_MOSS.get();
        states.simpleBlock(carpet, states.models().carpet("verdigris_moss", states.blockTexture("verdigris_moss_block")));
        states.blockItem(carpet);

        states.crossPlant(RustworksBlocks.RIVETGRASS.get(), "rivetgrass");
        ModelFile bottom = states.models().cross("tall_rivetgrass_bottom", states.blockTexture("tall_rivetgrass_bottom")).renderType("cutout");
        ModelFile top = states.models().cross("tall_rivetgrass_top", states.blockTexture("tall_rivetgrass_top")).renderType("cutout");
        states.getVariantBuilder(RustworksBlocks.TALL_RIVETGRASS.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? bottom : top).build());

        states.crossPlant(RustworksBlocks.VERDIGRIS_BLOOM.get(), "verdigris_bloom");
        states.pottedPlant(RustworksBlocks.POTTED_VERDIGRIS_BLOOM.get(), "verdigris_bloom");

        states.crossPlant(RustworksBlocks.WIREVINE.get(), "wirevine");
        states.simpleBlock(RustworksBlocks.WIREVINE_PLANT.get(),
                states.models().cross("wirevine_plant", states.blockTexture("wirevine_plant")).renderType("cutout"));
    }

    static void blockLoot(ModBlockLootProvider loot) {
        loot.add(RustworksBlocks.RIVETGRASS.get(), loot.shearsOnlyDrop(RustworksBlocks.RIVETGRASS.get()));
        loot.add(RustworksBlocks.TALL_RIVETGRASS.get(), loot.doublePlantShearsDrop(RustworksBlocks.RIVETGRASS.get()));
        loot.pottedContents(RustworksBlocks.POTTED_VERDIGRIS_BLOOM.get());
        loot.add(RustworksBlocks.WIREVINE.get(), loot.shearsOnlyDrop(RustworksBlocks.WIREVINE.get()));
        loot.add(RustworksBlocks.WIREVINE_PLANT.get(), loot.shearsOnlyDrop(RustworksBlocks.WIREVINE.get()));
    }

    static void recipes(RecipeOutput output) {
        Block bloom = RustworksBlocks.VERDIGRIS_BLOOM.get();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CYAN_DYE).requires(bloom).group("cyan_dye")
                .unlockedBy(ModRecipeProvider.hasName(bloom), ModRecipeProvider.hasItem(bloom))
                .save(output, Upfall.id("cyan_dye_from_verdigris_bloom"));
        Block mossBlock = RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, RustworksBlocks.VERDIGRIS_MOSS.get(), 3).define('#', mossBlock).pattern("##")
                .unlockedBy(ModRecipeProvider.hasName(mossBlock), ModRecipeProvider.hasItem(mossBlock))
                .save(output, Upfall.id("verdigris_moss_carpet"));
    }

    static void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_HOE).add(RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get(), RustworksBlocks.VERDIGRIS_MOSS.get());
        tags.tag(BlockTags.SMALL_FLOWERS).add(RustworksBlocks.VERDIGRIS_BLOOM.get());
        tags.tag(BlockTags.FLOWER_POTS).add(RustworksBlocks.POTTED_VERDIGRIS_BLOOM.get());
        tags.tag(BlockTags.CLIMBABLE).add(RustworksBlocks.WIREVINE.get(), RustworksBlocks.WIREVINE_PLANT.get());
        tags.tag(BlockTags.SWORD_EFFICIENT).add(RustworksBlocks.RIVETGRASS.get(), RustworksBlocks.TALL_RIVETGRASS.get(),
                RustworksBlocks.VERDIGRIS_BLOOM.get(), RustworksBlocks.WIREVINE.get(), RustworksBlocks.WIREVINE_PLANT.get());
        tags.tag(BlockTags.REPLACEABLE_BY_TREES).add(RustworksBlocks.RIVETGRASS.get(), RustworksBlocks.TALL_RIVETGRASS.get(),
                RustworksBlocks.WIREVINE.get(), RustworksBlocks.WIREVINE_PLANT.get());
        tags.tag(ModTags.Blocks.FEATURE_REPLACEABLE).add(RustworksBlocks.RIVETGRASS.get(), RustworksBlocks.TALL_RIVETGRASS.get(),
                RustworksBlocks.VERDIGRIS_BLOOM.get(), RustworksBlocks.WIREVINE.get(), RustworksBlocks.WIREVINE_PLANT.get(),
                RustworksBlocks.VERDIGRIS_MOSS.get());
    }

    static void particles(ModParticleProvider particles) {
        particles.addSpriteSet(ModParticles.RUST_FLAKE.get(), Upfall.id("rust_flake"), 4);
    }

    static void dataMaps(ModDataMapProvider dataMaps) {
        var compostables = dataMaps.builder(NeoForgeDataMaps.COMPOSTABLES);
        compostables.add(RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get().asItem().builtInRegistryHolder(), new Compostable(0.65F), false);
        compostables.add(RustworksBlocks.VERDIGRIS_MOSS.get().asItem().builtInRegistryHolder(), new Compostable(0.3F), false);
        compostables.add(RustworksBlocks.RIVETGRASS.get().asItem().builtInRegistryHolder(), new Compostable(0.3F), false);
        compostables.add(RustworksBlocks.VERDIGRIS_BLOOM.get().asItem().builtInRegistryHolder(), new Compostable(0.65F), false);
        compostables.add(RustworksBlocks.WIREVINE.get().asItem().builtInRegistryHolder(), new Compostable(0.5F), false);
    }
}
