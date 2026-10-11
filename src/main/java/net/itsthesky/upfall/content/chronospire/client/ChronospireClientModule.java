package net.itsthesky.upfall.content.chronospire.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ChronospireClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(ChronoRegistries.DRIFT_PLATFORM.get(), DriftPlatformRenderer::new));
    }
}
