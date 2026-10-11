package net.itsthesky.upfall.registry;

import com.google.common.collect.ImmutableSet;
import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPoiTypes {
    private ModPoiTypes() {}

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Upfall.MODID);

    public static final DeferredHolder<PoiType, PoiType> HALO_PORTAL = POI_TYPES.register("halo_portal",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.HALO_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));
    public static final DeferredHolder<PoiType, PoiType> UPDRAFT_VENT = POI_TYPES.register("updraft_vent",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.UPDRAFT_VENT.get().getStateDefinition().getPossibleStates()), 0, 1));
}
