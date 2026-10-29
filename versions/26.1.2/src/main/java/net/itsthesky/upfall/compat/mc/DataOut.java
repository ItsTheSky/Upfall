package net.itsthesky.upfall.compat.mc;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.LongStream;

public final class DataOut {
    private final ValueOutput out;

    private DataOut(ValueOutput out) {
        this.out = out;
    }

    public static DataOut of(ValueOutput out) {
        return new DataOut(out);
    }

    public ValueOutput raw() {
        return out;
    }

    public void putInt(String key, int value) {
        out.putInt(key, value);
    }

    public void putLong(String key, long value) {
        out.putLong(key, value);
    }

    public void putFloat(String key, float value) {
        out.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        out.putDouble(key, value);
    }

    public void putBoolean(String key, boolean value) {
        out.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        out.putString(key, value);
    }

    public void putIntArray(String key, int[] value) {
        out.putIntArray(key, value);
    }

    public void putLongArray(String key, long[] value) {
        out.store(key, Codec.LONG_STREAM, LongStream.of(value));
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        out.store(key, codec, value);
    }

    public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
        out.storeNullable(key, codec, value);
    }

    public void putItem(String key, ItemStack stack) {
        out.store(key, ItemStack.OPTIONAL_CODEC, stack);
    }

    public void putUUID(String key, UUID value) {
        out.store(key, UUIDUtil.CODEC, value);
    }

    public void putBlockPos(String key, BlockPos pos) {
        out.store(key, BlockPos.CODEC, pos);
    }

    public void putCompound(String key, CompoundTag value) {
        out.store(key, CompoundTag.CODEC, value);
    }

    public void merge(CompoundTag value) {
        out.store(value);
    }

    public DataOut child(String key) {
        return new DataOut(out.child(key));
    }

    public ChildList childList(String key) {
        return new ChildList(out.childrenList(key));
    }

    public void saveItems(NonNullList<ItemStack> items) {
        ContainerHelper.saveAllItems(out, items);
    }

    public static final class ChildList {
        private final ValueOutput.ValueOutputList list;

        private ChildList(ValueOutput.ValueOutputList list) {
            this.list = list;
        }

        public DataOut add() {
            return new DataOut(list.addChild());
        }
    }
}
