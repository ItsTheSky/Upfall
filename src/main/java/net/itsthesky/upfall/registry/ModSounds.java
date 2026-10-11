package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HALO_PORTAL_AMBIENT = register("block.halo_portal.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HALO_PORTAL_TRIGGER = register("block.halo_portal.trigger");
    public static final DeferredHolder<SoundEvent, SoundEvent> HALO_PORTAL_TRAVEL = register("block.halo_portal.travel");
    public static final DeferredHolder<SoundEvent, SoundEvent> HALO_PORTAL_IGNITE = register("block.halo_portal.ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> UPDRAFT_WHOOSH = register("block.updraft_vent.whoosh");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_EQUIP_NIMBUSITE = register("item.armor.equip_nimbusite");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_EQUIP_LEVITITE = register("item.armor.equip_levitite");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOUBLE_JUMP = register("entity.player.levitite_double_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_RESCUE = register("entity.player.void_rescue");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ARCHIPELAGO_MOOD = register("ambient.drifting_archipelago.mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ARCHIPELAGO_ADDITIONS = register("ambient.drifting_archipelago.additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_MISTSEA_LOOP = register("ambient.mistsea.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_MISTSEA_MOOD = register("ambient.mistsea.mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_HANGING_ROOTS_ADDITIONS = register("ambient.hanging_roots.additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_GILDED_RIDGE_ADDITIONS = register("ambient.gilded_ridge.additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_UPFALL = register("music.upfall");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYRAY_AMBIENT = register("entity.skyray.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYRAY_HURT = register("entity.skyray.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKYRAY_DEATH = register("entity.skyray.death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Upfall.id(name)));
    }
}
