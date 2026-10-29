package net.itsthesky.upfall.content.chronospire;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.content.chronospire.data.ChronospireData;
import net.itsthesky.upfall.content.chronospire.item.HourglassTracker;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class ChronospireModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        ChronoBlocks.BLOCKS.register(modEventBus);
        ChronoBlocks.ITEMS.register(modEventBus);
        ChronoRegistries.BLOCK_ENTITIES.register(modEventBus);
        ChronoRegistries.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        ChronoRegistries.ENTITY_TYPES.register(modEventBus);
        ChronoRegistries.ITEMS.register(modEventBus);
        ChronoRegistries.STRUCTURE_TYPES.register(modEventBus);
        ChronoRegistries.STRUCTURE_PIECES.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(ChronoBlocks.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.add(ChronoRegistries.REWINDING_HOURGLASS);
        NeoForge.EVENT_BUS.addListener(HourglassTracker::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HourglassTracker::onLogout);
    }

    @Override
    public DataContribution data() {
        return new ChronospireData();
    }
}
