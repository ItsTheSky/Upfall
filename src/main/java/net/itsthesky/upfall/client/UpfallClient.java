package net.itsthesky.upfall.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModDataComponents;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModFluids;
import net.itsthesky.upfall.registry.ModParticles;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Upfall.MODID, dist = Dist.CLIENT)
public class UpfallClient {
    public static final int SKYGRASS_ITEM_COLOR = 0x9FD4C4;

    public UpfallClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(UpfallClient::registerBlockColors);
        modEventBus.addListener(UpfallClient::registerItemColors);
        modEventBus.addListener(UpfallClient::registerParticles);
        modEventBus.addListener(UpfallClient::registerClientExtensions);
        modEventBus.addListener(UpfallClient::registerDimensionEffects);
        modEventBus.addListener(UpfallClient::clientSetup);
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

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.UPDRAFT_COMPASS.get(), ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> stack.get(ModDataComponents.UPDRAFT_TARGET.get()))));
    }

    private static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ModDimensions.UPFALL_EFFECTS, new UpfallSkyEffects());
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> level != null && pos != null
                ? BiomeColors.getAverageGrassColor(level, pos)
                : GrassColor.getDefaultColor(), ModBlocks.SKYGRASS_BLOCK.get());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> SKYGRASS_ITEM_COLOR, ModBlocks.SKYGRASS_BLOCK.get());
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PETAL.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.PETAL));
        event.registerSpriteSet(ModParticles.GILDED_DUST.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.GILDED_DUST));
        event.registerSpriteSet(ModParticles.RISING_DROPLET.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.RISING_DROPLET));
        event.registerSpriteSet(ModParticles.MIST_MOTE.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.MIST_MOTE));
        event.registerSpriteSet(ModParticles.RUST_FLAKE.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.RUST_FLAKE));
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = Upfall.id("block/skywater_still");
            private static final ResourceLocation FLOW = Upfall.id("block/skywater_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }
        }, ModFluids.SKYWATER_TYPE.get());
    }
}
