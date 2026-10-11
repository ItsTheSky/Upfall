package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksLoot;
import net.itsthesky.upfall.content.rustworks.block.ExhaustVentBlock;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFeatures;
import net.itsthesky.upfall.content.rustworks.worldgen.StalledAutomatonFeature;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class RustworksAutomatonGameTests {
    private RustworksAutomatonGameTests() {}

    private static final int GROUND = 4;

    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void stalledAutomatonPosesStandAndHideTheirCache(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(12, GROUND + 1, 12);
        int seed = 0;
        for (StalledAutomatonFeature.Pose pose : StalledAutomatonFeature.Pose.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                platform(helper);
                BlockPos cache = StalledAutomatonFeature.build(level, RandomSource.create(seed++), helper.absolutePos(origin), facing, pose, true);
                helper.assertTrue(cache != null, "No cache in " + pose + " " + facing);
                helper.assertTrue(level.getBlockState(cache).is(ModBlocks.SKY_CACHE.get()), "Cache block missing in " + pose);
                helper.assertTrue(level.getBlockEntity(cache) instanceof RandomizableContainerBlockEntity box
                        && RustworksLoot.AUTOMATON.equals(box.getLootTable()), "Cache loot table missing in " + pose);
                for (Direction dir : Direction.values()) {
                    helper.assertTrue(!level.getBlockState(cache.relative(dir)).isAir(), "Cache exposed to air (" + dir + ") in " + pose + " " + facing);
                }
                int metal = 0;
                int top = 0;
                boolean eye = false;
                for (BlockPos pos : BlockPos.betweenClosed(1, 0, 1, 22, 15, 22)) {
                    BlockState state = helper.getBlockState(pos);
                    if (state.is(RustworksBlocks.RUSTY_PLATING.get()) || state.is(RustworksBlocks.COG_PLATING.get())
                            || state.is(RustworksBlocks.RUSTY_PIPE.get()) || state.is(RustworksBlocks.RUSTY_COG.get())) {
                        metal++;
                        top = Math.max(top, pos.getY() - GROUND);
                    }
                    eye |= state.is(Blocks.WAXED_OXIDIZED_COPPER_BULB);
                }
                helper.assertTrue(metal >= 15, pose + " too small: " + metal);
                helper.assertTrue(top >= 4 && top <= 8, pose + " height out of range: " + top);
                helper.assertTrue(eye, pose + " has no lamp eye");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void stalledAutomatonNeedsFlatGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StalledAutomatonFeature feature = RustworksFeatures.STALLED_AUTOMATON.get();
        int placed = 0;
        for (int seed = 0; seed < 6; seed++) {
            platform(helper);
            if (feature.place(context(helper, feature, seed))) placed++;
        }
        helper.assertTrue(placed == 6, "Automaton should fit on a flat platform, placed " + placed);
        clear(helper);
        helper.assertFalse(feature.place(context(helper, feature, 7)), "Automaton placed in the void");
        helper.succeed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 260)
    public static void exhaustVentLaunchesEntities(GameTestHelper helper) {
        BlockPos vent = new BlockPos(12, 1, 12);
        helper.setBlock(vent, RustworksBlocks.EXHAUST_VENT.get().defaultBlockState());
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, vent.above());
        double start = helper.absolutePos(vent).getY() + 1.0;
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.getY() - start >= 10.0, "Pig not launched yet, at +" + (pig.getY() - start));
            helper.assertTrue(pig.fallDistance < 1.0F, "Fall distance not reset: " + pig.fallDistance);
            helper.assertBlockProperty(vent, ExhaustVentBlock.ACTIVE, true);
        });
    }

    private static FeaturePlaceContext<NoneFeatureConfiguration> context(GameTestHelper helper, StalledAutomatonFeature feature, int seed) {
        ServerLevel level = helper.getLevel();
        return new FeaturePlaceContext<>(Optional.empty(), level, level.getChunkSource().getGenerator(), RandomSource.create(seed),
                helper.absolutePos(new BlockPos(12, GROUND + 1, 12)), NoneFeatureConfiguration.INSTANCE);
    }

    private static void platform(GameTestHelper helper) {
        clear(helper);
        for (int x = 1; x < 23; x++) {
            for (int z = 1; z < 23; z++) {
                for (int y = 1; y <= GROUND; y++) {
                    BlockState state = (y == GROUND ? RustworksBlocks.OXIDE_TURF.get() : RustworksBlocks.RUSTSLATE.get()).defaultBlockState();
                    helper.getLevel().setBlock(helper.absolutePos(new BlockPos(x, y, z)), state, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static void clear(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 1, 0, 23, 15, 23)) {
            helper.getLevel().setBlock(helper.absolutePos(pos), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
