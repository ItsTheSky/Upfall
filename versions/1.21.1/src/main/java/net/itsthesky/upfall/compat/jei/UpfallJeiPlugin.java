package net.itsthesky.upfall.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class UpfallJeiPlugin implements IModPlugin {
    public static final RecipeType<RecipeHolder<QuantumIncantationRecipe>> INCANTATION =
            RecipeType.createRecipeHolderType(Upfall.id("quantum_incantation"));

    @Override
    public ResourceLocation getPluginUid() {
        return Upfall.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new IncantationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        registration.addRecipes(INCANTATION, level.getRecipeManager().getAllRecipesFor(QuantumRecipeTypes.INCANTATION.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(QuantumBlocks.QUANTUM_PEDESTAL.get(), INCANTATION);
    }
}
