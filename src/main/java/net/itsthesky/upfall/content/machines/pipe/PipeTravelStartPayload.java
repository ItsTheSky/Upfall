package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McCodecs;
import net.itsthesky.upfall.content.machines.pipe.client.PipeTravelClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PipeTravelStartPayload(List<Vec3> points, double progress, float speed, Vec3 push) implements CustomPacketPayload {
    private static final int MAX_POINTS = PipeNetwork.MAX_SEGMENTS * 2 + 8;
    public static final Type<PipeTravelStartPayload> TYPE = new Type<>(Upfall.id("pipe_travel_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeTravelStartPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.points.size());
                payload.points.forEach(point -> McCodecs.writeVec3(buf, point));
                buf.writeDouble(payload.progress);
                buf.writeFloat(payload.speed);
                McCodecs.writeVec3(buf, payload.push);
            },
            buf -> {
                int count = Math.min(buf.readVarInt(), MAX_POINTS);
                List<Vec3> points = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    points.add(McCodecs.readVec3(buf));
                }
                return new PipeTravelStartPayload(points, buf.readDouble(), buf.readFloat(), McCodecs.readVec3(buf));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(PipeTravelStartPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PipeTravelClient.start(payload));
    }
}
