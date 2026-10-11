package net.itsthesky.upfall.content.chronospire.worldgen;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class ChronospireStructure extends Structure {
    public static final MapCodec<ChronospireStructure> CODEC = simpleCodec(ChronospireStructure::new);

    static final int MIN_BASE = 100;
    static final int MAX_BASE = 148;
    static final int PREFERRED_BASE = 124;
    static final int MAX_SOLID = 10;

    public ChronospireStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        NoiseColumn[] columns = new NoiseColumn[13];
        int[][] offsets = new int[13][2];
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i;
            offsets[1 + i] = new int[]{(int) Math.round(9 * Math.cos(a)), (int) Math.round(9 * Math.sin(a))};
            offsets[7 + i] = new int[]{(int) Math.round(19 * Math.cos(a + 0.5)), (int) Math.round(19 * Math.sin(a + 0.5))};
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = context.chunkGenerator().getBaseColumn(x + offsets[i][0], z + offsets[i][1], context.heightAccessor(), context.randomState());
        }
        int bestBase = -1;
        int bestCount = Integer.MAX_VALUE;
        for (int base = MIN_BASE; base <= MAX_BASE; base += 4) {
            int count = 0;
            for (int i = 0; i < columns.length && count < bestCount; i++) {
                boolean inner = i < 7;
                int from = inner ? base - 20 : base + 20;
                int to = inner ? base + 90 : base + 70;
                for (int y = from; y <= to; y += 3) {
                    if (!columns[i].getBlock(y).isAir()) count++;
                }
            }
            if (count < bestCount || count == bestCount && Math.abs(base - PREFERRED_BASE) < Math.abs(bestBase - PREFERRED_BASE)) {
                bestCount = count;
                bestBase = base;
            }
        }
        if (bestCount > MAX_SOLID) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, bestBase, z);
        int rotation = context.random().nextInt(4);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new ChronospirePiece(origin, rotation, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ChronoRegistries.CHRONOSPIRE.get();
    }
}
