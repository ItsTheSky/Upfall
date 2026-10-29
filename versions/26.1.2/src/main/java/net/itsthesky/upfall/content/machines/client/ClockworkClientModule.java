package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public class ClockworkClientModule implements UpfallClientModule {
    private static final StandaloneModelKey<BlockStateModelPart> COG = new StandaloneModelKey<>(() -> "upfall:block/hastening_gear_cog");

    static BlockStateModelPart cog() {
        return Minecraft.getInstance().getModelManager().getStandaloneModel(COG);
    }

    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(MachineBlockEntities.REWINDER.get(), RewinderRenderer::new);
            event.registerBlockEntityRenderer(MachineBlockEntities.HASTENING_GEAR.get(), HasteningGearRenderer::new);
        });
        modEventBus.addListener((ModelEvent.RegisterStandalone event) ->
                event.register(COG, SimpleUnbakedStandaloneModel.simpleModelWrapper(Upfall.id("block/hastening_gear_cog"))));
    }
}
