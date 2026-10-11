package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MobSounds {
    private MobSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DUSKWING_AMBIENT = register("entity.duskwing.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUSKWING_HURT = register("entity.duskwing.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUSKWING_DEATH = register("entity.duskwing.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAGMAW_AMBIENT = register("entity.cragmaw.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAGMAW_HURT = register("entity.cragmaw.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAGMAW_DEATH = register("entity.cragmaw.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISTJELLY_AMBIENT = register("entity.mistjelly.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISTJELLY_HURT = register("entity.mistjelly.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISTJELLY_DEATH = register("entity.mistjelly.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYGRAZER_AMBIENT = register("entity.skygrazer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYGRAZER_HURT = register("entity.skygrazer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYGRAZER_DEATH = register("entity.skygrazer.death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Upfall.id(name)));
    }
}
