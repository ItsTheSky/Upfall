package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.machines.item.PhaseWatchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

final class PhaseWatchProperty {
    private PhaseWatchProperty() {}

    static float value(@Nullable Level level) {
        Level world = level != null ? level : Minecraft.getInstance().level;
        if (world == null || !PhaseWatchItem.hasSignal(world)) return 0.0F;
        return 0.2F * (ChronoClock.phase(world).ordinal() + 1);
    }
}
