package net.itsthesky.upfall.content.reliquary.worldgen;

import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
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

public class ReliquaryPiece extends StructurePiece {
    public enum Kind { CUBE, LINK, DEBRIS, APPROACH, TRAIL, FRAGMENT }

    private final BlockPos origin;
    private final long seed;
    private final Kind kind;
    private final int index;
    private ReliquaryBlueprint blueprint;

    private record CachedPlan(long seed, BlockPos origin, ReliquaryLayout.Plan plan) {}

    private static volatile CachedPlan lastPlan;

    public ReliquaryPiece(ReliquaryLayout.Plan plan, long seed, Kind kind, int index) {
        super(ReliquaryStructures.TILTED_RELIQUARY_PIECE.get(), 0, new BoundingBox(plan.origin()));
        this.origin = plan.origin();
        this.seed = seed;
        this.kind = kind;
        this.index = index;
        setOrientation(null);
        this.blueprint = build(plan);
        boundingBox = blueprint.bounds();
    }

    public ReliquaryPiece(CompoundTag tag) {
        super(ReliquaryStructures.TILTED_RELIQUARY_PIECE.get(), tag);
        this.origin = BlockPos.of(tag.getLong("Origin"));
        this.seed = tag.getLong("Seed");
        this.kind = Kind.values()[tag.getInt("Kind")];
        this.index = tag.getInt("Index");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putLong("Seed", seed);
        tag.putInt("Kind", kind.ordinal());
        tag.putInt("Index", index);
    }

    public Kind kind() {
        return kind;
    }

    public synchronized ReliquaryBlueprint blueprint() {
        if (blueprint == null) {
            CachedPlan cached = lastPlan;
            if (cached == null || cached.seed() != seed || !cached.origin().equals(origin)) {
                cached = new CachedPlan(seed, origin, ReliquaryLayout.plan(seed, origin));
                lastPlan = cached;
            }
            blueprint = build(cached.plan());
        }
        return blueprint;
    }

    private ReliquaryBlueprint build(ReliquaryLayout.Plan plan) {
        long pieceSeed = seed * 31L + kind.ordinal() * 1000L + index;
        return switch (kind) {
            case CUBE -> ReliquaryBlueprint.cube(plan, index, pieceSeed);
            case LINK -> ReliquaryBlueprint.link(plan, index, pieceSeed);
            case DEBRIS -> ReliquaryBlueprint.debris(plan, index, pieceSeed);
            case APPROACH -> ReliquaryBlueprint.approach(plan, pieceSeed);
            case TRAIL -> ReliquaryBlueprint.trail(plan, index, pieceSeed);
            case FRAGMENT -> ReliquaryBlueprint.fragments(plan, index, pieceSeed);
        };
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        blueprint().place(level, box, random);
    }
}
