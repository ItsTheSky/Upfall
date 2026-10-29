package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.FlowerBlock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        for (BlockFamily family : ModBlockFamilies.all()) {
            generateRecipes(family, FeatureFlags.DEFAULT_FLAGS);
        }

        UpfallRecipes.build(new RecipeContext(this));

        for (FlowerBlock flower : List.of(ModBlocks.DUSKBLOOM.get(), RustworksBlocks.VERDIGRIS_BLOOM.get())) {
            shapeless(RecipeCategory.FOOD, new ItemStackTemplate(Items.SUSPICIOUS_STEW,
                    DataComponentPatch.builder().set(DataComponents.SUSPICIOUS_STEW_EFFECTS, flower.getSuspiciousEffects()).build()))
                    .requires(Items.BOWL).requires(Items.BROWN_MUSHROOM).requires(Items.RED_MUSHROOM).requires(flower)
                    .group("suspicious_stew").unlockedBy(getHasName(flower), has(flower))
                    .save(output, Upfall.MODID + ":suspicious_stew_from_" + getItemName(flower));
        }
    }

    public static String hasName(ItemLike item) {
        return getHasName(item);
    }

    public static String itemName(ItemLike item) {
        return getItemName(item);
    }

    RecipeOutput recipeOutput() {
        return output;
    }

    Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item) {
        return has(item);
    }

    Criterion<InventoryChangeTrigger.TriggerInstance> hasTag(TagKey<Item> tag) {
        return has(tag);
    }

    Ingredient ingredient(TagKey<Item> tag) {
        return tag(tag);
    }

    ShapedRecipeBuilder shapedBuilder(RecipeCategory category, ItemLike result, int count) {
        return shaped(category, result, count);
    }

    ShapelessRecipeBuilder shapelessBuilder(RecipeCategory category, ItemLike result, int count) {
        return shapeless(category, result, count);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Upfall Recipes";
        }
    }
}
