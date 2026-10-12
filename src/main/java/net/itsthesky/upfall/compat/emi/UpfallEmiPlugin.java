package net.itsthesky.upfall.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;

@EmiEntrypoint
public class UpfallEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory INCANTATION = new EmiRecipeCategory(
            Upfall.id("quantum_incantation"), EmiStack.of(QuantumBlocks.QUANTUM_PEDESTAL.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(INCANTATION);
        registry.addWorkstation(INCANTATION, EmiStack.of(QuantumBlocks.QUANTUM_PEDESTAL.get()));
        for (var holder : registry.getRecipeManager().getAllRecipesFor(QuantumRecipeTypes.INCANTATION.get())) {
            registry.addRecipe(new IncantationEmiRecipe(holder));
        }
    }
}
