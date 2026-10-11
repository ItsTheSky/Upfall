package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RustworksSounds {
    private RustworksSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);
    static final Map<String, String> SUBTITLES = new LinkedHashMap<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_MOOD = register("ambient.rustworks.mood", "Machinery groans in the distance");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ADDITIONS = register("ambient.rustworks.additions", "Pipes clank and hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXHAUST_VENT_HISS = register("block.exhaust_vent.hiss", "Exhaust vent hisses");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path, String subtitle) {
        SUBTITLES.put(path, subtitle);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }
}
