package net.itsthesky.upfall.content.music.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class MusicClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(StructureMusicClient::onSelectMusic);
        NeoForge.EVENT_BUS.addListener(StructureMusicClient::onLogout);
    }
}
