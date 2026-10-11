package net.itsthesky.upfall.content;

import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public interface UpfallModule {
    default void register(IEventBus modEventBus) {
    }

    default DataContribution data() {
        return DataContribution.NONE;
    }
}
