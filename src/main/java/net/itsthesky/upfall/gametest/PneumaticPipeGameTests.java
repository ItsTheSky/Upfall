package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.pipe.PipeNetwork;
import net.itsthesky.upfall.content.machines.pipe.PipeRoute;
import net.itsthesky.upfall.content.machines.pipe.PipeTravel;
import net.itsthesky.upfall.content.machines.pipe.PneumaticPipeEntranceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class PneumaticPipeGameTests {
    private PneumaticPipeGameTests() {}

    private static void entrance(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing));
    }

    private static void exit(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing));
    }

    private static void pipe(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Block.updateFromNeighbourShapes(MachinesBlocks.PNEUMATIC_PIPE.get().defaultBlockState(),
                helper.getLevel(), helper.absolutePos(pos)));
    }

    private static Optional<PipeRoute> route(GameTestHelper helper, BlockPos entrance) {
        return PipeNetwork.findRoute(helper.getLevel(), helper.absolutePos(entrance), exit -> true);
    }

    @GameTest(template = "empty")
    public static void pneumaticRouteIsShortest(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 4);
        entrance(helper, start, Direction.WEST);
        for (int x = 2; x <= 6; x++) {
            pipe(helper, new BlockPos(x, 1, 4));
        }
        exit(helper, new BlockPos(7, 1, 4), Direction.EAST);
        pipe(helper, new BlockPos(3, 1, 5));
        pipe(helper, new BlockPos(3, 1, 6));
        exit(helper, new BlockPos(3, 1, 7), Direction.SOUTH);

        PipeRoute near = route(helper, start).orElseThrow(() -> new GameTestAssertException("No route found"));
        helper.assertTrue(near.exit().equals(helper.absolutePos(new BlockPos(3, 1, 7))), "Shortest exit should win, got " + near.exit());
        helper.assertTrue(near.length() == 5, "Route length should be 5, got " + near.length());
        helper.assertTrue(near.exitFacing() == Direction.SOUTH, "Exit facing should be south");
        helper.assertBlockProperty(new BlockPos(3, 1, 4), PipeBlock.SOUTH, true);
        helper.assertBlockProperty(new BlockPos(2, 1, 4), PipeBlock.WEST, true);
        helper.assertBlockProperty(new BlockPos(2, 1, 4), PipeBlock.UP, false);

        helper.setBlock(new BlockPos(3, 1, 6), Blocks.AIR);
        PipeRoute far = route(helper, start).orElseThrow(() -> new GameTestAssertException("No route after removing the branch"));
        helper.assertTrue(far.exit().equals(helper.absolutePos(new BlockPos(7, 1, 4))), "Far exit expected, got " + far.exit());
        helper.assertTrue(far.length() == 6, "Route length should be 6, got " + far.length());

        helper.setBlock(new BlockPos(5, 1, 4), Blocks.AIR);
        helper.assertTrue(route(helper, start).isEmpty(), "Broken network should have no route");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pneumaticRouteTieBreakIsDeterministic(GameTestHelper helper) {
        BlockPos start = new BlockPos(4, 1, 1);
        entrance(helper, start, Direction.NORTH);
        pipe(helper, new BlockPos(4, 1, 2));
        pipe(helper, new BlockPos(4, 1, 3));
        exit(helper, new BlockPos(3, 1, 3), Direction.WEST);
        exit(helper, new BlockPos(5, 1, 3), Direction.EAST);
        exit(helper, new BlockPos(4, 2, 3), Direction.UP);
        PipeRoute first = route(helper, start).orElseThrow(() -> new GameTestAssertException("No route found"));
        helper.assertTrue(first.exit().equals(helper.absolutePos(new BlockPos(4, 2, 3))), "Ties resolve in Direction order (up first), got " + first.exit());
        for (int i = 0; i < 5; i++) {
            helper.assertTrue(route(helper, start).orElseThrow().equals(first), "Route must be stable");
        }
        helper.setBlock(new BlockPos(4, 2, 3), Blocks.AIR);
        PipeRoute second = route(helper, start).orElseThrow();
        helper.assertTrue(second.exit().equals(helper.absolutePos(new BlockPos(3, 1, 3))), "West comes before east, got " + second.exit());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pneumaticExitFacingIntoEntranceDoesNotConnect(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 1);
        entrance(helper, start, Direction.UP);
        exit(helper, new BlockPos(2, 1, 1), Direction.WEST);
        helper.assertTrue(route(helper, start).isEmpty(), "An exit's outlet must not connect");
        exit(helper, new BlockPos(2, 1, 1), Direction.EAST);
        helper.assertTrue(route(helper, start).map(r -> r.length() == 1).orElse(false), "Back-to-back entrance and exit connect");
        helper.succeed();
    }

    private static List<BlockPos> snake(int count) {
        List<BlockPos> cells = new ArrayList<>();
        int layer = 0;
        while (cells.size() < count) {
            int y = 1 + layer * 2;
            boolean reverseLayer = layer % 2 == 1;
            for (int row = 0; row < 11 && cells.size() < count; row++) {
                int z = reverseLayer ? 21 - row * 2 : 1 + row * 2;
                boolean forward = (row % 2 == 0) == (layer % 2 == 0);
                for (int i = 0; i < 22 && cells.size() < count; i++) {
                    cells.add(new BlockPos(forward ? 1 + i : 22 - i, y, z));
                }
                if (row < 10 && cells.size() < count) {
                    cells.add(new BlockPos(forward ? 22 : 1, y, reverseLayer ? z - 1 : z + 1));
                }
            }
            if (cells.size() < count) {
                BlockPos last = cells.get(cells.size() - 1);
                cells.add(last.above());
            }
            layer++;
        }
        return cells;
    }

    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void pneumaticRouteIsCapped(GameTestHelper helper) {
        List<BlockPos> cells = snake(700);
        BlockPos start = cells.get(0);
        entrance(helper, start, Direction.DOWN);
        for (int i = 1; i < 400; i++) {
            pipe(helper, cells.get(i));
        }
        exit(helper, cells.get(400), Direction.UP);
        PipeRoute reachable = route(helper, start).orElseThrow(() -> new GameTestAssertException("A 400-segment route should be found"));
        helper.assertTrue(reachable.length() == 400, "Snake route length should be 400, got " + reachable.length());
        for (int i = 400; i < 650; i++) {
            pipe(helper, cells.get(i));
        }
        exit(helper, cells.get(650), Direction.UP);
        helper.assertTrue(route(helper, start).isEmpty(), "Routes longer than the cap must be ignored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pneumaticItemsReachChest(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 1);
        entrance(helper, start, Direction.UP);
        pipe(helper, new BlockPos(2, 1, 1));
        pipe(helper, new BlockPos(3, 1, 1));
        exit(helper, new BlockPos(4, 1, 1), Direction.UP);
        helper.setBlock(new BlockPos(4, 2, 1), Blocks.CHEST);
        helper.spawnItem(Items.DIAMOND, new BlockPos(1, 2, 1));
        helper.succeedWhen(() -> {
            Container chest = (Container) helper.getBlockEntity(new BlockPos(4, 2, 1));
            helper.assertTrue(chest.countItem(Items.DIAMOND) == 1, "Diamond should arrive in the chest");
            helper.assertItemEntityNotPresent(Items.DIAMOND);
        });
    }

    @GameTest(template = "empty")
    public static void pneumaticItemsPopOutWithoutContainer(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 1);
        entrance(helper, start, Direction.UP);
        pipe(helper, new BlockPos(2, 1, 1));
        pipe(helper, new BlockPos(3, 1, 1));
        exit(helper, new BlockPos(4, 1, 1), Direction.EAST);
        IItemHandler inlet = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(start), Direction.UP);
        helper.assertTrue(inlet != null, "Entrance must expose an item handler");
        helper.assertTrue(inlet.insertItem(0, new ItemStack(Items.COBBLESTONE, 16), true).isEmpty(), "Simulated insert should be accepted");
        PneumaticPipeEntranceBlockEntity entrance = helper.getBlockEntity(start);
        helper.assertTrue(entrance.inTransit() == 0, "Simulation must not enqueue");
        helper.assertTrue(inlet.insertItem(0, new ItemStack(Items.COBBLESTONE, 16), false).isEmpty(), "Insert should be accepted");
        helper.assertTrue(entrance.inTransit() == 1, "Item should be in transit");
        helper.succeedWhen(() -> {
            helper.assertItemEntityCountIs(Items.COBBLESTONE, new BlockPos(5, 1, 1), 1.5, 16);
            helper.assertTrue(entrance.inTransit() == 0, "Transit queue should be empty");
        });
    }

    @GameTest(template = "empty")
    public static void pneumaticRejectsItemsWithoutRoute(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 1);
        entrance(helper, start, Direction.UP);
        pipe(helper, new BlockPos(2, 1, 1));
        IItemHandler inlet = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(start), Direction.UP);
        helper.assertTrue(inlet != null, "Entrance must expose an item handler");
        ItemStack rest = inlet.insertItem(0, new ItemStack(Items.COBBLESTONE, 4), false);
        helper.assertTrue(rest.getCount() == 4, "Without an exit the entrance must refuse items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pneumaticHopperFeedsEntrance(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 1, 1);
        entrance(helper, start, Direction.NORTH);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        pipe(helper, new BlockPos(1, 1, 2));
        pipe(helper, new BlockPos(1, 1, 3));
        exit(helper, new BlockPos(1, 1, 4), Direction.SOUTH);
        helper.setBlock(new BlockPos(1, 1, 5), Blocks.BARREL);
        HopperBlockEntity hopper = helper.getBlockEntity(new BlockPos(1, 2, 1));
        hopper.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        helper.succeedWhen(() -> {
            Container barrel = (Container) helper.getBlockEntity(new BlockPos(1, 1, 5));
            helper.assertTrue(barrel.countItem(Items.IRON_INGOT) == 3, "All ingots should reach the barrel, got " + barrel.countItem(Items.IRON_INGOT));
        });
    }

    @SuppressWarnings("removal")
    private static ServerPlayer rider(GameTestHelper helper, Vec3 at) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(at));
        player.setShiftKeyDown(true);
        helper.onEachTick(() -> {
            if (!player.isRemoved()) player.doTick();
        });
        return player;
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void pneumaticCarriesPlayerToExit(GameTestHelper helper) {
        entrance(helper, new BlockPos(1, 1, 1), Direction.UP);
        for (int x = 2; x <= 4; x++) {
            pipe(helper, new BlockPos(x, 1, 1));
        }
        exit(helper, new BlockPos(5, 1, 1), Direction.UP);
        ServerPlayer player = rider(helper, new Vec3(1.5, 2.0, 1.5));
        float health = player.getHealth();
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(PipeTravel.isTraveling(player), "Sneaking on the entrance should start the travel");
            helper.assertTrue(player.noPhysics && player.getForcedPose() == Pose.SWIMMING, "Traveler must be noclip and crawling");
            helper.succeedWhen(() -> {
                helper.assertFalse(PipeTravel.isTraveling(player), "Travel should end");
                Vec3 eject = helper.absoluteVec(new Vec3(5.5, 2.0, 1.5));
                helper.assertTrue(player.position().distanceTo(eject) < 0.6, "Player should be ejected above the exit, at " + player.position());
                helper.assertTrue(!player.noPhysics && player.getForcedPose() == null, "Travel state must be cleared");
                helper.assertTrue(player.getHealth() == health, "Travel must not hurt");
                player.discard();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void pneumaticBrokenPipeEjectsPlayer(GameTestHelper helper) {
        entrance(helper, new BlockPos(1, 1, 1), Direction.UP);
        for (int x = 2; x <= 6; x++) {
            pipe(helper, new BlockPos(x, 1, 1));
        }
        exit(helper, new BlockPos(7, 1, 1), Direction.UP);
        ServerPlayer player = rider(helper, new Vec3(1.5, 2.0, 1.5));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(PipeTravel.isTraveling(player), "Travel should have started");
            helper.setBlock(new BlockPos(4, 1, 1), Blocks.AIR);
            helper.succeedWhen(() -> {
                helper.assertFalse(PipeTravel.isTraveling(player), "Breaking the network must stop the travel");
                helper.assertTrue(player.getForcedPose() == null && !player.noPhysics, "Travel state must be cleared");
                helper.assertTrue(helper.getLevel().noCollision(player, player.getBoundingBox()), "Player must be put in a free spot");
                player.discard();
            });
        });
    }
}
