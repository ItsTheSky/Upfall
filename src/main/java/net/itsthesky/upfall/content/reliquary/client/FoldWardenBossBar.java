package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.itsthesky.upfall.Upfall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

final class FoldWardenBossBar {
    private FoldWardenBossBar() {}

    private static final ResourceLocation TEXTURE = Upfall.id("textures/gui/fold_warden_bossbar.png");
    private static final String NAME_KEY = "entity.upfall.fold_warden";
    private static final int FRAME_WIDTH = 196;
    private static final int FRAME_HEIGHT = 13;
    private static final int FILL_WIDTH = 182;
    private static final int FILL_HEIGHT = 5;
    private static final int FRAME_INSET = 7;
    private static final int FILL_TOP = 4;

    static void render(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        if (!(boss.getName().getContents() instanceof TranslatableContents contents) || !NAME_KEY.equals(contents.getKey())) return;
        event.setCanceled(true);
        GuiGraphics graphics = event.getGuiGraphics();
        int x = event.getX();
        int y = event.getY();
        int row = boss.getColor() == BossEvent.BossBarColor.BLUE ? 18 : 0;

        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, x - FRAME_INSET, y, 0.0F, row, FRAME_WIDTH, FRAME_HEIGHT, 256, 64);
        int filled = Mth.ceil(Mth.clamp(boss.getProgress(), 0.0F, 1.0F) * FILL_WIDTH);
        if (filled > 0) {
            graphics.blit(TEXTURE, x, y + FILL_TOP, 0.0F, row + FRAME_HEIGHT, filled, FILL_HEIGHT, 256, 64);
        }
        RenderSystem.disableBlend();

        Minecraft minecraft = Minecraft.getInstance();
        Component name = boss.getName();
        int width = minecraft.font.width(name);
        graphics.drawString(minecraft.font, name, graphics.guiWidth() / 2 - width / 2, y - 9, 0xFFFFFF);
        event.setIncrement(FRAME_HEIGHT + minecraft.font.lineHeight + 3);
    }
}
