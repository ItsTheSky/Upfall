package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HamletEntities {
    private HamletEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Loftkin>> LOFTKIN = ENTITY_TYPES.register("loftkin",
            () -> EntityType.Builder.of(Loftkin::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.62F)
                    .clientTrackingRange(10).build("upfall:loftkin"));

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplinePulley>> ZIPLINE_PULLEY = ENTITY_TYPES.register("zipline_pulley",
            () -> EntityType.Builder.<ZiplinePulley>of(ZiplinePulley::new, MobCategory.MISC).sized(0.375F, 0.375F)
                    .clientTrackingRange(10).updateInterval(2).build("upfall:zipline_pulley"));

    static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LOFTKIN.get(), Loftkin.createAttributes().build());
    }
}
