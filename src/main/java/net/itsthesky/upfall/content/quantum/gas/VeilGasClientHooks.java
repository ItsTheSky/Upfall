package net.itsthesky.upfall.content.quantum.gas;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

final class VeilGasClientHooks {
    private VeilGasClientHooks() {}

    static Player localPlayer() {
        return Minecraft.getInstance().player;
    }
}
