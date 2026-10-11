package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.content.UpfallModules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.Set;

public final class UpfallBiomeFeatures {
    private UpfallBiomeFeatures() {}

    private record Entry(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature, Set<ResourceKey<Biome>> biomes) {}

    private static final Set<ResourceKey<Biome>> ALL = Set.copyOf(ModBiomes.ALL);
    private static final Set<ResourceKey<Biome>> ISLANDS = Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.HANGING_ROOTS, ModBiomes.GILDED_RIDGE);

    private static final List<Entry> ORDER = List.of(
            new Entry(GenerationStep.Decoration.LOCAL_MODIFICATIONS, ModPlacedFeatures.SKY_STREAM, Set.of(ModBiomes.DRIFTING_ARCHIPELAGO)),
            new Entry(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.DRIFTING_RUINS,
                    Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.GILDED_RIDGE, ModBiomes.MISTSEA)),
            new Entry(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.INVERTED_OBSERVATORY, Set.of(ModBiomes.HANGING_ROOTS)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_ORES, ModPlacedFeatures.ORE_NIMBUSITE, ALL),
            new Entry(GenerationStep.Decoration.UNDERGROUND_ORES, ModPlacedFeatures.GILDED_VEIN, Set.of(ModBiomes.GILDED_RIDGE)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_ORES, ModPlacedFeatures.ORE_LEVITITE, Set.of(ModBiomes.GILDED_RIDGE)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.MISTGLOW_CRYSTAL, ALL),
            new Entry(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.UPFALL_CASCADE,
                    Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.GILDED_RIDGE)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.UPFALL_CASCADE_DENSE, Set.of(ModBiomes.HANGING_ROOTS)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.GLOWVINES,
                    Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.GILDED_RIDGE, ModBiomes.MISTSEA)),
            new Entry(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.GLOWVINES_DENSE, Set.of(ModBiomes.HANGING_ROOTS)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SKYREACH_TREES, Set.of(ModBiomes.DRIFTING_ARCHIPELAGO)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SKYREACH_TREES_SPARSE,
                    Set.of(ModBiomes.MISTSEA, ModBiomes.HANGING_ROOTS, ModBiomes.GILDED_RIDGE)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.GILDROOT_TREES, Set.of(ModBiomes.HANGING_ROOTS)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_GILDED_TUFT,
                    Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.MISTSEA, ModBiomes.HANGING_ROOTS)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_GILDED_TUFT_DENSE, Set.of(ModBiomes.GILDED_RIDGE)),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_CLOUD_FERN, ALL),
            new Entry(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PATCH_DUSKBLOOM, ISLANDS),
            new Entry(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, ModPlacedFeatures.UPDRAFT_FROM_CLOUDS, ALL),
            new Entry(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, ModPlacedFeatures.UPDRAFT_FROM_ISLANDS, ISLANDS));

    public static void addFeatures(BiomeGenerationSettings.Builder builder, ResourceKey<Biome> biome) {
        for (Entry entry : ORDER) {
            if (entry.biomes().contains(biome)) {
                builder.addFeature(entry.step(), entry.feature());
            }
        }
        UpfallModules.forEachData(data -> data.biomeFeatures((step, feature, biomes) -> {
            if (biomes.contains(biome)) {
                builder.addFeature(step, feature);
            }
        }));
    }
}
