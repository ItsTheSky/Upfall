package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFeatures;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class IronbarkGameTests {
    private IronbarkGameTests() {}

    private static void grow(GameTestHelper helper, ResourceKey<ConfiguredFeature<?, ?>> key, int tall) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), RustworksBlocks.OXIDE_TURF.get());
                for (int y = 2; y < 24; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
        BlockPos origin = helper.absolutePos(new BlockPos(3, 2, 3));
        ConfiguredFeature<?, ?> feature = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(key).value();
        helper.assertTrue(feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(7), origin), "Feature did not place");
        int logs = 0;
        int veined = 0;
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, 0, -8), origin.offset(8, tall + 2, 8))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModTags.Blocks.IRONBARK_LOGS)) logs++;
            if (state.is(RustworksBlocks.IRON_VEINED_IRONBARK_LOG.get())) veined++;
            if (state.is(ModBlocks.IRONBARK.leaves().get())) leaves++;
        }
        helper.assertTrue(logs >= 6 && leaves >= 20, "Tree too small: logs=" + logs + " leaves=" + leaves);
        if (tall > 12) {
            helper.assertTrue(veined >= 1, "Expected at least one iron-veined log on large tree");
        }
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void ironbarkTreeGrows(GameTestHelper helper) {
        grow(helper, RustworksFeatures.Configured.IRONBARK_TREE, 12);
    }

    @UpfallGameTest(template = "empty")
    public static void ironbarkLargeTreeGrows(GameTestHelper helper) {
        grow(helper, RustworksFeatures.Configured.IRONBARK_TREE_LARGE, 18);
    }

    @UpfallGameTest(template = "empty")
    public static void ironVeinedLogStripDropsIron(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = helper.absolutePos(rel);
        helper.setBlock(rel, RustworksBlocks.IRON_VEINED_IRONBARK_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        axe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

        BlockState after = helper.getBlockState(rel);
        helper.assertTrue(after.is(ModBlocks.IRONBARK.strippedLog().get()) && after.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.X,
                "Expected stripped ironbark log on X axis, got " + after);
        int nuggets = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(2.0)).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(Items.IRON_NUGGET))
                .mapToInt(ItemStack::getCount)
                .sum();
        helper.assertTrue(nuggets >= 2 && nuggets <= 4, "Expected 2-4 iron nuggets, got " + nuggets);
        helper.succeed();
    }
}
