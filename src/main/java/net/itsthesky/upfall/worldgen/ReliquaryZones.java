package net.itsthesky.upfall.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.compat.mc.McIds;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReliquaryZones implements DensityFunction.SimpleFunction {
    public static final MapCodec<ReliquaryZones> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NoiseHolder.CODEC.fieldOf("noise").forGetter(ReliquaryZones::noise),
            NoiseHolder.CODEC.fieldOf("rustworks").forGetter(ReliquaryZones::rustworks),
            NoiseHolder.CODEC.fieldOf("archipelago").forGetter(ReliquaryZones::archipelago),
            Codec.DOUBLE.fieldOf("mist_threshold").forGetter(ReliquaryZones::mistThreshold)
    ).apply(instance, ReliquaryZones::new));
    public static final KeyDispatchDataCodec<ReliquaryZones> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public static final NormalNoise.NoiseParameters NOISE_PARAMETERS = new NormalNoise.NoiseParameters(-4, 1.0);
    public static final int CELL_CHUNKS = 160;
    public static final int CELL = CELL_CHUNKS * 16;
    private static final int JITTER_CHUNKS = 44;
    private static final double PRESENCE = 0.85;
    public static final double INNER_RADIUS = 260.0;
    public static final double OUTER_RADIUS = 340.0;
    private static final double RUST_PROBE = 220.0;

    private final NoiseHolder noise;
    private final NoiseHolder rustworks;
    private final NoiseHolder archipelago;
    private final double mistThreshold;
    private final Map<Long, Boolean> validCells = new ConcurrentHashMap<>();
    private volatile Long worldKey;

    public ReliquaryZones(NoiseHolder noise, NoiseHolder rustworks, NoiseHolder archipelago, double mistThreshold) {
        this.noise = noise;
        this.rustworks = rustworks;
        this.archipelago = archipelago;
        this.mistThreshold = mistThreshold;
    }

    public static ReliquaryZones of(RandomState state) {
        return new ReliquaryZones(wired(state, ModNoises.RELIQUARY), wired(state, ModNoises.RUSTWORKS), wired(state, ModNoises.ARCHIPELAGO),
                UpfallTerrain.BiomeThresholds.MISTSEA);
    }

    private static NoiseHolder wired(RandomState state, ResourceKey<NormalNoise.NoiseParameters> key) {
        NormalNoise instance = state.getOrCreateNoise(key);
        return new NoiseHolder(Holder.direct(instance.parameters()), instance);
    }

    public static long worldKey(long seed) {
        NormalNoise instance = NormalNoise.create(WorldgenRandom.Algorithm.XOROSHIRO.newInstance(seed).forkPositional()
                .fromHashOf(McIds.id(ModNoises.RELIQUARY)), NOISE_PARAMETERS);
        return worldKey(instance.getValue(0.5, 0.0, 0.5), instance.getValue(1031.25, 17.0, -2077.75));
    }

    private static long worldKey(double first, double second) {
        return mix(Double.doubleToLongBits(first) * 0x9E3779B97F4A7C15L ^ Double.doubleToLongBits(second));
    }

    private long key() {
        Long key = worldKey;
        if (key == null) {
            key = worldKey(noise.getValue(0.5, 0.0, 0.5), noise.getValue(1031.25, 17.0, -2077.75));
            worldKey = key;
        }
        return key;
    }

    private static long mix(long h) {
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    public static @Nullable ChunkPos nominalCenter(long key, int cellX, int cellZ) {
        long h = mix(key ^ mix(cellX * 0x632BE59BD9B4E019L + cellZ * 0x85157AF5L));
        if ((h >>> 40) / (double) (1L << 24) >= PRESENCE) return null;
        int span = JITTER_CHUNKS * 2 + 1;
        int jx = (int) Math.floorMod(h, (long) span) - JITTER_CHUNKS;
        int jz = (int) Math.floorMod(h >>> 20, (long) span) - JITTER_CHUNKS;
        return new ChunkPos(cellX * CELL_CHUNKS + CELL_CHUNKS / 2 + jx, cellZ * CELL_CHUNKS + CELL_CHUNKS / 2 + jz);
    }

    public static int cellOfChunk(int chunk) {
        return Math.floorDiv(chunk, CELL_CHUNKS);
    }

    public @Nullable ChunkPos center(int cellX, int cellZ) {
        ChunkPos center = nominalCenter(key(), cellX, cellZ);
        if (center == null) return null;
        boolean valid = validCells.computeIfAbsent(McChunks.key(cellX, cellZ), k -> clearOfRustworks(center));
        return valid ? center : null;
    }

    public @Nullable ChunkPos centerNear(int blockX, int blockZ) {
        return center(cellOfChunk(blockX >> 4), cellOfChunk(blockZ >> 4));
    }

    private boolean clearOfRustworks(ChunkPos center) {
        RustworksZones rust = new RustworksZones(rustworks, archipelago, mistThreshold);
        int x = center.getMiddleBlockX();
        int z = center.getMiddleBlockZ();
        if (rust.compute(new SinglePointContext(x, 0, z)) > -RustworksZones.LIMIT) return false;
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            for (double radius : new double[]{RUST_PROBE, OUTER_RADIUS}) {
                int px = x + (int) Math.round(Math.cos(angle) * radius);
                int pz = z + (int) Math.round(Math.sin(angle) * radius);
                if (rust.compute(new SinglePointContext(px, 0, pz)) > -RustworksZones.LIMIT) return false;
            }
        }
        return true;
    }

    public double clearing(int x, int z) {
        ChunkPos center = centerNear(x, z);
        if (center == null) return 0.0;
        double dx = x - center.getMiddleBlockX();
        double dz = z - center.getMiddleBlockZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance >= OUTER_RADIUS) return 0.0;
        double t = Mth.clamp((OUTER_RADIUS - distance) / (OUTER_RADIUS - INNER_RADIUS), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    @Override
    public double compute(FunctionContext context) {
        return clearing(context.blockX(), context.blockZ());
    }

    public NoiseHolder noise() {
        return noise;
    }

    public NoiseHolder rustworks() {
        return rustworks;
    }

    public NoiseHolder archipelago() {
        return archipelago;
    }

    public double mistThreshold() {
        return mistThreshold;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ReliquaryZones(visitor.visitNoise(noise), visitor.visitNoise(rustworks), visitor.visitNoise(archipelago), mistThreshold));
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
