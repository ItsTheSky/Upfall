package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class AerieModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        AerieBlocks.BLOCKS.register(modEventBus);
        AerieBlocks.BLOCK_ITEMS.register(modEventBus);
        AerieItems.ITEMS.register(modEventBus);
        AerieStructures.TYPES.register(modEventBus);
        AerieStructures.PIECES.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(AerieItems.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.addAll(AerieBlocks.CREATIVE_ORDER);
    }

    @Override
    public DataContribution data() {
        return new AerieData();
    }
}
