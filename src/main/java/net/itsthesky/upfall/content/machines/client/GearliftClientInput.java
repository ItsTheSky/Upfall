package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.compat.mc.client.McClientNet;
import net.itsthesky.upfall.content.machines.GearliftPayload;
import net.itsthesky.upfall.content.machines.block.GearliftBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;

final class GearliftClientInput {
    private GearliftClientInput() {}

    private static boolean jumpWasDown;
    private static boolean sneakWasDown;

    static void onClientTick(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        var options = Minecraft.getInstance().options;
        boolean jump = options.keyJump.isDown();
        boolean sneak = options.keyShift.isDown();
        if (player.onGround() && !player.isSpectator()
                && GearliftBlock.below(player.level(), player.getX(), player.getY(), player.getZ()) != null) {
            if (jump && !jumpWasDown) {
                McClientNet.sendToServer(new GearliftPayload(true));
            } else if (sneak && !sneakWasDown) {
                McClientNet.sendToServer(new GearliftPayload(false));
            }
        }
        jumpWasDown = jump;
        sneakWasDown = sneak;
    }
}
