package net.itsthesky.upfall.worldgen;

import com.mojang.datafixers.util.Pair;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.content.rustworks.RustworksSounds;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModParticles;
import net.itsthesky.upfall.registry.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
//? if >=26.1 {
/*import net.minecraft.sounds.Musics;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
*///?} else {
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
//?}

import java.util.List;
//? if >=26.1 {
/*import java.util.Optional;
*///?}

public final class ModBiomes {
    private ModBiomes() {}

    public static final ResourceKey<Biome> DRIFTING_ARCHIPELAGO = key("drifting_archipelago");
    public static final ResourceKey<Biome> MISTSEA = key("mistsea");
    public static final ResourceKey<Biome> HANGING_ROOTS = key("hanging_roots");
    public static final ResourceKey<Biome> GILDED_RIDGE = key("gilded_ridge");
    public static final ResourceKey<Biome> RUSTWORKS = key("rustworks");
    public static final List<ResourceKey<Biome>> ALL = List.of(DRIFTING_ARCHIPELAGO, MISTSEA, HANGING_ROOTS, GILDED_RIDGE, RUSTWORKS);

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Upfall.id(name));
    }

    public static BiomeSource biomeSource(HolderGetter<Biome> biomes) {
        Climate.Parameter any = Climate.Parameter.span(-2.0F, 2.0F);
        Climate.Parameter zero = Climate.Parameter.point(0.0F);
        float mist = UpfallTerrain.BiomeThresholds.MISTSEA;
        float inv = UpfallTerrain.BiomeThresholds.INVERSION;
        float gild = UpfallTerrain.BiomeThresholds.GILDED;
        float rust = UpfallTerrain.BiomeThresholds.RUSTWORKS;
        Climate.Parameter islands = Climate.Parameter.span(mist, 2.0F);
        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(List.of(
                Pair.of(Climate.parameters(any, any, Climate.Parameter.span(-2.0F, mist), any, zero, any, 0.0F),
                        biomes.getOrThrow(MISTSEA)),
                Pair.of(Climate.parameters(any, Climate.Parameter.span(-2.0F, rust), islands, any, zero, Climate.Parameter.span(inv, 2.0F), 0.0F),
                        biomes.getOrThrow(HANGING_ROOTS)),
                Pair.of(Climate.parameters(Climate.Parameter.span(gild, 2.0F), Climate.Parameter.span(-2.0F, rust), islands, any, zero,
                                Climate.Parameter.span(-2.0F, inv), 0.0F),
                        biomes.getOrThrow(GILDED_RIDGE)),
                Pair.of(Climate.parameters(any, Climate.Parameter.span(rust, 2.0F), islands, any, zero, any, 0.0F),
                        biomes.getOrThrow(RUSTWORKS)),
                Pair.of(Climate.parameters(Climate.Parameter.span(-2.0F, gild), Climate.Parameter.span(-2.0F, rust), islands, any, zero,
                                Climate.Parameter.span(-2.0F, inv), 0.0F),
                        biomes.getOrThrow(DRIFTING_ARCHIPELAGO)))));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(DRIFTING_ARCHIPELAGO, biome(features, carvers, DRIFTING_ARCHIPELAGO, new Look()
                .skyColor(0xA9C8E8).fogColor(0xE8B4C0).waterColor(0x9FC8E0).waterFogColor(0x7C7A9E)
                .grassColorOverride(0x9FD4C4).foliageColorOverride(0x9FD4C4)
                .ambientParticle(ModParticles.PETAL.get(), 0.0025F)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_ARCHIPELAGO_ADDITIONS, 0.006))));

        context.register(MISTSEA, biome(features, carvers, MISTSEA, new Look()
                .skyColor(0xB7CDE6).fogColor(0xF2EEF5).waterColor(0xB4D2E8).waterFogColor(0x9A98BA)
                .grassColorOverride(0xB4DCCF).foliageColorOverride(0xB4DCCF)
                .ambientParticle(ModParticles.MIST_MOTE.get(), 0.006F)
                .ambientLoopSound(ModSounds.AMBIENT_MISTSEA_LOOP)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_MISTSEA_MOOD, 6000, 8, 2.0))));

        context.register(HANGING_ROOTS, biome(features, carvers, HANGING_ROOTS, new Look()
                .skyColor(0xA0B8DE).fogColor(0xD9A9C2).waterColor(0x9FC8E0).waterFogColor(0x7C7A9E)
                .grassColorOverride(0xC6D89A).foliageColorOverride(0xE2B85A)
                .ambientParticle(ModParticles.RISING_DROPLET.get(), 0.004F)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_HANGING_ROOTS_ADDITIONS, 0.008))));

        context.register(GILDED_RIDGE, biome(features, carvers, GILDED_RIDGE, new Look()
                .skyColor(0xB2C2E2).fogColor(0xE8C49A).waterColor(0xA9C8E8).waterFogColor(0x7C7A9E)
                .grassColorOverride(0xD8C98A).foliageColorOverride(0xE2B85A)
                .ambientParticle(ModParticles.GILDED_DUST.get(), 0.005F)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_GILDED_RIDGE_ADDITIONS, 0.008))));

        context.register(RUSTWORKS, biome(features, carvers, RUSTWORKS, new Look()
                .skyColor(0xB4A2B4).fogColor(0xC98A6E).waterColor(0x5FB8A0).waterFogColor(0x3E6E66)
                .grassColorOverride(0xB0705A).foliageColorOverride(0x5FA898)
                .ambientParticle(ModParticles.RUST_FLAKE.get(), 0.006F)
                .ambientMoodSound(new AmbientMoodSettings(RustworksSounds.AMBIENT_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(RustworksSounds.AMBIENT_ADDITIONS, 0.006))));
    }

    private static MobSpawnSettings spawns(ResourceKey<Biome> key) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        if (key != GILDED_RIDGE) {
            //? if >=26.1 {
            /*builder.addSpawn(MobCategory.AMBIENT, key == MISTSEA ? 12 : 8, new MobSpawnSettings.SpawnerData(ModEntities.SKYRAY.get(), 1, 3));
            *///?} else {
            builder.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SKYRAY.get(), key == MISTSEA ? 12 : 8, 1, 3));
            //?}
        }
        UpfallModules.forEachData(data -> data.biomeSpawns(key, builder));
        return builder.build();
    }

    private static Music music() {
        return new Music(ModSounds.MUSIC_UPFALL, 6000, 18000, false);
    }

    private static Biome biome(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers,
                               ResourceKey<Biome> key, Look look) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        UpfallBiomeFeatures.addFeatures(generation, key);
        UpfallModules.forEachData(data -> data.biomeCarvers(key, generation));
        Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.7F)
                .downfall(0.4F)
                .specialEffects(look.effects())
                .mobSpawnSettings(spawns(key))
                .generationSettings(generation.build());
        //? if >=26.1 {
        /*look.applyAttributes(builder);
        *///?}
        return builder.build();
    }

    private static final class Look {
        private int sky;
        private int fog;
        private int water;
        private int waterFog;
        private int grass;
        private int foliage;
        private ParticleOptions particle;
        private float particleChance;
        private Holder<SoundEvent> loop;
        private AmbientMoodSettings mood;
        private AmbientAdditionsSettings additions;

        Look skyColor(int color) {
            sky = color;
            return this;
        }

        Look fogColor(int color) {
            fog = color;
            return this;
        }

        Look waterColor(int color) {
            water = color;
            return this;
        }

        Look waterFogColor(int color) {
            waterFog = color;
            return this;
        }

        Look grassColorOverride(int color) {
            grass = color;
            return this;
        }

        Look foliageColorOverride(int color) {
            foliage = color;
            return this;
        }

        Look ambientParticle(ParticleOptions options, float probability) {
            particle = options;
            particleChance = probability;
            return this;
        }

        Look ambientLoopSound(Holder<SoundEvent> sound) {
            loop = sound;
            return this;
        }

        Look ambientMoodSound(AmbientMoodSettings settings) {
            mood = settings;
            return this;
        }

        Look ambientAdditionsSound(AmbientAdditionsSettings settings) {
            additions = settings;
            return this;
        }

        BiomeSpecialEffects effects() {
            BiomeSpecialEffects.Builder builder = new BiomeSpecialEffects.Builder()
                    .waterColor(water).grassColorOverride(grass).foliageColorOverride(foliage);
            //? if <26.1 {
            builder.skyColor(sky).fogColor(fog).waterFogColor(waterFog)
                    .ambientParticle(new AmbientParticleSettings(particle, particleChance))
                    .ambientMoodSound(mood)
                    .backgroundMusic(music());
            if (loop != null) builder.ambientLoopSound(loop);
            if (additions != null) builder.ambientAdditionsSound(additions);
            //?}
            return builder.build();
        }

        //? if >=26.1 {
        /*void applyAttributes(Biome.BiomeBuilder builder) {
            // The fog scale is the 1.21.1 UpfallSkyEffects#getBrightnessDependentFogColor factor at the fixed dimension time.
            builder.setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.opaque(sky))
                    .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.scaleRGB(ARGB.opaque(fog), 1.0F, 0.98F, 0.97F))
                    .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.opaque(waterFog))
                    .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(particle, particleChance))
                    .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.ofNullable(loop), Optional.of(mood),
                            additions == null ? List.of() : List.of(additions)))
                    .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music()), Optional.of(Musics.CREATIVE), Optional.empty()));
        }
        *///?}
    }
}
