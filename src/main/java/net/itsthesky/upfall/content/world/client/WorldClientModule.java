package net.itsthesky.upfall.content.world.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.world.WorldParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class WorldClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((RegisterParticleProvidersEvent event) ->
                event.registerSpriteSet(WorldParticles.CLOUD_DRIP.get(), CloudDripParticle.Provider::new));
    }
}
