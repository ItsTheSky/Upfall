package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class IncantationLogic {
    public static final int SIDES = 4;

    private IncantationLogic() {}

    public static DataResult<List<Ingredient>> checkSides(List<Ingredient> list) {
        return list.size() == SIDES ? DataResult.success(list)
                : DataResult.error(() -> "An incantation needs exactly " + SIDES + " ingredients");
    }

    static MapCodec<QuantumIncantationRecipe> codec(Codec<Ingredient> ingredient, Codec<List<Ingredient>> ingredients) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                ingredient.fieldOf("base").forGetter(QuantumIncantationRecipe::base),
                ingredients.validate(IncantationLogic::checkSides).fieldOf("ingredients")
                        .forGetter(QuantumIncantationRecipe::ingredients),
                ingredient.optionalFieldOf("catalyst").forGetter(QuantumIncantationRecipe::catalystOverride),
                QuantumUpgrade.CODEC.fieldOf("upgrade").forGetter(QuantumIncantationRecipe::upgrade)
        ).apply(i, QuantumIncantationRecipe::new));
    }

    static StreamCodec<RegistryFriendlyByteBuf, QuantumIncantationRecipe> streamCodec() {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, QuantumIncantationRecipe::base,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), QuantumIncantationRecipe::ingredients,
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), QuantumIncantationRecipe::catalystOverride,
                ByteBufCodecs.idMapper(id -> QuantumUpgrade.values()[id], QuantumUpgrade::ordinal), QuantumIncantationRecipe::upgrade,
                QuantumIncantationRecipe::new);
    }

    public static Ingredient catalyst(Optional<Ingredient> override) {
        return override.orElseGet(() -> Ingredient.of(QuantumItems.QUANTUM_STONE.get()));
    }

    public static boolean matches(Ingredient base, List<Ingredient> ingredients, Ingredient catalyst, IncantationInput input) {
        if (!base.test(input.center()) || input.sides().size() != ingredients.size()) return false;
        for (ItemStack corner : input.corners()) {
            if (!catalyst.test(corner)) return false;
        }
        return matchSides(ingredients, input.sides(), 0, new boolean[ingredients.size()]);
    }

    private static boolean matchSides(List<Ingredient> ingredients, List<ItemStack> sides, int index, boolean[] used) {
        if (index == sides.size()) return true;
        for (int i = 0; i < ingredients.size(); i++) {
            if (used[i] || !ingredients.get(i).test(sides.get(index))) continue;
            used[i] = true;
            if (matchSides(ingredients, sides, index + 1, used)) return true;
            used[i] = false;
        }
        return false;
    }

    public static ItemStack assemble(IncantationInput input, QuantumUpgrade upgrade) {
        ItemStack result = input.center().copy();
        QuantumUpgrades.apply(result, upgrade);
        return result;
    }

    public static List<ItemStack> stacks(Ingredient ingredient) {
        //? if >=26.1 {
        /*return ingredient.items().map(holder -> new ItemStack(holder)).toList();
        *///?} else {
        return Arrays.asList(ingredient.getItems());
        //?}
    }
}
