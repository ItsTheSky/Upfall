package net.itsthesky.upfall.compat.mc.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if >=26.1 {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;
*///?} else {
import net.neoforged.neoforge.network.PacketDistributor;
//?}

public final class McClientNet {
    private McClientNet() {}

    public static void sendToServer(CustomPacketPayload payload) {
        //? if >=26.1 {
        /*ClientPacketDistributor.sendToServer(payload);
        *///?} else {
        PacketDistributor.sendToServer(payload);
        //?}
    }
}
