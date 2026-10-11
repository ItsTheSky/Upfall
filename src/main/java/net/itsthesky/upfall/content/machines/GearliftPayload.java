package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.block.GearliftBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GearliftPayload(boolean up) implements CustomPacketPayload {
    public static final Type<GearliftPayload> TYPE = new Type<>(Upfall.id("gearlift"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GearliftPayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(GearliftPayload::new, GearliftPayload::up).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, GearliftPayload::handle);
    }

    private static void handle(GearliftPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                GearliftBlock.ride(player, payload.up());
            }
        });
    }
}
