package net.itsthesky.upfall.gametest;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.cave.GasCaveNetwork;
import net.itsthesky.upfall.content.quantum.cave.GasCaveNetworks;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumMap;
import java.util.Map;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class GasCaveGameTests {
    private GasCaveGameTests() {}

    static RandomState upfallNoise(GameTestHelper helper, long seed) {
        var registries = helper.getLevel().registryAccess();
        var settings = registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(UpfallTerrain.SETTINGS);
        return RandomState.create(settings.value(), registries.lookupOrThrow(Registries.NOISE), seed);
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void gasCavesStayInsideIslands(GameTestHelper helper) {
        RandomState state = upfallNoise(helper, 20261012L);
        java.util.function.ToDoubleFunction<BlockPos> terrain = GasCaveNetworks.interpolated(state.router().finalDensity());
        int networks = 0;
        int entrances = 0;
        long cells = 0;
        Map<GasCaveNetwork.DeadEnd, Integer> kinds = new EnumMap<>(GasCaveNetwork.DeadEnd.class);
        for (int cx = -40; cx < 40; cx++) {
            for (int cz = -40; cz < 40; cz++) {
                GasCaveNetwork network = GasCaveNetworks.compute(state, new ChunkPos(cx, cz));
                if (network.isEmpty()) continue;
                networks++;
                if (network.entrance() != null) entrances++;
                network.deadEndKinds().forEach(kind -> kinds.merge(kind, 1, Integer::sum));
                for (LongIterator it = network.gasBlocks().iterator(); it.hasNext(); ) {
                    long key = it.nextLong();
                    cells++;
                    if (network.entranceBlocks().contains(key)) continue;
                    BlockPos pos = BlockPos.of(key);
                    helper.assertTrue(terrain.applyAsDouble(pos.above(3)) > 0.0 && terrain.applyAsDouble(pos.below(2)) > 0.0,
                            "Gas cave pierces its island near " + pos);
                }
            }
        }
        Upfall.LOGGER.info("[test] gasCaves: {} networks in 6400 chunks, {} with an entrance, {} gas blocks, dead ends {}",
                networks, entrances, cells, kinds);
        helper.assertTrue(networks > 20, "Too few gas cave networks: " + networks);
        helper.assertTrue(entrances > 0, "No gas cave opens on an island flank");
        helper.assertTrue(kinds.keySet().size() >= 4, "Dead end scenes missing: " + kinds);
        helper.succeed();
    }
}
