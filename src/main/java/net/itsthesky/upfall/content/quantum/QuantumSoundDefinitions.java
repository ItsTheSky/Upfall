package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.datagen.ModSoundProvider;

import static net.itsthesky.upfall.datagen.ModSoundProvider.def;
import static net.itsthesky.upfall.datagen.ModSoundProvider.event;

final class QuantumSoundDefinitions {
    private QuantumSoundDefinitions() {}

    static void register(ModSoundProvider s) {
        s.addSound(QuantumSounds.GAS_HISS, def(QuantumSounds.GAS_HISS).with(event("block.fire.extinguish").pitch(1.6).volume(0.4),
                event("entity.breeze.idle_ground").pitch(0.6).volume(0.5)));
        s.addSound(QuantumSounds.BOTTLE_FILL, def(QuantumSounds.BOTTLE_FILL).with(event("item.bottle.fill_dragonbreath").pitch(0.8)));
        s.addSound(QuantumSounds.CLUSTER_SHIFT, def(QuantumSounds.CLUSTER_SHIFT).with(event("block.amethyst_cluster.break").pitch(1.6).volume(0.7),
                event("entity.enderman.teleport").pitch(1.8).volume(0.3)));
        s.addSound(QuantumSounds.CHEST_SHIFT, def(QuantumSounds.CHEST_SHIFT).with(event("entity.shulker.teleport").pitch(0.7),
                event("block.ender_chest.close").pitch(0.6)));
        s.addSound(QuantumSounds.FROST, def(QuantumSounds.FROST).with(event("block.powder_snow.place").pitch(1.4),
                event("block.glass.hit").pitch(1.8).volume(0.6)));
        s.addSound(QuantumSounds.LIGHTS_OUT, def(QuantumSounds.LIGHTS_OUT).with(event("block.beacon.deactivate").pitch(1.4).volume(0.8),
                event("block.lever.click").pitch(0.5)));
        s.addSound(QuantumSounds.FLOOR_CRUMBLE, def(QuantumSounds.FLOOR_CRUMBLE).with(event("block.deepslate.break").pitch(0.7),
                event("block.pointed_dripstone.fall").pitch(0.8)));
        s.addSound(QuantumSounds.WHISPER, def(QuantumSounds.WHISPER).with(event("block.ender_chest.open").pitch(0.7).volume(0.6),
                event("entity.allay.item_taken").pitch(0.5).volume(0.6), event("block.chest.close").pitch(0.8).volume(0.5),
                event("block.stone.step").pitch(0.6)));
        s.addSound(QuantumSounds.RITUAL_START, def(QuantumSounds.RITUAL_START).with(event("block.beacon.activate").pitch(1.6),
                event("block.amethyst_block.chime").pitch(0.5)));
        s.addSound(QuantumSounds.RITUAL_HUM, def(QuantumSounds.RITUAL_HUM).with(event("block.beacon.ambient").pitch(1.8).volume(0.8),
                event("block.amethyst_block.resonate").pitch(1.2).volume(0.6)));
        s.addSound(QuantumSounds.RITUAL_SUCCESS, def(QuantumSounds.RITUAL_SUCCESS).with(event("block.enchantment_table.use").pitch(0.8),
                event("block.end_portal_frame.fill").pitch(1.2)));
        s.addSound(QuantumSounds.RITUAL_FAIL, def(QuantumSounds.RITUAL_FAIL).with(event("block.beacon.deactivate").pitch(0.6),
                event("block.glass.break").pitch(0.7)));
        s.addSound(QuantumSounds.PEDESTAL_PLACE, def(QuantumSounds.PEDESTAL_PLACE).with(event("block.end_portal_frame.fill").pitch(1.4).volume(0.6)));
        s.addSound(QuantumSounds.UNCERTAIN_DODGE, def(QuantumSounds.UNCERTAIN_DODGE).with(event("entity.illusioner.mirror_move").pitch(1.4).volume(0.7)));
        s.addSound(QuantumSounds.ENTANGLE, def(QuantumSounds.ENTANGLE).with(event("block.chain.place").pitch(1.6),
                event("block.amethyst_block.chime").pitch(1.5)));
        s.addSound(QuantumSounds.REWIND, def(QuantumSounds.REWIND).with(event("item.totem.use").pitch(1.5).volume(0.6),
                event("block.respawn_anchor.set_spawn").pitch(1.6)));
    }
}
