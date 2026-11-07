package net.itsthesky.upfall.content.ores;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class OresModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        OresBlocks.BLOCKS.register(modEventBus);
        OresBlocks.BLOCK_ITEMS.register(modEventBus);
    }

    @Override
    public DataContribution data() {
        return new OresData();
    }
}
