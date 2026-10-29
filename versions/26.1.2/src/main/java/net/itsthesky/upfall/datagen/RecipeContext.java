package net.itsthesky.upfall.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class RecipeContext {
    private final ModRecipeProvider provider;

    RecipeContext(ModRecipeProvider provider) {
        this.provider = provider;
    }

    public RecipeOutput output() {
        return provider.recipeOutput();
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return provider.shapedBuilder(category, result, 1);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return provider.shapedBuilder(category, result, count);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return provider.shapelessBuilder(category, result, 1);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return provider.shapelessBuilder(category, result, count);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return provider.hasItem(item);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return provider.hasTag(tag);
    }

    public Ingredient tag(TagKey<Item> tag) {
        return provider.ingredient(tag);
    }

    public static String hasName(ItemLike item) {
        return ModRecipeProvider.hasName(item);
    }

    public static String itemName(ItemLike item) {
        return ModRecipeProvider.itemName(item);
    }

    public SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        CookingBookCategory cooking = category == RecipeCategory.FOOD ? CookingBookCategory.FOOD : blastingCategory(result);
        return SimpleCookingRecipeBuilder.smelting(ingredient, category, cooking, result, experience, time);
    }

    public SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.blasting(ingredient, category, blastingCategory(result), result, experience, time);
    }

    public SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.smoking(ingredient, category, result, experience, time);
    }

    public SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int time) {
        return SimpleCookingRecipeBuilder.campfireCooking(ingredient, category, result, experience, time);
    }

    public SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result) {
        return SingleItemRecipeBuilder.stonecutting(ingredient, category, result, 1);
    }

    public SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        return SingleItemRecipeBuilder.stonecutting(ingredient, category, result, count);
    }

    public SmithingTransformRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return SmithingTransformRecipeBuilder.smithing(template, base, addition, category, result);
    }

    public void save(RecipeBuilder builder, Identifier id) {
        builder.save(output(), ResourceKey.create(Registries.RECIPE, id));
    }

    public void save(SmithingTransformRecipeBuilder builder, Identifier id) {
        builder.save(output(), ResourceKey.create(Registries.RECIPE, id));
    }

    private static CookingBookCategory blastingCategory(ItemLike result) {
        return result.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
    }
}
