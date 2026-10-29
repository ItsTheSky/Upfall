package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.mobs.entity.Cragmaw;
import net.itsthesky.upfall.content.mobs.entity.Duskwing;
import net.itsthesky.upfall.content.mobs.entity.Mistjelly;
import net.itsthesky.upfall.content.mobs.entity.Skygrazer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MobEntities {
    private MobEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Duskwing>> DUSKWING = ENTITY_TYPES.register("duskwing",
            () -> McRegistration.entityType(notInPeaceful(EntityType.Builder.of(Duskwing::new, MobCategory.MONSTER).sized(0.9F, 0.7F).clientTrackingRange(10)), "duskwing"));
    public static final DeferredHolder<EntityType<?>, EntityType<Cragmaw>> CRAGMAW = ENTITY_TYPES.register("cragmaw",
            () -> McRegistration.entityType(notInPeaceful(EntityType.Builder.of(Cragmaw::new, MobCategory.MONSTER).sized(1.3F, 1.2F).clientTrackingRange(8)), "cragmaw"));
    public static final DeferredHolder<EntityType<?>, EntityType<Mistjelly>> MISTJELLY = ENTITY_TYPES.register("mistjelly",
            () -> McRegistration.entityType(EntityType.Builder.of(Mistjelly::new, MobCategory.WATER_AMBIENT).sized(1.3F, 1.8F).clientTrackingRange(8), "mistjelly"));
    public static final DeferredHolder<EntityType<?>, EntityType<Skygrazer>> SKYGRAZER = ENTITY_TYPES.register("skygrazer",
            () -> McRegistration.entityType(EntityType.Builder.of(Skygrazer::new, MobCategory.CREATURE).sized(0.95F, 1.3F).clientTrackingRange(10), "skygrazer"));

    private static <T extends Mob> EntityType.Builder<T> notInPeaceful(EntityType.Builder<T> builder) {
        //? if >=26.1 {
        /*return builder.notInPeaceful();
        *///?} else {
        return builder;
        //?}
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(DUSKWING.get(), Duskwing.createAttributes().build());
        event.put(CRAGMAW.get(), Cragmaw.createAttributes().build());
        event.put(MISTJELLY.get(), Mistjelly.createAttributes().build());
        event.put(SKYGRAZER.get(), Skygrazer.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(DUSKWING.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                Duskwing::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CRAGMAW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cragmaw::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MISTJELLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                Mistjelly::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SKYGRAZER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Skygrazer::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
