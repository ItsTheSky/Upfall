package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.gravity.InvertedGravity;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class GravityGameTests {
    private GravityGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void ventFillsAndColumnCollapses(GameTestHelper helper) {
        BlockPos vent = new BlockPos(3, 1, 3);
        helper.setBlock(vent, ModBlocks.UPDRAFT_VENT.get());
        helper.runAfterDelay(10, () -> {
            helper.assertBlockPresent(ModBlocks.UPDRAFT_MIST.get(), vent.above());
            helper.assertBlockPresent(ModBlocks.UPDRAFT_MIST.get(), vent.above(6));
            helper.setBlock(vent, Blocks.AIR);
            helper.succeedWhen(() -> {
                helper.assertBlockNotPresent(ModBlocks.UPDRAFT_MIST.get(), vent.above());
                helper.assertBlockNotPresent(ModBlocks.UPDRAFT_MIST.get(), vent.above(6));
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void updraftLiftsEntities(GameTestHelper helper) {
        BlockPos vent = new BlockPos(3, 1, 3);
        helper.setBlock(vent, ModBlocks.UPDRAFT_VENT.get());
        helper.runAfterDelay(5, () -> {
            ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, vent.above());
            double startY = stand.getY();
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(stand.getY() > startY + 2.0, "The updraft did not lift the entity: " + startY + " -> " + stand.getY());
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void weightlessLowersGravity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        double normal = player.getAttributeValue(Attributes.GRAVITY);
        player.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, 200));
        double weightless = player.getAttributeValue(Attributes.GRAVITY);
        helper.assertTrue(weightless < normal * 0.5, "Weightless must strongly reduce gravity: " + normal + " -> " + weightless);
        helper.assertTrue(player.getAttributeValue(Attributes.JUMP_STRENGTH) > 0.42, "Weightless must raise jump strength");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void denseCloudSoftensFalls(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), ModBlocks.DENSE_CLOUD.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        var cloudState = ModBlocks.DENSE_CLOUD.get().defaultBlockState();
        ModBlocks.DENSE_CLOUD.get().fallOn(helper.getLevel(), cloudState, helper.absolutePos(new BlockPos(3, 1, 3)), player, 13.0F);
        helper.assertTrue(player.getHealth() >= 14.0F && player.getHealth() < 20.0F, "Dense cloud should halve fall damage: " + player.getHealth());
        helper.succeed();
    }

    private static final int CEILING_Y = 12;

    private static double placeCeiling(GameTestHelper helper) {
        for (int x = 2; x <= 8; x++) {
            for (int z = 2; z <= 8; z++) {
                helper.setBlock(new BlockPos(x, CEILING_Y, z), ModBlocks.PEARLSTONE.get());
            }
        }
        return helper.absolutePos(new BlockPos(0, CEILING_Y, 0)).getY();
    }

    @SuppressWarnings("removal")
    private static ServerPlayer tickedPlayer(GameTestHelper helper, Vec3 relative, boolean weightless) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(relative);
        player.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        if (weightless) {
            player.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, 2000));
        }
        helper.onEachTick(player::doTick);
        return player;
    }

    private static void remove(GameTestHelper helper, ServerPlayer... players) {
        for (ServerPlayer player : players) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    private static void assertStandingOnCeiling(GameTestHelper helper, ServerPlayer player, double ceiling) {
        helper.assertTrue(InvertedGravity.isInverted(player), "The player should be inverted under the ceiling");
        helper.assertTrue(Math.abs(player.getBoundingBox().maxY - ceiling) < 1.0E-3,
                "The player should rest against the ceiling: top " + player.getBoundingBox().maxY + ", ceiling " + ceiling);
        helper.assertTrue(player.onGround(), "Resting against the ceiling must count as ground");
    }

    @GameTest(template = "empty_large", timeoutTicks = 120, skyAccess = true)
    public static void weightlessFallsOntoCeiling(GameTestHelper helper) {
        double ceiling = placeCeiling(helper);
        helper.setBlock(new BlockPos(17, 1, 17), ModBlocks.PEARLSTONE.get());
        helper.setBlock(new BlockPos(3, 2, 7), ModBlocks.PEARLSTONE.get());
        ServerPlayer under = tickedPlayer(helper, new Vec3(5.5, 3.0, 5.5), true);
        ServerPlayer open = tickedPlayer(helper, new Vec3(17.5, 2.0, 17.5), true);
        ServerPlayer plain = tickedPlayer(helper, new Vec3(3.5, 3.0, 7.5), false);
        helper.startSequence()
                .thenWaitUntil(() -> assertStandingOnCeiling(helper, under, ceiling))
                .thenExecute(() -> {
                    helper.assertTrue(under.getAttributeValue(Attributes.GRAVITY) < 0.0, "Inverted gravity must be negative");
                    helper.assertTrue(under.getEyeHeight() < 0.5F, "The eye must move to the bottom of the box: " + under.getEyeHeight());
                    helper.assertTrue(under.fallDistance == 0.0F, "No fall distance builds up while inverted");
                    double openGravity = open.getAttributeValue(Attributes.GRAVITY);
                    helper.assertTrue(!InvertedGravity.isInverted(open) && openGravity > 0.0 && openGravity < 0.05,
                            "Without a ceiling Weightless only lowers gravity: " + openGravity);
                    helper.assertTrue(!InvertedGravity.isInverted(plain) && plain.getAttributeValue(Attributes.GRAVITY) > 0.07,
                            "Without Weightless the gravity stays normal under a ceiling");
                    remove(helper, under, open, plain);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void invertedJumpPushesAwayFromCeiling(GameTestHelper helper) {
        double ceiling = placeCeiling(helper);
        ServerPlayer player = tickedPlayer(helper, new Vec3(5.5, 3.0, 5.5), true);
        helper.startSequence()
                .thenWaitUntil(() -> assertStandingOnCeiling(helper, player, ceiling))
                .thenExecute(() -> player.setJumping(true))
                .thenIdle(1)
                .thenExecute(() -> {
                    player.setJumping(false);
                    helper.assertTrue(player.getDeltaMovement().y < -0.3, "The jump must push away from the ceiling: " + player.getDeltaMovement());
                    helper.assertTrue(player.getBoundingBox().maxY < ceiling - 0.3, "The player should have left the ceiling");
                    helper.assertFalse(player.onGround(), "Mid-jump the player is not grounded");
                })
                .thenWaitUntil(() -> assertStandingOnCeiling(helper, player, ceiling))
                .thenExecute(() -> remove(helper, player))
                .thenSucceed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 300)
    public static void leavingTheCeilingIsSafe(GameTestHelper helper) {
        double ceiling = placeCeiling(helper);
        ServerPlayer player = tickedPlayer(helper, new Vec3(5.5, 3.0, 5.5), true);
        helper.startSequence()
                .thenWaitUntil(() -> assertStandingOnCeiling(helper, player, ceiling))
                .thenExecute(() -> player.setShiftKeyDown(true))
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertFalse(InvertedGravity.isInverted(player), "Sneaking lets go of the ceiling");
                    helper.assertTrue(player.getAttributeValue(Attributes.GRAVITY) > 0.0, "Gravity points down again");
                    helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "Letting go mid-air grants Slow Falling");
                    player.removeEffect(MobEffects.SLOW_FALLING);
                    player.setShiftKeyDown(false);
                })
                .thenWaitUntil(() -> assertStandingOnCeiling(helper, player, ceiling))
                .thenExecute(() -> player.removeEffect(ModEffects.WEIGHTLESS))
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertFalse(InvertedGravity.isInverted(player), "Without Weightless the gravity is restored");
                    helper.assertTrue(player.getEyeHeight() > 1.5F, "The eye returns to its usual height: " + player.getEyeHeight());
                    helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "Losing Weightless on the ceiling grants Slow Falling");
                    remove(helper, player);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 60)
    public static void weightlessMobsAreNotInverted(GameTestHelper helper) {
        placeCeiling(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 3, 5));
        zombie.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, 200));
        helper.runAfterDelay(20, () -> {
            helper.assertFalse(InvertedGravity.isInverted(zombie), "Only players are inverted");
            helper.assertTrue(zombie.getAttributeValue(Attributes.GRAVITY) > 0.0, "Weightless mobs keep a low, downward gravity");
            helper.succeed();
        });
    }
}
