package net.itsthesky.upfall.client.armor;

import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

//? if >=26.1 {
/*import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
*///?} else {
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
//?}

public class ArmorClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(ArmorClientModule::registerClientExtensions);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        register(event, UpfallArmorModels.Style.LEVITITE, EquipmentSlot.HEAD, ModItems.LEVITITE_HELMET);
        register(event, UpfallArmorModels.Style.LEVITITE, EquipmentSlot.CHEST, ModItems.LEVITITE_CHESTPLATE);
        register(event, UpfallArmorModels.Style.LEVITITE, EquipmentSlot.LEGS, ModItems.LEVITITE_LEGGINGS);
        register(event, UpfallArmorModels.Style.LEVITITE, EquipmentSlot.FEET, ModItems.LEVITITE_BOOTS);
        register(event, UpfallArmorModels.Style.NIMBUSITE, EquipmentSlot.HEAD, ModItems.NIMBUSITE_HELMET);
        register(event, UpfallArmorModels.Style.NIMBUSITE, EquipmentSlot.CHEST, ModItems.NIMBUSITE_CHESTPLATE);
        register(event, UpfallArmorModels.Style.NIMBUSITE, EquipmentSlot.LEGS, ModItems.NIMBUSITE_LEGGINGS);
        register(event, UpfallArmorModels.Style.NIMBUSITE, EquipmentSlot.FEET, ModItems.NIMBUSITE_BOOTS);
    }

    private static void register(RegisterClientExtensionsEvent event, UpfallArmorModels.Style style, EquipmentSlot slot, DeferredItem<?> item) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            //? if >=26.1 {
            /*@Override
            public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                return layerType == EquipmentClientInfo.LayerType.HUMANOID_BABY ? original : UpfallArmorModels.get(style, slot);
            }
            *///?} else {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return UpfallArmorModels.get(style, slot);
            }
            //?}
        };
        event.registerItem(extensions, item.get());
    }
}
