package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.worldgen.PipeBridge;
import net.itsthesky.upfall.content.rustworks.worldgen.PipeBridgePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class PipeBridgeGameTests {
    private PipeBridgeGameTests() {}

    @GameTest(template = "empty")
    public static void pipeBridgeSegmentsCoverTheBridge(GameTestHelper helper) {
        RandomSource random = RandomSource.create(7L);
        for (int i = 0; i < 40; i++) {
            Direction dir = Direction.from2DDataValue(i % 4);
            int gap = PipeBridge.MIN_GAP + random.nextInt(PipeBridge.MAX_GAP - PipeBridge.MIN_GAP + 1);
            int a = 5 + random.nextInt(20);
            int ha = 120 + random.nextInt(40);
            int hb = ha - PipeBridge.MAX_DY + random.nextInt(2 * PipeBridge.MAX_DY + 1);
            PipeBridge bridge = PipeBridge.create(random, 1000 * i, -500, dir, a, a + gap + 1, ha, hb, 2 + random.nextInt(2));
            helper.assertTrue(PipeBridge.load(bridge.save()).equals(bridge), "NBT round trip failed for " + bridge);

            StructurePiecesBuilder builder = new StructurePiecesBuilder();
            PipeBridgePiece.addAll(builder, bridge);
            List<StructurePiece> pieces = builder.build().pieces();
            int next = bridge.first();
            for (StructurePiece piece : pieces) {
                PipeBridgePiece segment = (PipeBridgePiece) piece;
                helper.assertTrue(segment.from() == next && segment.to() - segment.from() < PipeBridge.SEGMENT, "Segments must tile the bridge");
                next = segment.to() + 1;
                for (int t = segment.from(); t <= segment.to(); t++) {
                    Map<BlockPos, BlockState> blocks = rasterize(bridge, t, t);
                    for (BlockPos pos : blocks.keySet()) {
                        helper.assertTrue(segment.getBoundingBox().isInside(pos), "Block " + pos + " outside its segment box (t=" + t + ")");
                    }
                }
            }
            helper.assertTrue(next == bridge.last() + 1, "Segments stop before the bridge end");

            Map<BlockPos, BlockState> all = rasterize(bridge, bridge.first(), bridge.last());
            helper.assertTrue(all.values().stream().noneMatch(s -> s.is(RustworksBlocks.RUSTY_COG.get())), "Bridges must not use cog blocks");
            helper.assertTrue(all.values().stream().anyMatch(s -> s.is(RustworksBlocks.RUSTSLATE.get())), "Flange rings missing");
            for (int t = bridge.first(); t <= bridge.last(); t++) {
                if (bridge.missing(t)) continue;
                for (int u = -1; u <= 1; u++) {
                    BlockState deck = all.get(bridge.at(t, u, bridge.deck(t)));
                    helper.assertTrue(deck != null && deck.is(RustworksBlocks.RUSTY_PLATING.get()), "Deck hole at t=" + t + " u=" + u);
                    helper.assertTrue(all.get(bridge.at(t, u, bridge.deck(t) + 1)) == null, "Deck obstructed at t=" + t + " u=" + u);
                }
            }
            int mid = (bridge.a() + bridge.b()) / 2;
            if (!bridge.missing(mid)) {
                BlockState core = all.get(bridge.at(mid, 0, bridge.deck(mid) - bridge.radius()));
                helper.assertTrue(core != null && core.isAir() == bridge.hollow(), "Pipe core does not match hollow flag");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pipeBridgeSearchFindsGaps(GameTestHelper helper) {
        for (int gap : new int[]{PipeBridge.MIN_GAP, 30, PipeBridge.MAX_GAP}) {
            Optional<PipeBridge> bridge = search(gap, 4, true);
            helper.assertTrue(bridge.isPresent(), "No bridge over a " + gap + " block gap");
            PipeBridge b = bridge.get();
            helper.assertTrue(b.dir() == Direction.EAST && b.b() - b.a() - 1 == gap && b.ha() == 150 && b.hb() == 154,
                    "Wrong bridge over a " + gap + " block gap: " + b);
        }
        helper.assertTrue(search(PipeBridge.MIN_GAP - 2, 0, true).isEmpty(), "Gap too small should be skipped");
        helper.assertTrue(search(PipeBridge.MAX_GAP + 6, 0, true).isEmpty(), "Gap too large should be skipped");
        helper.assertTrue(search(30, PipeBridge.MAX_DY + 4, true).isEmpty(), "Landing too high should be skipped");
        helper.assertTrue(search(30, 0, false).isEmpty(), "Landing outside Rustworks should be skipped");
        helper.succeed();
    }

    private static Optional<PipeBridge> search(int gap, int rise, boolean landingValid) {
        int edge = 20;
        PipeBridge.Heights heights = (x, z) -> x <= edge ? 150 : x > edge + gap ? 150 + rise : 60;
        PipeBridge.Biomes biomes = (x, y, z) -> landingValid || x <= edge;
        RandomSource random = RandomSource.create(gap * 31L + rise);
        for (int attempt = 0; attempt < 4; attempt++) {
            Optional<PipeBridge> bridge = PipeBridge.search(random, 0, 0, heights, biomes);
            if (bridge.isPresent()) return bridge;
        }
        return Optional.empty();
    }

    private static Map<BlockPos, BlockState> rasterize(PipeBridge bridge, int from, int to) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        PipeBridge.Sink sink = new PipeBridge.Sink() {
            @Override
            public boolean isOpen(BlockPos pos) {
                return !blocks.containsKey(pos);
            }

            @Override
            public void set(BlockPos pos, BlockState state) {
                blocks.put(pos.immutable(), state);
            }
        };
        for (int t = from; t <= to; t++) {
            bridge.place(t, sink);
        }
        return blocks;
    }
}
