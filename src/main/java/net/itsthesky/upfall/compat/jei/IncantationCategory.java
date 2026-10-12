package net.itsthesky.upfall.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.itsthesky.upfall.compat.IncantationDisplay;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class IncantationCategory implements IRecipeCategory<RecipeHolder<QuantumIncantationRecipe>> {
    private final IDrawable icon;
    private final IDrawable arrow;

    public IncantationCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(QuantumBlocks.QUANTUM_PEDESTAL.get()));
        this.arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<RecipeHolder<QuantumIncantationRecipe>> getRecipeType() {
        return UpfallJeiPlugin.INCANTATION;
    }

    @Override
    public Component getTitle() {
        return IncantationDisplay.title();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return IncantationDisplay.width();
    }

    @Override
    public int getHeight() {
        return IncantationDisplay.height();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<QuantumIncantationRecipe> holder, IFocusGroup focuses) {
        QuantumIncantationRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, IncantationDisplay.PITCH + 1, IncantationDisplay.PITCH + 1)
                .setStandardSlotBackground().addItemStacks(IncantationDisplay.bases(recipe));
        for (int i = 0; i < QuantumIncantationRecipe.SIDES; i++) {
            int[] cell = IncantationDisplay.SIDE_CELLS[i];
            builder.addSlot(RecipeIngredientRole.INPUT, IncantationDisplay.cellX(cell) + 1, IncantationDisplay.cellY(cell) + 1)
                    .setStandardSlotBackground().addIngredients(recipe.ingredients().get(i));
        }
        for (int[] cell : IncantationDisplay.CORNER_CELLS) {
            builder.addSlot(RecipeIngredientRole.CATALYST, IncantationDisplay.cellX(cell) + 1, IncantationDisplay.cellY(cell) + 1)
                    .setStandardSlotBackground().addIngredients(recipe.catalyst());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, IncantationDisplay.OUTPUT_X + 5, IncantationDisplay.PITCH + 1)
                .setOutputSlotBackground().addItemStacks(IncantationDisplay.results(recipe));
    }

    @Override
    public void draw(RecipeHolder<QuantumIncantationRecipe> recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        IncantationDisplay.drawRing(graphics);
        arrow.draw(graphics, IncantationDisplay.ARROW_X, IncantationDisplay.ARROW_Y + 1);
        IncantationDisplay.drawHint(graphics, 0xFF555555);
    }
}
