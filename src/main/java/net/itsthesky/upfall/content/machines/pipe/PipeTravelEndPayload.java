package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McCodecs;
import net.itsthesky.upfall.content.machines.pipe.client.PipeTravelClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PipeTravelEndPayload(Vec3 motion) implements CustomPacketPayload {
    public static final Type<PipeTravelEndPayload> TYPE = new Type<>(Upfall.id("pipe_travel_end"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeTravelEndPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> McCodecs.writeVec3(buf, payload.motion), buf -> new PipeTravelEndPayload(McCodecs.readVec3(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(PipeTravelEndPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PipeTravelClient.end(payload));
    }
}
