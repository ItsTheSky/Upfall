package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModTestStructureProvider implements DataProvider {
    private final PackOutput output;

    public ModTestStructureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(empty(cache, "empty", 8, 8, 8), empty(cache, "empty_large", 24, 16, 24));
    }

    private CompletableFuture<?> empty(CachedOutput cache, String name, int x, int y, int z) {
        CompoundTag tag = new CompoundTag();
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(x));
        size.add(IntTag.valueOf(y));
        size.add(IntTag.valueOf(z));
        tag.put("size", size);
        tag.put("blocks", new ListTag());
        tag.put("entities", new ListTag());
        tag.put("palette", new ListTag());
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Upfall.MODID).resolve("structure").resolve(name + ".nbt");
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, bytes);
                cache.writeIfNeeded(path, bytes.toByteArray(), com.google.common.hash.Hashing.sha1().hashBytes(bytes.toByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String getName() {
        return "Upfall GameTest structures";
    }
}
