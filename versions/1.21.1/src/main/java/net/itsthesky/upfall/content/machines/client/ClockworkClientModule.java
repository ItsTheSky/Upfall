package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ClockworkClientModule implements UpfallClientModule {
    private static final ModelResourceLocation COG = ModelResourceLocation.standalone(Upfall.id("block/hastening_gear_cog"));

    static BakedModel cog() {
        return Minecraft.getInstance().getModelManager().getModel(COG);
    }

    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(MachineBlockEntities.REWINDER.get(), RewinderRenderer::new);
            event.registerBlockEntityRenderer(MachineBlockEntities.HASTENING_GEAR.get(), HasteningGearRenderer::new);
        });
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> event.register(COG));
    }
}
