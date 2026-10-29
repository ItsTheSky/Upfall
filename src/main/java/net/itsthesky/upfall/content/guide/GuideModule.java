package net.itsthesky.upfall.content.guide;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class GuideModule implements UpfallModule {
    public static final ResourceLocation GUIDE_ID = Upfall.id("guide");
    static final String GUIDEME_ID = "guideme";
    private static final String RECEIVED_TAG = "upfall:received_almanac";

    @Override
    public void register(IEventBus modEventBus) {
        GuideItems.ITEMS.register(modEventBus);
        if (!isGuideMeLoaded()) {
            return;
        }
        ModCreativeTabs.EXTRA.add(GuideItems.SKY_ALMANAC);
        GuideCompat.register();
        NeoForge.EVENT_BUS.addListener(GuideModule::onLogin);
    }

    static boolean isGuideMeLoaded() {
        return ModList.get().isLoaded(GUIDEME_ID);
    }

    @Override
    public DataContribution data() {
        return new GuideData();
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = McNbt.getCompound(data, Player.PERSISTED_NBT_TAG);
        if (McNbt.getBoolean(persisted, RECEIVED_TAG)) {
            return;
        }
        persisted.putBoolean(RECEIVED_TAG, true);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
        player.getInventory().placeItemBackInInventory(new ItemStack(GuideItems.SKY_ALMANAC.get()));
    }
}
