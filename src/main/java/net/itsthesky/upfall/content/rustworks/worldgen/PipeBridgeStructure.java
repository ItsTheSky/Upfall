package net.itsthesky.upfall.content.rustworks.worldgen;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.HashCommon;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.engine.EngineStructures;
import net.itsthesky.upfall.content.engine.worldgen.StalledEngineStructure;
import net.itsthesky.upfall.worldgen.SurfaceSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PipeBridgeStructure extends Structure {
    public static final MapCodec<PipeBridgeStructure> CODEC = simpleCodec(PipeBridgeStructure::new);

    private static final int ATTEMPTS = 3;
    private static final int BIOME_PROBE_Y = 128;
    // Structure starts only reference chunks up to 8 chunks away from their own chunk.
    private static final int REACH = 8 * 16;
    private static final int CONFLICT_MARGIN = 6;
    private static final int ENGINE_CLEARANCE = 44;
    private static final Map<Key, Optional<PipeBridge>> PLANS = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Optional<PipeBridge>> eldest) {
            return size() > 4096;
        }
    };

    private record Key(long seed, long chunk) {}

    public PipeBridgeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<PipeBridge> plan = plan(context, context.chunkPos());
        if (plan.isEmpty() || overlapsStrongerNeighbour(context, plan.get())) return Optional.empty();
        PipeBridge bridge = plan.get();
        return Optional.of(new GenerationStub(bridge.at(bridge.a(), 0, bridge.ha()), builder -> PipeBridgePiece.addAll(builder, bridge)));
    }

    private static Optional<PipeBridge> plan(GenerationContext context, ChunkPos chunk) {
        Key key = new Key(context.seed(), McChunks.key(chunk));
        synchronized (PLANS) {
            Optional<PipeBridge> cached = PLANS.get(key);
            if (cached != null) return cached;
        }
        Optional<PipeBridge> plan = compute(context, chunk);
        synchronized (PLANS) {
            PLANS.put(key, plan);
        }
        return plan;
    }

    private static Optional<PipeBridge> compute(GenerationContext context, ChunkPos chunk) {
        if (!validBiome(context, chunk.getMiddleBlockX(), BIOME_PROBE_Y, chunk.getMiddleBlockZ())) return Optional.empty();
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(context.seed(), McChunks.x(chunk), McChunks.z(chunk));
        PipeBridge.Heights heights = new SurfaceSampler(context.chunkGenerator(), context.heightAccessor(), context.randomState())::height;
        PipeBridge.Biomes biomes = (x, y, z) -> validBiome(context, x, y, z);
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = chunk.getBlockX(random.nextInt(16));
            int z = chunk.getBlockZ(random.nextInt(16));
            Optional<PipeBridge> bridge = PipeBridge.search(random, x, z, heights, biomes);
            if (bridge.isPresent() && withinReach(chunk, bridge.get().bounds()) && !nearEngine(context, bridge.get().bounds())) {
                return bridge;
            }
        }
        return Optional.empty();
    }

    private static boolean validBiome(GenerationContext context, int x, int y, int z) {
        return context.validBiome().test(context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z),
                context.randomState().sampler()));
    }

    private static boolean withinReach(ChunkPos chunk, BoundingBox box) {
        return box.minX() >= chunk.getMinBlockX() - REACH && box.maxX() <= chunk.getMaxBlockX() + REACH
                && box.minZ() >= chunk.getMinBlockZ() - REACH && box.maxZ() <= chunk.getMaxBlockZ() + REACH;
    }

    private static boolean nearEngine(GenerationContext context, BoundingBox box) {
        RandomSpreadStructurePlacement engine = placement(context, EngineStructures.STALLED_ENGINE_SET);
        if (engine == null) return false;
        int spacing = engine.spacing();
        int regionX = Math.floorDiv(box.getCenter().getX() >> 4, spacing);
        int regionZ = Math.floorDiv(box.getCenter().getZ() >> 4, spacing);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos candidate = engine.getPotentialStructureChunk(context.seed(), (regionX + dx) * spacing, (regionZ + dz) * spacing);
                int reach = ENGINE_CLEARANCE + StalledEngineStructure.SITE_SEARCH;
                if (distanceSqr(box, candidate.getMiddleBlockX(), candidate.getMiddleBlockZ()) >= reach * reach) continue;
                Optional<BlockPos> site = StalledEngineStructure.site(context, candidate);
                if (site.isPresent() && distanceSqr(box, site.get().getX(), site.get().getZ()) < ENGINE_CLEARANCE * ENGINE_CLEARANCE) return true;
            }
        }
        return false;
    }

    private static int distanceSqr(BoundingBox box, int x, int z) {
        int ox = Math.max(0, Math.max(box.minX() - x, x - box.maxX()));
        int oz = Math.max(0, Math.max(box.minZ() - z, z - box.maxZ()));
        return ox * ox + oz * oz;
    }

    private static boolean overlapsStrongerNeighbour(GenerationContext context, PipeBridge bridge) {
        RandomSpreadStructurePlacement own = placement(context, RustworksStructures.PIPE_BRIDGE_SET);
        if (own == null) return false;
        ChunkPos self = context.chunkPos();
        BoundingBox mine = bridge.bounds().inflatedBy(CONFLICT_MARGIN);
        int spacing = own.spacing();
        int range = Mth.positiveCeilDiv(2 * REACH + 16 + CONFLICT_MARGIN, spacing * 16) + 1;
        int regionX = Math.floorDiv(McChunks.x(self), spacing);
        int regionZ = Math.floorDiv(McChunks.z(self), spacing);
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                ChunkPos other = own.getPotentialStructureChunk(context.seed(), (regionX + dx) * spacing, (regionZ + dz) * spacing);
                if (other.equals(self) || !stronger(context.seed(), other, self)) continue;
                if (other.getMinBlockX() - REACH > mine.maxX() || other.getMaxBlockX() + REACH < mine.minX()
                        || other.getMinBlockZ() - REACH > mine.maxZ() || other.getMaxBlockZ() + REACH < mine.minZ()) continue;
                Optional<PipeBridge> plan = plan(context, other);
                if (plan.isPresent() && plan.get().bounds().intersects(mine)) return true;
            }
        }
        return false;
    }

    private static boolean stronger(long seed, ChunkPos a, ChunkPos b) {
        long ha = HashCommon.mix(seed ^ McChunks.key(a));
        long hb = HashCommon.mix(seed ^ McChunks.key(b));
        return ha != hb ? ha > hb : McChunks.key(a) > McChunks.key(b);
    }

    private static RandomSpreadStructurePlacement placement(GenerationContext context, ResourceKey<StructureSet> key) {
        return context.registryAccess().lookup(Registries.STRUCTURE_SET)
                .flatMap(registry -> registry.get(key))
                .map(Holder::value)
                .map(StructureSet::placement)
                .filter(RandomSpreadStructurePlacement.class::isInstance)
                .map(RandomSpreadStructurePlacement.class::cast)
                .orElse(null);
    }

    @Override
    public StructureType<?> type() {
        return RustworksStructures.TYPE.get();
    }
}
