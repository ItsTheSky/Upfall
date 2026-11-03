package net.itsthesky.upfall.content.music;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MusicSounds {
    private MusicSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RUSTWORKS = register("music.rustworks");
    public static final DeferredHolder<SoundEvent, SoundEvent> TILTED_RELIQUARY = register("music.structure.tilted_reliquary");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHRONOSPIRE = register("music.structure.chronospire");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GROTTO = register("music.structure.crystal_grotto");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEARL_AERIE = register("music.structure.pearl_aerie");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_HAMLET = register("music.structure.cloud_hamlet");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIFTING_VAULT = register("music.structure.shifting_vault");
    public static final DeferredHolder<SoundEvent, SoundEvent> STALLED_ENGINE = register("music.structure.stalled_engine");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }
}
