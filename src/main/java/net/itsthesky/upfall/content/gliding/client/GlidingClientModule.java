package net.itsthesky.upfall.content.gliding.client;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.gliding.GliderItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class GlidingClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(GlidingClientModule::registerClientExtensions);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return GliderArmPose.PROXY.getValue();
            }
        };
        event.registerItem(extensions, GliderItems.DOWN_GLIDER.get(), GliderItems.DUSK_GLIDER.get());
    }
}
