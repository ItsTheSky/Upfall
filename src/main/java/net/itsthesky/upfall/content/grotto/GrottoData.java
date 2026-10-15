package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class GrottoData implements DataContribution {
    @Override
    public void blockStates(ModBlockStateProvider states) {
        for (Block block : new Block[]{GrottoBlocks.LUMEN_ROCK.get(), GrottoBlocks.MOONSHELL.get(), GrottoBlocks.LUMEN_DRUSE.get(),
                GrottoBlocks.LUMEN_MOSS.get()}) {
            states.simpleBlockWithItem(block, states.cubeAll(block));
        }
        states.crossPlant(GrottoBlocks.LUMEN_SPROUTS.get(), "lumen_sprouts");

        ModelFile pool = states.models().getBuilder("prism_pool")
                .texture("particle", states.blockTexture("prism_pool")).texture("all", states.blockTexture("prism_pool"))
                .renderType("translucent")
                .element().from(0, 0, 0).to(16, 14, 16)
                .allFaces((dir, face) -> {
                    face.texture("#all");
                    if (dir != Direction.UP) face.cullface(dir);
                }).end();
        states.simpleBlockWithItem(GrottoBlocks.PRISM_POOL.get(), pool);

        for (GrottoBlocks.CrystalSet set : GrottoBlocks.CRYSTALS) {
            String name = ModBlockStateProvider.name(set.block().get());
            states.simpleBlockWithItem(set.block().get(), states.glowingCube(name, "solid"));
            for (var cluster : set.clusters()) {
                clusterStates(states, cluster.get());
            }
        }

        Block lantern = GrottoBlocks.MISTGLOW_LANTERN.get();
        ModelFile standing = states.models().withExistingParent("mistglow_lantern", states.mcLoc("block/template_lantern"))
                .texture("lantern", states.blockTexture("mistglow_lantern")).renderType("cutout");
        ModelFile hanging = states.models().withExistingParent("mistglow_lantern_hanging", states.mcLoc("block/template_hanging_lantern"))
                .texture("lantern", states.blockTexture("mistglow_lantern")).renderType("cutout");
        states.getVariantBuilder(lantern).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : standing).build(), LanternBlock.WATERLOGGED);
        states.itemModels().withExistingParent("mistglow_lantern", states.mcLoc("item/generated"))
                .texture("layer0", states.modLoc("item/mistglow_lantern"));
    }

    private static void clusterStates(ModBlockStateProvider states, AmethystClusterBlock block) {
        String name = ModBlockStateProvider.name(block);
        ModelFile model = states.models().cross(name, states.blockTexture(name)).renderType("cutout");
        states.getVariantBuilder(block).forAllStatesExcept(state -> {
            ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
            return switch (state.getValue(AmethystClusterBlock.FACING)) {
                case DOWN -> builder.rotationX(180).build();
                case NORTH -> builder.rotationX(90).build();
                case SOUTH -> builder.rotationX(90).rotationY(180).build();
                case EAST -> builder.rotationX(90).rotationY(90).build();
                case WEST -> builder.rotationX(90).rotationY(270).build();
                default -> builder.build();
            };
        }, AmethystClusterBlock.WATERLOGGED);
        states.itemModels().withExistingParent(name, states.mcLoc("item/generated")).texture("layer0", states.blockTexture(name));
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(GrottoBlocks.MISTGLOW_DUST.get());
        models.basicItem(GrottoBlocks.LUMEN_SHARD.get());
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        loot.add(ModBlocks.MISTGLOW_CRYSTAL.get(), loot.createSilkTouchDispatchTable(ModBlocks.MISTGLOW_CRYSTAL.get(),
                (LootPoolEntryContainer.Builder<?>) loot.explosionDecay(ModBlocks.MISTGLOW_CRYSTAL.get(),
                        LootItem.lootTableItem(GrottoBlocks.MISTGLOW_DUST.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 4.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(loot.fortuneEnchantment()))
                                .apply(LimitCount.limitCount(IntRange.range(1, 4))))));

        loot.add(ModBlocks.GILDED_DUSKSLATE.get(), loot.createSilkTouchDispatchTable(ModBlocks.GILDED_DUSKSLATE.get(),
                (LootPoolEntryContainer.Builder<?>) loot.explosionDecay(ModBlocks.GILDED_DUSKSLATE.get(),
                        LootItem.lootTableItem(Items.GOLD_NUGGET)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 6.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(loot.fortuneEnchantment())))));

        for (GrottoBlocks.CrystalSet set : GrottoBlocks.CRYSTALS) {
            Block cluster = set.cluster().get();
            loot.add(cluster, loot.createSilkTouchDispatchTable(cluster, LootItem.lootTableItem(GrottoBlocks.LUMEN_SHARD.get())
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)))
                    .apply(ApplyBonusCount.addOreBonusCount(loot.fortuneEnchantment()))
                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                    .otherwise((LootPoolEntryContainer.Builder<?>) loot.explosionDecay(cluster, LootItem.lootTableItem(GrottoBlocks.LUMEN_SHARD.get())
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))))));
            for (var bud : new Block[]{set.large().get(), set.medium().get(), set.small().get()}) {
                loot.add(bud, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(loot.hasSilkTouch()).add(LootItem.lootTableItem(bud))));
            }
        }
    }

    @Override
    public void recipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MISTGLOW_CRYSTAL.get())
                .define('#', GrottoBlocks.MISTGLOW_DUST.get()).pattern("##").pattern("##")
                .unlockedBy(ModRecipeProvider.hasName(GrottoBlocks.MISTGLOW_DUST.get()), ModRecipeProvider.hasItem(GrottoBlocks.MISTGLOW_DUST.get()))
                .save(output, Upfall.id("mistglow_crystal_from_dust"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, GrottoBlocks.MISTGLOW_LANTERN.get())
                .define('G', Items.GOLD_NUGGET).define('N', ModItems.NIMBUSITE_INGOT.get()).define('D', GrottoBlocks.MISTGLOW_DUST.get())
                .pattern("GNG").pattern("DDD").pattern("GNG")
                .unlockedBy(ModRecipeProvider.hasName(GrottoBlocks.MISTGLOW_DUST.get()), ModRecipeProvider.hasItem(GrottoBlocks.MISTGLOW_DUST.get()))
                .save(output, Upfall.id("mistglow_lantern"));

        crystalBlock(output, GrottoBlocks.AZURE, Items.LIGHT_BLUE_DYE);
        crystalBlock(output, GrottoBlocks.ROSE, Items.PINK_DYE);
        crystalBlock(output, GrottoBlocks.VERDANT, Items.LIME_DYE);
    }

    private static void crystalBlock(RecipeOutput output, GrottoBlocks.CrystalSet set, net.minecraft.world.item.Item dye) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, set.block().get())
                .requires(GrottoBlocks.LUMEN_SHARD.get(), 4).requires(dye)
                .unlockedBy(ModRecipeProvider.hasName(GrottoBlocks.LUMEN_SHARD.get()), ModRecipeProvider.hasItem(GrottoBlocks.LUMEN_SHARD.get()))
                .save(output, Upfall.id(set.name() + "_crystal_block"));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        var pickaxe = tags.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        pickaxe.add(GrottoBlocks.LUMEN_ROCK.get(), GrottoBlocks.MOONSHELL.get(), GrottoBlocks.LUMEN_DRUSE.get(),
                GrottoBlocks.MISTGLOW_LANTERN.get());
        for (GrottoBlocks.CrystalSet set : GrottoBlocks.CRYSTALS) {
            pickaxe.add(set.block().get());
            set.clusters().forEach(cluster -> pickaxe.add(cluster.get()));
        }
        tags.tag(BlockTags.MINEABLE_WITH_HOE).add(GrottoBlocks.LUMEN_MOSS.get());
        tags.tag(BlockTags.SWORD_EFFICIENT).add(GrottoBlocks.LUMEN_SPROUTS.get());
        tags.tag(BlockTags.REPLACEABLE).add(GrottoBlocks.LUMEN_SPROUTS.get());
        tags.tag(BlockTags.CRYSTAL_SOUND_BLOCKS).add(GrottoBlocks.LUMEN_DRUSE.get());
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(GrottoStructures.CRYSTAL_GROTTO, new GrottoStructure(
                new Structure.StructureSettings.Builder(HolderSet.direct(ModBiomes.ALL.stream().map(biomes::getOrThrow).toList()))
                        .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(GrottoStructures.CRYSTAL_GROTTO_SET, new StructureSet(structures.getOrThrow(GrottoStructures.CRYSTAL_GROTTO),
                new RandomSpreadStructurePlacement(59, 22, RandomSpreadType.LINEAR, 1572864021)));
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new GrottoLoot.Provider(output, registries));
    }
}
