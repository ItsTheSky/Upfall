package net.itsthesky.upfall.content.ores;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class OresData implements DataContribution {
    private record Ore(String name, DeferredBlock<DropExperienceBlock> pearlstone, DeferredBlock<DropExperienceBlock> rustslate,
                       Item drop, int size, float discardOnAir, int count, int minY, int maxY) {
        ResourceKey<ConfiguredFeature<?, ?>> configured() {
            return ResourceKey.create(Registries.CONFIGURED_FEATURE, Upfall.id("ore_" + name));
        }

        ResourceKey<PlacedFeature> placed() {
            return ResourceKey.create(Registries.PLACED_FEATURE, Upfall.id("ore_" + name));
        }
    }

    private static final List<Ore> ORES = List.of(
            new Ore("coal", OresBlocks.PEARLSTONE_COAL_ORE, OresBlocks.RUSTSLATE_COAL_ORE, Items.COAL, 15, 0.0F, 26, 70, 260),
            new Ore("iron", OresBlocks.PEARLSTONE_IRON_ORE, OresBlocks.RUSTSLATE_IRON_ORE, Items.RAW_IRON, 9, 0.0F, 22, 70, 230),
            new Ore("diamond", OresBlocks.PEARLSTONE_DIAMOND_ORE, OresBlocks.RUSTSLATE_DIAMOND_ORE, Items.DIAMOND, 7, 0.5F, 9, 70, 150));

    private static final Set<ResourceKey<net.minecraft.world.level.biome.Biome>> BIOMES = Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.RUSTWORKS);

    private static final Map<String, String> FRENCH = Map.of(
            "pearlstone_coal_ore", "Minerai de charbon de pearlstone",
            "pearlstone_iron_ore", "Minerai de fer de pearlstone",
            "pearlstone_diamond_ore", "Minerai de diamant de pearlstone",
            "rustslate_coal_ore", "Minerai de charbon de rustslate",
            "rustslate_iron_ore", "Minerai de fer de rustslate",
            "rustslate_diamond_ore", "Minerai de diamant de rustslate");

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        FRENCH.forEach((name, text) -> lang.add("block.upfall." + name, text));
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        for (DeferredBlock<DropExperienceBlock> ore : OresBlocks.ALL) {
            states.simpleBlockWithItem(ore.get(), states.cubeAll(ore.get()));
        }
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        for (Ore ore : ORES) {
            loot.add(ore.pearlstone().get(), loot.createOreDrop(ore.pearlstone().get(), ore.drop()));
            loot.add(ore.rustslate().get(), loot.createOreDrop(ore.rustslate().get(), ore.drop()));
        }
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(OresBlocks.blocks(OresBlocks.ALL));
        tags.tag(BlockTags.NEEDS_STONE_TOOL).add(OresBlocks.blocks(OresBlocks.IRON));
        tags.tag(BlockTags.NEEDS_IRON_TOOL).add(OresBlocks.blocks(OresBlocks.DIAMOND));
        tags.tag(BlockTags.COAL_ORES).add(OresBlocks.blocks(OresBlocks.COAL));
        tags.tag(BlockTags.IRON_ORES).add(OresBlocks.blocks(OresBlocks.IRON));
        tags.tag(BlockTags.DIAMOND_ORES).add(OresBlocks.blocks(OresBlocks.DIAMOND));
        tags.tag(Tags.Blocks.ORES_COAL).add(OresBlocks.blocks(OresBlocks.COAL));
        tags.tag(Tags.Blocks.ORES_IRON).add(OresBlocks.blocks(OresBlocks.IRON));
        tags.tag(Tags.Blocks.ORES_DIAMOND).add(OresBlocks.blocks(OresBlocks.DIAMOND));
    }

    @Override
    public void itemTags(ModTagProviders.Items tags) {
        tags.copy(BlockTags.COAL_ORES, ItemTags.COAL_ORES);
        tags.copy(BlockTags.IRON_ORES, ItemTags.IRON_ORES);
        tags.copy(BlockTags.DIAMOND_ORES, ItemTags.DIAMOND_ORES);
        tags.copy(Tags.Blocks.ORES_COAL, Tags.Items.ORES_COAL);
        tags.copy(Tags.Blocks.ORES_IRON, Tags.Items.ORES_IRON);
        tags.copy(Tags.Blocks.ORES_DIAMOND, Tags.Items.ORES_DIAMOND);
    }

    @Override
    public void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        BlockMatchTest pearlstone = new BlockMatchTest(ModBlocks.PEARLSTONE.get());
        BlockMatchTest rustslate = new BlockMatchTest(RustworksBlocks.RUSTSLATE.get());
        for (Ore ore : ORES) {
            FeatureUtils.register(context, ore.configured(), Feature.ORE, new OreConfiguration(List.of(
                    OreConfiguration.target(pearlstone, ore.pearlstone().get().defaultBlockState()),
                    OreConfiguration.target(rustslate, ore.rustslate().get().defaultBlockState())), ore.size(), ore.discardOnAir()));
        }
    }

    @Override
    public void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        for (Ore ore : ORES) {
            PlacementUtils.register(context, ore.placed(), configured.getOrThrow(ore.configured()), List.of(
                    CountPlacement.of(ore.count()), InSquarePlacement.spread(),
                    HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.minY()), VerticalAnchor.absolute(ore.maxY())), BiomeFilter.biome()));
        }
    }

    @Override
    public void biomeFeatures(BiomeFeatureSink sink) {
        for (Ore ore : ORES) {
            sink.add(GenerationStep.Decoration.UNDERGROUND_ORES, ore.placed(), BIOMES);
        }
    }
}
