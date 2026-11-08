package net.itsthesky.upfall.gametest;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.cave.GasCaveNetwork;
import net.itsthesky.upfall.content.quantum.cave.GasCaveNetworks;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.EnumMap;
import java.util.Map;

public final class GasCaveGameTests {
    private GasCaveGameTests() {}

    static RandomState upfallNoise(GameTestHelper helper, long seed) {
        var registries = helper.getLevel().registryAccess();
        var settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(UpfallTerrain.SETTINGS);
        return RandomState.create(settings.value(), registries.lookupOrThrow(Registries.NOISE), seed);
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 2400)
    public static void gasCavesStayInsideIslands(GameTestHelper helper) {
        RandomState state = upfallNoise(helper, 20261012L);
        java.util.function.ToDoubleFunction<BlockPos> terrain = GasCaveNetworks.interpolated(state.router().finalDensity());
        int networks = 0;
        long cells = 0;
        long islandBlocks = 0;
        int rooms = 0;
        double accessible = 0.0;
        int pockets = 0;
        Map<GasCaveNetwork.Entrance, Integer> entrances = new EnumMap<>(GasCaveNetwork.Entrance.class);
        long start = System.nanoTime();
        for (int cx = -40; cx < 40; cx++) {
            for (int cz = -40; cz < 40; cz++) {
                GasCaveNetwork network = GasCaveNetworks.compute(state, new ChunkPos(cx, cz));
                if (network.isEmpty()) continue;
                networks++;
                GasCaveNetwork.Stats stats = network.stats();
                cells += stats.carved();
                islandBlocks += stats.islandBlocks();
                rooms += stats.rooms();
                accessible += stats.accessible();
                pockets += stats.closedPockets();
                stats.openByEntrance().keySet().forEach(type -> entrances.merge(type, 1, Integer::sum));
                helper.assertTrue(stats.openBlocks() > 0, "Gas cave network without an opening in chunk " + cx + ", " + cz);
                helper.assertTrue(stats.breaches() == 0, "Gas cave room breaches its island in chunk " + cx + ", " + cz);
                helper.assertTrue(stats.accessible() >= 0.5, "Gas cave network mostly sealed in chunk " + cx + ", " + cz + ": " + stats);
                helper.assertTrue(stats.closedPockets() <= 3, "Too many sealed gas pockets in chunk " + cx + ", " + cz + ": " + stats);
                for (LongIterator it = network.gasBlocks().iterator(); it.hasNext(); ) {
                    long key = it.nextLong();
                    if (network.entranceBlocks().contains(key)) continue;
                    BlockPos pos = BlockPos.of(key);
                    for (Direction dir : Direction.values()) {
                        BlockPos next = pos.relative(dir);
                        helper.assertTrue(network.isGas(next) || terrain.applyAsDouble(next) > 0.0, "Gas cave pierces its island near " + pos);
                    }
                }
            }
        }
        Upfall.LOGGER.info("[test] gasCaves: {} networks in 6400 chunks in {} ms, {} gas blocks ({}% of island rock), {} rooms, {}% accessible, {} sealed pockets, entrances {}",
                networks, (System.nanoTime() - start) / 1_000_000L, cells, islandBlocks == 0 ? 0 : cells * 100 / islandBlocks, rooms,
                networks == 0 ? 0 : Math.round(accessible * 100.0 / networks), pockets, entrances);
        helper.assertTrue(networks >= 10, "Too few gas cave networks: " + networks);
        helper.assertTrue(entrances.size() == GasCaveNetwork.Entrance.values().length, "Entrance types missing: " + entrances);
        helper.succeed();
    }
}
