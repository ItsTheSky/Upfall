package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final int SIDES = 4;

    public static final MapCodec<QuantumIncantationRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(QuantumIncantationRecipe::base),
            Ingredient.LIST_CODEC_NONEMPTY.validate(list -> list.size() == SIDES ? DataResult.success(list)
                    : DataResult.error(() -> "An incantation needs exactly " + SIDES + " ingredients")).fieldOf("ingredients")
                    .forGetter(QuantumIncantationRecipe::ingredients),
            Ingredient.CODEC_NONEMPTY.optionalFieldOf("catalyst").forGetter(QuantumIncantationRecipe::catalystOverride),
            QuantumUpgrade.CODEC.fieldOf("upgrade").forGetter(QuantumIncantationRecipe::upgrade)
    ).apply(i, QuantumIncantationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuantumIncantationRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, QuantumIncantationRecipe::base,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), QuantumIncantationRecipe::ingredients,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), QuantumIncantationRecipe::catalystOverride,
            ByteBufCodecs.idMapper(id -> QuantumUpgrade.values()[id], QuantumUpgrade::ordinal), QuantumIncantationRecipe::upgrade,
            QuantumIncantationRecipe::new);

    public QuantumIncantationRecipe {
        ingredients = List.copyOf(ingredients);
    }

    public Ingredient catalyst() {
        return catalystOverride.orElseGet(() -> Ingredient.of(QuantumItems.QUANTUM_STONE.get()));
    }

    @Override
    public boolean matches(IncantationInput input, Level level) {
        if (!base.test(input.center()) || input.sides().size() != ingredients.size()) return false;
        Ingredient catalyst = catalyst();
        for (ItemStack corner : input.corners()) {
            if (!catalyst.test(corner)) return false;
        }
        return matchSides(input.sides(), 0, new boolean[ingredients.size()]);
    }

    private boolean matchSides(List<ItemStack> sides, int index, boolean[] used) {
        if (index == sides.size()) return true;
        for (int i = 0; i < ingredients.size(); i++) {
            if (used[i] || !ingredients.get(i).test(sides.get(index))) continue;
            used[i] = true;
            if (matchSides(sides, index + 1, used)) return true;
            used[i] = false;
        }
        return false;
    }

    @Override
    public ItemStack assemble(IncantationInput input, HolderLookup.Provider registries) {
        ItemStack result = input.center().copy();
        QuantumUpgrades.apply(result, upgrade);
        return result;
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
