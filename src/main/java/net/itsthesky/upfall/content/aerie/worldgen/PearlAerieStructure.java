package net.itsthesky.upfall.content.aerie.worldgen;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.aerie.AerieStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PearlAerieStructure extends Structure {
    public static final MapCodec<PearlAerieStructure> CODEC = simpleCodec(PearlAerieStructure::new);

    static final int MIN_BASE = 124;
    static final int MAX_BASE = 160;
    static final int PREFERRED_BASE = 140;
    static final double MAX_SOLID_RATIO = 0.04;
    static final int MIN_TOWERS = 4;

    public PearlAerieStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        long seed = context.random().nextLong();
        BlockPos probe = new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        AerieLayout.Plan draft = AerieLayout.plan(seed, probe);
        if (draft.towers().size() < MIN_TOWERS) {
            return Optional.empty();
        }

        record Probe(NoiseColumn column, int from, int to) {}
        List<Probe> probes = new ArrayList<>();
        for (AerieLayout.Tower tower : draft.towers()) {
            int[][] offsets = {{0, 0}, {tower.radius(), 0}, {-tower.radius(), 0}, {0, tower.radius()}, {0, -tower.radius()}};
            for (int[] offset : offsets) {
                NoiseColumn column = context.chunkGenerator().getBaseColumn(tower.base().getX() + offset[0], tower.base().getZ() + offset[1],
                        context.heightAccessor(), context.randomState());
                probes.add(new Probe(column, tower.bottom(), tower.top()));
            }
        }

        int bestBase = -1;
        int bestSolid = Integer.MAX_VALUE;
        int samples = 0;
        for (int base = MIN_BASE; base <= MAX_BASE; base += 4) {
            int solid = 0;
            int total = 0;
            for (Probe sample : probes) {
                for (int y = sample.from() + base; y <= sample.to() + base; y += 3) {
                    total++;
                    if (!sample.column().getBlock(y).isAir()) solid++;
                }
            }
            if (solid < bestSolid || solid == bestSolid && Math.abs(base - PREFERRED_BASE) < Math.abs(bestBase - PREFERRED_BASE)) {
                bestSolid = solid;
                bestBase = base;
                samples = total;
            }
        }
        if (bestSolid > samples * MAX_SOLID_RATIO) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(probe.getX(), bestBase, probe.getZ());
        return Optional.of(new GenerationStub(origin, builder -> addPieces(builder, origin, seed)));
    }

    public static void addPieces(StructurePiecesBuilder builder, BlockPos origin, long seed) {
        AerieLayout.Plan plan = AerieLayout.plan(seed, origin);
        for (int i = 0; i < plan.towers().size(); i++) {
            builder.addPiece(new AeriePiece(origin, seed, false, i));
        }
        for (int i = 0; i < plan.bridges().size(); i++) {
            builder.addPiece(new AeriePiece(origin, seed, true, i));
        }
    }

    @Override
    public StructureType<?> type() {
        return AerieStructures.PEARL_AERIE_TYPE.get();
    }
}
