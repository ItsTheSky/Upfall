package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.content.engine.EngineBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.content.rustworks.block.LeakingPipeBlock;
import net.itsthesky.upfall.content.rustworks.block.StillflowCauldronBlock;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class RustworksGameTests {
    private RustworksGameTests() {}

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state) {
        return net.minecraft.world.level.block.Block.getDrops(state, helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), null, null, ItemStack.EMPTY);
    }

    private static int count(List<ItemStack> stacks, net.minecraft.world.item.Item item) {
        return stacks.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static void craft(GameTestHelper helper, int width, int height, List<ItemStack> grid, net.minecraft.world.level.ItemLike result, int count) {
        var level = helper.getLevel();
        var input = CraftingInput.of(width, height, grid);
        ItemStack out = McLevel.serverRecipes(level).getRecipeFor(RecipeType.CRAFTING, input, level)
                //? if >=26.1 {
                /*.map(holder -> holder.value().assemble(input))
                *///?} else {
                .map(holder -> holder.value().assemble(input, level.registryAccess()))
                //?}
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(out.is(result.asItem()) && out.getCount() == count, "Crafting " + result + " gave " + out);
    }

    private static ItemStack s(net.minecraft.world.level.ItemLike item) {
        return new ItemStack(item);
    }

    @UpfallGameTest(template = "empty")
    public static void metalBlockRecipes(GameTestHelper helper) {
        var plate = s(RustworksItems.RUSTY_PLATE.get());
        var none = ItemStack.EMPTY;
        var plating = s(RustworksBlocks.RUSTY_PLATING.get());
        var pipe = s(RustworksBlocks.RUSTY_PIPE.get());
        var dust = s(RustworksItems.KAIRITE_DUST.get());
        craft(helper, 2, 1, List.of(s(Items.COPPER_INGOT), dust), RustworksItems.RUSTY_PLATE.get(), 4);
        craft(helper, 2, 2, List.of(plate, plate, plate, plate), RustworksBlocks.RUSTY_PLATING.get(), 4);
        craft(helper, 2, 2, List.of(plate, plating, plating, plate), RustworksBlocks.COG_PLATING.get(), 4);
        craft(helper, 3, 3, List.of(plate, none, plate, plate, none, plate, plate, none, plate), RustworksBlocks.RUSTY_PIPE.get(), 6);
        craft(helper, 3, 3, List.of(none, plate, none, plate, plate, plate, none, pipe, none), RustworksBlocks.PIPE_VALVE.get(), 1);
        craft(helper, 3, 3, List.of(none, plate, none, plate, plating, plate, none, plate, none), RustworksBlocks.RUSTY_COG.get(), 1);
        craft(helper, 1, 2, List.of(pipe, dust), RustworksBlocks.LEAKING_PIPE.get(), 1);
        craft(helper, 1, 2, List.of(s(RustworksBlocks.PIPE_VALVE.get()), dust), EngineBlocks.DRAIN_VALVE.get(), 1);
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void kairiteOreDropsFollowGlow(GameTestHelper helper) {
        BlockState ore = RustworksBlocks.KAIRITE_ORE.get().defaultBlockState();
        for (int i = 0; i < 20; i++) {
            List<ItemStack> peak = drops(helper, ore.setValue(KairiteOreBlock.GLOW, 2));
            helper.assertTrue(count(peak, RustworksItems.KAIRITE_GEAR.get()) == 1 && count(peak, RustworksItems.KAIRITE_DUST.get()) == 0,
                    "Peak ore must drop exactly one gear: " + peak);
            for (int glow = 0; glow < 2; glow++) {
                List<ItemStack> idle = drops(helper, ore.setValue(KairiteOreBlock.GLOW, glow));
                int dust = count(idle, RustworksItems.KAIRITE_DUST.get());
                helper.assertTrue(dust >= 1 && dust <= 2 && count(idle, RustworksItems.KAIRITE_GEAR.get()) == 0,
                        "Idle ore must drop 1-2 dust: " + idle);
            }
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void kairitePhaseWindow(GameTestHelper helper) {
        int peak = 0;
        for (int phase = 0; phase < KairiteOreBlock.PERIOD; phase++) {
            if (KairiteOreBlock.glowAt(phase) == 2) peak++;
        }
        helper.assertTrue(peak == KairiteOreBlock.PEAK_LENGTH, "Peak window must last " + KairiteOreBlock.PEAK_LENGTH + " ticks, was " + peak);
        BlockPos pos = new BlockPos(5, 70, -9);
        helper.assertTrue(KairiteOreBlock.phase(1000, pos) == KairiteOreBlock.phase(1000 + KairiteOreBlock.PERIOD, pos), "Phase must repeat every period");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 100)
    public static void kairiteOreTicksToCurrentPhase(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, RustworksBlocks.KAIRITE_ORE.get());
        helper.runAfterDelay(5, () -> {
            long now = helper.getLevel().getGameTime();
            BlockState state = helper.getBlockState(pos);
            int expected = KairiteOreBlock.glowAt(KairiteOreBlock.phase(now, helper.absolutePos(pos)));
            helper.assertTrue(state.getValue(KairiteOreBlock.GLOW) == expected
                            || KairiteOreBlock.glowAt(KairiteOreBlock.phase(now - 5, helper.absolutePos(pos))) == state.getValue(KairiteOreBlock.GLOW),
                    "Ore glow " + state.getValue(KairiteOreBlock.GLOW) + " does not match phase glow " + expected);
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty")
    public static void leakingPipeFillsCauldronInSteps(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 8, 1), RustworksBlocks.LEAKING_PIPE.get());
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.CAULDRON);
        BlockPos pipe = helper.absolutePos(new BlockPos(1, 8, 1));
        for (int expected = 1; expected <= 3; expected++) {
            helper.assertTrue(LeakingPipeBlock.tryFillCauldron(helper.getLevel(), pipe), "Drip " + expected + " should fill");
            BlockState state = helper.getBlockState(new BlockPos(1, 2, 1));
            helper.assertTrue(state.is(RustworksBlocks.STILLFLOW_CAULDRON.get()) && state.getValue(LayeredCauldronBlock.LEVEL) == expected,
                    "Expected stillflow level " + expected + ", got " + state);
        }
        helper.assertFalse(LeakingPipeBlock.tryFillCauldron(helper.getLevel(), pipe), "Full cauldron must not accept more");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void leakingPipeBlockedBySolid(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 8, 1), RustworksBlocks.LEAKING_PIPE.get());
        helper.setBlock(new BlockPos(1, 5, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.CAULDRON);
        helper.assertFalse(LeakingPipeBlock.tryFillCauldron(helper.getLevel(), helper.absolutePos(new BlockPos(1, 8, 1))), "Solid block must stop the drip");
        helper.setBlock(new BlockPos(1, 5, 1), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 8, 1), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 14, 1), RustworksBlocks.LEAKING_PIPE.get());
        helper.assertFalse(LeakingPipeBlock.tryFillCauldron(helper.getLevel(), helper.absolutePos(new BlockPos(1, 14, 1))), "Cauldron beyond range must stay empty");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void kairiteDustConvertsFullStillflow(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int level = 1; level <= 3; level++) {
            helper.setBlock(pos, RustworksBlocks.STILLFLOW_CAULDRON.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, level));
            ItemStack dust = new ItemStack(RustworksItems.KAIRITE_DUST.get(), 2);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, dust);
            helper.useBlock(pos, player);
            if (level < 3) {
                helper.assertTrue(dust.getCount() == 2 && helper.getBlockState(pos).is(RustworksBlocks.STILLFLOW_CAULDRON.get()),
                        "Level " + level + " must not convert");
            } else {
                //? if >=26.1 {
                /*helper.assertBlock(pos, b -> b == Blocks.CAULDRON, b -> net.minecraft.network.chat.Component.literal("Full cauldron must become empty"));
                *///?} else {
                helper.assertBlock(pos, b -> b == Blocks.CAULDRON, () -> "Full cauldron must become empty");
                //?}
                helper.assertTrue(dust.getCount() == 1, "One dust must be consumed");
                helper.assertTrue(count(McPlayers.mainItems(player.getInventory()), RustworksItems.KAIRITE_GEAR.get()) == 1, "Player must receive one gear");
            }
        }
        helper.succeed();
    }
}
