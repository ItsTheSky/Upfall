package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ModFeatureKeys {
    private ModFeatureKeys() {}

    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYREACH_TREE = key("skyreach_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GILDROOT_TREE = key("gildroot_tree");

    public static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id(name));
    }
}
