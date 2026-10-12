package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.cave.GasCaveCarver;
import net.itsthesky.upfall.content.quantum.cave.GasCaveFeature;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.data.loot.LootTableProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class QuantumWorldgen {
    private QuantumWorldgen() {}

    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(Registries.CARVER, Upfall.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Upfall.MODID);

    public static final DeferredHolder<WorldCarver<?>, GasCaveCarver> GAS_CAVE_CARVER = CARVERS.register("gas_caves", GasCaveCarver::new);
    public static final DeferredHolder<Feature<?>, GasCaveFeature> GAS_CAVE_FEATURE = FEATURES.register("gas_cave_props", GasCaveFeature::new);

    public static final ResourceKey<ConfiguredWorldCarver<?>> GAS_CAVES = ResourceKey.create(Registries.CONFIGURED_CARVER, Upfall.id("gas_caves"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> GAS_CAVE_PROPS = ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id("gas_cave_props"));
    public static final ResourceKey<PlacedFeature> GAS_CAVE_PROPS_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id("gas_cave_props"));

    static void configuredCarvers(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        context.register(GAS_CAVES, GAS_CAVE_CARVER.get().configured(new CarverConfiguration(1.0F, ConstantHeight.of(VerticalAnchor.absolute(128)),
                ConstantFloat.of(1.0F), VerticalAnchor.bottom(), CarverDebugSettings.DEFAULT,
                context.lookup(Registries.BLOCK).getOrThrow(ModTags.Blocks.GAS_CAVE_REPLACEABLE))));
    }

    static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(GAS_CAVE_PROPS, new ConfiguredFeature<>(GAS_CAVE_FEATURE.get(), FeatureConfiguration.NONE));
    }

    static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        context.register(GAS_CAVE_PROPS_PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(GAS_CAVE_PROPS),
                List.of(net.minecraft.world.level.levelgen.placement.BiomeFilter.biome())));
    }

    static void structures(BootstrapContext<net.minecraft.world.level.levelgen.structure.Structure> context) {
        context.register(QuantumStructures.SHIFTING_VAULT, new net.itsthesky.upfall.content.quantum.vault.ShiftingVaultStructure(
                new net.minecraft.world.level.levelgen.structure.Structure.StructureSettings.Builder(
                        context.lookup(Registries.BIOME).getOrThrow(QuantumStructures.HAS_SHIFTING_VAULT))
                        .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                        .terrainAdapation(net.minecraft.world.level.levelgen.structure.TerrainAdjustment.NONE)
                        .build()));
    }

    static void structureSets(BootstrapContext<net.minecraft.world.level.levelgen.structure.StructureSet> context) {
        context.register(QuantumStructures.SHIFTING_VAULT_SET, new net.minecraft.world.level.levelgen.structure.StructureSet(
                context.lookup(Registries.STRUCTURE).getOrThrow(QuantumStructures.SHIFTING_VAULT),
                new net.itsthesky.upfall.content.quantum.vault.ConfiguredSpreadPlacement(1210202614)));
    }

    static void biomeCarvers(ResourceKey<Biome> biome, BiomeGenerationSettings.Builder generation) {
        generation.addCarver(GenerationStep.Carving.AIR, GAS_CAVES);
    }

    static final Set<ResourceKey<Biome>> CAVE_BIOMES = Set.copyOf(ModBiomes.ALL);
    static final GenerationStep.Decoration PROPS_STEP = GenerationStep.Decoration.UNDERGROUND_DECORATION;

    static void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new Named("Quantum loot tables", new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(QuantumLoot::new, LootContextParamSets.CHEST)), registries)));
        generator.addProvider(event.includeServer(), new net.minecraft.data.tags.BiomeTagsProvider(output, registries, Upfall.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(QuantumStructures.HAS_SHIFTING_VAULT).add(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.HANGING_ROOTS, ModBiomes.GILDED_RIDGE,
                        ModBiomes.RUSTWORKS);
            }

            @Override
            public String getName() {
                return "Shifting Vault Biome Tags";
            }
        });
    }

    record Named(String name, net.minecraft.data.DataProvider delegate) implements net.minecraft.data.DataProvider {
        @Override
        public CompletableFuture<?> run(net.minecraft.data.CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
