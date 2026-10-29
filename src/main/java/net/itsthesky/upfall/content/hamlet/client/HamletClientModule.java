package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.HamletEntities;
import net.itsthesky.upfall.content.hamlet.HamletItems;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class HamletClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(HamletClientModule::registerLayers);
        modEventBus.addListener(HamletClientModule::registerRenderers);
        modEventBus.addListener(HamletClientModule::registerClientExtensions);
        NeoForge.EVENT_BUS.addListener(HamletClientModule::onEntityJoin);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LoftkinModel.LAYER, LoftkinModel::createLayer);
        event.registerLayerDefinition(ZiplinePulleyModelParts.LAYER, ZiplinePulleyModelParts::createLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HamletEntities.LOFTKIN.get(), LoftkinRenderer::new);
        event.registerEntityRenderer(HamletEntities.ZIPLINE_PULLEY.get(), ZiplinePulleyRenderer::new);
        event.registerBlockEntityRenderer(HamletBlocks.ROPE_ANCHOR_ENTITY.get(), RopeLineRenderer::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                //? if >=26.1 {
                /*return entity.getVehicle() instanceof ZiplinePulley ? HumanoidModel.ArmPose.THROW_TRIDENT : null;
                *///?} else {
                return entity.getVehicle() instanceof ZiplinePulley ? HumanoidModel.ArmPose.THROW_SPEAR : null;
                //?}
            }
        }, HamletItems.ZIPLINE_HANDLE.get());
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof ZiplinePulley pulley) {
            Minecraft.getInstance().getSoundManager().play(new ZiplineSoundInstance(pulley));
        }
    }
}
