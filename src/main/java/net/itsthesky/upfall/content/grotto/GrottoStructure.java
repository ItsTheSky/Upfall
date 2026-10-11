package net.itsthesky.upfall.content.grotto;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class GrottoStructure extends Structure {
    public static final MapCodec<GrottoStructure> CODEC = simpleCodec(GrottoStructure::new);
    private static final int[][] RIM_SAMPLES = {{6, 0}, {-6, 0}, {0, 6}, {0, -6}};

    public GrottoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        RandomSource random = context.random();
        int x = chunk.getBlockX(4 + random.nextInt(8));
        int z = chunk.getBlockZ(4 + random.nextInt(8));
        int top = cloudTop(context, x, z);
        if (top < 0) return Optional.empty();
        for (int[] offset : RIM_SAMPLES) {
            int other = cloudTop(context, x + offset[0], z + offset[1]);
            if (other < 0 || Math.abs(other - top) > 6) return Optional.empty();
        }
        long seed = random.nextLong();
        return Optional.of(new GenerationStub(new BlockPos(x, GrottoLayout.CENTER_Y, z),
                builder -> builder.addPiece(GrottoPiece.create(seed, x, z, top))));
    }

    private static int cloudTop(GenerationContext context, int x, int z) {
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        for (int y = context.heightAccessor().getMaxBuildHeight() - 1; y >= UpfallTerrain.CLOUD_BAND_MIN; y--) {
            if (!column.getBlock(y).isAir()) {
                return y >= 50 && y <= UpfallTerrain.CLOUD_BAND_MAX + 2 ? y : -1;
            }
        }
        return -1;
    }

    @Override
    public StructureType<?> type() {
        return GrottoStructures.TYPE.get();
    }
}
