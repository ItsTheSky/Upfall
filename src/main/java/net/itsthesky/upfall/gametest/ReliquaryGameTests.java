package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.itsthesky.upfall.content.reliquary.ReliquaryEntities;
import net.itsthesky.upfall.content.reliquary.ReliquaryItems;
import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.itsthesky.upfall.content.reliquary.item.FoldAnchorData;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryBlueprint;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPiece;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModDataComponents;
import net.itsthesky.upfall.worldgen.ReliquaryZones;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReliquaryGameTests {
    private ReliquaryGameTests() {}

    private static final BlockPos ORIGIN = new BlockPos(1000, 160, -2000);

    private static List<ReliquaryBlueprint> blueprints(long seed) {
        ReliquaryLayout.Plan plan = ReliquaryLayout.plan(seed, ORIGIN);
        List<ReliquaryBlueprint> all = new ArrayList<>();
        for (int i = 0; i < plan.cubes().size(); i++) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.CUBE, i).blueprint());
        }
        for (int i = 0; i < plan.links().size(); i++) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.LINK, i).blueprint());
        }
        for (int i = 0; i < plan.debris().size(); i++) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.DEBRIS, i).blueprint());
        }
        for (int i = 0; i < plan.fragments().size(); i++) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.FRAGMENT, i).blueprint());
        }
        if (plan.approach() != null) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.APPROACH, 0).blueprint());
        }
        for (int i = 0; i < plan.trails().size(); i++) {
            all.add(new ReliquaryPiece(plan, seed, ReliquaryPiece.Kind.TRAIL, i).blueprint());
        }
        return all;
    }

    private static List<ItemStack> roll(GameTestHelper helper, ResourceKey<LootTable> key) {
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
    }

    private static int count(List<ItemStack> loot, Item item) {
        return loot.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @UpfallGameTest(template = "empty")
    public static void reliquaryLayoutIsBoundedAndComplete(GameTestHelper helper) {
        for (long seed = 0; seed < 50; seed++) {
            ReliquaryLayout.Plan plan = ReliquaryLayout.plan(seed, ORIGIN);
            helper.assertTrue(plan.equals(ReliquaryLayout.plan(seed, ORIGIN)), "Layout is not deterministic for seed " + seed);
            int satellites = plan.cubes().size() - 1;
            helper.assertTrue(satellites >= ReliquaryLayout.MIN_SATELLITES && satellites <= ReliquaryLayout.MAX_SATELLITES,
                    "Satellite count " + satellites + " out of range for seed " + seed);
            helper.assertTrue(plan.cubes().get(0).interior() == ReliquaryLayout.Interior.CORE, "First cube is not the core for seed " + seed);
            helper.assertTrue(plan.shardCount() >= ReliquaryLayout.REQUIRED_SHARDS, "Only " + plan.shardCount() + " shard caches for seed " + seed);
            helper.assertTrue(plan.fragmentCount() >= 25, "Only " + plan.fragmentCount() + " small cubes for seed " + seed);
            for (ReliquaryLayout.Cube cube : plan.cubes()) {
                int reach = Math.max(Math.abs(cube.cx() - ORIGIN.getX()), Math.abs(cube.cz() - ORIGIN.getZ()));
                helper.assertTrue(reach <= ReliquaryLayout.MAX_REACH, "Cube out of reach for seed " + seed + ": " + reach);
                helper.assertTrue(cube.cy() >= 100 && cube.cy() <= 300, "Cube out of height range for seed " + seed + ": " + cube.cy());
            }
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void reliquaryBlueprintsStayInBounds(GameTestHelper helper) {
        for (long seed = 0; seed < 8; seed++) {
            for (ReliquaryBlueprint bp : blueprints(seed)) {
                for (BlockPos pos : bp.blocks().keySet()) {
                    int reach = Math.max(Math.abs(pos.getX() - ORIGIN.getX()), Math.abs(pos.getZ() - ORIGIN.getZ()));
                    helper.assertTrue(reach <= ReliquaryLayout.MAX_REACH, "Block out of reach for seed " + seed + ": " + pos);
                    helper.assertTrue(pos.getY() >= 90 && pos.getY() <= 310, "Block out of height range for seed " + seed + ": " + pos);
                }
            }
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void reliquaryCoreHoldsSocketsSealsAndLoot(GameTestHelper helper) {
        for (long seed = 0; seed < 12; seed++) {
            int sockets = 0;
            int seals = 0;
            boolean core = false;
            boolean archive = false;
            int satelliteCaches = 0;
            for (ReliquaryBlueprint bp : blueprints(seed)) {
                for (BlockState state : bp.blocks().values()) {
                    if (state.is(ReliquaryBlocks.FOLD_SOCKET.get())) sockets++;
                    if (state.is(ReliquaryBlocks.FOLD_SEAL.get())) seals++;
                }
                core |= bp.hasCache(ReliquaryStructures.CORE_LOOT);
                archive |= bp.hasCache(ReliquaryStructures.ARCHIVE_LOOT);
                if (bp.hasCache(ReliquaryStructures.SATELLITE_LOOT)) satelliteCaches++;
            }
            helper.assertTrue(sockets == FoldSocketBlock.REQUIRED, "Expected 4 fold sockets for seed " + seed + ", got " + sockets);
            helper.assertTrue(seals > 0, "No fold seal for seed " + seed);
            helper.assertTrue(core, "No core loot cache for seed " + seed);
            helper.assertTrue(archive, "No archive loot cache for seed " + seed);
            helper.assertTrue(satelliteCaches >= ReliquaryLayout.REQUIRED_SHARDS, "Too few satellite caches for seed " + seed + ": " + satelliteCaches);
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 200)
    public static void foldSocketsDissolveSealsOnlyWhenAllFilled(GameTestHelper helper) {
        BlockPos[] sockets = {new BlockPos(2, 2, 2), new BlockPos(6, 2, 2), new BlockPos(2, 2, 6), new BlockPos(6, 2, 6)};
        BlockPos[] seals = {new BlockPos(4, 2, 4), new BlockPos(4, 3, 4), new BlockPos(5, 2, 4)};
        for (BlockPos socket : sockets) {
            helper.setBlock(socket, ReliquaryBlocks.FOLD_SOCKET.get());
        }
        for (BlockPos seal : seals) {
            helper.setBlock(seal, ReliquaryBlocks.FOLD_SEAL.get());
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ReliquaryItems.FOLD_SHARD.get(), 4));
        for (int i = 0; i < 3; i++) {
            helper.useBlock(sockets[i], player);
        }
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Each socket should consume one shard");
        for (int i = 0; i < 3; i++) {
            helper.assertBlockProperty(sockets[i], FoldSocketBlock.FILLED, true);
        }
        helper.runAfterDelay(60, () -> {
            for (BlockPos seal : seals) {
                helper.assertBlockPresent(ReliquaryBlocks.FOLD_SEAL.get(), seal);
            }
            helper.useBlock(sockets[3], player);
        });
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(sockets[3], FoldSocketBlock.FILLED, true);
            for (BlockPos seal : seals) {
                helper.assertBlockNotPresent(ReliquaryBlocks.FOLD_SEAL.get(), seal);
            }
            helper.assertBlockPresent(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), seals[0]);
            helper.assertBlockPresent(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), seals[2]);
            helper.assertBlockPresent(Blocks.AIR, seals[1]);
            helper.assertEntityPresent(ReliquaryEntities.FOLD_WARDEN.get());
            cleanup(helper);
        });
    }

    private static List<FoldSatellite> satellites(GameTestHelper helper, FoldWarden warden) {
        return helper.getLevel().getEntitiesOfClass(FoldSatellite.class, warden.getBoundingBox().inflate(32.0),
                satellite -> satellite.isAlive() && satellite.isOwnedBy(warden));
    }

    private static void cleanup(GameTestHelper helper) {
        for (FoldWarden warden : helper.getLevel().getEntitiesOfClass(FoldWarden.class, helper.getBounds())) {
            satellites(helper, warden).forEach(Entity::discard);
            warden.discard();
        }
    }

    private static void kill(GameTestHelper helper, Entity entity) {
        //? if >=26.1 {
        /*entity.kill(helper.getLevel());
        *///?} else {
        entity.kill();
        //?}
    }

    private static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        //? if >=26.1 {
        /*player.teleportTo(level, x, y, z, java.util.Set.of(), 0.0F, 0.0F, true);
        *///?} else {
        player.teleportTo(level, x, y, z, 0.0F, 0.0F);
        //?}
    }

    private static void strike(GameTestHelper helper, FoldWarden warden, Player player, float amount) {
        warden.invulnerableTime = 0;
        McEntities.hurt(warden, helper.getLevel().damageSources().playerAttack(player), amount);
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 400)
    public static void foldWardenShieldHoldsUntilSatellitesFall(GameTestHelper helper) {
        FoldWarden warden = helper.spawn(ReliquaryEntities.FOLD_WARDEN.get(), new BlockPos(12, 2, 12));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        float[] health = new float[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(warden.phase() == FoldWarden.Phase.CUBE, "Warden is still awakening"))
                .thenExecute(() -> {
                    health[0] = warden.getHealth();
                    strike(helper, warden, player, 20.0F);
                    helper.assertTrue(warden.getHealth() == health[0], "Shielded warden took damage");
                    List<FoldSatellite> satellites = satellites(helper, warden);
                    helper.assertTrue(satellites.size() == 4, "Expected 4 satellites, found " + satellites.size());
                    satellites.forEach(satellite -> kill(helper, satellite));
                })
                .thenWaitUntil(() -> helper.assertFalse(warden.isShielded(), "Shield is still up"))
                .thenExecute(() -> {
                    strike(helper, warden, player, 20.0F);
                    helper.assertTrue(warden.getHealth() < health[0], "Exposed warden took no damage");
                    cleanup(helper);
                })
                .thenSucceed();
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 500)
    public static void foldWardenUnfoldsAtHalfHealth(GameTestHelper helper) {
        FoldWarden warden = helper.spawn(ReliquaryEntities.FOLD_WARDEN.get(), new BlockPos(12, 2, 12));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(warden.phase() == FoldWarden.Phase.CUBE, "Warden is still awakening"))
                .thenExecute(() -> satellites(helper, warden).forEach(satellite -> kill(helper, satellite)))
                .thenWaitUntil(() -> helper.assertFalse(warden.isShielded(), "Shield is still up"))
                .thenExecute(() -> {
                    strike(helper, warden, player, 1000.0F);
                    helper.assertTrue(warden.isAlive() && warden.getHealth() == FoldWarden.PHASE_TWO_HEALTH,
                            "Phase one must stop at half health, got " + warden.getHealth());
                })
                .thenWaitUntil(() -> helper.assertTrue(warden.phase() == FoldWarden.Phase.WYRM, "Warden has not unfolded: " + warden.phase()))
                .thenExecute(() -> {
                    helper.assertTrue(warden.getBbWidth() < 2.5F, "Wyrm keeps the cube hitbox: " + warden.getBbWidth());
                    helper.assertTrue(warden.segments()[0].isActive(), "Wyrm segments are not active");
                    helper.assertTrue(McEntities.hurt(warden.segments()[0], helper.getLevel().damageSources().playerAttack(player), 10.0F),
                            "Segments must forward damage to the warden");
                    helper.assertTrue(warden.getHealth() < FoldWarden.PHASE_TWO_HEALTH, "Segment hit did not hurt the warden");
                    cleanup(helper);
                })
                .thenSucceed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 240)
    public static void foldLatticeDissolves(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, ReliquaryBlocks.FOLD_LATTICE.get());
        helper.assertBlockPresent(ReliquaryBlocks.FOLD_LATTICE.get(), pos);
        helper.succeedWhen(() -> helper.assertBlockNotPresent(ReliquaryBlocks.FOLD_LATTICE.get(), pos));
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 200)
    public static void foldWardenDropsAnchorAndHeart(GameTestHelper helper) {
        FoldWarden warden = helper.spawn(ReliquaryEntities.FOLD_WARDEN.get(), new BlockPos(12, 2, 12));
        //? if >=26.1 {
        /*LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(warden.getType().getDefaultLootTable().orElseThrow());
        *///?} else {
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(warden.getType().getDefaultLootTable());
        //?}
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, warden)
                .withParameter(LootContextParams.ORIGIN, warden.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        for (int i = 0; i < 20; i++) {
            List<ItemStack> loot = table.getRandomItems(params);
            helper.assertTrue(count(loot, ReliquaryItems.FOLD_ANCHOR.get()) == 1, "Warden must drop one fold anchor: " + loot);
            helper.assertTrue(count(loot, ReliquaryItems.FOLD_HEART.get()) == 1, "Warden must drop one fold heart: " + loot);
            int shards = count(loot, ReliquaryItems.FOLD_SHARD.get());
            helper.assertTrue(shards >= 1 && shards <= 3, "Warden must drop 1 to 3 fold shards: " + loot);
        }
        warden.discard();
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void foldAnchorRecallsAndCostsDurability(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos anchorPos = helper.absolutePos(new BlockPos(2, 2, 2));
        teleport(player, helper.getLevel(), anchorPos.getX() + 0.5, anchorPos.getY(), anchorPos.getZ() + 0.5);
        ItemStack anchor = new ItemStack(ReliquaryItems.FOLD_ANCHOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, anchor);
        anchor.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        FoldAnchorData data = anchor.get(ModDataComponents.FOLD_ANCHOR);
        helper.assertTrue(data != null && data.pos().pos().equals(anchorPos), "First use must set the anchor on the player position");
        helper.assertTrue(anchor.getDamageValue() == 0, "Setting the anchor must be free");
        McPlayers.removeCooldown(player, ReliquaryItems.FOLD_ANCHOR.get());
        teleport(player, helper.getLevel(), anchorPos.getX() + 10.5, anchorPos.getY(), anchorPos.getZ() + 0.5);
        anchor.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.blockPosition().equals(anchorPos), "Second use must teleport back to the anchor: " + player.blockPosition());
        helper.assertTrue(anchor.getDamageValue() == 1, "Recall must cost exactly one durability");
        helper.assertTrue(!anchor.has(ModDataComponents.FOLD_ANCHOR), "Anchor must be consumed by the recall");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void reliquaryLootTablesRoll(GameTestHelper helper) {
        int archiveItems = 0;
        for (int i = 0; i < 40; i++) {
            List<ItemStack> satellite = roll(helper, ReliquaryStructures.SATELLITE_LOOT);
            helper.assertTrue(count(satellite, ReliquaryItems.FOLD_SHARD.get()) == 1, "Satellite loot must hold exactly one fold shard: " + satellite);
            List<ItemStack> core = roll(helper, ReliquaryStructures.CORE_LOOT);
            helper.assertTrue(count(core, ReliquaryItems.FOLD_ANCHOR.get()) == 0, "Fold anchor must drop from the warden, not the core: " + core);
            archiveItems += roll(helper, ReliquaryStructures.ARCHIVE_LOOT).size();
        }
        helper.assertTrue(archiveItems > 0, "Archive loot never yields anything");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void reliquaryZonesAreDeterministic(GameTestHelper helper) {
        int present = 0;
        for (long seed = 0; seed < 20; seed++) {
            long key = ReliquaryZones.worldKey(seed);
            helper.assertTrue(key == ReliquaryZones.worldKey(seed), "World key is not deterministic for seed " + seed);
            for (int cx = -3; cx <= 3; cx++) {
                for (int cz = -3; cz <= 3; cz++) {
                    ChunkPos a = ReliquaryZones.nominalCenter(key, cx, cz);
                    helper.assertTrue(Objects.equals(a, ReliquaryZones.nominalCenter(key, cx, cz)), "Zone center differs for seed " + seed);
                    if (a == null) continue;
                    present++;
                    helper.assertTrue(ReliquaryZones.cellOfChunk(McChunks.x(a)) == cx && ReliquaryZones.cellOfChunk(McChunks.z(a)) == cz,
                            "Zone center " + a + " escapes its cell " + cx + "," + cz);
                }
            }
        }
        helper.assertTrue(present > 500, "Too few reliquary zones: " + present);
        helper.succeed();
    }
}
