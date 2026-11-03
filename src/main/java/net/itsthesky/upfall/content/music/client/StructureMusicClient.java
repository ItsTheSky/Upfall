package net.itsthesky.upfall.content.music.client;

import net.itsthesky.upfall.content.music.StructureMusic;
import net.minecraft.sounds.Music;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

public final class StructureMusicClient {
    private StructureMusicClient() {}

    private static int current = StructureMusic.NONE;

    public static void set(int index) {
        current = index;
    }

    static void onSelectMusic(SelectMusicEvent event) {
        Music music = StructureMusic.music(current);
        if (music != null) {
            event.setMusic(music);
        }
    }

    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        current = StructureMusic.NONE;
    }
}
