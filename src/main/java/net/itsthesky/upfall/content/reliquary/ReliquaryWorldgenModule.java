package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class ReliquaryWorldgenModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        ReliquaryStructures.TYPES.register(modEventBus);
        ReliquaryStructures.PIECES.register(modEventBus);
        ReliquaryStructures.PLACEMENTS.register(modEventBus);
    }

    @Override
    public DataContribution data() {
        return new ReliquaryWorldgenData();
    }
}
