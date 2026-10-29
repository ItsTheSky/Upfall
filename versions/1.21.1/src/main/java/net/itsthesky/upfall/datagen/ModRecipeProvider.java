package net.itsthesky.upfall.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (BlockFamily family : ModBlockFamilies.all()) {
            generateRecipes(output, family, FeatureFlags.DEFAULT_FLAGS);
        }

        UpfallRecipes.build(new RecipeContext(output));
    }

    public static net.minecraft.advancements.Criterion<net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item) {
        return has(item);
    }

    public static net.minecraft.advancements.Criterion<net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance> hasItem(TagKey<Item> tag) {
        return has(tag);
    }

    public static String hasName(ItemLike item) {
        return getHasName(item);
    }

    public static String itemName(ItemLike item) {
        return getItemName(item);
    }
}
