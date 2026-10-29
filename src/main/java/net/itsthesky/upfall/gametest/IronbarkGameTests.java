package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFeatures;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

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
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, 0, -8), origin.offset(8, tall + 2, 8))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.IRONBARK.log().get())) logs++;
            if (state.is(ModBlocks.IRONBARK.leaves().get())) leaves++;
        }
        helper.assertTrue(logs >= 6 && leaves >= 20, "Tree too small: logs=" + logs + " leaves=" + leaves);
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
}
