package net.itsthesky.upfall.content.gravity.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.neoforged.bus.api.IEventBus;

public class GravityClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        InvertedGravityClient.register(modEventBus);
    }
}
