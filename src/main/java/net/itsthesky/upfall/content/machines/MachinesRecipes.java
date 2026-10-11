package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Map;

final class MachinesRecipes {
    private MachinesRecipes() {}

    static final Map<String, String> APPLIES_TO = Map.of(
            "pneumatic_pipe_blueprint", "Copper Block",
            "void_tether_blueprint", "Ender Pearl",
            "rewinder_blueprint", "Anvil",
            "hastening_gear_blueprint", "Redstone Block",
            "phase_watch_blueprint", "Clock",
            "gearlift_blueprint", "Piston");

    static void register(RecipeOutput output) {
        smithing(output, "pneumatic_pipe", MachinesItems.PNEUMATIC_PIPE_BLUEPRINT, Items.COPPER_BLOCK, MachinesBlocks.PNEUMATIC_PIPE.get().asItem(), 8);
        smithing(output, "void_tether", MachinesItems.VOID_TETHER_BLUEPRINT, Items.ENDER_PEARL, MachinesBlocks.VOID_TETHER.get().asItem(), 1);
        smithing(output, "rewinder", MachinesItems.REWINDER_BLUEPRINT, Items.ANVIL, MachinesBlocks.REWINDER.get().asItem(), 1);
        smithing(output, "hastening_gear", MachinesItems.HASTENING_GEAR_BLUEPRINT, Items.REDSTONE_BLOCK, MachinesBlocks.HASTENING_GEAR.get().asItem(), 1);
        smithing(output, "phase_watch", MachinesItems.PHASE_WATCH_BLUEPRINT, Items.CLOCK, MachinesItems.PHASE_WATCH.get(), 1);
        smithing(output, "gearlift", MachinesItems.GEARLIFT_BLUEPRINT, Items.PISTON, MachinesBlocks.GEARLIFT.get().asItem(), 2);

        Item pipe = MachinesBlocks.PNEUMATIC_PIPE.get().asItem();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get())
                .requires(pipe).requires(Items.HOPPER)
                .unlockedBy(ModRecipeProvider.hasName(pipe), ModRecipeProvider.hasItem(pipe))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get())
                .requires(pipe).requires(Items.DROPPER)
                .unlockedBy(ModRecipeProvider.hasName(pipe), ModRecipeProvider.hasItem(pipe))
                .save(output);
    }

    private static void smithing(RecipeOutput output, String machine, DeferredItem<SmithingTemplateItem> blueprint, Item base, Item result, int count) {
        output.accept(Upfall.id(machine + "_smithing"), new SmithingTransformRecipe(Ingredient.of(blueprint.get()), Ingredient.of(base),
                Ingredient.of(RustworksItems.KAIRITE_GEAR.get()), new ItemStack(result, count)), null);
    }
}
