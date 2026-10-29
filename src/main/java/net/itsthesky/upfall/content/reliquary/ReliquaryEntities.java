package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.reliquary.boss.FoldFragment;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReliquaryEntities {
    private ReliquaryEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<FoldWarden>> FOLD_WARDEN = ENTITY_TYPES.register("fold_warden",
            () -> McRegistration.entityType(EntityType.Builder.of(FoldWarden::new, MobCategory.MONSTER).sized(3.6F, 4.4F).fireImmune().clientTrackingRange(12),
                    "fold_warden"));
    public static final DeferredHolder<EntityType<?>, EntityType<FoldSatellite>> FOLD_SATELLITE = ENTITY_TYPES.register("fold_satellite",
            () -> McRegistration.entityType(EntityType.Builder.of(FoldSatellite::new, MobCategory.MONSTER).sized(1.1F, 1.1F).fireImmune().clientTrackingRange(12),
                    "fold_satellite"));
    public static final DeferredHolder<EntityType<?>, EntityType<FoldFragment>> FOLD_FRAGMENT = ENTITY_TYPES.register("fold_fragment",
            () -> McRegistration.entityType(EntityType.Builder.<FoldFragment>of(FoldFragment::new, MobCategory.MISC).sized(0.8F, 0.8F).fireImmune().clientTrackingRange(10)
                    .updateInterval(1), "fold_fragment"));

    public static <T extends Entity> T create(EntityType<T> type, ServerLevel level) {
        //? if >=26.1 {
        /*return type.create(level, net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED);
        *///?} else {
        return type.create(level);
        //?}
    }

    static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FOLD_WARDEN.get(), FoldWarden.createAttributes().build());
        event.put(FOLD_SATELLITE.get(), FoldSatellite.createAttributes().build());
    }
}
