package net.itsthesky.upfall.content.aerie.worldgen;

import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.content.aerie.AerieStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class AeriePiece extends StructurePiece {
    private final BlockPos origin;
    private final long seed;
    private final boolean bridge;
    private final int index;
    private AerieBlueprint blueprint;

    public AeriePiece(BlockPos origin, long seed, boolean bridge, int index) {
        super(AerieStructures.PEARL_AERIE_PIECE.get(), 0, new BoundingBox(origin));
        this.origin = origin;
        this.seed = seed;
        this.bridge = bridge;
        this.index = index;
        setOrientation(null);
        boundingBox = blueprint().bounds();
    }

    public AeriePiece(CompoundTag tag) {
        super(AerieStructures.PEARL_AERIE_PIECE.get(), tag);
        this.origin = BlockPos.of(McNbt.getLong(tag, "Origin"));
        this.seed = McNbt.getLong(tag, "Seed");
        this.bridge = McNbt.getBoolean(tag, "Bridge");
        this.index = McNbt.getInt(tag, "Index");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putLong("Seed", seed);
        tag.putBoolean("Bridge", bridge);
        tag.putInt("Index", index);
    }

    public synchronized AerieBlueprint blueprint() {
        if (blueprint == null) {
            AerieLayout.Plan plan = AerieLayout.plan(seed, origin);
            long pieceSeed = seed * 31L + (bridge ? 1000L : 0L) + index;
            blueprint = bridge ? AerieBlueprint.bridge(plan, index, pieceSeed) : AerieBlueprint.tower(plan, index, pieceSeed);
        }
        return blueprint;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        blueprint().place(level, box, random);
    }
}
