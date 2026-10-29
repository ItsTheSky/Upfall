package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public class MachinesClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                ItemProperties.register(MachinesItems.PHASE_WATCH.get(), Upfall.id("phase"), (ClampedItemPropertyFunction) (stack, level, entity, seed) -> PhaseWatchProperty.value(level))));
        NeoForge.EVENT_BUS.addListener(GearliftClientInput::onClientTick);
    }
}
