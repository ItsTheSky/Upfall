package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.content.machines.pipe.PneumaticPipes;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;

public class MachinesModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        MachinesBlocks.BLOCKS.register(modEventBus);
        MachinesBlocks.BLOCK_ITEMS.register(modEventBus);
        MachinesItems.ITEMS.register(modEventBus);
        MachineBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(MachineBlockEntities::registerCapabilities);
        modEventBus.addListener(GearliftPayload::register);
        PneumaticPipes.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(MachinesBlocks.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.addAll(MachinesItems.CREATIVE_ORDER);
    }

    @Override
    public DataContribution data() {
        return new MachinesData();
    }
}
