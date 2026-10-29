package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.content.world.WorldFeatures;
import net.itsthesky.upfall.content.world.feature.SkyLakeFeature;
import net.itsthesky.upfall.content.world.item.NimbusFlaskItem;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Optional;

public final class WorldGameTests {
    private WorldGameTests() {}

    @UpfallGameTest(template = "empty_large", timeoutTicks = 200)
    public static void lakesNeverSpill(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SkyLakeFeature.Config config = new SkyLakeFeature.Config(4, 7, 3);
        int placed = 0;
        for (int seed = 0; seed < 24; seed++) {
            buildPlatform(helper);
            BlockPos origin = helper.absolutePos(new BlockPos(13, 9, 13));
            boolean ok = WorldFeatures.SKY_LAKE.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                    level.getChunkSource().getGenerator(), RandomSource.create(seed), origin, config));
            if (ok) placed++;
            for (int x = 0; x < 24; x++) {
                for (int z = 0; z < 24; z++) {
                    for (int y = 1; y < 15; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!helper.getBlockState(pos).is(ModFluids.SKYWATER_BLOCK.get())) continue;
                        helper.assertTrue(contains(helper, pos.below()), "Skywater over an open floor at " + pos + " (seed " + seed + ")");
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            helper.assertTrue(contains(helper, pos.relative(dir)), "Skywater spills " + dir + " at " + pos + " (seed " + seed + ")");
                        }
                    }
                }
            }
        }
        helper.assertTrue(placed >= 8, "Too few lakes could be placed: " + placed);
        helper.succeed();
    }

    private static boolean contains(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        return state.is(ModFluids.SKYWATER_BLOCK.get()) || state.isSolid() && state.getFluidState().isEmpty();
    }

    private static void buildPlatform(GameTestHelper helper) {
        for (int x = 0; x < 24; x++) {
            for (int z = 0; z < 24; z++) {
                for (int y = 1; y < 15; y++) {
                    BlockState state = Blocks.AIR.defaultBlockState();
                    boolean inside = x >= 2 && x < 22 && z >= 2 && z < 22 && !(x >= 16 && z >= 15) && !(x == 11 && z == 15);
                    if (inside && y >= 6 && y <= 8) {
                        state = (y == 8 ? ModBlocks.SKYGRASS_BLOCK.get() : y == 7 ? ModBlocks.SKYSOIL.get() : ModBlocks.PEARLSTONE.get()).defaultBlockState();
                    }
                    helper.getLevel().setBlock(helper.absolutePos(new BlockPos(x, y, z)), state, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @UpfallGameTest(template = "empty")
    public static void nimbusFlaskCondensesFadingCloud(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 3, 4);
        int placed = NimbusFlaskItem.condense(helper.getLevel(), helper.absolutePos(center));
        helper.assertTrue(placed == 21, "A flask should condense a 21-block disc, got " + placed);
        helper.assertBlockPresent(WorldBlocks.FADING_CLOUD.get(), center.offset(2, 0, 0));
        helper.assertBlockNotPresent(WorldBlocks.FADING_CLOUD.get(), center.offset(2, 0, 2));
        helper.assertTrue(helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(center), WorldBlocks.FADING_CLOUD.get()),
                "Fading cloud must schedule its own evaporation");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void cloudstalkNeedsCloud(GameTestHelper helper) {
        BlockPos soil = new BlockPos(3, 1, 3);
        helper.setBlock(soil, ModBlocks.DENSE_CLOUD.get());
        helper.setBlock(soil.above(), WorldBlocks.CLOUDSTALK.get());
        helper.setBlock(soil.above(2), WorldBlocks.CLOUDSTALK.get());
        helper.setBlock(soil.above(3), WorldBlocks.CLOUDBLOOM.get());
        helper.assertBlockPresent(WorldBlocks.CLOUDBLOOM.get(), soil.above(3));
        helper.setBlock(soil, Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(WorldBlocks.CLOUDSTALK.get(), soil.above());
            helper.assertBlockNotPresent(WorldBlocks.CLOUDBLOOM.get(), soil.above(3));
        });
    }

    @UpfallGameTest(template = "empty")
    public static void skylilyFloatsOnSkywaterOnly(GameTestHelper helper) {
        BlockPos skywater = new BlockPos(2, 1, 2);
        BlockPos water = new BlockPos(5, 1, 5);
        helper.setBlock(skywater, ModFluids.SKYWATER_BLOCK.get());
        helper.setBlock(water, Blocks.WATER);
        BlockState lily = WorldBlocks.SKYLILY.get().defaultBlockState();
        helper.assertTrue(lily.canSurvive(helper.getLevel(), helper.absolutePos(skywater.above())), "Skylily must float on Skywater");
        helper.assertFalse(lily.canSurvive(helper.getLevel(), helper.absolutePos(water.above())), "Skylily must not float on water");
        helper.succeed();
    }
}
