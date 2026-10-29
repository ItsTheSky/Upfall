package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.entity.Skyray;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Skyray>> SKYRAY = ENTITY_TYPES.register("skyray",
            () -> McRegistration.entityType(EntityType.Builder.of(Skyray::new, MobCategory.AMBIENT).sized(1.6F, 0.4F).clientTrackingRange(10), "skyray"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SKYRAY.get(), Skyray.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(SKYRAY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                Skyray::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
