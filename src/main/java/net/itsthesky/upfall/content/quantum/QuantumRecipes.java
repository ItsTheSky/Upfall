package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.machines.MachinesRecipes;
import net.itsthesky.upfall.content.mobs.MobItems;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.content.world.WorldItems;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Optional;

final class QuantumRecipes {
    private QuantumRecipes() {}

    static void register(RecipeContext recipes) {
        RecipeOutput output = recipes.output();
        recipes.shaped(RecipeCategory.DECORATIONS, QuantumBlocks.PEDESTAL.get())
                .define('S', ModBlocks.POLISHED_PEARLSTONE_VARIANTS.slab().get()).define('W', ModBlocks.PEARLSTONE_BRICK_VARIANTS.wall().get())
                .define('B', ModBlocks.PEARLSTONE_BRICKS.get())
                .pattern("SSS").pattern(" W ").pattern("BBB")
                .unlockedBy(RecipeContext.hasName(ModBlocks.PEARLSTONE_BRICKS.get()), recipes.has(ModBlocks.PEARLSTONE_BRICKS.get()))
                .save(output);
        MachinesRecipes.smithing(output, "quantum_pedestal_smithing", Ingredient.of(QuantumItems.QUANTUM_PEDESTAL_BLUEPRINT.get()),
                Ingredient.of(QuantumBlocks.PEDESTAL.get()), Ingredient.of(QuantumItems.QUANTUM_STONE.get()),
                QuantumBlocks.QUANTUM_PEDESTAL.get().asItem(), 1);

        incantation(output, QuantumUpgrade.VEILSIGHT, Ingredient.of(ModItems.LEVITITE_HELMET.get()),
                QuantumItems.VEIL_MASK.get(), GrottoBlocks.MISTGLOW_DUST.get(), GrottoBlocks.MISTGLOW_DUST.get(), GrottoBlocks.MISTGLOW_DUST.get());
        incantation(output, QuantumUpgrade.UNCERTAINTY, Ingredient.of(ModItems.LEVITITE_CHESTPLATE.get()),
                QuantumItems.BOTTLED_VEIL_GAS.get(), QuantumItems.BOTTLED_VEIL_GAS.get(), MobItems.MIST_GEL.get(), MobItems.MIST_GEL.get());
        incantation(output, QuantumUpgrade.ZIPLINE, Ingredient.of(ModItems.LEVITITE_CHESTPLATE.get()),
                HamletBlocks.ROPE_SPOOL.get(), HamletBlocks.ROPE_SPOOL.get(), HamletBlocks.ZIPLINE_ANCHOR.get(), MobItems.DUSKWING_PLUME.get());
        incantation(output, QuantumUpgrade.ANCHORED, Ingredient.of(ModItems.LEVITITE_LEGGINGS.get()),
                RustworksItems.KAIRITE_GEAR.get(), RustworksItems.KAIRITE_GEAR.get(), ModBlocks.DUSKSLATE.get(), ModBlocks.DUSKSLATE.get());
        incantation(output, QuantumUpgrade.SOLIDIFIER, Ingredient.of(ModItems.LEVITITE_LEGGINGS.get()),
                ModBlocks.DENSE_CLOUD.get(), ModBlocks.DENSE_CLOUD.get(), MobItems.MIST_GEL.get(), MobItems.MIST_GEL.get());
        incantation(output, QuantumUpgrade.MOSSWALKER, Ingredient.of(ModItems.LEVITITE_BOOTS.get()),
                RustworksBlocks.VERDIGRIS_MOSS.get(), RustworksBlocks.VERDIGRIS_MOSS.get(), Blocks.MOSS_BLOCK, GrottoBlocks.LUMEN_MOSS.get());
        incantation(output, QuantumUpgrade.SUPERPOSITION, Ingredient.of(ModItems.LEVITITE_PICKAXE.get()),
                ModItems.RAW_NIMBUSITE.get(), ModItems.LEVITITE_SHARD.get(), RustworksItems.KAIRITE_GEAR.get(), WorldItems.CLOUD_PEARL.get());
        incantation(output, QuantumUpgrade.ENTANGLEMENT, Ingredient.of(ModItems.LEVITITE_SWORD.get()),
                MobItems.CRAGMAW_FANG.get(), MobItems.CRAGMAW_FANG.get(), QuantumItems.FLICKER_SHARD.get(), QuantumItems.FLICKER_SHARD.get());
        incantation(output, QuantumUpgrade.REWIND, recipes.tag(ModTags.Items.LEVITITE_ARMOR),
                ChronoRegistries.REWINDING_HOURGLASS.get(), ChronoBlocks.HOUR_DIAL.get(), ChronoBlocks.HOUR_DIAL.get(), ChronoBlocks.HOUR_DIAL.get());
    }

    private static void incantation(RecipeOutput output, QuantumUpgrade upgrade, Ingredient base, ItemLike... ingredients) {
        QuantumIncantationRecipe recipe = new QuantumIncantationRecipe(base,
                Arrays.stream(ingredients).map(item -> Ingredient.of(item)).toList(), Optional.empty(), upgrade);
        //? if >=26.1 {
        /*output.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                Upfall.id("quantum_incantation/" + upgrade.getSerializedName())), recipe, null);
        *///?} else {
        output.accept(Upfall.id("quantum_incantation/" + upgrade.getSerializedName()), recipe, null);
        //?}
    }
}
