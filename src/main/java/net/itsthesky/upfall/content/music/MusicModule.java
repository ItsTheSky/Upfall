package net.itsthesky.upfall.content.music;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.neoforged.bus.api.IEventBus;

public class MusicModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        MusicSounds.SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(StructureMusicPayload::register);
        StructureMusic.register();
    }

    @Override
    public DataContribution data() {
        return new DataContribution() {
            @Override
            public void sounds(ModSoundProvider sounds) {
                sounds.addSound(MusicSounds.RUSTWORKS, ModSoundProvider.music("new_radius"));
                sounds.addSound(MusicSounds.TILTED_RELIQUARY, ModSoundProvider.music("sirens_in_darkness"));
                sounds.addSound(MusicSounds.CHRONOSPIRE, ModSoundProvider.music("synthwave"));
                sounds.addSound(MusicSounds.CRYSTAL_GROTTO, ModSoundProvider.music("synthwave"));
                sounds.addSound(MusicSounds.PEARL_AERIE, ModSoundProvider.music("synthwave_15k"));
                sounds.addSound(MusicSounds.CLOUD_HAMLET, ModSoundProvider.music("synthwave_15k"));
                sounds.addSound(MusicSounds.SHIFTING_VAULT, ModSoundProvider.music("another_august"));
                sounds.addSound(MusicSounds.STALLED_ENGINE, ModSoundProvider.music("another_august"));
            }
        };
    }
}
