package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record QuantumIncantationRecipe(Ingredient base, List<Ingredient> ingredients, Optional<Ingredient> catalystOverride,
                                       QuantumUpgrade upgrade) implements Recipe<IncantationInput> {
    public static final int SIDES = IncantationLogic.SIDES;

    public static final MapCodec<QuantumIncantationRecipe> CODEC = IncantationLogic.codec(Ingredient.CODEC_NONEMPTY, Ingredient.LIST_CODEC_NONEMPTY);

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
    public ItemStack assemble(IncantationInput input, HolderLookup.Provider registries) {
        return IncantationLogic.assemble(input, upgrade);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(base);
        list.addAll(ingredients);
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return QuantumRecipeTypes.INCANTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return QuantumRecipeTypes.INCANTATION.get();
    }

    public static class Serializer implements RecipeSerializer<QuantumIncantationRecipe> {
        @Override
        public MapCodec<QuantumIncantationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QuantumIncantationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
