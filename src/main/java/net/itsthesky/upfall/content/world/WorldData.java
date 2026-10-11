package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.world.feature.SkyLakeFeature;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModDataMapProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModParticleProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.itsthesky.upfall.worldgen.placement.SurfaceScanPlacement;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class WorldData implements DataContribution {
    private static final int ISLAND_MIN_Y = UpfallTerrain.CLOUD_BAND_MAX;
    private static final int ISLAND_MAX_Y = 300;

    private static final Set<ResourceKey<Biome>> ALL = Set.copyOf(ModBiomes.ALL);
    private static final Set<ResourceKey<Biome>> NOT_MISTSEA = Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.HANGING_ROOTS, ModBiomes.GILDED_RIDGE);

    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add("item.upfall.nimbus_flask.desc", "Condenses a short-lived cloud platform under your feet.");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(WorldBlocks.SKYSILT.get(), states.cubeAll(WorldBlocks.SKYSILT.get()));
        states.simpleBlockWithItem(WorldBlocks.RAINCLOUD.get(), states.cubeAll(WorldBlocks.RAINCLOUD.get()));
        states.simpleBlockWithItem(WorldBlocks.CLOUD_LAMP.get(), states.glowingCube("cloud_lamp", "solid"));
        states.translucentCube(WorldBlocks.FADING_CLOUD, false);

        ModelFile pearled = states.models().withExistingParent("pearled_cloud", states.mcLoc("block/block"))
                .texture("base", states.blockTexture("dense_cloud")).texture("pearls", states.blockTexture("pearled_cloud_pearls"))
                .texture("particle", states.blockTexture("dense_cloud")).renderType("cutout")
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).shade(false).ao(false).emissivity(15, 15)
                .allFaces((dir, face) -> face.texture("#pearls").cullface(dir)).end();
        states.simpleBlockWithItem(WorldBlocks.PEARLED_CLOUD.get(), pearled);

        ModelFile reedLower = states.models().cross("skyreed_bottom", states.blockTexture("skyreed_bottom")).renderType("cutout");
        ModelFile reedUpper = states.models().cross("skyreed_top", states.blockTexture("skyreed_top")).renderType("cutout");
        states.getVariantBuilder(WorldBlocks.SKYREED.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? reedLower : reedUpper).build());
        states.itemModels().withExistingParent("skyreed", states.mcLoc("item/generated")).texture("layer0", states.blockTexture("skyreed_top"));

        ModelFile lily = states.models().withExistingParent("skylily", states.mcLoc("block/block"))
                .texture("texture", states.blockTexture("skylily")).texture("particle", states.blockTexture("skylily"))
                .renderType("cutout").ao(false)
                .element().from(0, 0.25F, 0).to(16, 0.25F, 16).emissivity(6, 6)
                .face(Direction.UP).texture("#texture").uvs(0, 0, 16, 16).end()
                .face(Direction.DOWN).texture("#texture").uvs(0, 16, 16, 0).end().end();
        states.getVariantBuilder(WorldBlocks.SKYLILY.get()).forAllStates(state -> ConfiguredModel.allYRotations(lily, 0, false));
        states.itemModels().withExistingParent("skylily", states.mcLoc("item/generated")).texture("layer0", states.blockTexture("skylily"));

        states.crossPlant(WorldBlocks.CLOUDSTALK.get(), "cloudstalk");

        ModelFile bloom = states.models().withExistingParent("cloudbloom", states.mcLoc("block/block"))
                .texture("side", states.blockTexture("cloudbloom_side")).texture("top", states.blockTexture("cloudbloom_top"))
                .texture("particle", states.blockTexture("cloudbloom_side")).renderType("cutout").ao(false)
                .element().from(3, 1, 3).to(13, 8, 13).shade(false).emissivity(12, 12)
                .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? "#top" : "#side")).end()
                .element().from(5, 8, 5).to(11, 11, 11).shade(false).emissivity(12, 12)
                .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? "#top" : "#side")).end()
                .element().from(7, 0, 7).to(9, 1, 9)
                .allFaces((dir, face) -> face.texture("#side")).end();
        states.simpleBlock(WorldBlocks.CLOUDBLOOM.get(), bloom);
        states.blockItem(WorldBlocks.CLOUDBLOOM.get());
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(WorldItems.CLOUD_PEARL.get());
        models.basicItem(WorldItems.NIMBUS_FLASK.get());
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        Block reed = WorldBlocks.SKYREED.get();
        loot.add(reed, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(reed).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(reed)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))))));
        Block pearled = WorldBlocks.PEARLED_CLOUD.get();
        loot.add(pearled, loot.createSilkTouchDispatchTable(pearled, loot.explosionDecay(pearled,
                LootItem.lootTableItem(WorldItems.CLOUD_PEARL.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(loot.fortuneEnchantment())))));
    }

    @Override
    public void recipes(RecipeOutput output) {
        ItemLike pearl = WorldItems.CLOUD_PEARL.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WorldBlocks.CLOUD_LAMP.get())
                .pattern(" P ").pattern("PCP").pattern(" P ")
                .define('P', pearl).define('C', ModBlocks.DENSE_CLOUD.get())
                .unlockedBy(ModRecipeProvider.hasName(pearl), ModRecipeProvider.hasItem(pearl)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, WorldItems.NIMBUS_FLASK.get())
                .requires(Items.GLASS_BOTTLE).requires(pearl, 2).requires(ModBlocks.DENSE_CLOUD.get())
                .unlockedBy(ModRecipeProvider.hasName(pearl), ModRecipeProvider.hasItem(pearl)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("RRR").define('R', WorldBlocks.SKYREED_ITEM.get())
                .unlockedBy(ModRecipeProvider.hasName(WorldBlocks.SKYREED_ITEM.get()), ModRecipeProvider.hasItem(WorldBlocks.SKYREED_ITEM.get()))
                .save(output, Upfall.id("paper_from_skyreed"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(WorldBlocks.SKYSILT.get()), RecipeCategory.BUILDING_BLOCKS, Items.GLASS, 0.1F, 200)
                .unlockedBy(ModRecipeProvider.hasName(WorldBlocks.SKYSILT.get()), ModRecipeProvider.hasItem(WorldBlocks.SKYSILT.get()))
                .save(output, Upfall.id("glass_from_skysilt"));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(WorldBlocks.SKYSILT.get(), WorldBlocks.PEARLED_CLOUD.get(), WorldBlocks.RAINCLOUD.get(),
                WorldBlocks.CLOUD_LAMP.get(), WorldBlocks.FADING_CLOUD.get());
        tags.tag(ModTags.Blocks.SKY_PLANTABLE).add(WorldBlocks.SKYSILT.get(), WorldBlocks.RAINCLOUD.get());
        tags.tag(BlockTags.SWORD_EFFICIENT).add(WorldBlocks.SKYREED.get(), WorldBlocks.CLOUDSTALK.get());
        tags.tag(BlockTags.REPLACEABLE_BY_TREES).add(WorldBlocks.SKYREED.get());
        tags.tag(ModTags.Blocks.FEATURE_REPLACEABLE).add(WorldBlocks.SKYREED.get(), WorldBlocks.SKYLILY.get(),
                WorldBlocks.CLOUDSTALK.get(), WorldBlocks.CLOUDBLOOM.get());
    }

    @Override
    public void particles(ModParticleProvider particles) {
        particles.addSpriteSet(WorldParticles.CLOUD_DRIP.get(), Upfall.id("cloud_drip"), 2);
    }

    @Override
    public void dataMaps(ModDataMapProvider dataMaps) {
        var compostables = dataMaps.builder(NeoForgeDataMaps.COMPOSTABLES);
        compostables.add(WorldBlocks.SKYREED_ITEM.get().builtInRegistryHolder(), new Compostable(0.5F), false);
        compostables.add(WorldBlocks.SKYLILY_ITEM.get().builtInRegistryHolder(), new Compostable(0.65F), false);
        compostables.add(WorldBlocks.CLOUDSTALK.get().asItem().builtInRegistryHolder(), new Compostable(0.3F), false);
        compostables.add(WorldBlocks.CLOUDBLOOM.get().asItem().builtInRegistryHolder(), new Compostable(0.65F), false);
    }

    @Override
    public void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, WorldFeatures.Configured.SKY_POND, WorldFeatures.SKY_LAKE.get(), new SkyLakeFeature.Config(2, 4, 2));
        FeatureUtils.register(context, WorldFeatures.Configured.SKY_LAKE, WorldFeatures.SKY_LAKE.get(), new SkyLakeFeature.Config(5, 9, 4));
        FeatureUtils.register(context, WorldFeatures.Configured.NEEDLE_TIP, WorldFeatures.NEEDLE_TIP.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, WorldFeatures.Configured.CLOUDSTALK_PATCH, WorldFeatures.CLOUDSTALK_PATCH.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, WorldFeatures.Configured.CLOUDLET, WorldFeatures.CLOUDLET.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, WorldFeatures.Configured.RAINCLOUD_PATCH, WorldFeatures.RAINCLOUD_PATCH.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, WorldFeatures.Configured.ORE_PEARLED_CLOUD, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(ModBlocks.DENSE_CLOUD.get()), WorldBlocks.PEARLED_CLOUD.get().defaultBlockState())), 4));
    }

    @Override
    public void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        BlockPredicate onGrass = BlockPredicate.matchesBlocks(ModBlocks.SKYGRASS_BLOCK.get());
        BlockPredicate onMist = BlockPredicate.matchesBlocks(ModBlocks.MIST.get());
        BlockPredicate onNeedle = BlockPredicate.matchesBlocks(ModBlocks.DUSKSLATE.get(), ModBlocks.PEARLSTONE.get());
        BlockPredicate underCloud = BlockPredicate.matchesBlocks(ModBlocks.DENSE_CLOUD.get());
        int cloudMin = UpfallTerrain.CLOUD_BAND_MIN;
        int cloudMax = UpfallTerrain.CLOUD_BAND_MAX;

        register(context, configured, WorldFeatures.Placed.SKY_LAKE, WorldFeatures.Configured.SKY_LAKE,
                RarityFilter.onAverageOnceEvery(10), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, ISLAND_MIN_Y, ISLAND_MAX_Y), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.SKY_POND, WorldFeatures.Configured.SKY_POND,
                RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, ISLAND_MIN_Y, ISLAND_MAX_Y), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.SKY_POND_RARE, WorldFeatures.Configured.SKY_POND,
                RarityFilter.onAverageOnceEvery(12), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onGrass, ISLAND_MIN_Y, ISLAND_MAX_Y), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.ORE_PEARLED_CLOUD, WorldFeatures.Configured.ORE_PEARLED_CLOUD,
                CountPlacement.of(5), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(cloudMin + 10), VerticalAnchor.absolute(cloudMax)), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.RAINCLOUD_PATCH, WorldFeatures.Configured.RAINCLOUD_PATCH,
                RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), SurfaceScanPlacement.ceiling(underCloud, cloudMin, cloudMax), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.NEEDLE_TIP, WorldFeatures.Configured.NEEDLE_TIP,
                CountPlacement.of(10), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onNeedle, UpfallTerrain.MIN_Y, cloudMin + 14), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.CLOUDSTALKS, WorldFeatures.Configured.CLOUDSTALK_PATCH,
                RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onMist, cloudMin, cloudMax), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.CLOUDSTALKS_DENSE, WorldFeatures.Configured.CLOUDSTALK_PATCH,
                RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), SurfaceScanPlacement.floor(onMist, cloudMin, cloudMax), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.CLOUDLETS, WorldFeatures.Configured.CLOUDLET,
                RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(cloudMax + 4), VerticalAnchor.absolute(cloudMax + 30)), BiomeFilter.biome());
        register(context, configured, WorldFeatures.Placed.CLOUDLETS_RARE, WorldFeatures.Configured.CLOUDLET,
                RarityFilter.onAverageOnceEvery(9), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(cloudMax + 4), VerticalAnchor.absolute(cloudMax + 36)), BiomeFilter.biome());
    }

    @Override
    public void biomeFeatures(BiomeFeatureSink sink) {
        sink.add(GenerationStep.Decoration.LOCAL_MODIFICATIONS, WorldFeatures.Placed.SKY_LAKE, Set.of(ModBiomes.DRIFTING_ARCHIPELAGO));
        sink.add(GenerationStep.Decoration.LOCAL_MODIFICATIONS, WorldFeatures.Placed.SKY_POND,
                Set.of(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.GILDED_RIDGE));
        sink.add(GenerationStep.Decoration.LOCAL_MODIFICATIONS, WorldFeatures.Placed.SKY_POND_RARE, Set.of(ModBiomes.MISTSEA, ModBiomes.HANGING_ROOTS));
        sink.add(GenerationStep.Decoration.UNDERGROUND_ORES, WorldFeatures.Placed.ORE_PEARLED_CLOUD, ALL);
        sink.add(GenerationStep.Decoration.UNDERGROUND_DECORATION, WorldFeatures.Placed.RAINCLOUD_PATCH, ALL);
        sink.add(GenerationStep.Decoration.UNDERGROUND_DECORATION, WorldFeatures.Placed.NEEDLE_TIP, ALL);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, WorldFeatures.Placed.CLOUDSTALKS, NOT_MISTSEA);
        sink.add(GenerationStep.Decoration.VEGETAL_DECORATION, WorldFeatures.Placed.CLOUDSTALKS_DENSE, Set.of(ModBiomes.MISTSEA));
        sink.add(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, WorldFeatures.Placed.CLOUDLETS, Set.of(ModBiomes.MISTSEA));
        sink.add(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, WorldFeatures.Placed.CLOUDLETS_RARE, NOT_MISTSEA);
    }

    private static void register(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured,
                                 ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
        PlacementUtils.register(context, key, configured.getOrThrow(feature), new ArrayList<>(List.of(modifiers)));
    }
}
