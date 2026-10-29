package net.itsthesky.upfall.compat.mc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.LongStream;

public final class DataIn {
    private final ValueInput in;

    private DataIn(ValueInput in) {
        this.in = in;
    }

    public static DataIn of(ValueInput in) {
        return new DataIn(in);
    }

    public ValueInput raw() {
        return in;
    }

    @SuppressWarnings("deprecation")
    public HolderLookup.Provider lookup() {
        return in.lookup();
    }

    public boolean has(String key) {
        return in.read(key, ExtraCodecs.NBT).isPresent();
    }

    public int getIntOr(String key, int fallback) {
        return in.getIntOr(key, fallback);
    }

    public long getLongOr(String key, long fallback) {
        return in.getLongOr(key, fallback);
    }

    public float getFloatOr(String key, float fallback) {
        return in.getFloatOr(key, fallback);
    }

    public double getDoubleOr(String key, double fallback) {
        return in.getDoubleOr(key, fallback);
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return in.getBooleanOr(key, fallback);
    }

    public String getStringOr(String key, String fallback) {
        return in.getStringOr(key, fallback);
    }

    public Optional<Integer> getInt(String key) {
        return in.getInt(key);
    }

    public Optional<Long> getLong(String key) {
        return in.getLong(key);
    }

    public Optional<int[]> getIntArray(String key) {
        return in.getIntArray(key);
    }

    public int[] getIntArrayOrEmpty(String key) {
        return in.getIntArray(key).orElseGet(() -> new int[0]);
    }

    public long[] getLongArrayOrEmpty(String key) {
        return in.read(key, Codec.LONG_STREAM).map(LongStream::toArray).orElseGet(() -> new long[0]);
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        return in.read(key, codec);
    }

    public ItemStack getItem(String key) {
        return in.read(key, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    public Optional<UUID> getUUID(String key) {
        return in.read(key, UUIDUtil.CODEC);
    }

    public Optional<BlockPos> getBlockPos(String key) {
        return in.read(key, BlockPos.CODEC);
    }

    public Optional<CompoundTag> getCompound(String key) {
        return in.read(key, CompoundTag.CODEC);
    }

    @SuppressWarnings("deprecation")
    public CompoundTag asTag() {
        return in.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
    }

    public DataIn childOrEmpty(String key) {
        return new DataIn(in.childOrEmpty(key));
    }

    public Optional<DataIn> child(String key) {
        return in.child(key).map(DataIn::new);
    }

    public List<DataIn> childList(String key) {
        return in.childrenListOrEmpty(key).stream().map(DataIn::new).toList();
    }

    public void loadItems(NonNullList<ItemStack> items) {
        ContainerHelper.loadAllItems(in, items);
    }
}
