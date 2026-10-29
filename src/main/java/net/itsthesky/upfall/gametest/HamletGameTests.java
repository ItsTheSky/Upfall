package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.HamletEntities;
import net.itsthesky.upfall.content.hamlet.HamletItems;
import net.itsthesky.upfall.content.hamlet.HamletLoot;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.itsthesky.upfall.content.hamlet.world.HamletLayout;
import net.itsthesky.upfall.content.hamlet.world.HamletPieces;
import net.itsthesky.upfall.gametest.framework.GameTestCompat;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HamletGameTests {
    private HamletGameTests() {}

    private static BlockPos rel(GameTestHelper helper, BlockPos absolute) {
        return absolute.subtract(helper.absolutePos(BlockPos.ZERO));
    }

    @UpfallGameTest(template = "empty")
    public static void ropeIsClimbableAndHangs(GameTestHelper helper) {
        BlockPos beam = new BlockPos(3, 6, 3);
        helper.setBlock(beam, HamletBlocks.WOVEN_PLANKS.get());
        for (int y = 5; y >= 2; y--) {
            helper.setBlock(new BlockPos(3, y, 3), HamletBlocks.ROPE.get().defaultBlockState().setValue(RopeBlock.END, y == 2));
        }
        BlockState rope = helper.getBlockState(new BlockPos(3, 3, 3));
        helper.assertTrue(rope.is(BlockTags.CLIMBABLE), "Rope is not climbable");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 inRope = helper.absoluteVec(new Vec3(3.5, 3.0, 3.5));
        player.setPos(inRope.x, inRope.y, inRope.z);
        helper.assertTrue(player.onClimbable(), "A player inside the rope cannot climb it");
        helper.assertTrue(RopeBlock.isSupported(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 3))), "Rope segment unsupported");
        helper.setBlock(beam, Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            for (int y = 2; y <= 5; y++) {
                helper.assertBlockNotPresent(HamletBlocks.ROPE.get(), new BlockPos(3, y, 3));
            }
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty")
    public static void ropeTiedToSideWallSurvives(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 4, 3), HamletBlocks.WOVEN_PLANKS.get());
        helper.setBlock(new BlockPos(3, 4, 3), HamletBlocks.ROPE.get().defaultBlockState().setValue(RopeBlock.END, false));
        helper.setBlock(new BlockPos(3, 3, 3), HamletBlocks.ROPE.get().defaultBlockState());
        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(HamletBlocks.ROPE.get(), new BlockPos(3, 3, 3));
            helper.succeed();
        });
    }

    private static BlockPos[] buildZipline(GameTestHelper helper) {
        for (int x = 0; x <= 4; x++) {
            for (int z = 9; z <= 13; z++) {
                helper.setBlock(new BlockPos(x, 8, z), HamletBlocks.WOVEN_PLANKS.get());
                helper.setBlock(new BlockPos(x + 19, 3, z), HamletBlocks.WOVEN_PLANKS.get());
            }
        }
        BlockState log = ModBlocks.SKYREACH.log().get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int y = 9; y <= 11; y++) helper.setBlock(new BlockPos(3, y, 11), log);
        for (int y = 4; y <= 5; y++) helper.setBlock(new BlockPos(20, y, 11), log);
        BlockPos a = new BlockPos(3, 12, 11);
        BlockPos b = new BlockPos(20, 6, 11);
        helper.setBlock(a, HamletBlocks.ZIPLINE_ANCHOR.get().defaultBlockState().setValue(ZiplineAnchorBlock.FACING, Direction.EAST));
        helper.setBlock(b, HamletBlocks.ZIPLINE_ANCHOR.get().defaultBlockState().setValue(ZiplineAnchorBlock.FACING, Direction.WEST));
        RopeLines.LinkResult result = RopeLines.link(helper.getLevel(), helper.absolutePos(a), helper.absolutePos(b));
        helper.assertTrue(result == RopeLines.LinkResult.LINKED, "Zipline not linked: " + result);
        return new BlockPos[]{a, b};
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 300)
    public static void ziplineCarriesRiderToTheOtherPlatform(GameTestHelper helper) {
        BlockPos[] line = buildZipline(helper);
        ArmorStand rider = helper.spawn(EntityType.ARMOR_STAND, new Vec3(2.5, 9.0, 11.5));
        ZiplinePulley.Result result = ZiplinePulley.start(rider, helper.absolutePos(line[0]), helper.absolutePos(line[1]), 0.0);
        helper.assertTrue(result == ZiplinePulley.Result.STARTED, "Ride did not start: " + result);
        helper.assertTrue(rider.getVehicle() instanceof ZiplinePulley, "Rider is not on a pulley");
        double startY = rider.getY();
        helper.runAfterDelay(10, () -> helper.assertTrue(rider.isPassenger() && rider.getY() < startY, "Rider is not sliding down"));
        helper.succeedWhen(() -> {
            helper.assertFalse(rider.isPassenger(), "Still riding");
            BlockPos landing = rel(helper, rider.blockPosition());
            helper.assertTrue(landing.getX() >= 18 && landing.getY() == 4, "Rider did not land on the far platform: " + landing);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ZiplinePulley.class, helper.getBounds()).isEmpty(),
                    "Pulley left behind");
        });
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 300)
    public static void ziplineStopsBeforeAnObstacle(GameTestHelper helper) {
        BlockPos[] line = buildZipline(helper);
        for (int y = 3; y <= 9; y++) {
            for (int z = 10; z <= 12; z++) {
                helper.setBlock(new BlockPos(12, y, z), HamletBlocks.WOVEN_PLANKS.get());
            }
        }
        ArmorStand rider = helper.spawn(EntityType.ARMOR_STAND, new Vec3(2.5, 9.0, 11.5));
        helper.assertTrue(ZiplinePulley.start(rider, helper.absolutePos(line[0]), helper.absolutePos(line[1]), 0.0)
                == ZiplinePulley.Result.STARTED, "Ride did not start");
        helper.succeedWhen(() -> {
            helper.assertFalse(rider.isPassenger(), "Still riding");
            double x = rider.getX() - helper.absolutePos(BlockPos.ZERO).getX();
            helper.assertTrue(x < 12.0, "Rider passed through the wall: x=" + x);
        });
    }

    @UpfallGameTest(template = "empty_large")
    public static void ziplineStartsFromAnchorAndLettingGoDrops(GameTestHelper helper) {
        BlockPos[] line = buildZipline(helper);
        FakePlayer fake = FakePlayerFactory.getMinecraft(helper.getLevel());
        Vec3 stand = helper.absoluteVec(new Vec3(2.5, 9.0, 11.5));
        McEntities.snapTo(fake, stand.x, stand.y, stand.z, 0.0F, 0.0F);
        helper.assertTrue(ZiplinePulley.rideFromAnchor(fake, helper.absolutePos(line[0])) != ZiplinePulley.Result.STARTED, "A fake player rode");

        ArmorStand rider = helper.spawn(EntityType.ARMOR_STAND, new Vec3(2.5, 9.0, 11.5));
        helper.assertTrue(ZiplinePulley.rideFromAnchor(rider, helper.absolutePos(new BlockPos(20, 6, 11))) == ZiplinePulley.Result.TOO_FAR,
                "Rode from an anchor out of reach");
        ZiplinePulley.Result result = ZiplinePulley.rideFromAnchor(rider, helper.absolutePos(line[0]));
        helper.assertTrue(result == ZiplinePulley.Result.STARTED, "Ride from the anchor did not start: " + result);
        helper.assertTrue(ZiplinePulley.rideFromAnchor(rider, helper.absolutePos(line[0])) == ZiplinePulley.Result.BUSY, "Could ride twice");
        helper.runAfterDelay(8, () -> {
            double hanging = rider.getY();
            rider.stopRiding();
            helper.assertFalse(rider.isPassenger(), "Could not let go");
            helper.assertTrue(Math.abs(rider.getY() - hanging) < 0.01, "Letting go moved the rider: " + hanging + " -> " + rider.getY());
        });
        helper.runAfterDelay(12, () -> {
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ZiplinePulley.class, helper.getBounds()).isEmpty(),
                    "Pulley left behind");
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty_large")
    public static void breakingAnAnchorUntiesTheLine(GameTestHelper helper) {
        BlockPos[] line = buildZipline(helper);
        helper.destroyBlock(line[1]);
        RopeAnchorBlockEntity anchor = GameTestCompat.blockEntity(helper, line[0], RopeAnchorBlockEntity.class);
        helper.assertTrue(anchor.links().isEmpty(), "The other end still holds the line");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty_large")
    public static void ropeAnchorsTieLines(GameTestHelper helper) {
        BlockState peg = HamletBlocks.ROPE_ANCHOR.get().defaultBlockState();
        BlockPos[] pegs = {new BlockPos(2, 2, 2), new BlockPos(12, 2, 2), new BlockPos(2, 2, 12), new BlockPos(12, 2, 12), new BlockPos(20, 2, 20)};
        for (BlockPos pos : pegs) {
            helper.setBlock(pos.below(), HamletBlocks.WOVEN_PLANKS.get());
            helper.setBlock(pos, peg);
        }
        var level = helper.getLevel();
        helper.assertTrue(RopeLines.link(level, helper.absolutePos(pegs[0]), helper.absolutePos(pegs[1])) == RopeLines.LinkResult.LINKED, "Rope line not tied");
        RopeAnchorBlockEntity end = GameTestCompat.blockEntity(helper, pegs[1], RopeAnchorBlockEntity.class);
        helper.assertTrue(end.isLinkedTo(helper.absolutePos(pegs[0])), "Line not tied on both ends");
        helper.setBlock(new BlockPos(20, 1, 2), HamletBlocks.WOVEN_PLANKS.get());
        helper.setBlock(new BlockPos(20, 2, 2), HamletBlocks.ZIPLINE_ANCHOR.get());
        helper.assertTrue(RopeLines.link(level, helper.absolutePos(pegs[1]), helper.absolutePos(new BlockPos(20, 2, 2))) == RopeLines.LinkResult.MISMATCH,
                "A rope line was tied to a zipline anchor");
        helper.assertTrue(RopeLines.link(level, helper.absolutePos(pegs[0]), helper.absolutePos(pegs[0])) == RopeLines.LinkResult.SAME, "Tied to itself");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void loftkinBartersUpfallGoods(GameTestHelper helper) {
        Loftkin loftkin = helper.spawn(HamletEntities.LOFTKIN.get(), new BlockPos(3, 1, 3));
        List<MerchantOffer> offers = loftkin.getOffers();
        helper.assertTrue(offers.size() >= 6, "Too few offers: " + offers.size());
        helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(HamletItems.ZIPLINE_HANDLE.get())), "No Zipline Handle offer");
        helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get())), "No Nimbusite template offer");
        for (MerchantOffer offer : offers) {
            String costNamespace = BuiltInRegistries.ITEM.getKey(offer.getBaseCostA().getItem()).getNamespace();
            helper.assertTrue(costNamespace.equals(Upfall.MODID), "Offer asks for a non-Upfall good: " + offer.getBaseCostA());
            helper.assertTrue(offer.satisfiedBy(offer.getCostA().copy(), offer.getCostB().copy()), "Offer cannot be paid with its own cost");
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty_large", timeoutTicks = 500)
    public static void loftkinStaysOnItsPlatform(GameTestHelper helper) {
        for (int x = 8; x <= 14; x++) {
            for (int z = 8; z <= 14; z++) {
                helper.setBlock(new BlockPos(x, 6, z), HamletBlocks.WOVEN_PLANKS.get());
            }
        }
        Loftkin loftkin = helper.spawn(HamletEntities.LOFTKIN.get(), new BlockPos(11, 7, 11));
        loftkin.setHome(helper.absolutePos(new BlockPos(11, 7, 11)));
        helper.runAfterDelay(400, () -> {
            BlockPos at = rel(helper, loftkin.blockPosition());
            helper.assertTrue(at.getY() == 7 && at.getX() >= 8 && at.getX() <= 14 && at.getZ() >= 8 && at.getZ() <= 14,
                    "Loftkin left its platform: " + at);
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty_large")
    public static void fallingLoftkinOpensParasol(GameTestHelper helper) {
        Loftkin loftkin = helper.spawn(HamletEntities.LOFTKIN.get(), new BlockPos(11, 14, 11));
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(loftkin.hasParasol(), "No parasol while falling");
            helper.assertTrue(loftkin.getDeltaMovement().y > -0.2, "Falling too fast: " + loftkin.getDeltaMovement().y);
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty")
    public static void lostLoftkinReturnsHome(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), HamletBlocks.WOVEN_PLANKS.get());
        Loftkin loftkin = helper.spawn(HamletEntities.LOFTKIN.get(), new BlockPos(3, 2, 3));
        loftkin.setHome(helper.absolutePos(new BlockPos(3, 2, 3)));
        loftkin.teleportTo(loftkin.getX(), loftkin.getY() - 40, loftkin.getZ());
        loftkin.returnHome();
        helper.assertTrue(rel(helper, loftkin.blockPosition()).equals(new BlockPos(3, 2, 3)), "Loftkin not back home");
        helper.succeed();
    }

    private static List<ItemStack> roll(GameTestHelper helper, ResourceKey<LootTable> key) {
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        return table.getRandomItems(new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO))).create(LootContextParamSets.CHEST));
    }

    @UpfallGameTest(template = "empty")
    public static void hamletCachesHoldLoot(GameTestHelper helper) {
        for (int i = 0; i < 30; i++) {
            helper.assertFalse(roll(helper, HamletLoot.HOME).isEmpty(), "Empty home loot");
            List<ItemStack> workshop = roll(helper, HamletLoot.WORKSHOP);
            helper.assertTrue(workshop.stream().anyMatch(s -> s.is(HamletItems.ZIPLINE_HANDLE.get())), "Workshop without a Zipline Handle: " + workshop);
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 400)
    public static void hamletLayoutsAreConnectedAndRideable(GameTestHelper helper) {
        for (long seed = 0; seed < 40; seed++) {
            HamletLayout.Plan plan = HamletLayout.plan(RandomSource.create(seed), new BlockPos(0, 70, 0));
            List<HamletLayout.Platform> platforms = plan.platforms();
            helper.assertTrue(platforms.size() >= 4, "Seed " + seed + ": only " + platforms.size() + " platforms");
            for (HamletLayout.Kind kind : new HamletLayout.Kind[]{HamletLayout.Kind.MARKET, HamletLayout.Kind.WORKSHOP, HamletLayout.Kind.HOME}) {
                helper.assertTrue(platforms.stream().anyMatch(p -> p.kind == kind), "Seed " + seed + ": no " + kind);
            }
            Set<Integer> reached = new HashSet<>(Set.of(0));
            ArrayDeque<Integer> queue = new ArrayDeque<>(List.of(0));
            while (!queue.isEmpty()) {
                int at = queue.poll();
                for (HamletLayout.Bridge b : plan.bridges()) {
                    if (b.from() == at && reached.add(b.to())) queue.add(b.to());
                }
                for (HamletLayout.Zipline z : plan.ziplines()) {
                    if (z.from() == at && reached.add(z.to())) queue.add(z.to());
                    if (z.to() == at && reached.add(z.from())) queue.add(z.from());
                }
            }
            helper.assertTrue(reached.size() == platforms.size(), "Seed " + seed + ": unreachable platforms");

            HamletPieces.Preview preview = HamletPieces.preview(plan, seed);
            helper.assertTrue(preview.inhabitants() >= 3, "Seed " + seed + ": only " + preview.inhabitants() + " Loftkin");
            helper.assertTrue(preview.caches() >= 1, "Seed " + seed + ": no Sky Cache");
            for (HamletLayout.Zipline zipline : plan.ziplines()) {
                checkRide(helper, preview.blocks(), zipline.anchorFrom(), zipline.anchorTo(), seed);
                checkRide(helper, preview.blocks(), zipline.anchorTo(), zipline.anchorFrom(), seed);
            }
            checkBridges(helper, preview.blocks(), seed);
        }
        helper.succeed();
    }

    private static void checkRide(GameTestHelper helper, Map<BlockPos, BlockState> blocks, BlockPos from, BlockPos to, long seed) {
        Vec3 a = ZiplinePulley.attach(from);
        Vec3 b = ZiplinePulley.attach(to);
        double length = a.distanceTo(b);
        helper.assertTrue(length <= 64.0, "Seed " + seed + ": zipline too long " + length);
        double sag = RopeLines.sag(a, b, true);
        double margin = ZiplinePulley.endMargin(a, b);
        for (double d = margin; d <= length - margin; d += 0.25) {
            Vec3 grip = RopeLines.point(a, b, sag, d / length);
            AABB box = new AABB(grip.x - 0.3, grip.y - 2.1, grip.z - 0.3, grip.x + 0.3, grip.y, grip.z + 0.3).deflate(0.01);
            for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
                BlockState state = blocks.get(pos);
                if (state == null || state.isAir()) continue;
                VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (!shape.isEmpty() && shape.bounds().move(pos).intersects(box)) {
                    helper.fail("Seed " + seed + ": zipline " + from.toShortString() + " -> " + to.toShortString() + " hits " + state + " at "
                            + pos.toShortString() + " (" + String.format("%.1f", d) + "/" + String.format("%.1f", length) + ")");
                }
            }
        }
    }

    private static void checkBridges(GameTestHelper helper, Map<BlockPos, BlockState> blocks, long seed) {
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            if (!entry.getValue().is(HamletBlocks.ROPE_BRIDGE.get())) continue;
            BlockPos pos = entry.getKey();
            double surface = pos.getY() + entry.getValue().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).max(Direction.Axis.Y);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockState next = blocks.get(pos.relative(side).above(dy));
                    if (next == null || !next.is(HamletBlocks.ROPE_BRIDGE.get())) continue;
                    double other = pos.getY() + dy + next.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).max(Direction.Axis.Y);
                    helper.assertTrue(Math.abs(other - surface) <= 0.6, "Seed " + seed + ": bridge step too high at " + pos.toShortString());
                }
            }
        }
    }

    @UpfallGameTest(template = "empty")
    public static void loftkinSpawnsPersistentFromStructure(GameTestHelper helper) {
        //? if >=26.1 {
        /*Loftkin loftkin = HamletEntities.LOFTKIN.get().create(helper.getLevel(), MobSpawnType.STRUCTURE);
        *///?} else {
        Loftkin loftkin = HamletEntities.LOFTKIN.get().create(helper.getLevel());
        //?}
        helper.assertTrue(loftkin != null, "Cannot create a Loftkin");
        Vec3 pos = helper.absoluteVec(new Vec3(3.5, 1.0, 3.5));
        McEntities.snapTo(loftkin, pos.x, pos.y, pos.z);
        loftkin.setHome(BlockPos.containing(pos));
        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(loftkin, helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.containing(pos)),
                MobSpawnType.STRUCTURE, null);
        loftkin.setPersistenceRequired();
        helper.getLevel().addFreshEntity(loftkin);
        helper.assertTrue(loftkin.isPersistenceRequired() && !loftkin.removeWhenFarAway(1000.0), "Loftkin would despawn");
        helper.assertTrue(BlockPos.containing(pos).equals(loftkin.getHome()), "Loftkin lost its home");
        helper.succeed();
    }
}
