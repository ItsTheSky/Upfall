package net.itsthesky.upfall.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface DataContribution {
    DataContribution NONE = new DataContribution() {
    };

    @FunctionalInterface
    interface BiomeFeatureSink {
        void add(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature, Set<ResourceKey<Biome>> biomes);
    }

    default void lang(ModLanguageProvider lang) {}

    default void blockStates(ModBlockStateProvider states) {}

    default void itemModels(ModItemModelProvider models) {}

    default void blockLoot(ModBlockLootProvider loot) {}

    default void recipes(RecipeOutput output) {}

    default void blockTags(ModTagProviders.Blocks tags) {}

    default void itemTags(ModTagProviders.Items tags) {}

    default void sounds(ModSoundProvider sounds) {}

    default void particles(ModParticleProvider particles) {}

    default void dataMaps(ModDataMapProvider dataMaps) {}

    default void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {}

    default void placedFeatures(BootstrapContext<PlacedFeature> context) {}

    default void biomeFeatures(BiomeFeatureSink sink) {}

    default void biomeSpawns(ResourceKey<Biome> biome, MobSpawnSettings.Builder spawns) {}

    default void structures(BootstrapContext<Structure> context) {}

    default void structureSets(BootstrapContext<StructureSet> context) {}

    default void enchantments(BootstrapContext<Enchantment> context) {}

    default void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                              CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {}
}
