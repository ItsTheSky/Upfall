package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RustworksWorldgenGameTests {
    private RustworksWorldgenGameTests() {}

    private static final int STEP = 32;
    private static final int HALF = 10240;
    private static final int FRAGMENT = 128;

    private record Climate2D(BiomeSource source, Climate.Sampler sampler) {
        ResourceKey<Biome> biome(int x, int z) {
            Holder<Biome> biome = source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(128), QuartPos.fromBlock(z), sampler);
            return biome.unwrapKey().orElseThrow();
        }
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 100)
    public static void rustworksIsRareButFindable(GameTestHelper helper) {
        for (long seed : new long[]{1L, 42L, 20261011L}) {
            boolean[][] rust = sample(climate(helper, seed));
            int n = rust.length;
            int count = 0;
            for (boolean[] row : rust) for (boolean b : row) if (b) count++;
            int[] all = Arrays.stream(distances(rust)).flatMapToInt(Arrays::stream).sorted().toArray();
            double share = count / (double) (n * n);
            int median = all[all.length / 2] * STEP;
            int p95 = all[(int) (all.length * 0.95)] * STEP;
            int max = all[all.length - 1] * STEP;
            int[] all8 = zoneDiameters(rust);
            int[] sizes = Arrays.stream(all8).filter(d -> d >= FRAGMENT).toArray();
            int fragments = all8.length - sizes.length;
            int spacing = sizes.length == 0 ? Integer.MAX_VALUE : (int) Math.sqrt((double) n * n * STEP * STEP / sizes.length);
            Upfall.LOGGER.info("Rustworks (seed {}): {}% of the map, distance to nearest median {} / p95 {} / max {} blocks, {} zones (spacing ~{}), diameters {}, {} fragments",
                    seed, String.format("%.2f", share * 100), median, p95, max, sizes.length, spacing, Arrays.toString(sizes), fragments);
            helper.assertTrue(share > 0.02 && share < 0.1, "Rustworks share out of range: " + share + " (seed " + seed + ")");
            helper.assertTrue(p95 <= 3500, "Rustworks too hard to find: p95 distance " + p95 + " (seed " + seed + ")");
            helper.assertTrue(sizes.length > 0 && sizes[0] >= 450, "Rustworks zone too small: " + Arrays.toString(sizes) + " (seed " + seed + ")");
            helper.assertTrue(fragments * 10 <= sizes.length, "Too many Rustworks fragments: " + fragments + " (seed " + seed + ")");
            helper.assertTrue(spacing >= 2000 && spacing <= 4500, "Rustworks zone spacing out of range: " + spacing + " (seed " + seed + ")");
        }
        helper.succeed();
    }

    private static int[] zoneDiameters(boolean[][] rust) {
        int n = rust.length;
        boolean[][] seen = new boolean[n][n];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!rust[i][j] || seen[i][j]) continue;
                int area = 0;
                boolean border = false;
                seen[i][j] = true;
                queue.add(new int[]{i, j});
                while (!queue.isEmpty()) {
                    int[] cell = queue.poll();
                    area++;
                    if (cell[0] == 0 || cell[1] == 0 || cell[0] == n - 1 || cell[1] == n - 1) border = true;
                    for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
                        int a = cell[0] + d[0];
                        int b = cell[1] + d[1];
                        if (a < 0 || b < 0 || a >= n || b >= n || seen[a][b] || !rust[a][b]) continue;
                        seen[a][b] = true;
                        queue.add(new int[]{a, b});
                    }
                }
                if (!border) sizes.add((int) (2.0 * Math.sqrt(area / Math.PI) * STEP));
            }
        }
        return sizes.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    private static Climate2D climate(GameTestHelper helper, long seed) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        NoiseGeneratorSettings settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(UpfallTerrain.SETTINGS).value();
        RandomState state = RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), seed);
        return new Climate2D(ModBiomes.biomeSource(registries.lookupOrThrow(Registries.BIOME)), state.sampler());
    }

    private static boolean[][] sample(Climate2D climate) {
        int n = 2 * HALF / STEP;
        boolean[][] rust = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rust[i][j] = climate.biome(-HALF + i * STEP, -HALF + j * STEP) == ModBiomes.RUSTWORKS;
            }
        }
        return rust;
    }

    private static int[][] distances(boolean[][] rust) {
        int n = rust.length;
        int[][] dist = new int[n][n];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = rust[i][j] ? 0 : Integer.MAX_VALUE;
                if (rust[i][j]) queue.add(new int[]{i, j});
            }
        }
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    int i = cell[0] + di;
                    int j = cell[1] + dj;
                    if (i < 0 || j < 0 || i >= n || j >= n || dist[i][j] != Integer.MAX_VALUE) continue;
                    dist[i][j] = dist[cell[0]][cell[1]] + 1;
                    queue.add(new int[]{i, j});
                }
            }
        }
        return dist;
    }
}
