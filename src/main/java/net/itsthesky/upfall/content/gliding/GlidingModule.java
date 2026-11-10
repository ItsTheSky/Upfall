package net.itsthesky.upfall.content.gliding;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.itsthesky.upfall.registry.UpfallTab;
import net.neoforged.bus.api.IEventBus;

public class GlidingModule implements UpfallModule {
    private static final DataContribution DATA = new GlidingData();

    @Override
    public void register(IEventBus modEventBus) {
        GliderItems.ITEMS.register(modEventBus);
        ModCreativeTabs.add(GliderItems.DOWN_GLIDER, UpfallTab.ITEMS);
        ModCreativeTabs.add(GliderItems.DUSK_GLIDER, UpfallTab.ITEMS);
        Gliding.register();
    }

    @Override
    public DataContribution data() {
        return DATA;
    }
}
