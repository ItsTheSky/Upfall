package net.itsthesky.upfall.content.engine.worldgen;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.chronospire.data.ChronospireData;
import net.itsthesky.upfall.content.engine.EngineStructures;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.SurfaceSampler;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StalledEngineStructure extends Structure {
    public static final MapCodec<StalledEngineStructure> CODEC = simpleCodec(StalledEngineStructure::new);

    static final int MIN_SURFACE = UpfallTerrain.CLOUD_BAND_MAX + 16;
    static final int INNER_RADIUS = 11;
    static final int OUTER_RADIUS = 18;
    static final int INNER_TOLERANCE = 5;
    static final int OUTER_BELOW = 12;
    static final int OUTER_ABOVE = 10;
    static final int EDGE_START = 16;
    static final int EDGE_MAX = 60;
    static final int EDGE_STEP = 4;
    static final int SPIRE_PROBE_Y = 124;
    public static final int SITE_SEARCH = 48;
    private static final int SITE_STEP = 16;
    private static final int[][] OFFSETS = offsets();
    private static final Map<Key, Optional<BlockPos>> SITES = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Optional<BlockPos>> eldest) {
            return size() > 1024;
        }
    };

    private record Key(long seed, long chunk) {}

    public StalledEngineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<BlockPos> site = site(context, context.chunkPos());
        if (site.isEmpty()) return Optional.empty();
        int x = site.get().getX();
        int z = site.get().getZ();
        int floor = site.get().getY();

        SurfaceSampler sampler = new SurfaceSampler(context.chunkGenerator(), context.heightAccessor(), context.randomState());
        int outflowDir = -1;
        int outflowLength = 0;
        for (int distance = EDGE_START; distance <= EDGE_MAX && outflowDir < 0; distance += EDGE_STEP) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (surface(sampler, x + dir.getStepX() * distance, z + dir.getStepZ() * distance) < 0) {
                    outflowDir = dir.get2DDataValue();
                    outflowLength = distance;
                    break;
                }
            }
        }

        float spire = spireAngle(context, x, z);
        BlockPos origin = new BlockPos(x, floor, z);
        int rotation = context.random().nextInt(4);
        long seed = context.random().nextLong();
        int dir = outflowDir;
        int length = outflowLength;
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(
                new StalledEnginePiece(new StalledEngineLayout(origin, rotation, seed, dir, length, spire)))));
    }

    public static Optional<BlockPos> site(GenerationContext context, ChunkPos chunk) {
        Key key = new Key(context.seed(), McChunks.key(chunk));
        synchronized (SITES) {
            Optional<BlockPos> cached = SITES.get(key);
            if (cached != null) return cached;
        }
        Optional<BlockPos> site = Optional.empty();
        SurfaceSampler sampler = new SurfaceSampler(context.chunkGenerator(), context.heightAccessor(), context.randomState());
        for (int[] offset : OFFSETS) {
            site = fit(context, sampler, chunk.getMiddleBlockX() + offset[0], chunk.getMiddleBlockZ() + offset[1]);
            if (site.isPresent()) break;
        }
        synchronized (SITES) {
            SITES.put(key, site);
        }
        return site;
    }

    private static Optional<BlockPos> fit(GenerationContext context, SurfaceSampler sampler, int x, int z) {
        int[] inner = new int[9];
        if (!context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(MIN_SURFACE + 60), QuartPos.fromBlock(z),
                context.randomState().sampler()).is(ModBiomes.RUSTWORKS)) {
            return Optional.empty();
        }
        inner[0] = surface(sampler, x, z);
        if (inner[0] < 0) return Optional.empty();
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 4 * i;
            inner[i + 1] = surface(sampler, x + (int) Math.round(INNER_RADIUS * Math.cos(a)), z + (int) Math.round(INNER_RADIUS * Math.sin(a)));
            if (inner[i + 1] < 0) return Optional.empty();
        }
        int[] sorted = inner.clone();
        Arrays.sort(sorted);
        int floor = sorted[sorted.length / 2];
        for (int y : inner) {
            if (Math.abs(y - floor) > INNER_TOLERANCE) return Optional.empty();
        }
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 4 * i + Math.PI / 8;
            int y = surface(sampler, x + (int) Math.round(OUTER_RADIUS * Math.cos(a)), z + (int) Math.round(OUTER_RADIUS * Math.sin(a)));
            if (y < 0 || y < floor - OUTER_BELOW || y > floor + OUTER_ABOVE) return Optional.empty();
        }
        return Optional.of(new BlockPos(x, floor, z));
    }

    static int surface(SurfaceSampler sampler, int x, int z) {
        int y = sampler.height(x, z) - 1;
        return y >= MIN_SURFACE ? y : -1;
    }

    static float spireAngle(GenerationContext context, int x, int z) {
        Optional<StructureSet> set = context.registryAccess().lookup(Registries.STRUCTURE_SET)
                .flatMap(registry -> registry.get(ChronospireData.STRUCTURE_SET)).map(Holder::value);
        if (set.isEmpty() || !(set.get().placement() instanceof RandomSpreadStructurePlacement spread)) {
            return Float.NaN;
        }
        int spacing = spread.spacing();
        int regionX = Math.floorDiv(x >> 4, spacing);
        int regionZ = Math.floorDiv(z >> 4, spacing);
        double best = Double.MAX_VALUE;
        float angle = Float.NaN;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                ChunkPos candidate = spread.getPotentialStructureChunk(context.seed(), (regionX + dx) * spacing, (regionZ + dz) * spacing);
                int cx = candidate.getMiddleBlockX();
                int cz = candidate.getMiddleBlockZ();
                double distance = Math.hypot(cx - x, cz - z);
                if (distance >= best) continue;
                if (!context.biomeSource().getNoiseBiome(QuartPos.fromBlock(cx), QuartPos.fromBlock(SPIRE_PROBE_Y), QuartPos.fromBlock(cz),
                        context.randomState().sampler()).is(ChronospireData.HAS_CHRONOSPIRE)) continue;
                best = distance;
                angle = (float) Math.atan2(cz - z, cx - x);
            }
        }
        return angle;
    }

    private static int[][] offsets() {
        List<int[]> offsets = new ArrayList<>();
        for (int dx = -SITE_SEARCH; dx <= SITE_SEARCH; dx += SITE_STEP) {
            for (int dz = -SITE_SEARCH; dz <= SITE_SEARCH; dz += SITE_STEP) offsets.add(new int[]{dx, dz});
        }
        offsets.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
        return offsets.toArray(int[][]::new);
    }

    @Override
    public StructureType<?> type() {
        return EngineStructures.TYPE.get();
    }
}
