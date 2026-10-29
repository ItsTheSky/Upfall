package net.itsthesky.upfall.content.quantum.client;

import net.itsthesky.upfall.compat.mc.client.McClientNet;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.itsthesky.upfall.content.quantum.upgrade.ZiplineLatchPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class QuantumClientExtras {
    private QuantumClientExtras() {}

    static void register(IEventBus modEventBus) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(QuantumBlocks.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
            event.registerBlockEntityRenderer(QuantumBlocks.QUANTUM_PEDESTAL_ENTITY.get(), QuantumPedestalRenderer::new);
        });
        NeoForge.EVENT_BUS.addListener(QuantumClientExtras::onRightClickEmpty);
    }

    private static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && !player.isPassenger()
                && QuantumUpgrades.equipped(player, QuantumUpgrade.ZIPLINE)) {
            McClientNet.sendToServer(ZiplineLatchPayload.INSTANCE);
        }
    }
}
