package net.itsthesky.upfall.content;

import net.neoforged.bus.api.IEventBus;

public interface UpfallClientModule {
    default void register(IEventBus modEventBus) {
    }
}
