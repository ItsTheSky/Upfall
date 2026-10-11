package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFeatures;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFloraWorldgen;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksStructures;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class RustworksModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        RustworksSounds.SOUND_EVENTS.register(modEventBus);
        RustworksBlocks.BLOCKS.register(modEventBus);
        RustworksBlocks.BLOCK_ITEMS.register(modEventBus);
        RustworksItems.ITEMS.register(modEventBus);
        RustworksFeatures.FEATURES.register(modEventBus);
        RustworksFloraWorldgen.FEATURES.register(modEventBus);
        RustworksStructures.TYPES.register(modEventBus);
        RustworksStructures.PIECES.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(RustworksBlocks.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.add(RustworksItems.KAIRITE_DUST);
        ModCreativeTabs.EXTRA.add(RustworksItems.KAIRITE_GEAR);
        ModCreativeTabs.EXTRA.add(RustworksItems.RUSTY_PLATE);
    }

    @Override
    public DataContribution data() {
        return new RustworksData();
    }
}
