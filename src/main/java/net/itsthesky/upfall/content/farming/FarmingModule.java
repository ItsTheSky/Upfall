package net.itsthesky.upfall.content.farming;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class FarmingModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        FarmingBlocks.BLOCKS.register(modEventBus);
        FarmingBlocks.ITEMS.register(modEventBus);
        FarmingEvents.register();
    }

    @Override
    public DataContribution data() {
        return new FarmingData();
    }
}
