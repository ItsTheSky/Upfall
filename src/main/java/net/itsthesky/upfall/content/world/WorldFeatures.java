package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.world.feature.CloudletFeature;
import net.itsthesky.upfall.content.world.feature.CloudstalkPatchFeature;
import net.itsthesky.upfall.content.world.feature.NeedleTipFeature;
import net.itsthesky.upfall.content.world.feature.RaincloudPatchFeature;
import net.itsthesky.upfall.content.world.feature.SkyLakeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WorldFeatures {
    private WorldFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Upfall.MODID);

    public static final DeferredHolder<Feature<?>, SkyLakeFeature> SKY_LAKE = FEATURES.register("sky_lake",
            () -> new SkyLakeFeature(SkyLakeFeature.Config.CODEC));
    public static final DeferredHolder<Feature<?>, NeedleTipFeature> NEEDLE_TIP = FEATURES.register("needle_tip",
            () -> new NeedleTipFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CloudstalkPatchFeature> CLOUDSTALK_PATCH = FEATURES.register("cloudstalk_patch",
            () -> new CloudstalkPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CloudletFeature> CLOUDLET = FEATURES.register("cloudlet",
            () -> new CloudletFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, RaincloudPatchFeature> RAINCLOUD_PATCH = FEATURES.register("raincloud_patch",
            () -> new RaincloudPatchFeature(NoneFeatureConfiguration.CODEC));

    public static final class Configured {
        public static final ResourceKey<ConfiguredFeature<?, ?>> SKY_POND = key("sky_pond");
        public static final ResourceKey<ConfiguredFeature<?, ?>> SKY_LAKE = key("sky_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> NEEDLE_TIP = key("needle_tip");
        public static final ResourceKey<ConfiguredFeature<?, ?>> CLOUDSTALK_PATCH = key("cloudstalk_patch");
        public static final ResourceKey<ConfiguredFeature<?, ?>> CLOUDLET = key("cloudlet");
        public static final ResourceKey<ConfiguredFeature<?, ?>> RAINCLOUD_PATCH = key("raincloud_patch");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_PEARLED_CLOUD = key("ore_pearled_cloud");

        private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
            return ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id(name));
        }
    }

    public static final class Placed {
        public static final ResourceKey<PlacedFeature> SKY_POND = key("sky_pond");
        public static final ResourceKey<PlacedFeature> SKY_POND_RARE = key("sky_pond_rare");
        public static final ResourceKey<PlacedFeature> SKY_LAKE = key("sky_lake");
        public static final ResourceKey<PlacedFeature> NEEDLE_TIP = key("needle_tip");
        public static final ResourceKey<PlacedFeature> CLOUDSTALKS = key("cloudstalks");
        public static final ResourceKey<PlacedFeature> CLOUDSTALKS_DENSE = key("cloudstalks_dense");
        public static final ResourceKey<PlacedFeature> CLOUDLETS = key("cloudlets");
        public static final ResourceKey<PlacedFeature> CLOUDLETS_RARE = key("cloudlets_rare");
        public static final ResourceKey<PlacedFeature> RAINCLOUD_PATCH = key("raincloud_patch");
        public static final ResourceKey<PlacedFeature> ORE_PEARLED_CLOUD = key("ore_pearled_cloud");

        private static ResourceKey<PlacedFeature> key(String name) {
            return ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id(name));
        }
    }
}
