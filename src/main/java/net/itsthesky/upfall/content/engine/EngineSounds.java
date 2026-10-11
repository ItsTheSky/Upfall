package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EngineSounds {
    private EngineSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);
    static final Map<String, String> SUBTITLES = new LinkedHashMap<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> VALVE_TURN = register("block.drain_valve.turn", "Valve turns");
    public static final DeferredHolder<SoundEvent, SoundEvent> REGULATOR_CLANK = register("block.drain_regulator.clank", "Regulator clanks");
    public static final DeferredHolder<SoundEvent, SoundEvent> REGULATOR_DRAIN = register("block.drain_regulator.drain", "Mist drains away");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path, String subtitle) {
        SUBTITLES.put(path, subtitle);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }
}
