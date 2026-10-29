package net.itsthesky.upfall.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModFluids;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

@Mod(value = Upfall.MODID, dist = Dist.CLIENT)
public class UpfallClient {
    public static final int SKYGRASS_ITEM_COLOR = 0x9FD4C4;

    public UpfallClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(UpfallClient::registerBlockColors);
        modEventBus.addListener(UpfallParticle::registerProviders);
        modEventBus.addListener(UpfallClient::registerFluidModels);
        modEventBus.addListener(UpfallClient::registerDimensionEffects);
        modEventBus.addListener(UpfallClient::registerItemProperties);
        modEventBus.addListener(UpfallClient::registerLayers);
        modEventBus.addListener(UpfallClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(UpfallFog::onRenderFog);
        NeoForge.EVENT_BUS.addListener(UpfallFog::onComputeFogColor);
        NeoForge.EVENT_BUS.addListener(LevititeMovementHandler::onClientTick);
        UpfallClientModules.ALL.forEach(module -> module.register(modEventBus));
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SkyrayModel.LAYER, SkyrayModel::createLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SKYRAY.get(), SkyrayRenderer::new);
    }

    private static void registerItemProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Upfall.id("updraft_compass"), UpdraftCompassAngle.MAP_CODEC);
    }

    private static void registerDimensionEffects(RegisterCustomEnvironmentEffectRendererEvent event) {
        UpfallSkyEffects effects = new UpfallSkyEffects();
        event.registerSkyboxRenderer(ModDimensions.UPFALL_EFFECTS, effects);
        event.registerCloudRenderer(ModDimensions.UPFALL_EFFECTS, effects);
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grass()), ModBlocks.SKYGRASS_BLOCK.get());
    }

    // the fluid layer comes from texture alpha: versions/26.1.2 ships opaque skywater textures to match the 1.21.1 solid layer
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(new Material(Upfall.id("block/skywater_still")), new Material(Upfall.id("block/skywater_flow")), null, null),
                ModFluids.SKYWATER, ModFluids.FLOWING_SKYWATER);
    }
}
