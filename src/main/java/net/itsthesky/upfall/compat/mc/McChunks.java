package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public final class McChunks {
    private McChunks() {}

    public static int x(ChunkPos pos) {
        //? if >=26.1 {
        /*return pos.x();
        *///?} else {
        return pos.x;
        //?}
    }

    public static int z(ChunkPos pos) {
        //? if >=26.1 {
        /*return pos.z();
        *///?} else {
        return pos.z;
        //?}
    }

    public static long key(ChunkPos pos) {
        //? if >=26.1 {
        /*return pos.pack();
        *///?} else {
        return pos.toLong();
        //?}
    }

    public static long key(int x, int z) {
        //? if >=26.1 {
        /*return ChunkPos.pack(x, z);
        *///?} else {
        return ChunkPos.asLong(x, z);
        //?}
    }

    public static long key(BlockPos pos) {
        //? if >=26.1 {
        /*return ChunkPos.pack(pos);
        *///?} else {
        return ChunkPos.asLong(pos);
        //?}
    }

    public static ChunkPos of(BlockPos pos) {
        //? if >=26.1 {
        /*return ChunkPos.containing(pos);
        *///?} else {
        return new ChunkPos(pos);
        //?}
    }

    public static ChunkPos of(long key) {
        //? if >=26.1 {
        /*return ChunkPos.unpack(key);
        *///?} else {
        return new ChunkPos(key);
        //?}
    }
}
