package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RustworksFeatures {
    private RustworksFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Upfall.MODID);

    public static final DeferredHolder<Feature<?>, PlungingPipeFeature> PLUNGING_PIPE = FEATURES.register("plunging_pipe", PlungingPipeFeature::new);
    public static final DeferredHolder<Feature<?>, GiantValveFeature> GIANT_VALVE = FEATURES.register("giant_valve", GiantValveFeature::new);
    public static final DeferredHolder<Feature<?>, CogClusterFeature> COG_CLUSTER = FEATURES.register("cog_cluster", CogClusterFeature::new);
    public static final DeferredHolder<Feature<?>, LeakingPipeFeature> LEAKING_PIPE = FEATURES.register("leaking_pipe", LeakingPipeFeature::new);
    public static final DeferredHolder<Feature<?>, ExhaustVentFeature> EXHAUST_VENT = FEATURES.register("exhaust_vent", ExhaustVentFeature::new);
    public static final DeferredHolder<Feature<?>, StalledAutomatonFeature> STALLED_AUTOMATON = FEATURES.register("stalled_automaton", StalledAutomatonFeature::new);
    public static final DeferredHolder<Feature<?>, IronbarkTreeFeature> IRONBARK_TREE = FEATURES.register("ironbark_tree",
            () -> new IronbarkTreeFeature(NoneFeatureConfiguration.CODEC, false));
    public static final DeferredHolder<Feature<?>, IronbarkTreeFeature> IRONBARK_TREE_LARGE = FEATURES.register("ironbark_tree_large",
            () -> new IronbarkTreeFeature(NoneFeatureConfiguration.CODEC, true));

    public static final class Configured {
        public static final ResourceKey<ConfiguredFeature<?, ?>> PLUNGING_PIPE = key("plunging_pipe");
        public static final ResourceKey<ConfiguredFeature<?, ?>> GIANT_VALVE = key("giant_valve");
        public static final ResourceKey<ConfiguredFeature<?, ?>> COG_CLUSTER = key("cog_cluster");
        public static final ResourceKey<ConfiguredFeature<?, ?>> LEAKING_PIPE = key("leaking_pipe");
        public static final ResourceKey<ConfiguredFeature<?, ?>> EXHAUST_VENT = key("exhaust_vent");
        public static final ResourceKey<ConfiguredFeature<?, ?>> STALLED_AUTOMATON = key("stalled_automaton");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_KAIRITE = key("ore_kairite");
        public static final ResourceKey<ConfiguredFeature<?, ?>> IRONBARK_TREE = key("ironbark_tree");
        public static final ResourceKey<ConfiguredFeature<?, ?>> IRONBARK_TREE_LARGE = key("ironbark_tree_large");
        public static final ResourceKey<ConfiguredFeature<?, ?>> IRONBARK_GROVE = key("ironbark_grove");

        private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
            return ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id(name));
        }
    }

    public static final class Placed {
        public static final ResourceKey<PlacedFeature> PLUNGING_PIPE = key("plunging_pipe");
        public static final ResourceKey<PlacedFeature> GIANT_VALVE = key("giant_valve");
        public static final ResourceKey<PlacedFeature> COG_CLUSTER = key("cog_cluster");
        public static final ResourceKey<PlacedFeature> LEAKING_PIPE = key("leaking_pipe");
        public static final ResourceKey<PlacedFeature> EXHAUST_VENT = key("exhaust_vent");
        public static final ResourceKey<PlacedFeature> STALLED_AUTOMATON = key("stalled_automaton");
        public static final ResourceKey<PlacedFeature> ORE_KAIRITE = key("ore_kairite");
        public static final ResourceKey<PlacedFeature> IRONBARK_GROVE = key("ironbark_grove");
        public static final ResourceKey<PlacedFeature> IRONBARK_SOLITARY = key("ironbark_solitary");
        public static final ResourceKey<PlacedFeature> IRONBARK_LARGE = key("ironbark_large");

        private static ResourceKey<PlacedFeature> key(String name) {
            return ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id(name));
        }
    }
}
