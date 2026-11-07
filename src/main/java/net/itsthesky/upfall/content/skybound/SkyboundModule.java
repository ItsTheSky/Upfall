package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.neoforged.bus.api.IEventBus;

public class SkyboundModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(SkyboundPack::addPackFinders);
        SkyboundEvents.register();
    }

    @Override
    public DataContribution data() {
        return new SkyboundData();
    }
}
