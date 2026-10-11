package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.itsthesky.upfall.worldgen.placement.SurfaceScanPlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RustworksWorldgen {
    private RustworksWorldgen() {}

    private static final int MIN_Y = UpfallTerrain.CLOUD_BAND_MAX;
    private static final int MAX_Y = 300;
    private static final Set<ResourceKey<Biome>> RUSTWORKS = Set.of(ModBiomes.RUSTWORKS);

    public static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, RustworksFeatures.Configured.PLUNGING_PIPE, RustworksFeatures.PLUNGING_PIPE.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.GIANT_VALVE, RustworksFeatures.GIANT_VALVE.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.COG_CLUSTER, RustworksFeatures.COG_CLUSTER.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.LEAKING_PIPE, RustworksFeatures.LEAKING_PIPE.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.EXHAUST_VENT, RustworksFeatures.EXHAUST_VENT.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.STALLED_AUTOMATON, RustworksFeatures.STALLED_AUTOMATON.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.ORE_KAIRITE, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(RustworksBlocks.RUSTSLATE.get()), RustworksBlocks.KAIRITE_ORE.get().defaultBlockState())), 5));
        FeatureUtils.register(context, RustworksFeatures.Configured.IRONBARK_TREE, RustworksFeatures.IRONBARK_TREE.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.IRONBARK_TREE_LARGE, RustworksFeatures.IRONBARK_TREE_LARGE.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, RustworksFeatures.Configured.IRONBARK_GROVE, Feature.RANDOM_PATCH, new RandomPatchConfiguration(6, 5, 2,
                Holder.direct(new PlacedFeature(Holder.direct(new ConfiguredFeature<>(RustworksFeatures.IRONBARK_TREE.get(), NoneFeatureConfiguration.INSTANCE)), List.of()))));
        RustworksFloraWorldgen.configuredFeatures(context);
    }

    public static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        BlockPredicate onTurf = BlockPredicate.matchesBlocks(RustworksBlocks.OXIDE_TURF.get());
        register(context, configured, RustworksFeatures.Placed.PLUNGING_PIPE, RustworksFeatures.Configured.PLUNGING_PIPE,
                RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.GIANT_VALVE, RustworksFeatures.Configured.GIANT_VALVE,
                RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.COG_CLUSTER, RustworksFeatures.Configured.COG_CLUSTER,
                RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.LEAKING_PIPE, RustworksFeatures.Configured.LEAKING_PIPE,
                RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.EXHAUST_VENT, RustworksFeatures.Configured.EXHAUST_VENT,
                InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.STALLED_AUTOMATON, RustworksFeatures.Configured.STALLED_AUTOMATON,
                RarityFilter.onAverageOnceEvery(120), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.ORE_KAIRITE, RustworksFeatures.Configured.ORE_KAIRITE,
                CountPlacement.of(8), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(MIN_Y + 10), VerticalAnchor.absolute(240)), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.IRONBARK_GROVE, RustworksFeatures.Configured.IRONBARK_GROVE,
                RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.IRONBARK_SOLITARY, RustworksFeatures.Configured.IRONBARK_TREE,
                RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RustworksFeatures.Placed.IRONBARK_LARGE, RustworksFeatures.Configured.IRONBARK_TREE_LARGE,
                RarityFilter.onAverageOnceEvery(14), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onTurf, MIN_Y, MAX_Y), BiomeFilter.biome());
        RustworksFloraWorldgen.placedFeatures(context);
    }

    public static void biomeFeatures(DataContribution.BiomeFeatureSink sink) {
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.PLUNGING_PIPE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.GIANT_VALVE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.LEAKING_PIPE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.COG_CLUSTER, RUSTWORKS);
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.STALLED_AUTOMATON, RUSTWORKS);
        sink.add(GenerationStep.Decoration.SURFACE_STRUCTURES, RustworksFeatures.Placed.EXHAUST_VENT, RUSTWORKS);
        sink.add(GenerationStep.Decoration.UNDERGROUND_ORES, RustworksFeatures.Placed.ORE_KAIRITE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, RustworksFeatures.Placed.IRONBARK_LARGE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, RustworksFeatures.Placed.IRONBARK_GROVE, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, RustworksFeatures.Placed.IRONBARK_SOLITARY, RUSTWORKS);
        RustworksFloraWorldgen.biomeFeatures(sink);
    }

    private static void register(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured,
                                 ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
        PlacementUtils.register(context, key, configured.getOrThrow(feature), new ArrayList<>(List.of(modifiers)));
    }
}
