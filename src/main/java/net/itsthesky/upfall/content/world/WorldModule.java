package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class WorldModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        WorldBlocks.BLOCKS.register(modEventBus);
        WorldBlocks.BLOCK_ITEMS.register(modEventBus);
        WorldItems.ITEMS.register(modEventBus);
        WorldFeatures.FEATURES.register(modEventBus);
        WorldParticles.PARTICLE_TYPES.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(WorldBlocks.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.add(WorldItems.CLOUD_PEARL);
        ModCreativeTabs.EXTRA.add(WorldItems.NIMBUS_FLASK);
    }

    @Override
    public DataContribution data() {
        return new WorldData();
    }
}
