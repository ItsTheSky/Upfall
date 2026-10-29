package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.engine.EngineBlocks;
import net.itsthesky.upfall.content.engine.EngineLoot;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlockEntity;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.content.engine.worldgen.StalledEngineLayout;
import net.itsthesky.upfall.content.engine.worldgen.StalledEnginePiece;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.gametest.framework.GameTestCompat;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class EngineGameTests {
    private EngineGameTests() {}

    @UpfallGameTest(template = "empty")
    public static void stalledEngineLayoutHasAllParts(GameTestHelper helper) {
        for (int rotation = 0; rotation < 4; rotation++) {
            StalledEngineLayout layout = new StalledEngineLayout(new BlockPos(0, 140, 0), rotation, 42L + rotation, rotation - 1, 24, 0.6F);
            helper.assertTrue(layout.stateAt(layout.heartgear()).is(RustworksBlocks.HEARTGEAR.get()), "Heartgear missing");
            helper.assertTrue(layout.valves().size() == 3, "Expected three drain valves");
            for (BlockPos valve : layout.valves()) {
                BlockState state = layout.stateAt(valve);
                helper.assertTrue(state.is(EngineBlocks.DRAIN_VALVE.get()) && !state.getValue(DrainValveBlock.OPEN), "Closed valve expected at " + valve);
                BlockState support = layout.stateAt(valve.relative(state.getValue(DrainValveBlock.FACING).getOpposite()));
                helper.assertTrue(support != null && !support.isAir(), "Valve at " + valve + " hangs in the air");
            }
            helper.assertTrue(layout.stateAt(layout.regulator()).is(EngineBlocks.DRAIN_REGULATOR.get()), "Regulator missing");
            helper.assertTrue(layout.stateAt(layout.vaultChest()).is(ModBlocks.SKY_CACHE.get()), "Vault chest missing");
            helper.assertTrue(layout.caches().size() == 3 && layout.caches().stream().allMatch(p -> layout.stateAt(p).is(ModBlocks.SKY_CACHE.get())), "Caches missing");
            helper.assertTrue(layout.lecterns().size() == 3 && layout.lecterns().keySet().stream().allMatch(p -> layout.stateAt(p).is(Blocks.LECTERN)),
                    "Lecterns missing");
            int mist = 0;
            boolean chestFlooded = false;
            for (BoundingBox box : layout.flood()) {
                chestFlooded |= box.isInside(layout.vaultChest());
                for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                    BlockState state = layout.stateAt(pos);
                    if (state != null && state.is(EngineBlocks.STAGNANT_MIST.get())) mist++;
                }
            }
            helper.assertTrue(chestFlooded && mist > 150, "Vault should be flooded, mist " + mist);
            helper.assertTrue(layout.spirePipe().size() >= 8, "Spire main too short: " + layout.spirePipe().size());
            BlockPos far = layout.spirePipe().get(layout.spirePipe().size() - 1);
            double angle = Math.atan2(far.getZ(), far.getX());
            helper.assertTrue(Math.abs(angle - 0.6) < 0.2, "Spire main should point along its angle, got " + angle);
            helper.assertTrue(layout.columns().stream().anyMatch(c -> c.kind() == StalledEngineLayout.ColumnKind.OUTFLOW), "No outflow pipe");
            helper.assertTrue(layout.bounds().isInside(layout.vaultChest()) && layout.bounds().minY() <= 40, "Bounds too small");
            StalledEngineLayout again = new StalledEngineLayout(new BlockPos(0, 140, 0), rotation, 42L + rotation, rotation - 1, 24, 0.6F);
            helper.assertTrue(again.blocks().equals(layout.blocks()), "Layout must be deterministic");
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 1200)
    public static void stalledEnginePieceGeneratesChunkByChunk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StalledEnginePiece piece = new StalledEnginePiece(new StalledEngineLayout(new BlockPos(16000, 140, 16000), 1, 7L, 2, 20, 2.0F));
        BoundingBox bounds = piece.getBoundingBox();
        for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
            for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), McLevel.minY(level), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), McLevel.maxYExclusive(level), chunk.getMaxBlockZ());
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), chunkBox, chunk, BlockPos.ZERO);
            }
        }
        StalledEngineLayout layout = piece.layout();
        helper.assertTrue(level.getBlockState(layout.heartgear()).is(RustworksBlocks.HEARTGEAR.get()), "Heartgear not placed");
        helper.assertTrue(level.getBlockEntity(layout.vaultChest()) instanceof RandomizableContainer vault
                && EngineLoot.VAULT.equals(vault.getLootTable()), "Vault chest without loot table");
        for (BlockPos cache : layout.caches()) {
            helper.assertTrue(level.getBlockEntity(cache) instanceof RandomizableContainer container
                    && EngineLoot.CACHE.equals(container.getLootTable()), "Cache without loot table at " + cache);
        }
        for (BlockPos pos : layout.lecterns().keySet()) {
            helper.assertTrue(level.getBlockEntity(pos) instanceof LecternBlockEntity lectern && lectern.hasBook()
                    && lectern.getBook().has(DataComponents.WRITTEN_BOOK_CONTENT), "Lectern without book at " + pos);
        }
        helper.assertTrue(level.getBlockEntity(layout.regulator()) instanceof DrainRegulatorBlockEntity regulator && regulator.valveCount() == 3,
                "Regulator not wired to its valves");
        StalledEngineLayout.Column outflow = layout.columns().stream().filter(c -> c.kind() == StalledEngineLayout.ColumnKind.OUTFLOW)
                .findFirst().orElseThrow();
        helper.assertTrue(level.getBlockState(new BlockPos(outflow.x(), 50, outflow.z())).is(RustworksBlocks.RUSTY_PIPE.get()),
                "Outflow pipe should sink toward the clouds");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 400)
    public static void drainValvesEmptyTheVault(GameTestHelper helper) {
        BlockPos regulatorPos = new BlockPos(2, 1, 2);
        helper.setBlock(regulatorPos, EngineBlocks.DRAIN_REGULATOR.get().defaultBlockState());
        List<BlockPos> valves = List.of(new BlockPos(6, 1, 2), new BlockPos(9, 1, 2), new BlockPos(12, 1, 2));
        for (BlockPos valve : valves) {
            helper.setBlock(valve, EngineBlocks.DRAIN_VALVE.get().defaultBlockState().setValue(DrainValveBlock.FACING, Direction.UP));
        }
        BoundingBox room = new BoundingBox(3, 1, 8, 8, 4, 12);
        for (BlockPos pos : BlockPos.betweenClosed(room.minX(), room.minY(), room.minZ(), room.maxX(), room.maxY(), room.maxZ())) {
            helper.setBlock(pos, EngineBlocks.STAGNANT_MIST.get());
        }
        BlockPos regulatorAbs = helper.absolutePos(regulatorPos);
        BlockPos roomMin = helper.absolutePos(new BlockPos(room.minX(), room.minY(), room.minZ()));
        BlockPos roomMax = helper.absolutePos(new BlockPos(room.maxX(), room.maxY(), room.maxZ()));
        DrainRegulatorBlockEntity regulator = GameTestCompat.blockEntity(helper, regulatorPos, DrainRegulatorBlockEntity.class);
        regulator.configure(valves.stream().map(v -> helper.absolutePos(v).subtract(regulatorAbs)).toList(),
                List.of(BoundingBox.fromCorners(roomMin.subtract(regulatorAbs), roomMax.subtract(regulatorAbs))));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(helper.getBlockState(regulatorPos).getDestroyProgress(player, helper.getLevel(), regulatorAbs) == 0.0F,
                "Sealed regulator must be unbreakable");
        helper.assertTrue(EngineBlocks.STAGNANT_MIST.get().defaultBlockState().getDestroySpeed(helper.getLevel(), roomMin) < 0,
                "Stagnant mist must be unbreakable");
        helper.assertFalse(EngineBlocks.STAGNANT_MIST.get().defaultBlockState().getCollisionShape(helper.getLevel(), roomMin).isEmpty(),
                "Stagnant mist must block the way");

        Runnable turn0 = () -> turn(helper, valves.get(0));
        Runnable turn1 = () -> turn(helper, valves.get(1));
        helper.startSequence()
                .thenExecute(turn0)
                .thenExecute(turn1)
                .thenExecuteAfter(DrainRegulatorBlockEntity.CHECK_INTERVAL * 4, () -> {
                    helper.assertBlockPresent(EngineBlocks.STAGNANT_MIST.get(), new BlockPos(room.maxX(), room.maxY(), room.maxZ()));
                    helper.assertTrue(!regulator.draining(), "Two valves must not drain the vault");
                    helper.assertTrue(regulator.openValves(helper.getLevel()) == 2, "Two valves should be open");
                    turn(helper, valves.get(2));
                })
                .thenWaitUntil(() -> {
                    for (BlockPos pos : BlockPos.betweenClosed(room.minX(), room.minY(), room.minZ(), room.maxX(), room.maxY(), room.maxZ())) {
                        helper.assertBlockNotPresent(EngineBlocks.STAGNANT_MIST.get(), pos);
                    }
                    helper.assertTrue(helper.getBlockState(regulatorPos).getValue(DrainRegulatorBlock.DRAINED), "Regulator should report drained");
                })
                .thenExecute(() -> helper.assertTrue(helper.getBlockState(regulatorPos).getDestroyProgress(player, helper.getLevel(), regulatorAbs) > 0.0F,
                        "Drained regulator becomes breakable"))
                .thenSucceed();
    }

    private static void turn(GameTestHelper helper, BlockPos valve) {
        DrainValveBlock.turn(helper.getLevel(), helper.absolutePos(valve), helper.getBlockState(valve), null);
        helper.assertTrue(DrainValveBlock.isOpen(helper.getBlockState(valve)), "Valve should be open after a turn");
    }

    @UpfallGameTest(template = "empty")
    public static void vaultLootAlwaysHoldsBlueprints(GameTestHelper helper) {
        var vault = helper.getLevel().getServer().reloadableRegistries().getLootTable(EngineLoot.VAULT);
        var cache = helper.getLevel().getServer().reloadableRegistries().getLootTable(EngineLoot.CACHE);
        LootParams params = new LootParams.Builder(helper.getLevel()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .create(LootContextParamSets.CHEST);
        int cacheBlueprints = 0;
        for (int i = 0; i < 60; i++) {
            List<ItemStack> loot = vault.getRandomItems(params);
            long blueprints = loot.stream().filter(EngineGameTests::isBlueprint).count();
            helper.assertTrue(blueprints >= 1 && blueprints <= 2, "Vault should hold 1 or 2 blueprints: " + loot);
            cacheBlueprints += (int) cache.getRandomItems(params).stream().filter(EngineGameTests::isBlueprint).count();
        }
        helper.assertTrue(cacheBlueprints > 5 && cacheBlueprints < 50, "Caches should sometimes hold a blueprint: " + cacheBlueprints);
        helper.succeed();
    }

    private static boolean isBlueprint(ItemStack stack) {
        return MachinesItems.blueprints().stream().anyMatch(b -> stack.is(b.get()));
    }
}
