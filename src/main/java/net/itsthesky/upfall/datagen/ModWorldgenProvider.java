package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModConfiguredFeatures;
import net.itsthesky.upfall.worldgen.ModPlacedFeatures;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.itsthesky.upfall.worldgen.ModNoises;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.NOISE, ModNoises::bootstrap)
            .add(Registries.NOISE_SETTINGS, UpfallTerrain::bootstrap)
            .add(Registries.DAMAGE_TYPE, context -> UpfallModules.forEachData(data -> data.damageTypes(context)))
            .add(Registries.CONFIGURED_CARVER, context -> UpfallModules.forEachData(data -> data.configuredCarvers(context)))
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.STRUCTURE, context -> UpfallModules.forEachData(data -> data.structures(context)))
            .add(Registries.STRUCTURE_SET, context -> UpfallModules.forEachData(data -> data.structureSets(context)))
            .add(Registries.ENCHANTMENT, context -> UpfallModules.forEachData(data -> data.enchantments(context)))
            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem);

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Upfall.MODID));
    }
}
