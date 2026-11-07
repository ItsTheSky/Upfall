package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public final class SkyboundPreset {
    private SkyboundPreset() {}

    public static final ResourceKey<WorldPreset> KEY = ResourceKey.create(Registries.WORLD_PRESET, ModDimensions.UPFALL_ID);

    public static void bootstrap(BootstrapContext<WorldPreset> context) {
        LevelStem overworld = ModDimensions.stem(context.lookup(Registries.DIMENSION_TYPE),
                context.lookup(Registries.NOISE_SETTINGS), context.lookup(Registries.BIOME));
        context.register(KEY, new WorldPreset(Map.of(LevelStem.OVERWORLD, overworld)));
    }
}
