package net.itsthesky.upfall.content.music;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.music.client.StructureMusicClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StructureMusicPayload(int index) implements CustomPacketPayload {
    public static final Type<StructureMusicPayload> TYPE = new Type<>(Upfall.id("structure_music"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureMusicPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(StructureMusicPayload::new, StructureMusicPayload::index).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, StructureMusicPayload::handle);
    }

    private static void handle(StructureMusicPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> StructureMusicClient.set(payload.index()));
    }
}
