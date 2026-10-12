package net.itsthesky.upfall.content.quantum.upgrade;

import net.itsthesky.upfall.Upfall;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ZiplineLatchPayload() implements CustomPacketPayload {
    public static final ZiplineLatchPayload INSTANCE = new ZiplineLatchPayload();
    public static final Type<ZiplineLatchPayload> TYPE = new Type<>(Upfall.id("quantum_zipline_latch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZiplineLatchPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, ZiplineLatchPayload::handle);
    }

    private static void handle(ZiplineLatchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuantumUpgradeEffects.latchFromAir(player);
            }
        });
    }
}
