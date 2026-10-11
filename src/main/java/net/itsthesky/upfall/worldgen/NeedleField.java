package net.itsthesky.upfall.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record NeedleField(NoiseHolder jitter, NoiseHolder shape, NoiseHolder presence, double maxHeight) implements DensityFunction.SimpleFunction {
    public static final MapCodec<NeedleField> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NoiseHolder.CODEC.fieldOf("jitter").forGetter(NeedleField::jitter),
            NoiseHolder.CODEC.fieldOf("shape").forGetter(NeedleField::shape),
            NoiseHolder.CODEC.fieldOf("presence").forGetter(NeedleField::presence),
            Codec.DOUBLE.fieldOf("max_height").forGetter(NeedleField::maxHeight)
    ).apply(instance, NeedleField::new));
    public static final KeyDispatchDataCodec<NeedleField> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    private static final int CELL = 40;
    private static final double JITTER = 0.32;
    private static final double CELL_SAMPLE = 5.3;

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        double best = 0.0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                best = Math.max(best, cone(cellX + dx, cellZ + dz, x, z));
            }
        }
        return best;
    }

    private double cone(int cellX, int cellZ, int x, int z) {
        double sx = cellX * CELL_SAMPLE;
        double sz = cellZ * CELL_SAMPLE;
        if (shape.getValue(sx, 71.0, sz) < -0.25) return 0.0;
        double px = (cellX + 0.5 + JITTER * unit(jitter.getValue(sx, 0.0, sz))) * CELL;
        double pz = (cellZ + 0.5 + JITTER * unit(jitter.getValue(sx, 37.0, sz))) * CELL;
        double height = Mth.clamp((presence.getValue(px, 0.0, pz) - 0.48) / 0.18, 0.0, 1.0) * maxHeight;
        if (height <= 0.0) return 0.0;
        double radius = 5.0 + 1.5 * unit(shape.getValue(sx, 0.0, sz));
        double distance = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
        return height * (1.0 - distance / radius);
    }

    private static double unit(double value) {
        return Mth.clamp(value, -1.0, 1.0);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new NeedleField(visitor.visitNoise(jitter), visitor.visitNoise(shape), visitor.visitNoise(presence), maxHeight));
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return maxHeight;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
