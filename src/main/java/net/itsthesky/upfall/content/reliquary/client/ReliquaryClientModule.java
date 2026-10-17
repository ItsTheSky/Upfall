package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.reliquary.ReliquaryEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ReliquaryClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(ReliquaryClientModule::registerLayers);
        modEventBus.addListener(ReliquaryClientModule::registerRenderers);
        NeoForge.EVENT_BUS.addListener(FoldWardenBossBar::render);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FoldWardenModel.LAYER, FoldWardenModel::createLayer);
        event.registerLayerDefinition(FoldSatelliteModel.LAYER, FoldSatelliteModel::createLayer);
        event.registerLayerDefinition(FoldWyrmSegmentModel.LAYER, FoldWyrmSegmentModel::createLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ReliquaryEntities.FOLD_WARDEN.get(), FoldWardenRenderer::new);
        event.registerEntityRenderer(ReliquaryEntities.FOLD_SATELLITE.get(), FoldSatelliteRenderer::new);
        event.registerEntityRenderer(ReliquaryEntities.FOLD_FRAGMENT.get(), FoldFragmentRenderer::new);
    }
}
