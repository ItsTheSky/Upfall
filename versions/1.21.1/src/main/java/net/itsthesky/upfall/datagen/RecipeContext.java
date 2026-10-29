package net.itsthesky.upfall.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class RecipeContext {
    private final RecipeOutput output;

    public RecipeContext(RecipeOutput output) {
        this.output = output;
    }

    public RecipeOutput output() {
        return output;
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return ShapedRecipeBuilder.shaped(category, result);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return ShapedRecipeBuilder.shaped(category, result, count);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return ShapelessRecipeBuilder.shapeless(category, result);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return ShapelessRecipeBuilder.shapeless(category, result, count);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return ModRecipeProvider.hasItem(item);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return ModRecipeProvider.hasItem(tag);
    }

    public Ingredient tag(TagKey<Item> tag) {
        return Ingredient.of(tag);
    }

    public static String hasName(ItemLike item) {
        return ModRecipeProvider.hasName(item);
    }

    public static String itemName(ItemLike item) {
        return ModRecipeProvider.itemName(item);
    }

    public SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.smelting(ingredient, category, result, experience, time);
    }

    public SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.blasting(ingredient, category, result, experience, time);
    }

    public SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.smoking(ingredient, category, result, experience, time);
    }

    public SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.campfireCooking(ingredient, category, result, experience, time);
    }

    public SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result) {
        return SingleItemRecipeBuilder.stonecutting(ingredient, category, result);
    }

    public SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        return SingleItemRecipeBuilder.stonecutting(ingredient, category, result, count);
    }

    public SmithingTransformRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return SmithingTransformRecipeBuilder.smithing(template, base, addition, category, result);
    }

    public void save(RecipeBuilder builder, ResourceLocation id) {
        builder.save(output, id);
    }

    public void save(SmithingTransformRecipeBuilder builder, ResourceLocation id) {
        builder.save(output, id);
    }
}
