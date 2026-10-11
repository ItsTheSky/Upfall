package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class EngineModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        EngineSounds.SOUND_EVENTS.register(modEventBus);
        EngineBlocks.BLOCKS.register(modEventBus);
        EngineBlocks.BLOCK_ITEMS.register(modEventBus);
        EngineBlocks.BLOCK_ENTITIES.register(modEventBus);
        EngineStructures.TYPES.register(modEventBus);
        EngineStructures.PIECES.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(EngineBlocks.CREATIVE_ORDER);
    }

    @Override
    public DataContribution data() {
        return new EngineData();
    }
}
