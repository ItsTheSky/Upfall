package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class QuantumRecipeTypes {
    private QuantumRecipeTypes() {}

    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Upfall.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Upfall.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<QuantumIncantationRecipe>> INCANTATION =
            TYPES.register("quantum_incantation", () -> RecipeType.simple(Upfall.id("quantum_incantation")));
    public static final DeferredHolder<RecipeSerializer<?>, QuantumIncantationRecipe.Serializer> INCANTATION_SERIALIZER =
            SERIALIZERS.register("quantum_incantation", QuantumIncantationRecipe.Serializer::new);
}
