package net.itsthesky.upfall.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record RustworksZones(NoiseHolder shape, NoiseHolder archipelago, double mistThreshold) implements DensityFunction.SimpleFunction {
    public static final MapCodec<RustworksZones> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NoiseHolder.CODEC.fieldOf("shape").forGetter(RustworksZones::shape),
            NoiseHolder.CODEC.fieldOf("archipelago").forGetter(RustworksZones::archipelago),
            Codec.DOUBLE.fieldOf("mist_threshold").forGetter(RustworksZones::mistThreshold)
    ).apply(instance, RustworksZones::new));
    public static final KeyDispatchDataCodec<RustworksZones> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public static final double LIMIT = 64.0;
    private static final int CELL = 2048;
    private static final double JITTER = 0.25;
    private static final double PRESENCE = 0.6;
    private static final double MIN_RADIUS = 320.0;
    private static final double MAX_RADIUS = 720.0;
    private static final double WARP = 0.15;
    private static final double MIST_MARGIN = 0.04;
    private static final double REACH = MAX_RADIUS * (1.0 + WARP) + LIMIT;

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        double best = -LIMIT;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                best = Math.max(best, zone(cellX + dx, cellZ + dz, x, z));
            }
        }
        return best;
    }

    private double zone(int cellX, int cellZ, int x, int z) {
        double px = (cellX + 0.5 + JITTER * (2.0 * hash(cellX, cellZ, 1) - 1.0)) * CELL;
        double pz = (cellZ + 0.5 + JITTER * (2.0 * hash(cellX, cellZ, 2) - 1.0)) * CELL;
        double distance = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
        if (distance > REACH || hash(cellX, cellZ, 3) >= PRESENCE) return -LIMIT;
        double radius = Mth.lerp(hash(cellX, cellZ, 4), MIN_RADIUS, MAX_RADIUS);
        if (!clearOfMistsea(px, pz, radius)) return -LIMIT;
        double warp = Mth.clamp(shape.getValue(x, 0.0, z), -1.0, 1.0);
        return Mth.clamp(radius * (1.0 + WARP * warp) - distance, -LIMIT, LIMIT);
    }

    private boolean clearOfMistsea(double px, double pz, double radius) {
        if (archipelago.getValue(px, 0.0, pz) < mistThreshold + 2.0 * MIST_MARGIN) return false;
        int misty = 0;
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6.0;
            if (archipelago.getValue(px + Math.cos(angle) * radius, 0.0, pz + Math.sin(angle) * radius) < mistThreshold + MIST_MARGIN) misty++;
        }
        return misty <= 2;
    }

    private double hash(int cellX, int cellZ, int salt) {
        double value = shape.getValue(cellX * 811.37 + salt * 97.13, 4096.0 + salt * 331.0, cellZ * 811.37 - salt * 53.71);
        return Mth.frac(value * 7919.0);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new RustworksZones(visitor.visitNoise(shape), visitor.visitNoise(archipelago), mistThreshold));
    }

    @Override
    public double minValue() {
        return -LIMIT;
    }

    @Override
    public double maxValue() {
        return LIMIT;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
