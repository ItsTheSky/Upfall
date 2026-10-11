package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.worldgen.feature.DriftingRuinsFeature;
import net.itsthesky.upfall.worldgen.feature.GildrootTreeFeature;
import net.itsthesky.upfall.worldgen.feature.InvertedObservatoryFeature;
import net.itsthesky.upfall.worldgen.feature.HangingGlowvineFeature;
import net.itsthesky.upfall.worldgen.feature.MistglowCrystalFeature;
import net.itsthesky.upfall.worldgen.feature.SkyStreamFeature;
import net.itsthesky.upfall.worldgen.feature.SkyreachTreeFeature;
import net.itsthesky.upfall.worldgen.feature.UpdraftColumnFeature;
import net.itsthesky.upfall.worldgen.feature.UpfallCascadeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Upfall.MODID);

    public static final DeferredHolder<Feature<?>, SkyreachTreeFeature> SKYREACH_TREE = FEATURES.register("skyreach_tree",
            () -> new SkyreachTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, GildrootTreeFeature> GILDROOT_TREE = FEATURES.register("gildroot_tree",
            () -> new GildrootTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, HangingGlowvineFeature> HANGING_GLOWVINE = FEATURES.register("hanging_glowvine",
            () -> new HangingGlowvineFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MistglowCrystalFeature> MISTGLOW_CRYSTAL = FEATURES.register("mistglow_crystal",
            () -> new MistglowCrystalFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, UpdraftColumnFeature> UPDRAFT_COLUMN = FEATURES.register("updraft_column",
            () -> new UpdraftColumnFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, UpfallCascadeFeature> UPFALL_CASCADE = FEATURES.register("upfall_cascade",
            () -> new UpfallCascadeFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, DriftingRuinsFeature> DRIFTING_RUINS = FEATURES.register("drifting_ruins",
            () -> new DriftingRuinsFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InvertedObservatoryFeature> INVERTED_OBSERVATORY = FEATURES.register("inverted_observatory",
            () -> new InvertedObservatoryFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, SkyStreamFeature> SKY_STREAM = FEATURES.register("sky_stream",
            () -> new SkyStreamFeature(NoneFeatureConfiguration.CODEC));
}
