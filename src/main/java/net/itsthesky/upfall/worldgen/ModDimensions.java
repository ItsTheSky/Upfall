package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public final class ModDimensions {
    private ModDimensions() {}

    public static final ResourceLocation UPFALL_ID = Upfall.id("the_upfall");
    public static final ResourceKey<Level> UPFALL = ResourceKey.create(Registries.DIMENSION, UPFALL_ID);
    public static final ResourceKey<LevelStem> UPFALL_STEM = ResourceKey.create(Registries.LEVEL_STEM, UPFALL_ID);
    public static final ResourceKey<DimensionType> UPFALL_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, UPFALL_ID);
    public static final ResourceLocation UPFALL_EFFECTS = UPFALL_ID;

    public static final long FIXED_TIME = 11800L;

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(UPFALL_TYPE, new DimensionType(
                OptionalLong.of(FIXED_TIME),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                UpfallTerrain.MIN_Y,
                UpfallTerrain.HEIGHT,
                UpfallTerrain.HEIGHT,
                BlockTags.INFINIBURN_OVERWORLD,
                UPFALL_EFFECTS,
                0.12F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> settings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(UPFALL_STEM, new LevelStem(types.getOrThrow(UPFALL_TYPE),
                new NoiseBasedChunkGenerator(ModBiomes.biomeSource(biomes), settings.getOrThrow(UpfallTerrain.SETTINGS))));
    }
}
