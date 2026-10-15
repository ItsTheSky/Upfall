package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class ReliquaryModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        ReliquaryBlocks.BLOCKS.register(modEventBus);
        ReliquaryBlocks.BLOCK_ITEMS.register(modEventBus);
        ReliquaryItems.ITEMS.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(ReliquaryItems.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.addAll(ReliquaryBlocks.CREATIVE_ORDER);
    }

    @Override
    public DataContribution data() {
        return new ReliquaryData();
    }
}
