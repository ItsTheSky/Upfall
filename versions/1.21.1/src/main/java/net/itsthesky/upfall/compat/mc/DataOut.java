package net.itsthesky.upfall.compat.mc;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class DataOut {
    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    private DataOut(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static DataOut of(CompoundTag tag, HolderLookup.Provider registries) {
        return new DataOut(tag, registries);
    }

    public CompoundTag tag() {
        return tag;
    }

    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        tag.putLong(key, value);
    }

    public void putFloat(String key, float value) {
        tag.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        tag.putDouble(key, value);
    }

    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    public void putIntArray(String key, int[] value) {
        tag.putIntArray(key, value);
    }

    public void putLongArray(String key, long[] value) {
        tag.putLongArray(key, value);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        tag.put(key, codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value).getOrThrow());
    }

    public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            store(key, codec, value);
        }
    }

    public void putItem(String key, ItemStack stack) {
        store(key, ItemStack.OPTIONAL_CODEC, stack);
    }

    public void putUUID(String key, UUID value) {
        tag.putUUID(key, value);
    }

    public void putBlockPos(String key, BlockPos pos) {
        tag.put(key, NbtUtils.writeBlockPos(pos));
    }

    public void putCompound(String key, CompoundTag value) {
        tag.put(key, value);
    }

    public void merge(CompoundTag value) {
        tag.merge(value);
    }

    public DataOut child(String key) {
        CompoundTag child = new CompoundTag();
        tag.put(key, child);
        return new DataOut(child, registries);
    }

    public ChildList childList(String key) {
        ListTag list = new ListTag();
        tag.put(key, list);
        return new ChildList(list, registries);
    }

    public void saveItems(NonNullList<ItemStack> items) {
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    public static final class ChildList {
        private final ListTag list;
        private final HolderLookup.Provider registries;

        private ChildList(ListTag list, HolderLookup.Provider registries) {
            this.list = list;
            this.registries = registries;
        }

        public DataOut add() {
            CompoundTag child = new CompoundTag();
            list.add(child);
            return new DataOut(child, registries);
        }
    }
}
