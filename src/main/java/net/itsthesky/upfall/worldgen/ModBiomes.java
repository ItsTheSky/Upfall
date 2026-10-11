package net.itsthesky.upfall.worldgen;

import com.mojang.datafixers.util.Pair;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.content.rustworks.RustworksSounds;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModParticles;
import net.itsthesky.upfall.registry.ModSounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

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

        context.register(DRIFTING_ARCHIPELAGO, biome(features, carvers, DRIFTING_ARCHIPELAGO, new BiomeSpecialEffects.Builder()
                .skyColor(0xA9C8E8).fogColor(0xE8B4C0).waterColor(0x9FC8E0).waterFogColor(0x7C7A9E)
                .grassColorOverride(0x9FD4C4).foliageColorOverride(0x9FD4C4)
                .ambientParticle(new AmbientParticleSettings(ModParticles.PETAL.get(), 0.0025F))
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_ARCHIPELAGO_ADDITIONS, 0.006))
                .backgroundMusic(music())));

        context.register(MISTSEA, biome(features, carvers, MISTSEA, new BiomeSpecialEffects.Builder()
                .skyColor(0xB7CDE6).fogColor(0xF2EEF5).waterColor(0xB4D2E8).waterFogColor(0x9A98BA)
                .grassColorOverride(0xB4DCCF).foliageColorOverride(0xB4DCCF)
                .ambientParticle(new AmbientParticleSettings(ModParticles.MIST_MOTE.get(), 0.006F))
                .ambientLoopSound(ModSounds.AMBIENT_MISTSEA_LOOP)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_MISTSEA_MOOD, 6000, 8, 2.0))
                .backgroundMusic(music())));

        context.register(HANGING_ROOTS, biome(features, carvers, HANGING_ROOTS, new BiomeSpecialEffects.Builder()
                .skyColor(0xA0B8DE).fogColor(0xD9A9C2).waterColor(0x9FC8E0).waterFogColor(0x7C7A9E)
                .grassColorOverride(0xC6D89A).foliageColorOverride(0xE2B85A)
                .ambientParticle(new AmbientParticleSettings(ModParticles.RISING_DROPLET.get(), 0.004F))
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_HANGING_ROOTS_ADDITIONS, 0.008))
                .backgroundMusic(music())));

        context.register(GILDED_RIDGE, biome(features, carvers, GILDED_RIDGE, new BiomeSpecialEffects.Builder()
                .skyColor(0xB2C2E2).fogColor(0xE8C49A).waterColor(0xA9C8E8).waterFogColor(0x7C7A9E)
                .grassColorOverride(0xD8C98A).foliageColorOverride(0xE2B85A)
                .ambientParticle(new AmbientParticleSettings(ModParticles.GILDED_DUST.get(), 0.005F))
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(ModSounds.AMBIENT_GILDED_RIDGE_ADDITIONS, 0.008))
                .backgroundMusic(music())));

        context.register(RUSTWORKS, biome(features, carvers, RUSTWORKS, new BiomeSpecialEffects.Builder()
                .skyColor(0xB4A2B4).fogColor(0xC98A6E).waterColor(0x5FB8A0).waterFogColor(0x3E6E66)
                .grassColorOverride(0xB0705A).foliageColorOverride(0x5FA898)
                .ambientParticle(new AmbientParticleSettings(ModParticles.RUST_FLAKE.get(), 0.006F))
                .ambientMoodSound(new AmbientMoodSettings(RustworksSounds.AMBIENT_MOOD, 6000, 8, 2.0))
                .ambientAdditionsSound(new AmbientAdditionsSettings(RustworksSounds.AMBIENT_ADDITIONS, 0.006))
                .backgroundMusic(music())));
    }

    private static MobSpawnSettings spawns(ResourceKey<Biome> key) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        if (key != GILDED_RIDGE) {
            builder.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SKYRAY.get(), key == MISTSEA ? 12 : 8, 1, 3));
        }
        UpfallModules.forEachData(data -> data.biomeSpawns(key, builder));
        return builder.build();
    }

    private static Music music() {
        return new Music(ModSounds.MUSIC_UPFALL, 6000, 18000, false);
    }

    private static Biome biome(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers,
                               ResourceKey<Biome> key, BiomeSpecialEffects.Builder effects) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        UpfallBiomeFeatures.addFeatures(generation, key);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.7F)
                .downfall(0.4F)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns(key))
                .generationSettings(generation.build())
                .build();
    }
}
