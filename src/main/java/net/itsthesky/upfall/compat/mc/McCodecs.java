package net.itsthesky.upfall.compat.mc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class McCodecs {
    private McCodecs() {}

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        //? if >=26.1 {
        /*return Vec3.STREAM_CODEC.decode(buf);
        *///?} else {
        return buf.readVec3();
        //?}
    }

    public static void writeVec3(FriendlyByteBuf buf, Vec3 vec) {
        //? if >=26.1 {
        /*Vec3.STREAM_CODEC.encode(buf, vec);
        *///?} else {
        buf.writeVec3(vec);
        //?}
    }
}
