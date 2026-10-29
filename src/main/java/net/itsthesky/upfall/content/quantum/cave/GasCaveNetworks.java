package net.itsthesky.upfall.content.quantum.cave;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

public final class GasCaveNetworks {
    private GasCaveNetworks() {}

    public static final int RANGE = 8;
    private static final int CACHE_SIZE = 2048;
    private static final Map<RandomState, Map<Long, CompletableFuture<GasCaveNetwork>>> CACHE = new WeakHashMap<>();

    public static GasCaveNetwork get(RandomState state, ChunkPos source) {
        CompletableFuture<GasCaveNetwork> future;
        boolean owner = false;
        synchronized (CACHE) {
            Map<Long, CompletableFuture<GasCaveNetwork>> cache = CACHE.computeIfAbsent(state, k -> new LinkedHashMap<>(256, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, CompletableFuture<GasCaveNetwork>> eldest) {
                    return size() > CACHE_SIZE;
                }
            });
            future = cache.get(McChunks.key(source));
            if (future == null) {
                future = new CompletableFuture<>();
                cache.put(McChunks.key(source), future);
                owner = true;
            }
        }
        if (owner) {
            try {
                future.complete(compute(state, source));
            } catch (RuntimeException e) {
                future.completeExceptionally(e);
                throw e;
            }
        }
        return future.join();
    }

    public static GasCaveNetwork compute(RandomState state, ChunkPos source) {
        if (!UpfallConfig.get(UpfallConfig.CAVES_ENABLED)) return GasCaveNetwork.EMPTY;
        RandomSource random = state.getOrCreateRandomFactory(Upfall.id("gas_caves")).at(McChunks.x(source), 0, McChunks.z(source));
        if (random.nextDouble() >= UpfallConfig.getDouble(UpfallConfig.CAVES_CHUNK_CHANCE)) return GasCaveNetwork.EMPTY;
        return GasCaveNetwork.build(source, random, interpolated(state.router().finalDensity()), settings());
    }

    private static final int CELL_WIDTH = 4;
    private static final int CELL_HEIGHT = 8;

    public static java.util.function.ToDoubleFunction<BlockPos> interpolated(DensityFunction density) {
        it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap corners = new it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap();
        return pos -> {
            int cx = Math.floorDiv(pos.getX(), CELL_WIDTH);
            int cy = Math.floorDiv(pos.getY(), CELL_HEIGHT);
            int cz = Math.floorDiv(pos.getZ(), CELL_WIDTH);
            double fx = (pos.getX() - cx * CELL_WIDTH) / (double) CELL_WIDTH;
            double fy = (pos.getY() - cy * CELL_HEIGHT) / (double) CELL_HEIGHT;
            double fz = (pos.getZ() - cz * CELL_WIDTH) / (double) CELL_WIDTH;
            double[] c = new double[8];
            for (int i = 0; i < 8; i++) {
                if ((i & 1) != 0 && fx == 0.0 || (i & 2) != 0 && fy == 0.0 || (i & 4) != 0 && fz == 0.0) {
                    c[i] = c[i & ~((fx == 0.0 ? 1 : 0) | (fy == 0.0 ? 2 : 0) | (fz == 0.0 ? 4 : 0))];
                    continue;
                }
                int x = (cx + (i & 1)) * CELL_WIDTH;
                int y = (cy + (i >> 1 & 1)) * CELL_HEIGHT;
                int z = (cz + (i >> 2 & 1)) * CELL_WIDTH;
                long key = BlockPos.asLong(x, y, z);
                if (corners.containsKey(key)) {
                    c[i] = corners.get(key);
                } else {
                    c[i] = density.compute(new DensityFunction.SinglePointContext(x, y, z));
                    corners.put(key, c[i]);
                }
            }
            return Mth.lerp3(fx, fy, fz, c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
        };
    }

    public static double density(DensityFunction density, BlockPos pos) {
        return density.compute(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static GasCaveNetwork.Settings settings() {
        return new GasCaveNetwork.Settings(
                UpfallConfig.getDouble(UpfallConfig.CAVES_DENSITY_MARGIN),
                UpfallConfig.getInt(UpfallConfig.CAVES_MIN_Y), 240,
                UpfallConfig.getInt(UpfallConfig.CAVES_MAX_LENGTH),
                UpfallConfig.getDouble(UpfallConfig.CAVES_BRANCH_CHANCE),
                UpfallConfig.getDouble(UpfallConfig.CAVES_ENTRANCE_CHANCE),
                UpfallConfig.getDouble(UpfallConfig.CAVES_DUG_CHANCE),
                UpfallConfig.getDouble(UpfallConfig.CAVES_DEAD_END_FEATURE_CHANCE),
                UpfallConfig.getDouble(UpfallConfig.CAVES_CLUSTER_CHANCE));
    }
}
