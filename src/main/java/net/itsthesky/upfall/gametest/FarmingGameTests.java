package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.content.farming.FarmingBlocks;
import net.itsthesky.upfall.content.ores.OresBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class FarmingGameTests {
    private FarmingGameTests() {}

    private static void use(GameTestHelper helper, Player player, ItemStack stack, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos clicked = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false);
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    @UpfallGameTest(template = "empty")
    public static void hoeTillsSkyGround(GameTestHelper helper) {
        BlockPos grass = new BlockPos(2, 1, 2);
        BlockPos soil = new BlockPos(4, 1, 2);
        BlockPos hanging = new BlockPos(6, 1, 2);
        helper.setBlock(grass, ModBlocks.SKYGRASS_BLOCK.get());
        helper.setBlock(soil, ModBlocks.SKYSOIL.get());
        helper.setBlock(hanging, ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState().setValue(SkygrassBlock.INVERTED, true));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (BlockPos pos : List.of(grass, soil, hanging)) {
            use(helper, player, new ItemStack(Items.IRON_HOE), pos);
        }
        helper.assertBlockPresent(Blocks.FARMLAND, grass);
        helper.assertBlockPresent(Blocks.FARMLAND, soil);
        helper.assertBlockPresent(ModBlocks.SKYGRASS_BLOCK.get(), hanging);
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void cloudberrySeedsGrowCloudberries(GameTestHelper helper) {
        BlockPos farmland = new BlockPos(2, 1, 2);
        helper.setBlock(farmland, Blocks.FARMLAND);
        use(helper, helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(FarmingBlocks.CLOUDBERRY_SEEDS.get()), farmland);
        BlockPos cropPos = farmland.above();
        helper.assertBlockPresent(FarmingBlocks.CLOUDBERRY_CROP.get(), cropPos);

        BlockState mature = FarmingBlocks.CLOUDBERRY_CROP.get().defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE);
        helper.setBlock(cropPos, mature);
        List<ItemStack> drops = Block.getDrops(mature, helper.getLevel(), helper.absolutePos(cropPos), null);
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(ModItems.CLOUDBERRY.get())), "A mature bush drops cloudberries: " + drops);
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(FarmingBlocks.CLOUDBERRY_SEEDS.get())), "A mature bush drops seeds: " + drops);
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void upfallOresDropVanillaResources(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        checkOre(helper, player, pickaxe, pos, OresBlocks.PEARLSTONE_COAL_ORE.get(), Items.COAL);
        checkOre(helper, player, pickaxe, pos, OresBlocks.RUSTSLATE_COAL_ORE.get(), Items.COAL);
        checkOre(helper, player, pickaxe, pos, OresBlocks.PEARLSTONE_IRON_ORE.get(), Items.RAW_IRON);
        checkOre(helper, player, pickaxe, pos, OresBlocks.RUSTSLATE_IRON_ORE.get(), Items.RAW_IRON);
        checkOre(helper, player, pickaxe, pos, OresBlocks.PEARLSTONE_DIAMOND_ORE.get(), Items.DIAMOND);
        checkOre(helper, player, pickaxe, pos, OresBlocks.RUSTSLATE_DIAMOND_ORE.get(), Items.DIAMOND);
        helper.assertTrue(!new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(OresBlocks.PEARLSTONE_DIAMOND_ORE.get().defaultBlockState()),
                "Diamond ore needs an iron pickaxe");
        helper.assertTrue(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(OresBlocks.PEARLSTONE_IRON_ORE.get().defaultBlockState()),
                "Iron ore can be mined with a stone pickaxe");
        helper.succeed();
    }

    private static void checkOre(GameTestHelper helper, Player player, ItemStack tool, BlockPos pos, Block ore, Item expected) {
        helper.setBlock(pos, ore);
        helper.assertTrue(tool.isCorrectToolForDrops(ore.defaultBlockState()), "A diamond pickaxe mines " + ore);
        List<ItemStack> drops = Block.getDrops(ore.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos), null, player, tool);
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(expected)), ore + " drops " + expected + ": " + drops);
    }

    @UpfallGameTest(template = "empty")
    public static void cobbledStonesCraftStoneTools(GameTestHelper helper) {
        for (Block cobbled : List.of(ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.COBBLED_DUSKSLATE.get(), RustworksBlocks.COBBLED_RUSTSLATE.get())) {
            ItemStack stack = new ItemStack(cobbled);
            helper.assertTrue(stack.is(ItemTags.STONE_TOOL_MATERIALS), cobbled + " is a stone tool material");
            helper.assertTrue(stack.is(ItemTags.STONE_CRAFTING_MATERIALS), cobbled + " is a stone crafting material");
        }
        helper.succeed();
    }
}
