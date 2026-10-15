package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class ModNoises {
    private ModNoises() {}

    public static final ResourceKey<NormalNoise.NoiseParameters> ARCHIPELAGO = key("archipelago");
    public static final ResourceKey<NormalNoise.NoiseParameters> INVERSION = key("inversion");
    public static final ResourceKey<NormalNoise.NoiseParameters> GILDED = key("gilded");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_MASK_LOW = key("island_mask_low");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_MASK_HIGH = key("island_mask_high");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLET_MASK = key("islet_mask");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_HEIGHT_LOW = key("island_height_low");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_HEIGHT_HIGH = key("island_height_high");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLET_HEIGHT = key("islet_height");
    public static final ResourceKey<NormalNoise.NoiseParameters> HILLS = key("hills");
    public static final ResourceKey<NormalNoise.NoiseParameters> EROSION = key("erosion");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIDGE_PEAKS = key("ridge_peaks");
    public static final ResourceKey<NormalNoise.NoiseParameters> CLOUD_HOLES = key("cloud_holes");
    public static final ResourceKey<NormalNoise.NoiseParameters> CLOUD_SURFACE = key("cloud_surface");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPIRES = key("spires");
    public static final ResourceKey<NormalNoise.NoiseParameters> NEEDLE_A = key("needle_a");
    public static final ResourceKey<NormalNoise.NoiseParameters> NEEDLE_B = key("needle_b");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_PATCHES = key("surface_patches");
    public static final ResourceKey<NormalNoise.NoiseParameters> STALACTITES = key("stalactites");
    public static final ResourceKey<NormalNoise.NoiseParameters> SWELLS = key("swells");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIFTS = key("rifts");
    public static final ResourceKey<NormalNoise.NoiseParameters> RUSTWORKS = key("rustworks");
    public static final ResourceKey<NormalNoise.NoiseParameters> RELIQUARY = key("reliquary");

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        register(context, ARCHIPELAGO, -10, 1.0, 1.0, 0.5);
        register(context, INVERSION, -9, 1.0, 0.6);
        register(context, GILDED, -9, 1.0, 0.6);
        register(context, ISLAND_MASK_LOW, -8, 1.0, 0.6, 0.4, 0.25);
        register(context, ISLAND_MASK_HIGH, -8, 1.0, 0.6, 0.4, 0.25);
        register(context, ISLET_MASK, -6, 1.0, 0.5, 0.3);
        register(context, ISLAND_HEIGHT_LOW, -8, 1.0, 0.5);
        register(context, ISLAND_HEIGHT_HIGH, -8, 1.0, 0.5);
        register(context, ISLET_HEIGHT, -7, 1.0, 0.5);
        register(context, HILLS, -6, 1.0, 0.5, 0.25);
        register(context, EROSION, -5, 1.0, 0.5, 0.25);
        register(context, RIDGE_PEAKS, -6, 1.0, 0.5, 0.3);
        register(context, CLOUD_HOLES, -7, 1.0, 0.5, 0.25);
        register(context, CLOUD_SURFACE, -5, 1.0, 0.5);
        register(context, SPIRES, -7, 1.0, 0.3);
        register(context, NEEDLE_A, -6, 1.0);
        register(context, NEEDLE_B, -6, 1.0);
        register(context, SURFACE_PATCHES, -4, 1.0, 0.5);
        register(context, STALACTITES, -3, 1.0, 0.4);
        register(context, SWELLS, -7, 1.0, 0.5);
        register(context, RIFTS, -8, 1.0, 0.4);
        register(context, RUSTWORKS, -9, 1.0, 0.6);
        context.register(RELIQUARY, ReliquaryZones.NOISE_PARAMETERS);
    }

    private static void register(BootstrapContext<NormalNoise.NoiseParameters> context, ResourceKey<NormalNoise.NoiseParameters> key,
                                 int firstOctave, double firstAmplitude, double... amplitudes) {
        context.register(key, new NormalNoise.NoiseParameters(firstOctave, firstAmplitude, amplitudes));
    }

    private static ResourceKey<NormalNoise.NoiseParameters> key(String name) {
        return ResourceKey.create(Registries.NOISE, Upfall.id(name));
    }
}
