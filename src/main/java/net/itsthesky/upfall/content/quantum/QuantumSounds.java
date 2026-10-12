package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class QuantumSounds {
    private QuantumSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);
    static final Map<String, String[]> SUBTITLES = new LinkedHashMap<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> GAS_HISS = register("block.veil_gas.hiss", "Veil Gas hisses", "Le Gaz voilé siffle");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOTTLE_FILL = register("item.bottle.fill_veil_gas", "Bottle fills with gas", "Une fiole se remplit de gaz");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLUSTER_SHIFT = register("block.flicker_shard_cluster.shift", "Crystal flickers away", "Un cristal vacille et disparaît");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEST_SHIFT = register("block.shifting_chest.shift", "Chest shifts", "Un coffre se déplace");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST = register("block.frost_glass.frost", "Glass frosts over", "Le verre se givre");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTS_OUT = register("block.vault_lamp.out", "Lamps go out", "Les lampes s'éteignent");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLOOR_CRUMBLE = register("block.crumbling_floor.crumble", "Floor crumbles", "Le sol s'effondre");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPER = register("ambient.shifting_vault.whisper", "Something moves behind you", "Quelque chose bouge derrière vous");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_START = register("block.quantum_pedestal.start", "Incantation begins", "Une incantation commence");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_HUM = register("block.quantum_pedestal.hum", "Quantum Pedestal hums", "Le Piédestal quantique vibre");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_SUCCESS = register("block.quantum_pedestal.success", "Incantation succeeds", "L'incantation réussit");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_FAIL = register("block.quantum_pedestal.fail", "Incantation collapses", "L'incantation s'effondre");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEDESTAL_PLACE = register("block.pedestal.place", "Item placed on pedestal", "Objet posé sur un piédestal");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNCERTAIN_DODGE = register("entity.player.uncertain_dodge", "Player blurs", "Le joueur devient flou");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTANGLE = register("entity.player.entangle", "Creatures entangle", "Des créatures s'intriquent");
    public static final DeferredHolder<SoundEvent, SoundEvent> REWIND = register("entity.player.rewind", "Time rewinds", "Le temps rembobine");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path, String subtitle, String subtitleFr) {
        SUBTITLES.put(path, new String[]{subtitle, subtitleFr});
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }
}
