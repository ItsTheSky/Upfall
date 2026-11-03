package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.itsthesky.upfall.registry.UpfallTab;

public class WorldModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        WorldBlocks.BLOCKS.register(modEventBus);
        WorldBlocks.BLOCK_ITEMS.register(modEventBus);
        WorldItems.ITEMS.register(modEventBus);
        WorldFeatures.FEATURES.register(modEventBus);
        WorldParticles.PARTICLE_TYPES.register(modEventBus);
        ModCreativeTabs.add(WorldItems.CLOUD_PEARL, UpfallTab.ITEMS);
        ModCreativeTabs.add(WorldItems.NIMBUS_FLASK, UpfallTab.ITEMS);
    }

    @Override
    public DataContribution data() {
        return new WorldData();
    }
}
