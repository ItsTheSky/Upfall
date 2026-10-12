package net.itsthesky.upfall.content.guide;

import guideme.Guide;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class GuideModule implements UpfallModule {
    public static final ResourceLocation GUIDE_ID = Upfall.id("guide");
    private static final String RECEIVED_TAG = "upfall:received_almanac";

    @Override
    public void register(IEventBus modEventBus) {
        GuideItems.ITEMS.register(modEventBus);
        ModCreativeTabs.EXTRA.add(GuideItems.SKY_ALMANAC);
        Guide.builder(GUIDE_ID).build();
        NeoForge.EVENT_BUS.addListener(GuideModule::onLogin);
    }

    @Override
    public DataContribution data() {
        return new GuideData();
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(RECEIVED_TAG)) {
            return;
        }
        persisted.putBoolean(RECEIVED_TAG, true);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
        player.getInventory().placeItemBackInInventory(new ItemStack(GuideItems.SKY_ALMANAC.get()));
    }
}
