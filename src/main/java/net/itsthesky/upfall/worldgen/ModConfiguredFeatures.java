package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import java.util.List;

public final class ModConfiguredFeatures {
    private ModConfiguredFeatures() {}

    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYREACH_TREE = ModFeatureKeys.SKYREACH_TREE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> GILDROOT_TREE = ModFeatureKeys.GILDROOT_TREE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> HANGING_GLOWVINE = ModFeatureKeys.key("hanging_glowvine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MISTGLOW_CRYSTAL = ModFeatureKeys.key("mistglow_crystal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKY_STREAM = ModFeatureKeys.key("sky_stream");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UPDRAFT_COLUMN = ModFeatureKeys.key("updraft_column");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UPFALL_CASCADE = ModFeatureKeys.key("upfall_cascade");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DRIFTING_RUINS = ModFeatureKeys.key("drifting_ruins");
    public static final ResourceKey<ConfiguredFeature<?, ?>> INVERTED_OBSERVATORY = ModFeatureKeys.key("inverted_observatory");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_NIMBUSITE = ModFeatureKeys.key("ore_nimbusite");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_LEVITITE = ModFeatureKeys.key("ore_levitite");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GILDED_VEIN = ModFeatureKeys.key("gilded_vein");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GILDED_TUFT = ModFeatureKeys.key("patch_gilded_tuft");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CLOUD_FERN = ModFeatureKeys.key("patch_cloud_fern");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_DUSKBLOOM = ModFeatureKeys.key("patch_duskbloom");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        none(context, SKYREACH_TREE, ModFeatures.SKYREACH_TREE.get());
        none(context, GILDROOT_TREE, ModFeatures.GILDROOT_TREE.get());
        none(context, HANGING_GLOWVINE, ModFeatures.HANGING_GLOWVINE.get());
        none(context, MISTGLOW_CRYSTAL, ModFeatures.MISTGLOW_CRYSTAL.get());
        none(context, SKY_STREAM, ModFeatures.SKY_STREAM.get());
        none(context, DRIFTING_RUINS, ModFeatures.DRIFTING_RUINS.get());
        none(context, INVERTED_OBSERVATORY, ModFeatures.INVERTED_OBSERVATORY.get());
        none(context, UPDRAFT_COLUMN, ModFeatures.UPDRAFT_COLUMN.get());
        none(context, UPFALL_CASCADE, ModFeatures.UPFALL_CASCADE.get());

        BlockMatchTest pearlstone = new BlockMatchTest(ModBlocks.PEARLSTONE.get());
        BlockMatchTest duskslate = new BlockMatchTest(ModBlocks.DUSKSLATE.get());
        FeatureUtils.register(context, ORE_NIMBUSITE, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(pearlstone, ModBlocks.NIMBUSITE_ORE.get().defaultBlockState()),
                OreConfiguration.target(duskslate, ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get().defaultBlockState())), 7));
        FeatureUtils.register(context, ORE_LEVITITE, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(duskslate, ModBlocks.LEVITITE_ORE.get().defaultBlockState())), 4, 0.5F));
        FeatureUtils.register(context, GILDED_VEIN, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(duskslate, ModBlocks.GILDED_DUSKSLATE.get().defaultBlockState())), 26));

        patch(context, PATCH_GILDED_TUFT, ModBlocks.GILDED_TUFT.get(), 32);
        patch(context, PATCH_CLOUD_FERN, ModBlocks.CLOUD_FERN.get(), 32);
        patch(context, PATCH_DUSKBLOOM, ModBlocks.DUSKBLOOM.get(), 24);
        UpfallModules.forEachData(data -> data.configuredFeatures(context));
    }

    private static void none(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key,
                             Feature<NoneFeatureConfiguration> feature) {
        FeatureUtils.register(context, key, feature, NoneFeatureConfiguration.INSTANCE);
    }

    private static void patch(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, Block block, int tries) {
        FeatureUtils.register(context, key, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(block)), List.of(), tries));
    }
}
