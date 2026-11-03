package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class GrottoModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        GrottoBlocks.BLOCKS.register(modEventBus);
        GrottoBlocks.ITEMS.register(modEventBus);
        GrottoStructures.TYPES.register(modEventBus);
        GrottoStructures.PIECES.register(modEventBus);
    }

    @Override
    public DataContribution data() {
        return new GrottoData();
    }
}
