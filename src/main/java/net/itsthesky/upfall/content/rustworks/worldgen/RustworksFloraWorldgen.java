package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.itsthesky.upfall.worldgen.placement.SurfaceScanPlacement;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RustworksFloraWorldgen {
    private RustworksFloraWorldgen() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Upfall.MODID);
    public static final DeferredHolder<Feature<?>, VerdigrisMossFeature> VERDIGRIS_MOSS = FEATURES.register("verdigris_moss", VerdigrisMossFeature::new);
    public static final DeferredHolder<Feature<?>, VerdigrisBloomFeature> VERDIGRIS_BLOOM = FEATURES.register("verdigris_bloom", VerdigrisBloomFeature::new);
    public static final DeferredHolder<Feature<?>, WirevineFeature> WIREVINE = FEATURES.register("wirevine", WirevineFeature::new);

    private static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_CONFIGURED = configured("verdigris_moss_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> BLOOM_CONFIGURED = configured("verdigris_bloom_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> RIVETGRASS_CONFIGURED = configured("rivetgrass_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> WIREVINE_CONFIGURED = configured("wirevine_hanging");
    private static final ResourceKey<PlacedFeature> MOSS_PLACED = placed("verdigris_moss_patch");
    private static final ResourceKey<PlacedFeature> BLOOM_PLACED = placed("verdigris_bloom_patch");
    private static final ResourceKey<PlacedFeature> RIVETGRASS_PLACED = placed("rivetgrass_patch");
    private static final ResourceKey<PlacedFeature> WIREVINE_PLACED = placed("wirevine_hanging");

    private static final int MIN_Y = UpfallTerrain.CLOUD_BAND_MAX;
    private static final int MAX_Y = 300;
    private static final Set<ResourceKey<Biome>> RUSTWORKS = Set.of(ModBiomes.RUSTWORKS);

    public static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, MOSS_CONFIGURED, VERDIGRIS_MOSS.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, BLOOM_CONFIGURED, VERDIGRIS_BLOOM.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, WIREVINE_CONFIGURED, WIREVINE.get(), NoneFeatureConfiguration.INSTANCE);
        BlockState tall = RustworksBlocks.TALL_RIVETGRASS.get().defaultBlockState();
        BlockState shortGrass = RustworksBlocks.RIVETGRASS.get().defaultBlockState();
        FeatureUtils.register(context, RIVETGRASS_CONFIGURED, Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(shortGrass, 5).add(tall, 2).build())),
                List.of(), 40));
    }

    public static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        BlockPredicate ground = BlockPredicate.matchesBlocks(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get());
        BlockPredicate mossGround = BlockPredicate.matchesBlocks(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get(),
                RustworksBlocks.RUSTY_PLATING.get(), RustworksBlocks.COG_PLATING.get());
        BlockPredicate ceiling = BlockPredicate.matchesBlocks(RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSOIL.get(),
                RustworksBlocks.RUSTY_PLATING.get(), RustworksBlocks.COG_PLATING.get(), RustworksBlocks.RUSTY_PIPE.get(),
                RustworksBlocks.LEAKING_PIPE.get(), RustworksBlocks.RUSTSLATE_BRICKS.get());
        register(context, configured, MOSS_PLACED, MOSS_CONFIGURED,
                CountPlacement.of(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(mossGround, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, RIVETGRASS_PLACED, RIVETGRASS_CONFIGURED,
                CountPlacement.of(4), InSquarePlacement.spread(), SurfaceScanPlacement.floor(ground, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, BLOOM_PLACED, BLOOM_CONFIGURED,
                CountPlacement.of(3), InSquarePlacement.spread(), SurfaceScanPlacement.floor(ground, MIN_Y, MAX_Y), BiomeFilter.biome());
        register(context, configured, WIREVINE_PLACED, WIREVINE_CONFIGURED,
                CountPlacement.of(5), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(ceiling, MIN_Y, MAX_Y), BiomeFilter.biome());
    }

    public static void biomeFeatures(DataContribution.BiomeFeatureSink sink) {
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, MOSS_PLACED, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, RIVETGRASS_PLACED, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, BLOOM_PLACED, RUSTWORKS);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, WIREVINE_PLACED, RUSTWORKS);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id(name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured,
                                 ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
        PlacementUtils.register(context, key, configured.getOrThrow(feature), new ArrayList<>(List.of(modifiers)));
    }
}
