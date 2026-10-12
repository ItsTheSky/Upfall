package net.itsthesky.upfall.content.quantum.vault;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumStructures;
import net.itsthesky.upfall.content.quantum.cave.GasCaveNetworks;
import net.itsthesky.upfall.worldgen.SurfaceSampler;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ShiftingVaultStructure extends Structure {
    public static final MapCodec<ShiftingVaultStructure> CODEC = simpleCodec(ShiftingVaultStructure::new);

    private static final int SEARCH = 32;
    private static final int SEARCH_STEP = 32;
    private static final int COLUMN_STEP = 4;
    private static final int REACH = 7 * 16;

    public record Site(BlockPos origin, VaultTunnel.Path tunnel) {}

    private record Key(long seed, long chunk) {}

    private static final java.util.Map<Key, Optional<Site>> SITES = new java.util.LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<Key, Optional<Site>> eldest) {
            return size() > 256;
        }
    };

    public ShiftingVaultStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Key key = new Key(context.seed(), context.chunkPos().toLong());
        Optional<Site> cached;
        synchronized (SITES) {
            cached = SITES.get(key);
        }
        if (cached == null) {
            Terrain terrain = Terrain.of(context.chunkGenerator(), context.heightAccessor(), context.randomState());
            cached = Optional.ofNullable(find(terrain, context.chunkPos(), RandomSource.create(context.seed() ^ context.chunkPos().toLong()),
                    UpfallConfig.getInt(UpfallConfig.VAULT_MARGIN)));
            synchronized (SITES) {
                SITES.put(key, cached);
            }
        }
        if (cached.isEmpty()) return Optional.empty();
        Site site = cached.get();
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(site.origin(), builder -> builder.addPiece(new ShiftingVaultPiece(site.origin(), seed, site.tunnel()))));
    }

    public static @Nullable Site find(Terrain terrain, ChunkPos chunk, RandomSource random, int margin) {
        int minLength = UpfallConfig.getInt(UpfallConfig.VAULT_TUNNEL_MIN);
        int maxLength = Math.max(minLength + 1, UpfallConfig.getInt(UpfallConfig.VAULT_TUNNEL_MAX));
        BoundingBox reach = new BoundingBox(chunk.getMiddleBlockX() - REACH, 0, chunk.getMiddleBlockZ() - REACH,
                chunk.getMiddleBlockX() + REACH, 0, chunk.getMiddleBlockZ() + REACH);
        for (int dx = -SEARCH; dx <= SEARCH; dx += SEARCH_STEP) {
            for (int dz = -SEARCH; dz <= SEARCH; dz += SEARCH_STEP) {
                int x = chunk.getMiddleBlockX() + dx - ShiftingVaultLayout.SIZE_X / 2;
                int z = chunk.getMiddleBlockZ() + dz - ShiftingVaultLayout.SIZE_Z / 2;
                BlockPos origin = fit(terrain, x, z, margin);
                if (origin == null) continue;
                ShiftingVaultLayout layout = new ShiftingVaultLayout(origin, 0L);
                VaultTunnel.Path tunnel = VaultTunnel.dig(layout.door(), layout.bounds().inflatedBy(2), reach, minLength, maxLength,
                        random, terrain::density);
                if (tunnel != null) return new Site(origin, tunnel);
            }
        }
        return null;
    }

    static @Nullable BlockPos fit(Terrain terrain, int x, int z, int margin) {
        int x0 = x - margin;
        int z0 = z - margin;
        int x1 = x + ShiftingVaultLayout.SIZE_X - 1 + margin;
        int z1 = z + ShiftingVaultLayout.SIZE_Z - 1 + margin;
        int top = Integer.MAX_VALUE;
        for (int sx = x0; ; sx = Math.min(sx + 8, x1)) {
            for (int sz = z0; ; sz = Math.min(sz + 8, z1)) {
                top = Math.min(top, terrain.surface(sx, sz));
                if (top - margin * 2 - ShiftingVaultLayout.SIZE_Y - 3 < UpfallTerrain.CLOUD_BAND_MAX + 8) return null;
                if (sz == z1) break;
            }
            if (sx == x1) break;
        }
        int vaultTop = top - margin - 3;
        int vaultBottom = vaultTop - ShiftingVaultLayout.SIZE_Y + 1;
        int yMin = vaultBottom - margin;
        int yMax = vaultTop + margin;
        for (int sx = x0; ; sx = Math.min(sx + COLUMN_STEP, x1)) {
            if (!terrain.solid(sx, z0, yMin, yMax) || !terrain.solid(sx, z1, yMin, yMax)) return null;
            if (sx == x1) break;
        }
        for (int sz = z0; ; sz = Math.min(sz + COLUMN_STEP, z1)) {
            if (!terrain.solid(x0, sz, yMin, yMax) || !terrain.solid(x1, sz, yMin, yMax)) return null;
            if (sz == z1) break;
        }
        for (int sx = x0 + COLUMN_STEP; sx < x1; sx += COLUMN_STEP) {
            for (int sz = z0 + COLUMN_STEP; sz < z1; sz += COLUMN_STEP) {
                if (!terrain.solid(sx, sz, yMin, yMax)) return null;
            }
        }
        return new BlockPos(x, vaultBottom, z);
    }

    @Override
    public StructureType<?> type() {
        return QuantumStructures.SHIFTING_VAULT_TYPE.get();
    }

    public interface Terrain {
        int surface(int x, int z);

        boolean solid(int x, int z, int minY, int maxY);

        double density(BlockPos pos);

        static Terrain of(ChunkGenerator generator, LevelHeightAccessor height, RandomState state) {
            SurfaceSampler sampler = new SurfaceSampler(generator, height, state);
            java.util.function.ToDoubleFunction<BlockPos> density = GasCaveNetworks.interpolated(state.router().finalDensity());
            return new Terrain() {
                @Override
                public int surface(int x, int z) {
                    return sampler.height(x, z);
                }

                @Override
                public boolean solid(int x, int z, int minY, int maxY) {
                    NoiseColumn column = generator.getBaseColumn(x, z, height, state);
                    for (int y = minY; y <= maxY; y++) {
                        if (column.getBlock(y).isAir()) return false;
                    }
                    return true;
                }

                @Override
                public double density(BlockPos pos) {
                    return density.applyAsDouble(pos);
                }
            };
        }
    }
}
