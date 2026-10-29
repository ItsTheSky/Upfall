package net.itsthesky.upfall.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.IncantationRecipeSync;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class UpfallJeiPlugin implements IModPlugin {
    public static final IRecipeHolderType<QuantumIncantationRecipe> INCANTATION = IRecipeHolderType.create(Upfall.id("quantum_incantation"));

    @Override
    public Identifier getPluginUid() {
        return Upfall.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new IncantationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // TODO: confirm JEI runs this after RecipesReceivedEvent, otherwise the list is empty on first join
        registration.addRecipes(INCANTATION, IncantationRecipeSync.received());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(INCANTATION, QuantumBlocks.QUANTUM_PEDESTAL.get());
    }
}
