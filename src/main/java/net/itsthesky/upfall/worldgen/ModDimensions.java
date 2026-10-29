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
//? if >=26.1 {
/*import net.minecraft.core.HolderSet;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import java.util.Optional;
*///?}

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
        //? if >=26.1 {
        /*HolderGetter<Timeline> timelines = context.lookup(Registries.TIMELINE);
        context.register(UPFALL_TYPE, new DimensionType(
                true,
                true,
                false,
                false,
                1.0,
                UpfallTerrain.MIN_Y,
                UpfallTerrain.HEIGHT,
                UpfallTerrain.HEIGHT,
                BlockTags.INFINIBURN_OVERWORLD,
                0.12F,
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, UPFALL_EFFECTS)
                        .set(NeoForgeEnvironmentAttributes.CUSTOM_CLOUDS, UPFALL_EFFECTS)
                        .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.ALWAYS, BedRule.Rule.ALWAYS, false, Optional.empty()))
                        .set(EnvironmentAttributes.CAN_START_RAID, false)
                        .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                        // 1.21.1 freezes this sky at FIXED_TIME: the values below are the 1.21.1 ones at that time,
                        // the sunrise alpha is already the rescaled UpfallSkyEffects one so the fog blends toward it too,
                        // and the ambient colour stands in for the 1.21.1 lightmap ambient light of 0.12.
                        .set(EnvironmentAttributes.SUN_ANGLE, 74.47754F)
                        .set(EnvironmentAttributes.MOON_ANGLE, 254.47754F)
                        .set(EnvironmentAttributes.STAR_ANGLE, 74.47754F)
                        .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.02306F)
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.colorFromFloat(0.30216F, 0.91F, 0.62F, 0.66F))
                        .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.79878F)
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.colorFromFloat(1.0F, 0.86232F, 0.86232F, 1.0F))
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.color(74, 65, 57))
                        .build(),
                // 1.21.1 still drives villager schedules and the moon phase from the overworld day time.
                HolderSet.direct(timelines.getOrThrow(Timelines.VILLAGER_SCHEDULE), timelines.getOrThrow(Timelines.MOON)),
                Optional.empty()));
        *///?} else {
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
        //?}
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> settings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(UPFALL_STEM, new LevelStem(types.getOrThrow(UPFALL_TYPE),
                new NoiseBasedChunkGenerator(ModBiomes.biomeSource(biomes), settings.getOrThrow(UpfallTerrain.SETTINGS))));
    }
}
