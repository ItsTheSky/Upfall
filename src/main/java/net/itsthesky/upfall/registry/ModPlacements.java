package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.worldgen.placement.SurfaceScanPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPlacements {
    private ModPlacements() {}

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Upfall.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<SurfaceScanPlacement>> SURFACE_SCAN =
            PLACEMENT_MODIFIERS.register("surface_scan", () -> () -> SurfaceScanPlacement.CODEC);
}
