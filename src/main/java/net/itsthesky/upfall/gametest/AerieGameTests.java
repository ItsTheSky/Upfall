package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.content.aerie.AerieBlocks;
import net.itsthesky.upfall.content.aerie.AerieItems;
import net.itsthesky.upfall.content.aerie.AerieLoot;
import net.itsthesky.upfall.content.aerie.worldgen.AerieBlueprint;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public final class AerieGameTests {
    private AerieGameTests() {}

    private static final BlockPos ORIGIN = new BlockPos(1000, 140, -2000);

    @UpfallGameTest(template = "empty")
    public static void aerieLayoutIsDeterministicAndBounded(GameTestHelper helper) {
        int multiTower = 0;
        for (long seed = 0; seed < 200; seed++) {
            AerieLayout.Plan plan = AerieLayout.plan(seed, ORIGIN);
            helper.assertTrue(plan.equals(AerieLayout.plan(seed, ORIGIN)), "Layout is not deterministic for seed " + seed);
            helper.assertTrue(plan.towers().get(0).kind() == AerieLayout.Kind.KEEP, "The keep must be the first tower");
            helper.assertTrue(plan.bridges().size() >= plan.towers().size() - 1, "Towers are not all connected for seed " + seed);
            for (AerieLayout.Tower tower : plan.towers()) {
                double reach = Math.hypot(tower.base().getX() - ORIGIN.getX(), tower.base().getZ() - ORIGIN.getZ()) + tower.radius();
                helper.assertTrue(reach <= AerieLayout.MAX_EXTENT, "Tower out of bounds for seed " + seed + ": " + reach);
            }
            if (plan.towers().size() >= 4) multiTower++;
        }
        helper.assertTrue(multiTower > 100, "Too few layouts reach four towers: " + multiTower);
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void aerieKeepHoldsTreasure(GameTestHelper helper) {
        for (long seed = 0; seed < 40; seed++) {
            AerieLayout.Plan plan = AerieLayout.plan(seed, ORIGIN);
            AerieBlueprint keep = AerieBlueprint.tower(plan, 0, seed);
            helper.assertTrue(keep.hasCache(AerieLoot.KEEP), "Keep without treasure chest for seed " + seed);
            helper.assertTrue(keep.trapCount() > 0, "Keep treasure is not trapped for seed " + seed);
            helper.assertTrue(keep.spawnerCount() >= 1, "Keep without spawner for seed " + seed);
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void aerieBridgesAvoidOtherTowers(GameTestHelper helper) {
        for (long seed = 0; seed < 60; seed++) {
            AerieLayout.Plan plan = AerieLayout.plan(seed, ORIGIN);
            for (int i = 0; i < plan.bridges().size(); i++) {
                AerieLayout.Bridge bridge = plan.bridges().get(i);
                AerieBlueprint bp = AerieBlueprint.bridge(plan, i, seed);
                helper.assertTrue(!bp.blocks().isEmpty(), "Empty bridge for seed " + seed);
                for (Map.Entry<BlockPos, BlockState> entry : bp.blocks().entrySet()) {
                    BlockPos pos = entry.getKey();
                    for (int t = 0; t < plan.towers().size(); t++) {
                        if (t == bridge.from() || t == bridge.to()) continue;
                        AerieLayout.Tower tower = plan.towers().get(t);
                        boolean inside = AerieLayout.inDisc(pos.getX() - tower.base().getX(), pos.getZ() - tower.base().getZ(), tower.radius())
                                && pos.getY() >= tower.bottom() && pos.getY() <= tower.top();
                        helper.assertTrue(!inside, "Bridge " + i + " cuts through tower " + t + " for seed " + seed);
                    }
                }
            }
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 120)
    public static void weatheredBricksCrumbleForGood(GameTestHelper helper) {
        for (int x = 2; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 2, 3), AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get());
        }
        helper.getLevel().scheduleTick(helper.absolutePos(new BlockPos(2, 2, 3)), AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get(), 1);
        helper.runAfterDelay(100, () -> {
            for (int x = 2; x <= 4; x++) {
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 2, 3));
            }
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty")
    public static void gravitySceptreTogglesWeightless(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sceptre = new ItemStack(AerieItems.GRAVITY_SCEPTRE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, sceptre);
        sceptre.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.hasEffect(ModEffects.WEIGHTLESS), "The sceptre did not grant Weightless");
        helper.assertTrue(sceptre.getDamageValue() == 1, "The sceptre should lose one durability per activation");
        helper.assertTrue(McPlayers.isOnCooldown(player, AerieItems.GRAVITY_SCEPTRE.get()), "The sceptre has no cooldown");
        McPlayers.removeCooldown(player, AerieItems.GRAVITY_SCEPTRE.get());
        sceptre.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!player.hasEffect(ModEffects.WEIGHTLESS), "Using the sceptre again must release Weightless");
        helper.assertTrue(sceptre.getDamageValue() == 1, "Releasing must not cost durability");
        helper.succeed();
    }
}
