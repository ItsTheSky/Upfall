package net.itsthesky.upfall.content.reliquary.worldgen;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
import net.itsthesky.upfall.worldgen.ReliquaryZones;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class TiltedReliquaryStructure extends Structure {
    public static final MapCodec<TiltedReliquaryStructure> CODEC = simpleCodec(TiltedReliquaryStructure::new);

    static final int MIN_BASE = 170;
    static final int MAX_BASE = 200;

    public TiltedReliquaryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        ChunkPos center = ReliquaryZones.of(context.randomState()).center(ReliquaryZones.cellOfChunk(McChunks.x(chunk)), ReliquaryZones.cellOfChunk(McChunks.z(chunk)));
        if (center == null || !center.equals(chunk)) {
            return Optional.empty();
        }
        int base = MIN_BASE + context.random().nextInt(MAX_BASE - MIN_BASE + 1);
        long seed = context.random().nextLong();
        BlockPos origin = new BlockPos(chunk.getMiddleBlockX(), base, chunk.getMiddleBlockZ());
        return Optional.of(new GenerationStub(origin, builder -> addPieces(builder, origin, seed)));
    }

    public static void addPieces(StructurePiecesBuilder builder, BlockPos origin, long seed) {
        ReliquaryLayout.Plan plan = ReliquaryLayout.plan(seed, origin);
        for (int i = 0; i < plan.cubes().size(); i++) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.CUBE, i));
        }
        for (int i = 0; i < plan.links().size(); i++) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.LINK, i));
        }
        for (int i = 0; i < plan.debris().size(); i++) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.DEBRIS, i));
        }
        for (int i = 0; i < plan.fragments().size(); i++) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.FRAGMENT, i));
        }
        if (plan.approach() != null) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.APPROACH, 0));
        }
        for (int i = 0; i < plan.trails().size(); i++) {
            builder.addPiece(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.TRAIL, i));
        }
    }

    @Override
    public StructureType<?> type() {
        return ReliquaryStructures.TILTED_RELIQUARY_TYPE.get();
    }
}
