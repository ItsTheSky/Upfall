package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class ReliquaryModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        ReliquaryBlocks.BLOCKS.register(modEventBus);
        ReliquaryBlocks.BLOCK_ITEMS.register(modEventBus);
        ReliquaryItems.ITEMS.register(modEventBus);
        ReliquaryEntities.ENTITY_TYPES.register(modEventBus);
        ReliquarySounds.SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(ReliquaryEntities::registerAttributes);
    }

    @Override
    public DataContribution data() {
        return new ReliquaryData();
    }
}
