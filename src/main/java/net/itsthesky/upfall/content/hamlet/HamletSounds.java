package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HamletSounds {
    private HamletSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);
    static final Map<String, String> SUBTITLES = new LinkedHashMap<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_AMBIENT = register("entity.loftkin.ambient", "Loftkin hums");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_HURT = register("entity.loftkin.hurt", "Loftkin hurts");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_DEATH = register("entity.loftkin.death", "Loftkin dies");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_YES = register("entity.loftkin.yes", "Loftkin agrees");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_NO = register("entity.loftkin.no", "Loftkin disagrees");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_GREET = register("entity.loftkin.greet", "Loftkin waves");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOFTKIN_PARASOL = register("entity.loftkin.parasol", "Parasol opens");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_ATTACH = register("entity.zipline_pulley.attach", "Pulley clicks onto the line");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_RIDE = register("entity.zipline_pulley.ride", "Zipline whirs");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZIPLINE_DETACH = register("entity.zipline_pulley.detach", "Pulley lets go");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROPE_TIE = register("block.rope.tie", "Rope tightens");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path, String subtitle) {
        SUBTITLES.put(path, subtitle);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }
}
