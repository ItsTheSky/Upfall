package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReliquarySounds {
    private ReliquarySounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_AWAKEN = register("entity.fold_warden.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_AMBIENT = register("entity.fold_warden.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_HURT = register("entity.fold_warden.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_DEATH = register("entity.fold_warden.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SHIELD_HIT = register("entity.fold_warden.shield_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SHIELD_BREAK = register("entity.fold_warden.shield_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SHIELD_RESTORE = register("entity.fold_warden.shield_restore");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SLAM_CHARGE = register("entity.fold_warden.slam_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SLAM = register("entity.fold_warden.slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_TILT = register("entity.fold_warden.tilt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_WEIGHT = register("entity.fold_warden.weight");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_DROP = register("entity.fold_warden.drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_UNFOLD = register("entity.fold_warden.unfold");
    public static final DeferredHolder<SoundEvent, SoundEvent> WYRM_AMBIENT = register("entity.fold_warden.wyrm_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WYRM_SHED = register("entity.fold_warden.shed");
    public static final DeferredHolder<SoundEvent, SoundEvent> WYRM_DIVE = register("entity.fold_warden.dive");
    public static final DeferredHolder<SoundEvent, SoundEvent> WYRM_BREATH_CHARGE = register("entity.fold_warden.breath_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> WYRM_BREATH = register("entity.fold_warden.breath");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Upfall.id(name)));
    }
}
