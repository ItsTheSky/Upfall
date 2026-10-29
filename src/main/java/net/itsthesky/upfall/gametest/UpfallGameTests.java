package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

public final class UpfallGameTests {
    private UpfallGameTests() {}

    @UpfallGameTest(template = "empty", timeoutTicks = 2400)
    public static void terrainShape(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(UpfallTerrain.SETTINGS);
        var generator = new NoiseBasedChunkGenerator(ModBiomes.biomeSource(registries.lookupOrThrow(Registries.BIOME)), settings);
        RandomState random = RandomState.create(settings.value(), registries.lookupOrThrow(Registries.NOISE), 20261011L);
        LevelHeightAccessor height = LevelHeightAccessor.create(UpfallTerrain.MIN_Y, UpfallTerrain.HEIGHT);
        int columns = 0;
        int islandColumns = 0;
        int cloudColumns = 0;
        for (int x = -512; x < 512; x += 32) {
            for (int z = -512; z < 512; z += 32) {
                NoiseColumn column = generator.getBaseColumn(x, z, height, random);
                boolean island = false;
                boolean cloud = false;
                for (int y = UpfallTerrain.MIN_Y; y < UpfallTerrain.MIN_Y + UpfallTerrain.HEIGHT; y++) {
                    BlockState state = column.getBlock(y);
                    helper.assertTrue(state.getFluidState().isEmpty(), "Unexpected fluid in the base terrain at " + x + " " + y + " " + z);
                    if (state.isAir()) continue;
                    if (y >= UpfallTerrain.CLOUD_BAND_MAX) island = true;
                    else if (y >= UpfallTerrain.CLOUD_BAND_MIN) cloud = true;
                }
                columns++;
                if (island) islandColumns++;
                if (cloud) cloudColumns++;
            }
        }
        float islandShare = islandColumns / (float) columns;
        float cloudShare = cloudColumns / (float) columns;
        Upfall.LOGGER.info("[test] terrainShape: island columns {}%, cloud columns {}%", Math.round(islandShare * 100), Math.round(cloudShare * 100));
        helper.assertTrue(islandShare > 0.15F && islandShare < 0.85F, "Island coverage out of range: " + islandShare);
        helper.assertTrue(cloudShare > 0.5F && cloudShare < 0.98F, "Cloud sea coverage out of range: " + cloudShare);
        helper.succeed();
    }
}
