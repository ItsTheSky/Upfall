package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.compat.mc.McNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class GrottoPiece extends StructurePiece {
    private final GrottoLayout layout;
    private final long seed;
    private final int x;
    private final int z;

    private GrottoPiece(GrottoLayout layout, long seed, int x, int z) {
        super(GrottoStructures.PIECE.get(), 0, layout.bounds());
        this.layout = layout;
        this.seed = seed;
        this.x = x;
        this.z = z;
    }

    public static GrottoPiece create(long seed, int x, int z, int topY) {
        return new GrottoPiece(new GrottoLayout(seed, x, z, topY), seed, x, z);
    }

    public GrottoPiece(CompoundTag tag) {
        super(GrottoStructures.PIECE.get(), tag);
        this.seed = McNbt.getLong(tag, "Seed");
        this.x = McNbt.getInt(tag, "X");
        this.z = McNbt.getInt(tag, "Z");
        this.layout = new GrottoLayout(seed, x, z, McNbt.getInt(tag, "TopY"));
    }

    public GrottoLayout layout() {
        return layout;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Seed", seed);
        tag.putInt("X", x);
        tag.putInt("Z", z);
        tag.putInt("TopY", layout.topY());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        int minX = Math.max(box.minX(), boundingBox.minX());
        int maxX = Math.min(box.maxX(), boundingBox.maxX());
        int minZ = Math.max(box.minZ(), boundingBox.minZ());
        int maxZ = Math.min(box.maxZ(), boundingBox.maxZ());
        int minY = Math.max(box.minY(), boundingBox.minY());
        int maxY = Math.min(box.maxY(), boundingBox.maxY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int px = minX; px <= maxX; px++) {
            for (int pz = minZ; pz <= maxZ; pz++) {
                for (int py = minY; py <= maxY; py++) {
                    BlockState state = layout.stateAt(px, py, pz);
                    if (state != null) {
                        level.setBlock(pos.set(px, py, pz), state, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        for (BlockPos cache : layout.caches()) {
            if (box.isInside(cache)) {
                RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(seed ^ cache.asLong()), cache, GrottoLoot.CRYSTAL_GROTTO);
            }
        }
    }
}
