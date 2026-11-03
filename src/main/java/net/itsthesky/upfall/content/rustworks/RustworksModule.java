package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFeatures;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksFloraWorldgen;
import net.itsthesky.upfall.content.rustworks.worldgen.RustworksStructures;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.itsthesky.upfall.registry.UpfallTab;

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
        ModCreativeTabs.add(RustworksItems.KAIRITE_DUST, UpfallTab.ITEMS);
        ModCreativeTabs.add(RustworksItems.KAIRITE_GEAR, UpfallTab.ITEMS);
        ModCreativeTabs.add(RustworksItems.RUSTY_PLATE, UpfallTab.ITEMS);
        //? if >=26.1 {
        /*modEventBus.addListener((net.neoforged.neoforge.event.RegisterCauldronInteractionEvent.Dispatcher event) ->
                event.register(net.itsthesky.upfall.Upfall.id("stillflow"), net.itsthesky.upfall.content.rustworks.block.StillflowCauldronBlock.INTERACTIONS));
        *///?}
    }

    @Override
    public DataContribution data() {
        return new RustworksData();
    }
}
