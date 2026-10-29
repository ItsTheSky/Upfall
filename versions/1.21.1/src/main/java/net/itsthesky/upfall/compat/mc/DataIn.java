package net.itsthesky.upfall.compat.mc;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DataIn {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    private DataIn(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static DataIn of(CompoundTag tag, HolderLookup.Provider registries) {
        return new DataIn(tag, registries);
    }

    public CompoundTag tag() {
        return tag;
    }

    public HolderLookup.Provider lookup() {
        return registries;
    }

    public boolean has(String key) {
        return tag.contains(key);
    }

    public int getIntOr(String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    public long getLongOr(String key, long fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
    }

    public float getFloatOr(String key, float fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getFloat(key) : fallback;
    }

    public double getDoubleOr(String key, double fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : fallback;
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    public String getStringOr(String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    public Optional<Integer> getInt(String key) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getInt(key)) : Optional.empty();
    }

    public Optional<Long> getLong(String key) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getLong(key)) : Optional.empty();
    }

    public Optional<int[]> getIntArray(String key) {
        return tag.contains(key, Tag.TAG_INT_ARRAY) ? Optional.of(tag.getIntArray(key)) : Optional.empty();
    }

    public int[] getIntArrayOrEmpty(String key) {
        return tag.getIntArray(key);
    }

    public long[] getLongArrayOrEmpty(String key) {
        return tag.getLongArray(key);
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag value = tag.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), value).resultOrPartial(error -> LOGGER.warn("Failed to read {}: {}", key, error));
    }

    public ItemStack getItem(String key) {
        return ItemStack.parseOptional(registries, tag.getCompound(key));
    }

    public Optional<UUID> getUUID(String key) {
        return tag.hasUUID(key) ? Optional.of(tag.getUUID(key)) : Optional.empty();
    }

    public Optional<BlockPos> getBlockPos(String key) {
        return NbtUtils.readBlockPos(tag, key);
    }

    public Optional<CompoundTag> getCompound(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(tag.getCompound(key)) : Optional.empty();
    }

    public CompoundTag asTag() {
        return tag;
    }

    public DataIn childOrEmpty(String key) {
        return new DataIn(tag.getCompound(key), registries);
    }

    public Optional<DataIn> child(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(new DataIn(tag.getCompound(key), registries)) : Optional.empty();
    }

    public List<DataIn> childList(String key) {
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        List<DataIn> children = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            children.add(new DataIn(list.getCompound(i), registries));
        }
        return children;
    }

    public void loadItems(NonNullList<ItemStack> items) {
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
