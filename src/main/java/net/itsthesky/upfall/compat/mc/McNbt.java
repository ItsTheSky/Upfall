package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

import java.util.Optional;
import java.util.UUID;

public final class McNbt {
    private McNbt() {}

    public static int getInt(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getIntOr(k, 0);
        *///?} else {
        return t.getInt(k);
        //?}
    }

    public static long getLong(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getLongOr(k, 0L);
        *///?} else {
        return t.getLong(k);
        //?}
    }

    public static float getFloat(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getFloatOr(k, 0.0F);
        *///?} else {
        return t.getFloat(k);
        //?}
    }

    public static double getDouble(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getDoubleOr(k, 0.0D);
        *///?} else {
        return t.getDouble(k);
        //?}
    }

    public static boolean getBoolean(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getBooleanOr(k, false);
        *///?} else {
        return t.getBoolean(k);
        //?}
    }

    public static String getString(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getStringOr(k, "");
        *///?} else {
        return t.getString(k);
        //?}
    }

    public static byte getByte(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getByteOr(k, (byte) 0);
        *///?} else {
        return t.getByte(k);
        //?}
    }

    public static short getShort(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getShortOr(k, (short) 0);
        *///?} else {
        return t.getShort(k);
        //?}
    }

    public static int[] getIntArray(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getIntArray(k).orElseGet(() -> new int[0]);
        *///?} else {
        return t.getIntArray(k);
        //?}
    }

    public static long[] getLongArray(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getLongArray(k).orElseGet(() -> new long[0]);
        *///?} else {
        return t.getLongArray(k);
        //?}
    }

    public static CompoundTag getCompound(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.getCompoundOrEmpty(k);
        *///?} else {
        return t.getCompound(k);
        //?}
    }

    public static ListTag getList(CompoundTag t, String k, int elementType) {
        //? if >=26.1 {
        /*return t.getListOrEmpty(k);
        *///?} else {
        return t.getList(k, elementType);
        //?}
    }

    public static CompoundTag listCompound(ListTag l, int i) {
        //? if >=26.1 {
        /*return l.getCompoundOrEmpty(i);
        *///?} else {
        return l.getCompound(i);
        //?}
    }

    public static int listInt(ListTag l, int i) {
        //? if >=26.1 {
        /*return l.getIntOr(i, 0);
        *///?} else {
        return l.getInt(i);
        //?}
    }

    public static void putUUID(CompoundTag t, String k, UUID u) {
        //? if >=26.1 {
        /*t.store(k, net.minecraft.core.UUIDUtil.CODEC, u);
        *///?} else {
        t.putUUID(k, u);
        //?}
    }

    public static Optional<UUID> readUUID(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.read(k, net.minecraft.core.UUIDUtil.CODEC);
        *///?} else {
        return t.hasUUID(k) ? Optional.of(t.getUUID(k)) : Optional.empty();
        //?}
    }

    public static void putBlockPos(CompoundTag t, String k, BlockPos p) {
        //? if >=26.1 {
        /*t.store(k, BlockPos.CODEC, p);
        *///?} else {
        t.put(k, NbtUtils.writeBlockPos(p));
        //?}
    }

    public static Optional<BlockPos> readBlockPos(CompoundTag t, String k) {
        //? if >=26.1 {
        /*return t.read(k, BlockPos.CODEC);
        *///?} else {
        return NbtUtils.readBlockPos(t, k);
        //?}
    }
}
