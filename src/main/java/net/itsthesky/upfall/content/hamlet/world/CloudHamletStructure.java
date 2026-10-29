package net.itsthesky.upfall.content.hamlet.world;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.hamlet.HamletStructures;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ReliquaryZones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class CloudHamletStructure extends Structure {
    public static final MapCodec<CloudHamletStructure> CODEC = simpleCodec(CloudHamletStructure::new);
    private static final int[][] SAMPLES = {{0, 0}, {26, 0}, {-26, 0}, {0, 26}, {0, -26}, {18, 18}, {-18, 18}, {18, -18}, {-18, -18}};
    private static final int CLOUD_LIMIT = 63;
    private static final int SCAN_TOP = 130;

    public CloudHamletStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        WorldgenRandom random = context.random();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (ReliquaryZones.of(context.randomState()).clearing(x, z) > 0.0) {
            return Optional.empty();
        }
        int cloudTop = 50;
        for (int[] sample : SAMPLES) {
            NoiseColumn column = context.chunkGenerator().getBaseColumn(x + sample[0], z + sample[1], context.heightAccessor(), context.randomState());
            for (int y = SCAN_TOP; y > McLevel.minY(context.heightAccessor()); y--) {
                if (!column.getBlock(y).isAir()) {
                    if (y > CLOUD_LIMIT) {
                        return Optional.empty();
                    }
                    cloudTop = Math.max(cloudTop, y);
                    break;
                }
            }
        }
        boolean mistsea = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(cloudTop), QuartPos.fromBlock(z),
                context.randomState().sampler()).is(ModBiomes.MISTSEA);
        if (!mistsea && random.nextInt(4) != 0) {
            return Optional.empty();
        }
        BlockPos center = new BlockPos(x, cloudTop + 9 + random.nextInt(3), z);
        HamletLayout.Plan plan = HamletLayout.plan(random, center);
        return Optional.of(new GenerationStub(center, builder -> HamletPieces.addPieces(builder, plan, random)));
    }

    @Override
    public StructureType<?> type() {
        return HamletStructures.CLOUD_HAMLET_TYPE.get();
    }
}
