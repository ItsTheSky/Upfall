package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlock;
import net.itsthesky.upfall.content.quantum.observe.QuantumObservation;
import net.itsthesky.upfall.content.quantum.vault.FrostGlassBlock;
import net.itsthesky.upfall.content.quantum.vault.ShiftingChestBlockEntity;
import net.itsthesky.upfall.content.quantum.vault.ShiftingVaultLayout;
import net.itsthesky.upfall.content.quantum.vault.ShiftingVaultStructure;
import net.itsthesky.upfall.content.quantum.vault.VaultCoreBlockEntity;
import net.itsthesky.upfall.content.quantum.vault.VaultTunnel;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.List;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class QuantumGameTests {
    private QuantumGameTests() {}

    @SuppressWarnings("removal")
    static ServerPlayer observer(GameTestHelper helper, BlockPos relative, BlockPos lookAt) {
        for (ServerPlayer stray : List.copyOf(helper.getLevel().players())) leave(helper, stray);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(relative));
        player.moveTo(feet.x, feet.y, feet.z);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));
        look(helper, player, lookAt);
        return player;
    }

    static void look(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        Vec3 target = Vec3.atCenterOf(helper.absolutePos(relative));
        Vec3 delta = target.subtract(player.getEyePosition());
        float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
    }

    static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
        if (helper.getLevel().players().contains(player)) {
            helper.getLevel().removePlayerImmediately(player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }


    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "quantum_observer_rule")
    public static void observationRule(GameTestHelper helper) {
        BlockPos target = new BlockPos(12, 2, 4);
        helper.setBlock(target, Blocks.GOLD_BLOCK);
        ServerPlayer player = observer(helper, new BlockPos(12, 1, 18), target);
        BlockPos abs = helper.absolutePos(target);
        helper.assertTrue(QuantumObservation.isObservedBy(player, abs), "Lit block in front of the player should be observed");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.assertTrue(QuantumObservation.isObservedBy(player, abs) == QuantumObservation.isLit(helper.getLevel(), abs),
                "Without a handheld light, only the light level decides");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));
        helper.setBlock(new BlockPos(12, 2, 10), ModBlocks.PEARLSTONE.get());
        helper.setBlock(new BlockPos(12, 3, 10), ModBlocks.PEARLSTONE.get());
        helper.assertFalse(QuantumObservation.isObservedBy(player, abs), "An opaque block should hide the target");
        helper.setBlock(new BlockPos(12, 2, 10), QuantumBlocks.VAULT_GLASS.get());
        helper.setBlock(new BlockPos(12, 3, 10), QuantumBlocks.VAULT_GLASS.get());
        helper.assertTrue(QuantumObservation.isObservedBy(player, abs), "Glass should not hide the target");
        helper.setBlock(new BlockPos(12, 2, 10), QuantumBlocks.FROST_GLASS.get().defaultBlockState().setValue(FrostGlassBlock.FROSTED, true));
        helper.setBlock(new BlockPos(12, 3, 10), QuantumBlocks.FROST_GLASS.get().defaultBlockState().setValue(FrostGlassBlock.FROSTED, true));
        helper.assertFalse(QuantumObservation.isObservedBy(player, abs), "Frosted glass should hide the target");
        helper.setBlock(new BlockPos(12, 2, 10), Blocks.AIR);
        helper.setBlock(new BlockPos(12, 3, 10), Blocks.AIR);
        look(helper, player, new BlockPos(12, 2, 23));
        helper.assertFalse(QuantumObservation.isObservedBy(player, abs), "A target behind the player is not observed");
        leave(helper, player);
        helper.succeed();
    }


    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "quantum_observer_cluster")
    public static void flickerClusterJumpsWhenUnobserved(GameTestHelper helper) {
        for (int x = 4; x <= 20; x++) {
            for (int y = 1; y <= 5; y++) helper.setBlock(new BlockPos(x, y, 4), ModBlocks.PEARLSTONE.get());
        }
        BlockPos cluster = new BlockPos(12, 3, 5);
        helper.setBlock(cluster, QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState().setValue(FlickerShardClusterBlock.FACING, Direction.SOUTH));
        ServerPlayer player = observer(helper, new BlockPos(12, 1, 16), cluster);
        helper.onEachTick(() -> {
            for (ServerPlayer other : List.copyOf(helper.getLevel().players())) {
                if (other != player) leave(helper, other);
            }
        });
        helper.runAfterDelay(120, () -> {
            helper.assertBlockPresent(QuantumBlocks.FLICKER_SHARD_CLUSTER.get(), cluster);
            look(helper, player, new BlockPos(12, 3, 23));
        });
        helper.runAfterDelay(380, () -> {
            leave(helper, player);
            helper.assertBlockNotPresent(QuantumBlocks.FLICKER_SHARD_CLUSTER.get(), cluster);
            int found = 0;
            for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(4, 0, 0)), helper.absolutePos(new BlockPos(20, 9, 12)))) {
                if (helper.getLevel().getBlockState(pos).is(QuantumBlocks.FLICKER_SHARD_CLUSTER.get())) found++;
            }
            helper.assertTrue(found == 1, "The cluster should have moved, not vanished nor duplicated: " + found);
            helper.succeed();
        });
    }


    @GameTest(template = "empty")
    public static void shiftingVaultLayout(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1000, 120, 1000);
        for (long seed = 0; seed < 8; seed++) {
            ShiftingVaultLayout layout = new ShiftingVaultLayout(origin, seed);
            helper.assertTrue(layout.chestSpots().size() == 3, "Three chest states expected");
            BlockPos initial = layout.chestSpots().get(ShiftingVaultLayout.INITIAL_STATE);
            helper.assertTrue(layout.stateAt(initial).is(QuantumBlocks.SHIFTING_CHEST.get()), "Initial chest missing");
            for (BlockPos spot : layout.chestSpots()) {
                helper.assertTrue(layout.interior().isInside(spot), "Chest spot outside of the vault " + spot);
                helper.assertTrue(layout.stateAt(spot.above(3)).is(QuantumBlocks.VAULT_LAMP.get()), "Chest spot should be lit " + spot);
            }
            helper.assertTrue(layout.stateAt(layout.core()).is(QuantumBlocks.VAULT_CORE.get()), "Core missing");
            helper.assertTrue(layout.stateAt(layout.door()) == null || layout.stateAt(layout.door()).isAir(), "Door blocked");
            boolean[][] seen = new boolean[ShiftingVaultLayout.CELLS][ShiftingVaultLayout.CELLS];
            ArrayDeque<int[]> queue = new ArrayDeque<>();
            queue.add(new int[]{ShiftingVaultLayout.ENTRANCE_CELL_X, ShiftingVaultLayout.ENTRANCE_CELL_Z});
            seen[ShiftingVaultLayout.ENTRANCE_CELL_X][ShiftingVaultLayout.ENTRANCE_CELL_Z] = true;
            while (!queue.isEmpty()) {
                int[] c = queue.poll();
                int[][] next = {{c[0], c[1] - 1}, {c[0] + 1, c[1]}, {c[0], c[1] + 1}, {c[0] - 1, c[1]}};
                for (int[] n : next) {
                    if (n[0] < 0 || n[1] < 0 || n[0] >= ShiftingVaultLayout.CELLS || n[1] >= ShiftingVaultLayout.CELLS || seen[n[0]][n[1]]) continue;
                    boolean wall = n[0] == c[0] ? layout.wallNorth(c[0], Math.max(c[1], n[1])) : layout.wallWest(Math.max(c[0], n[0]), c[1]);
                    if (!wall) {
                        seen[n[0]][n[1]] = true;
                        queue.add(n);
                    }
                }
            }
            helper.assertTrue(seen[0][3] && seen[3][0] && seen[6][3], "A chest state is unreachable (seed " + seed + ")");
            Vec3 eye = Vec3.atBottomCenterOf(layout.at(ShiftingVaultLayout.DOOR_X, ShiftingVaultLayout.FLOOR + 1, 25)).add(0.0, 1.62, 0.0);
            Vec3 chest = Vec3.atCenterOf(initial);
            for (double t = 0.0; t < 0.97; t += 0.01) {
                BlockPos pos = BlockPos.containing(eye.lerp(chest, t));
                BlockState state = layout.stateAt(pos);
                helper.assertTrue(state == null || state.isAir() || state.is(QuantumBlocks.VAULT_GLASS.get()) || pos.equals(initial),
                        "Sightline to the chest blocked by " + state + " at " + pos);
            }
            helper.assertTrue(!layout.frost().isEmpty() && layout.crumblingCells().size() >= 3 && layout.darkZones().size() == 2, "Traps missing");
            helper.assertTrue(new ShiftingVaultLayout(origin, seed).blocks().equals(layout.blocks()), "Layout must be deterministic");
        }
        helper.succeed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "quantum_observer_chest")
    public static void shiftingChestJumpsWhenUnobserved(GameTestHelper helper) {
        List<BlockPos> spots = List.of(new BlockPos(4, 2, 12), new BlockPos(20, 2, 12), new BlockPos(12, 2, 4));
        List<BlockPos> absolute = spots.stream().map(helper::absolutePos).toList();
        helper.setBlock(spots.get(0), QuantumBlocks.SHIFTING_CHEST.get());
        if (helper.getLevel().getBlockEntity(absolute.get(0)) instanceof ShiftingChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        }
        BlockPos coreRel = new BlockPos(12, 1, 20);
        helper.setBlock(coreRel, QuantumBlocks.VAULT_CORE.get());
        VaultCoreBlockEntity core = (VaultCoreBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(coreRel));
        BoundingBox inside = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(0, 0, 0)), helper.absolutePos(new BlockPos(23, 10, 23)));
        core.configure(absolute, List.of(Direction.EAST, Direction.WEST, Direction.SOUTH), 0, List.of(), List.of(), List.of(), inside);
        ServerPlayer player = observer(helper, new BlockPos(12, 1, 12), spots.get(0));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(core.chestState() == 0, "Observed chest must stay");
            look(helper, player, spots.get(1));
        });
        int[] moves = {0};
        int[] last = {0};
        helper.onEachTick(() -> {
            if (core.chestState() != last[0]) {
                moves[0]++;
                last[0] = core.chestState();
            }
            helper.assertTrue(helper.getTick() < 100 || core.chestState() != 1, "The chest jumped onto the observed state");
        });
        helper.runAfterDelay(200, () -> {
            leave(helper, player);
            helper.assertTrue(moves[0] >= 2, "Unobserved chest should keep jumping, moves " + moves[0]);
            int chests = 0;
            for (BlockPos spot : spots) {
                if (helper.getLevel().getBlockState(helper.absolutePos(spot)).is(QuantumBlocks.SHIFTING_CHEST.get())) chests++;
            }
            helper.assertTrue(chests == 1, "Exactly one chest expected, got " + chests);
            ShiftingChestBlockEntity moved = (ShiftingChestBlockEntity) helper.getLevel().getBlockEntity(absolute.get(core.chestState()));
            helper.assertTrue(moved.getItem(0).is(Items.DIAMOND) && moved.getItem(0).getCount() == 3, "Chest contents must follow the chest");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 12000)
    public static void shiftingVaultSites(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var settings = registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(UpfallTerrain.SETTINGS);
        var generator = new NoiseBasedChunkGenerator(ModBiomes.biomeSource(registries.lookupOrThrow(Registries.BIOME)), settings);
        RandomState state = RandomState.create(settings.value(), registries.lookupOrThrow(Registries.NOISE), 20261012L);
        LevelHeightAccessor height = LevelHeightAccessor.create(UpfallTerrain.MIN_Y, UpfallTerrain.HEIGHT);
        ShiftingVaultStructure.Terrain terrain = ShiftingVaultStructure.Terrain.of(generator, height, state);
        int margin = UpfallConfig.getInt(UpfallConfig.VAULT_MARGIN);
        int candidates = 0;
        int sites = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 24; i++) {
            ChunkPos chunk = new ChunkPos(i * 56 - 600, (i * 37) % 300 - 150);
            candidates++;
            ShiftingVaultStructure.Site site = ShiftingVaultStructure.find(terrain, chunk, RandomSource.create(i), margin);
            if (site == null) continue;
            sites++;
            VaultTunnel.Path tunnel = site.tunnel();
            int length = tunnel.main().size();
            helper.assertTrue(length >= UpfallConfig.getInt(UpfallConfig.VAULT_TUNNEL_MIN) - 5 && length <= UpfallConfig.getInt(UpfallConfig.VAULT_TUNNEL_MAX) + 5,
                    "Tunnel length out of range: " + length);
            helper.assertTrue(terrain.density(tunnel.exit().above()) < 0.0, "Tunnel must end in the open");
            ShiftingVaultLayout layout = new ShiftingVaultLayout(site.origin(), 0L);
            BoundingBox box = layout.bounds().inflatedBy(margin);
            for (int x = box.minX(); x <= box.maxX(); x += 6) {
                for (int z = box.minZ(); z <= box.maxZ(); z += 6) {
                    helper.assertTrue(terrain.solid(x, z, box.minY(), box.maxY()), "Vault margin is not solid at " + x + " " + z);
                }
            }
        }
        Upfall.LOGGER.info("[test] shiftingVaultSites: {} / {} candidate regions host a vault, {} ms per region",
                sites, candidates, (System.nanoTime() - start) / 1_000_000 / candidates);
        helper.assertTrue(sites > 0, "No Shifting Vault site found in " + candidates + " regions");
        helper.succeed();
    }
}
