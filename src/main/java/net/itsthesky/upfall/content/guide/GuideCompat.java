package net.itsthesky.upfall.content.guide;

import guideme.Guide;
import guideme.GuidesCommon;
import net.minecraft.world.entity.player.Player;

// Only class referencing GuideME, so it is never loaded when the mod is absent.
final class GuideCompat {
    private GuideCompat() {}

    static void register() {
        // GuideME 26 guide building loads client-only classes, so a dedicated server must skip it.
        //? if >=26.1 {
        /*if (!net.itsthesky.upfall.compat.mc.McEnv.isClient()) return;
        *///?}
        Guide.builder(GuideModule.GUIDE_ID).build();
    }

    static void open(Player player) {
        GuidesCommon.openGuide(player, GuideModule.GUIDE_ID);
    }
}
