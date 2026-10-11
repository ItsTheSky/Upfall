package net.itsthesky.upfall.content.mobs.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.mobs.MobEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class MobsClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(MobsClientModule::registerLayers);
        modEventBus.addListener(MobsClientModule::registerRenderers);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DuskwingModel.LAYER, DuskwingModel::createLayer);
        event.registerLayerDefinition(CragmawModel.LAYER, CragmawModel::createLayer);
        event.registerLayerDefinition(MistjellyModel.LAYER, MistjellyModel::createLayer);
        event.registerLayerDefinition(SkygrazerModel.LAYER, SkygrazerModel::createLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MobEntities.DUSKWING.get(), MobRenderers.DuskwingRenderer::new);
        event.registerEntityRenderer(MobEntities.CRAGMAW.get(), MobRenderers.CragmawRenderer::new);
        event.registerEntityRenderer(MobEntities.MISTJELLY.get(), MobRenderers.MistjellyRenderer::new);
        event.registerEntityRenderer(MobEntities.SKYGRAZER.get(), MobRenderers.SkygrazerRenderer::new);
    }
}
