package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
//? if <26.1 {
import net.neoforged.neoforge.common.data.ExistingFileHelper;
//?}
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSoundProvider extends SoundDefinitionsProvider {
    //? if >=26.1 {
    /*public ModSoundProvider(PackOutput output) {
        super(output, Upfall.MODID);
    }
    *///?} else {
    public ModSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Upfall.MODID, helper);
    }
    //?}

    @Override
    public void registerSounds() {
        add(ModSounds.HALO_PORTAL_AMBIENT, def(ModSounds.HALO_PORTAL_AMBIENT).with(event("block.portal.ambient").pitch(1.4).volume(0.4),
                event("block.amethyst_block.resonate").pitch(0.8).volume(0.3)));
        add(ModSounds.HALO_PORTAL_TRIGGER, def(ModSounds.HALO_PORTAL_TRIGGER).with(event("block.portal.trigger").pitch(1.5).volume(0.6)));
        add(ModSounds.HALO_PORTAL_TRAVEL, def(ModSounds.HALO_PORTAL_TRAVEL).with(event("block.portal.travel").pitch(1.6).volume(0.5)));
        add(ModSounds.HALO_PORTAL_IGNITE, def(ModSounds.HALO_PORTAL_IGNITE).with(event("block.beacon.activate").pitch(1.3),
                event("block.amethyst_block.chime").pitch(0.7)));
        add(ModSounds.UPDRAFT_WHOOSH, def(ModSounds.UPDRAFT_WHOOSH).with(event("entity.breeze.wind_burst").pitch(1.3).volume(0.5)));
        add(ModSounds.ARMOR_EQUIP_NIMBUSITE, def(ModSounds.ARMOR_EQUIP_NIMBUSITE).with(event("item.armor.equip_diamond").pitch(1.3)));
        add(ModSounds.ARMOR_EQUIP_LEVITITE, def(ModSounds.ARMOR_EQUIP_LEVITITE).with(event("item.armor.equip_netherite").pitch(1.4),
                event("block.amethyst_block.chime").pitch(1.2).volume(0.6)));
        add(ModSounds.DOUBLE_JUMP, def(ModSounds.DOUBLE_JUMP).with(event("entity.breeze.jump").pitch(1.5).volume(0.6)));
        add(ModSounds.VOID_RESCUE, def(ModSounds.VOID_RESCUE).with(event("block.respawn_anchor.charge").pitch(1.6)));
        add(ModSounds.AMBIENT_ARCHIPELAGO_MOOD, def(ModSounds.AMBIENT_ARCHIPELAGO_MOOD).with(event("ambient.soul_sand_valley.mood").pitch(1.6).volume(0.5)));
        add(ModSounds.AMBIENT_ARCHIPELAGO_ADDITIONS, def(ModSounds.AMBIENT_ARCHIPELAGO_ADDITIONS).with(event("entity.allay.ambient_without_item").pitch(0.6).volume(0.25),
                event("block.amethyst_block.chime").pitch(0.5).volume(0.4)));
        add(ModSounds.AMBIENT_MISTSEA_LOOP, def(ModSounds.AMBIENT_MISTSEA_LOOP).with(event("item.elytra.flying").pitch(0.5).volume(0.25)));
        add(ModSounds.AMBIENT_MISTSEA_MOOD, def(ModSounds.AMBIENT_MISTSEA_MOOD).with(event("ambient.basalt_deltas.mood").pitch(1.5).volume(0.4)));
        add(ModSounds.AMBIENT_HANGING_ROOTS_ADDITIONS, def(ModSounds.AMBIENT_HANGING_ROOTS_ADDITIONS).with(event("ambient.warped_forest.mood").pitch(1.4).volume(0.35)));
        add(ModSounds.AMBIENT_GILDED_RIDGE_ADDITIONS, def(ModSounds.AMBIENT_GILDED_RIDGE_ADDITIONS).with(event("block.amethyst_block.resonate").pitch(0.6).volume(0.5),
                event("block.beacon.ambient").pitch(1.8).volume(0.3)));
        add(ModSounds.SKYRAY_AMBIENT, def(ModSounds.SKYRAY_AMBIENT).with(event("entity.allay.ambient_without_item").pitch(0.55).volume(0.5),
                event("entity.phantom.flap").pitch(1.3).volume(0.4)));
        add(ModSounds.SKYRAY_HURT, def(ModSounds.SKYRAY_HURT).with(event("entity.phantom.hurt").pitch(1.5)));
        add(ModSounds.SKYRAY_DEATH, def(ModSounds.SKYRAY_DEATH).with(event("entity.phantom.death").pitch(1.4)));
        add(ModSounds.MUSIC_UPFALL, def(ModSounds.MUSIC_UPFALL).with(event("music.overworld.cherry_grove"), event("music.overworld.meadow")));
        UpfallModules.forEachData(data -> data.sounds(this));
    }

    public void addSound(DeferredHolder<SoundEvent, SoundEvent> sound, SoundDefinition definition) {
        add(sound, definition);
    }

    public static SoundDefinition def(DeferredHolder<SoundEvent, SoundEvent> event) {
        return definition().subtitle(ModLanguageProvider.subtitle(event.getId().getPath()));
    }

    public static SoundDefinition.Sound event(String vanillaEvent) {
        return sound(ResourceLocation.withDefaultNamespace(vanillaEvent), SoundDefinition.SoundType.EVENT);
    }
}
