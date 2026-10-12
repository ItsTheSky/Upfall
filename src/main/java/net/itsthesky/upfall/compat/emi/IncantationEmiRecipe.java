package net.itsthesky.upfall.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.itsthesky.upfall.compat.IncantationDisplay;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class IncantationEmiRecipe implements EmiRecipe {
    private final RecipeHolder<QuantumIncantationRecipe> holder;
    private final EmiIngredient base;
    private final List<EmiIngredient> sides;
    private final EmiIngredient catalyst;
    private final List<EmiStack> results;

    public IncantationEmiRecipe(RecipeHolder<QuantumIncantationRecipe> holder) {
        this.holder = holder;
        QuantumIncantationRecipe recipe = holder.value();
        this.base = EmiIngredient.of(IncantationDisplay.bases(recipe).stream().map(s -> (EmiIngredient) EmiStack.of(s)).toList());
        this.sides = recipe.ingredients().stream().map(EmiIngredient::of).toList();
        this.catalyst = EmiIngredient.of(recipe.catalyst());
        this.results = IncantationDisplay.results(recipe).stream().map(EmiStack::of).toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return UpfallEmiPlugin.INCANTATION;
    }

    @Override
    public ResourceLocation getId() {
        return holder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(base);
        inputs.addAll(sides);
        inputs.add(catalyst);
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return results;
    }

    @Override
    public int getDisplayWidth() {
        return IncantationDisplay.width();
    }

    @Override
    public int getDisplayHeight() {
        return IncantationDisplay.height();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, IncantationDisplay.RING, IncantationDisplay.RING,
                (graphics, mouseX, mouseY, delta) -> IncantationDisplay.drawRing(graphics));
        widgets.addSlot(base, IncantationDisplay.PITCH, IncantationDisplay.PITCH);
        for (int i = 0; i < sides.size(); i++) {
            int[] cell = IncantationDisplay.SIDE_CELLS[i];
            widgets.addSlot(sides.get(i), IncantationDisplay.cellX(cell), IncantationDisplay.cellY(cell));
        }
        for (int[] cell : IncantationDisplay.CORNER_CELLS) {
            widgets.addSlot(catalyst, IncantationDisplay.cellX(cell), IncantationDisplay.cellY(cell)).catalyst(true);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, IncantationDisplay.ARROW_X, IncantationDisplay.ARROW_Y + 1);
        widgets.addSlot(EmiIngredient.of(results), IncantationDisplay.OUTPUT_X, IncantationDisplay.PITCH - 4).large(true).recipeContext(this);
        widgets.addText(IncantationDisplay.hint(), IncantationDisplay.width() / 2, IncantationDisplay.HINT_Y, 0x555555, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
    }
}
