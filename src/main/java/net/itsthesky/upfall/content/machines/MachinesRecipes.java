package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Map;

public final class MachinesRecipes {
    private MachinesRecipes() {}

    static final Map<String, String> APPLIES_TO = Map.of(
            "pneumatic_pipe_blueprint", "Copper Block",
            "void_tether_blueprint", "Ender Pearl",
            "rewinder_blueprint", "Anvil",
            "hastening_gear_blueprint", "Redstone Block",
            "phase_watch_blueprint", "Clock",
            "gearlift_blueprint", "Piston");

    static void register(RecipeContext recipes) {
        RecipeOutput output = recipes.output();
        smithing(output, "pneumatic_pipe", MachinesItems.PNEUMATIC_PIPE_BLUEPRINT, Items.COPPER_BLOCK, MachinesBlocks.PNEUMATIC_PIPE.get().asItem(), 8);
        smithing(output, "void_tether", MachinesItems.VOID_TETHER_BLUEPRINT, Items.ENDER_PEARL, MachinesBlocks.VOID_TETHER.get().asItem(), 1);
        smithing(output, "rewinder", MachinesItems.REWINDER_BLUEPRINT, Items.ANVIL, MachinesBlocks.REWINDER.get().asItem(), 1);
        smithing(output, "hastening_gear", MachinesItems.HASTENING_GEAR_BLUEPRINT, Items.REDSTONE_BLOCK, MachinesBlocks.HASTENING_GEAR.get().asItem(), 1);
        smithing(output, "phase_watch", MachinesItems.PHASE_WATCH_BLUEPRINT, Items.CLOCK, MachinesItems.PHASE_WATCH.get(), 1);
        smithing(output, "gearlift", MachinesItems.GEARLIFT_BLUEPRINT, Items.PISTON, MachinesBlocks.GEARLIFT.get().asItem(), 2);

        Item pipe = MachinesBlocks.PNEUMATIC_PIPE.get().asItem();
        recipes.shapeless(RecipeCategory.MISC, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get())
                .requires(pipe).requires(Items.HOPPER)
                .unlockedBy(RecipeContext.hasName(pipe), recipes.has(pipe))
                .save(output);
        recipes.shapeless(RecipeCategory.MISC, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get())
                .requires(pipe).requires(Items.DROPPER)
                .unlockedBy(RecipeContext.hasName(pipe), recipes.has(pipe))
                .save(output);
    }

    private static void smithing(RecipeOutput output, String machine, DeferredItem<SmithingTemplateItem> blueprint, Item base, Item result, int count) {
        smithing(output, machine + "_smithing", Ingredient.of(blueprint.get()), Ingredient.of(base),
                Ingredient.of(RustworksItems.KAIRITE_GEAR.get()), result, count);
    }

    public static void smithing(RecipeOutput output, String id, Ingredient template, Ingredient base, Ingredient addition, Item result, int count) {
        //? if >=26.1 {
        /*output.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, Upfall.id(id)),
                new SmithingTransformRecipe(new net.minecraft.world.item.crafting.Recipe.CommonInfo(true), java.util.Optional.of(template), base,
                        java.util.Optional.of(addition), new net.minecraft.world.item.ItemStackTemplate(result, count)), null);
        *///?} else {
        output.accept(Upfall.id(id), new SmithingTransformRecipe(template, base, addition, new ItemStack(result, count)), null);
        //?}
    }
}
