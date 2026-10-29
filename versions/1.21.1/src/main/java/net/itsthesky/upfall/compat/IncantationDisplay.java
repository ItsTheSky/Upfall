package net.itsthesky.upfall.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class IncantationDisplay extends IncantationLayout {
    private IncantationDisplay() {}

    public static void drawRing(GuiGraphics graphics) {
        ringLines((x0, y0, x1, y1, color) -> graphics.fill(x0, y0, x1, y1, color));
    }

    public static void drawHint(GuiGraphics graphics, int color) {
        Component hint = hint();
        graphics.drawString(font(), hint, hintX(), HINT_Y, color, false);
    }
}
