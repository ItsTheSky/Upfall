package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.placement.SurfaceScanPlacement;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.ArrayList;
import java.util.List;

public final class ModPlacedFeatures {
    private ModPlacedFeatures() {}

    public static final ResourceKey<PlacedFeature> SKY_STREAM = key("sky_stream");
    public static final ResourceKey<PlacedFeature> UPDRAFT_FROM_CLOUDS = key("updraft_from_clouds");
    public static final ResourceKey<PlacedFeature> UPDRAFT_FROM_ISLANDS = key("updraft_from_islands");
    public static final ResourceKey<PlacedFeature> UPFALL_CASCADE = key("upfall_cascade");
    public static final ResourceKey<PlacedFeature> UPFALL_CASCADE_DENSE = key("upfall_cascade_dense");
    public static final ResourceKey<PlacedFeature> DRIFTING_RUINS = key("drifting_ruins");
    public static final ResourceKey<PlacedFeature> INVERTED_OBSERVATORY = key("inverted_observatory");
    public static final ResourceKey<PlacedFeature> ORE_NIMBUSITE = key("ore_nimbusite");
    public static final ResourceKey<PlacedFeature> GILDED_VEIN = key("gilded_vein");
    public static final ResourceKey<PlacedFeature> ORE_LEVITITE = key("ore_levitite");
    public static final ResourceKey<PlacedFeature> MISTGLOW_CRYSTAL = key("mistglow_crystal");
    public static final ResourceKey<PlacedFeature> GLOWVINES = key("glowvines");
    public static final ResourceKey<PlacedFeature> GLOWVINES_DENSE = key("glowvines_dense");
    public static final ResourceKey<PlacedFeature> SKYREACH_TREES = key("skyreach_trees");
    public static final ResourceKey<PlacedFeature> SKYREACH_TREES_SPARSE = key("skyreach_trees_sparse");
    public static final ResourceKey<PlacedFeature> GILDROOT_TREES = key("gildroot_trees");
    public static final ResourceKey<PlacedFeature> PATCH_GILDED_TUFT = key("patch_gilded_tuft");
    public static final ResourceKey<PlacedFeature> PATCH_GILDED_TUFT_DENSE = key("patch_gilded_tuft_dense");
    public static final ResourceKey<PlacedFeature> PATCH_CLOUD_FERN = key("patch_cloud_fern");
    public static final ResourceKey<PlacedFeature> PATCH_DUSKBLOOM = key("patch_duskbloom");

    private static final int MIN_Y = UpfallTerrain.CLOUD_BAND_MAX;
    private static final int MAX_Y = 300;

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        BlockPredicate onGrass = BlockPredicate.matchesBlocks(ModBlocks.SKYGRASS_BLOCK.get());
        BlockPredicate underIsland = BlockPredicate.matchesTag(ModTags.Blocks.SKY_PLANTABLE);
        BlockPredicate underRock = BlockPredicate.matchesBlocks(ModBlocks.PEARLSTONE.get(), ModBlocks.DUSKSLATE.get());
        BlockPredicate underSolid = BlockPredicate.solid();

        register(context, configured, SKY_STREAM, ModConfiguredFeatures.SKY_STREAM,
                RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, UPDRAFT_FROM_CLOUDS, ModConfiguredFeatures.UPDRAFT_COLUMN,
                CountPlacement.of(3), InSquarePlacement.spread(),
                SurfaceScanPlacement.floor(BlockPredicate.matchesBlocks(ModBlocks.MIST.get(), ModBlocks.DENSE_CLOUD.get()),
                        UpfallTerrain.CLOUD_BAND_MIN, MIN_Y),
                BiomeFilter.biome());
        register(context, configured, UPDRAFT_FROM_ISLANDS, ModConfiguredFeatures.UPDRAFT_COLUMN,
                RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, UPFALL_CASCADE, ModConfiguredFeatures.UPFALL_CASCADE,
                RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underSolid, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, UPFALL_CASCADE_DENSE, ModConfiguredFeatures.UPFALL_CASCADE,
                CountPlacement.of(1), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underIsland, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, DRIFTING_RUINS, ModConfiguredFeatures.DRIFTING_RUINS,
                RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, INVERTED_OBSERVATORY, ModConfiguredFeatures.INVERTED_OBSERVATORY,
                RarityFilter.onAverageOnceEvery(14), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underIsland, MIN_Y, MAX_Y),
                BiomeFilter.biome());
        register(context, configured, ORE_NIMBUSITE, ModConfiguredFeatures.ORE_NIMBUSITE,
                CountPlacement.of(18), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(90), VerticalAnchor.absolute(200)), BiomeFilter.biome());
        register(context, configured, GILDED_VEIN, ModConfiguredFeatures.GILDED_VEIN,
                CountPlacement.of(10), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(70), VerticalAnchor.absolute(280)), BiomeFilter.biome());
        register(context, configured, ORE_LEVITITE, ModConfiguredFeatures.ORE_LEVITITE,
                CountPlacement.of(12), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(70), VerticalAnchor.absolute(280)), BiomeFilter.biome());
        register(context, configured, MISTGLOW_CRYSTAL, ModConfiguredFeatures.MISTGLOW_CRYSTAL,
                CountPlacement.of(5), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underRock, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, GLOWVINES, ModConfiguredFeatures.HANGING_GLOWVINE,
                CountPlacement.of(2), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underSolid, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, GLOWVINES_DENSE, ModConfiguredFeatures.HANGING_GLOWVINE,
                CountPlacement.of(4), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underSolid, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, SKYREACH_TREES, ModConfiguredFeatures.SKYREACH_TREE,
                CountPlacement.of(UniformInt.of(0, 2)), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, SKYREACH_TREES_SPARSE, ModConfiguredFeatures.SKYREACH_TREE,
                RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, GILDROOT_TREES, ModConfiguredFeatures.GILDROOT_TREE,
                CountPlacement.of(5), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underIsland, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, PATCH_GILDED_TUFT, ModConfiguredFeatures.PATCH_GILDED_TUFT,
                CountPlacement.of(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, PATCH_GILDED_TUFT_DENSE, ModConfiguredFeatures.PATCH_GILDED_TUFT,
                CountPlacement.of(6), InSquarePlacement.spread(), SurfaceScanPlacement.floor(BlockPredicate.matchesTag(ModTags.Blocks.SKY_PLANTABLE), MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, PATCH_CLOUD_FERN, ModConfiguredFeatures.PATCH_CLOUD_FERN,
                CountPlacement.of(2), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, PATCH_DUSKBLOOM, ModConfiguredFeatures.PATCH_DUSKBLOOM,
                RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, MIN_Y, MAX_Y), BiomeFilter.biome());
        UpfallModules.forEachData(data -> data.placedFeatures(context));
    }

    private static void register(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured,
                                 ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
        PlacementUtils.register(context, key, configured.getOrThrow(feature), new ArrayList<>(List.of(modifiers)));
    }

    static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id(name));
    }
}
