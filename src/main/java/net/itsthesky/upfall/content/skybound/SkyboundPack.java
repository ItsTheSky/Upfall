package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
//? if >=26.1 {
/*import net.neoforged.neoforge.server.loading.ServerModLoader;
*///?} else {
import net.neoforged.neoforge.gametest.GameTestHooks;
//?}

import java.util.ArrayList;
import java.util.List;

public final class SkyboundPack {
    private SkyboundPack() {}

    public static final String NAME = "skybound";
    public static final ResourceLocation LOCATION = Upfall.id("data/" + Upfall.MODID + "/datapacks/" + NAME);
    public static final String ID = "mod/" + LOCATION;

    static void addPackFinders(AddPackFindersEvent event) {
        //? if >=26.1 {
        /*if (ServerModLoader.isGameTestServer()) return;
        *///?} else {
        if (GameTestHooks.isGametestServer()) return;
        //?}
        event.addPackFinders(LOCATION, PackType.SERVER_DATA, Component.translatable("pack.upfall.skybound"),
                PackSource.FEATURE, false, Pack.Position.TOP);
    }

    static void enableIfNeeded(MinecraftServer server) {
        if (!UpfallConfig.get(UpfallConfig.SKYBOUND_RECIPE_PACK) || !ModDimensions.isSkybound(server)) return;
        PackRepository packs = server.getPackRepository();
        if (!packs.isAvailable(ID) || packs.getSelectedIds().contains(ID)) return;
        List<String> selected = new ArrayList<>(packs.getSelectedIds());
        selected.add(ID);
        Upfall.LOGGER.info("Enabling the {} data pack for this Upfall world", ID);
        server.reloadResources(selected).exceptionally(error -> {
            Upfall.LOGGER.error("Failed to enable the {} data pack", ID, error);
            return null;
        });
    }
}
