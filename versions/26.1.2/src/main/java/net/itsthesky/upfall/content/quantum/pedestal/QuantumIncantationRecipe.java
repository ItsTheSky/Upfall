package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record QuantumIncantationRecipe(Ingredient base, List<Ingredient> ingredients, Optional<Ingredient> catalystOverride,
                                       QuantumUpgrade upgrade) implements Recipe<IncantationInput> {
    public static final int SIDES = IncantationLogic.SIDES;

    public static final MapCodec<QuantumIncantationRecipe> CODEC = IncantationLogic.codec(Ingredient.CODEC, Ingredient.CODEC.listOf());

    public static final StreamCodec<RegistryFriendlyByteBuf, QuantumIncantationRecipe> STREAM_CODEC = IncantationLogic.streamCodec();

    public QuantumIncantationRecipe {
        ingredients = List.copyOf(ingredients);
    }

    public Ingredient catalyst() {
        return IncantationLogic.catalyst(catalystOverride);
    }

    @Override
    public boolean matches(IncantationInput input, Level level) {
        return IncantationLogic.matches(base, ingredients, catalyst(), input);
    }

    @Override
    public ItemStack assemble(IncantationInput input) {
        return IncantationLogic.assemble(input, upgrade);
    }

    public ItemStack assemble(IncantationInput input, HolderLookup.Provider registries) {
        return assemble(input);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<IncantationInput>> getSerializer() {
        return QuantumRecipeTypes.INCANTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<IncantationInput>> getType() {
        return QuantumRecipeTypes.INCANTATION.get();
    }
}
