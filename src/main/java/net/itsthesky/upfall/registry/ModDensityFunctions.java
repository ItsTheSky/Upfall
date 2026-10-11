package net.itsthesky.upfall.registry;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.worldgen.NeedleField;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDensityFunctions {
    private ModDensityFunctions() {}

    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Upfall.MODID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<NeedleField>> NEEDLES =
            DENSITY_FUNCTION_TYPES.register("needles", () -> NeedleField.MAP_CODEC);
}
