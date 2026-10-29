package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksStructures;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksWorldgen;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;

final class RustworksData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        RustworksSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        RustworksFloraData.blockStates(states);

        for (DeferredBlock<?> block : List.of(RustworksBlocks.RUSTSOIL, RustworksBlocks.RUSTY_PLATING, RustworksBlocks.COG_PLATING,
                RustworksBlocks.HEARTGEAR)) {
            states.simpleBlockWithItem(block.get(), states.cubeAll(block.get()));
        }

        states.stone(RustworksBlocks.RUSTSLATE, RustworksBlocks.RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.COBBLED_RUSTSLATE, RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.POLISHED_RUSTSLATE, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        states.stone(RustworksBlocks.RUSTSLATE_BRICKS, RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);

        RustworksVentData.blockStates(states);
        RustworksModels.blockStates(states);
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(RustworksItems.KAIRITE_DUST.get());
        models.basicItem(RustworksItems.KAIRITE_GEAR.get());
        models.basicItem(RustworksItems.RUSTY_PLATE.get());
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        RustworksFloraData.blockLoot(loot);
        loot.add(RustworksBlocks.OXIDE_TURF.get(), loot.createSingleItemTableWithSilkTouch(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get()));
        loot.add(RustworksBlocks.RUSTSLATE.get(), loot.createSingleItemTableWithSilkTouch(RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE.get()));
        for (ModBlocks.StoneVariants variants : RustworksBlocks.allVariants()) {
            loot.add(variants.slab().get(), loot.createSlabItemTable(variants.slab().get()));
        }
        kairiteOreLoot(loot);
        loot.dropOther(RustworksBlocks.STILLFLOW_CAULDRON.get(), Items.CAULDRON);
    }

    private static void kairiteOreLoot(ModBlockLootProvider loot) {
        Block ore = RustworksBlocks.KAIRITE_ORE.get();
        LootItemBlockStatePropertyCondition.Builder peak = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ore)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(KairiteOreBlock.GLOW, 2));
        loot.add(ore, loot.createSilkTouchDispatchTable(ore, AlternativesEntry.alternatives(
                LootItem.lootTableItem(RustworksItems.KAIRITE_GEAR.get()).when(peak),
                (LootPoolEntryContainer.Builder<?>) loot.explosionDecay(ore, LootItem.lootTableItem(RustworksItems.KAIRITE_DUST.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(loot.fortuneEnchantment()))))));
    }

    @Override
    public void recipes(RecipeContext recipes) {
        RustworksFloraData.recipes(recipes);
        recipes.save(recipes.shapeless(RecipeCategory.MISC, RustworksItems.KAIRITE_GEAR.get())
                .requires(RustworksItems.KAIRITE_DUST.get(), 9)
                .unlockedBy(RecipeContext.hasName(RustworksItems.KAIRITE_DUST.get()), recipes.has(RustworksItems.KAIRITE_DUST.get())),
                Upfall.id("kairite_gear_from_dust"));

        metalRecipes(recipes);
        RustworksVentData.recipes(recipes);

        recipes.save(recipes.smelting(Ingredient.of(RustworksBlocks.COBBLED_RUSTSLATE.get()), RecipeCategory.BUILDING_BLOCKS,
                        RustworksBlocks.RUSTSLATE.get(), 0.1F, 200)
                .unlockedBy(RecipeContext.hasName(RustworksBlocks.COBBLED_RUSTSLATE.get()), recipes.has(RustworksBlocks.COBBLED_RUSTSLATE.get())),
                Upfall.id("rustslate_from_smelting"));
        square(recipes, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE.get());
        square(recipes, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.POLISHED_RUSTSLATE.get());

        family(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_VARIANTS);
        family(recipes, RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
        family(recipes, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        family(recipes, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);

        cutAll(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_VARIANTS);
        cutAll(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        cutAll(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cut(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE.get(), 1);
        cut(recipes, RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICKS.get(), 1);
        cutAll(recipes, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS);
        cutAll(recipes, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cut(recipes, RustworksBlocks.POLISHED_RUSTSLATE.get(), RustworksBlocks.RUSTSLATE_BRICKS.get(), 1);
        cutAll(recipes, RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.RUSTSLATE_BRICK_VARIANTS);
        cutAll(recipes, RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS);
    }

    private static void metalRecipes(RecipeContext recipes) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        Item dust = RustworksItems.KAIRITE_DUST.get();
        recipes.save(recipes.shapeless(RecipeCategory.MISC, plate, 4).requires(Items.COPPER_INGOT).requires(dust)
                .unlockedBy(RecipeContext.hasName(dust), recipes.has(dust)),
                Upfall.id("rusty_plate"));
        Block plating = RustworksBlocks.RUSTY_PLATING.get();
        Block pipe = RustworksBlocks.RUSTY_PIPE.get();
        Block valve = RustworksBlocks.PIPE_VALVE.get();
        metal(recipes, plating, 4).define('#', plate).pattern("##").pattern("##").save(recipes.output());
        metal(recipes, RustworksBlocks.COG_PLATING.get(), 4).define('#', plate).define('P', plating).pattern("#P").pattern("P#").save(recipes.output());
        metal(recipes, pipe, 6).define('#', plate).pattern("# #").pattern("# #").pattern("# #").save(recipes.output());
        metal(recipes, valve, 1).define('#', plate).define('P', pipe).pattern(" # ").pattern("###").pattern(" P ").save(recipes.output());
        metal(recipes, RustworksBlocks.RUSTY_COG.get(), 1).define('#', plate).define('P', plating).pattern(" # ").pattern("#P#").pattern(" # ").save(recipes.output());
        metal(recipes, RustworksBlocks.LEAKING_PIPE.get(), 1).define('P', pipe).define('D', dust).pattern("P").pattern("D")
                .unlockedBy(RecipeContext.hasName(dust), recipes.has(dust)).save(recipes.output());
    }

    private static ShapedRecipeBuilder metal(RecipeContext recipes, Block result, int count) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        return recipes.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(RecipeContext.hasName(plate), recipes.has(plate));
    }

    private static void square(RecipeContext recipes, Block result, Block input) {
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4).define('#', input).pattern("##").pattern("##")
                .unlockedBy(RecipeContext.hasName(input), recipes.has(input)).save(recipes.output());
    }

    private static void family(RecipeContext recipes, Block base, ModBlocks.StoneVariants variants) {
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, variants.stairs().get(), 4).define('#', base)
                .pattern("#  ").pattern("## ").pattern("###")
                .unlockedBy(RecipeContext.hasName(base), recipes.has(base)).save(recipes.output());
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, variants.slab().get(), 6).define('#', base).pattern("###")
                .unlockedBy(RecipeContext.hasName(base), recipes.has(base)).save(recipes.output());
        recipes.shaped(RecipeCategory.DECORATIONS, variants.wall().get(), 6).define('#', base).pattern("###").pattern("###")
                .unlockedBy(RecipeContext.hasName(base), recipes.has(base)).save(recipes.output());
    }

    private static void cutAll(RecipeContext recipes, Block base, ModBlocks.StoneVariants variants) {
        cut(recipes, base, variants.stairs().get(), 1);
        cut(recipes, base, variants.slab().get(), 2);
        cut(recipes, base, variants.wall().get(), 1);
    }

    private static void cut(RecipeContext recipes, Block input, Block result, int count) {
        recipes.save(recipes.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(RecipeContext.hasName(input), recipes.has(input)),
                Upfall.id(RecipeContext.itemName(result) + "_from_" + RecipeContext.itemName(input) + "_stonecutting"));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        RustworksFloraData.blockTags(tags);
        var pickaxe = tags.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        pickaxe.add(RustworksBlocks.EXHAUST_VENT.get());
        pickaxe.add(RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE.get(), RustworksBlocks.POLISHED_RUSTSLATE.get(),
                RustworksBlocks.RUSTSLATE_BRICKS.get(), RustworksBlocks.KAIRITE_ORE.get(), RustworksBlocks.RUSTY_PLATING.get(), RustworksBlocks.COG_PLATING.get(),
                RustworksBlocks.RUSTY_PIPE.get(), RustworksBlocks.LEAKING_PIPE.get(), RustworksBlocks.PIPE_VALVE.get(),
                RustworksBlocks.RUSTY_COG.get(), RustworksBlocks.HEARTGEAR.get(), RustworksBlocks.STILLFLOW_CAULDRON.get());
        for (ModBlocks.StoneVariants v : RustworksBlocks.allVariants()) {
            pickaxe.add(v.stairs().get(), v.slab().get(), v.wall().get());
            tags.tag(BlockTags.STAIRS).add(v.stairs().get());
            tags.tag(BlockTags.SLABS).add(v.slab().get());
            tags.tag(BlockTags.WALLS).add(v.wall().get());
        }
        tags.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get());
        tags.tag(BlockTags.DIRT).add(RustworksBlocks.OXIDE_TURF.get(), RustworksBlocks.RUSTSOIL.get());
        tags.tag(BlockTags.NEEDS_IRON_TOOL).add(RustworksBlocks.KAIRITE_ORE.get());
        tags.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(RustworksBlocks.HEARTGEAR.get());
        tags.tag(BlockTags.CAULDRONS).add(RustworksBlocks.STILLFLOW_CAULDRON.get());
    }

    @Override
    public void particles(net.itsthesky.upfall.datagen.ModParticleProvider particles) {
        RustworksFloraData.particles(particles);
    }

    @Override
    public void dataMaps(net.itsthesky.upfall.datagen.ModDataMapProvider dataMaps) {
        RustworksFloraData.dataMaps(dataMaps);
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        RustworksVentData.sounds(sounds);
        sounds.addSound(RustworksSounds.AMBIENT_MOOD, ModSoundProvider.def(RustworksSounds.AMBIENT_MOOD)
                .with(ModSoundProvider.event("ambient.basalt_deltas.mood").pitch(0.7).volume(0.5)));
        sounds.addSound(RustworksSounds.AMBIENT_ADDITIONS, ModSoundProvider.def(RustworksSounds.AMBIENT_ADDITIONS)
                .with(ModSoundProvider.event("block.chain.step").pitch(0.5).volume(0.4).weight(3),
                        ModSoundProvider.event("block.anvil.land").pitch(0.5).volume(0.08),
                        ModSoundProvider.event("block.iron_door.close").pitch(0.5).volume(0.2).weight(2),
                        ModSoundProvider.event("block.pointed_dripstone.drip_water").pitch(0.8).volume(0.5).weight(3),
                        ModSoundProvider.event("block.pointed_dripstone.drip_water_into_cauldron").pitch(0.7).volume(0.4).weight(2),
                        ModSoundProvider.event("entity.breeze.inhale").pitch(0.5).volume(0.25).weight(2),
                        ModSoundProvider.event("block.lava.extinguish").pitch(0.6).volume(0.1)));
    }

    @Override
    public void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RustworksWorldgen.configuredFeatures(context);
    }

    @Override
    public void placedFeatures(BootstrapContext<PlacedFeature> context) {
        RustworksWorldgen.placedFeatures(context);
    }

    @Override
    public void biomeFeatures(BiomeFeatureSink sink) {
        RustworksWorldgen.biomeFeatures(sink);
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        RustworksStructures.structures(context);
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        RustworksStructures.structureSets(context);
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(RustworksLoot.provider(ctx.output(), ctx.registries()));
        ctx.<Biome>tags(Registries.BIOME, "Pipe Bridge Biome Tags", tags -> tags.tag(RustworksStructures.HAS_PIPE_BRIDGE).add(ModBiomes.RUSTWORKS));
    }
}
