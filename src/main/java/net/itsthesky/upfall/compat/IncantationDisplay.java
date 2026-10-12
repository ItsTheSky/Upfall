package net.itsthesky.upfall.compat;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public final class IncantationDisplay {
    public static final int[][] SIDE_CELLS = {{1, 0}, {2, 1}, {1, 2}, {0, 1}};
    public static final int[][] CORNER_CELLS = {{0, 0}, {2, 0}, {0, 2}, {2, 2}};
    public static final int SLOT = 18;
    public static final int PITCH = 28;
    public static final int RING = PITCH * 2 + SLOT;
    public static final int ARROW_X = RING + 10;
    public static final int ARROW_Y = PITCH - 1;
    public static final int OUTPUT_X = ARROW_X + 32;
    public static final int HINT_Y = RING + 5;
    private static final int LINE = 0x90A68CFF;

    private IncantationDisplay() {}

    public static int cellX(int[] cell) {
        return cell[0] * PITCH;
    }

    public static int cellY(int[] cell) {
        return cell[1] * PITCH;
    }

    public static int width() {
        return Math.max(OUTPUT_X + 30, font().width(hint()) + 2);
    }

    public static int height() {
        return HINT_Y + font().lineHeight + 1;
    }

    public static void drawRing(GuiGraphics graphics) {
        int near = SLOT / 2;
        int far = PITCH * 2 + SLOT / 2;
        for (int k = 0; k < 2; k++) {
            int from = k * PITCH + SLOT;
            int to = (k + 1) * PITCH;
            graphics.fill(from, near, to, near + 1, LINE);
            graphics.fill(from, far, to, far + 1, LINE);
            graphics.fill(near, from, near + 1, to, LINE);
            graphics.fill(far, from, far + 1, to, LINE);
        }
        int mid = PITCH + SLOT / 2;
        graphics.fill(mid, SLOT, mid + 1, PITCH, LINE);
        graphics.fill(mid, PITCH + SLOT, mid + 1, PITCH * 2, LINE);
        graphics.fill(SLOT, mid, PITCH, mid + 1, LINE);
        graphics.fill(PITCH + SLOT, mid, PITCH * 2, mid + 1, LINE);
    }

    public static void drawHint(GuiGraphics graphics, int color) {
        Component hint = hint();
        graphics.drawString(font(), hint, (width() - font().width(hint)) / 2, HINT_Y, color, false);
    }

    public static List<ItemStack> bases(QuantumIncantationRecipe recipe) {
        List<ItemStack> all = Arrays.asList(recipe.base().getItems());
        List<ItemStack> applicable = all.stream().filter(s -> recipe.upgrade().canApplyTo(s)).toList();
        return applicable.isEmpty() ? all : applicable;
    }

    public static List<ItemStack> results(QuantumIncantationRecipe recipe) {
        return bases(recipe).stream().map(s -> {
            ItemStack result = s.copy();
            QuantumUpgrades.apply(result, recipe.upgrade());
            return result;
        }).toList();
    }

    public static Component hint() {
        return Component.translatable("compat.upfall.incantation.hint", UpfallConfig.getInt(UpfallConfig.RITUAL_TICKS) / 20);
    }

    public static Component title() {
        return Component.translatable("emi.category.upfall.quantum_incantation");
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }
}
