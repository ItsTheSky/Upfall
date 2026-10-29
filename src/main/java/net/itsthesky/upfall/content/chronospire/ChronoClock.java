package net.itsthesky.upfall.content.chronospire;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ChronoClock {
    private ChronoClock() {}

    public static final int DAY = 24000;
    public static final int PHASE_LENGTH = DAY / 4;
    public static final int FADE_TICKS = 400;

    public static long time(Level level) {
        Level source = level instanceof ServerLevel server ? server.getServer().overworld() : level;
        return McLevel.daylightCycle(source) ? McLevel.dayTime(source) : source.getGameTime();
    }

    public static ChronoPhase phaseAt(long time) {
        return ChronoPhase.byIndex((int) (Math.floorMod(time, DAY) / PHASE_LENGTH));
    }

    public static ChronoPhase phase(Level level) {
        return phaseAt(time(level));
    }

    public static int ticksUntilNextPhase(long time) {
        return PHASE_LENGTH - (int) Math.floorMod(time, PHASE_LENGTH);
    }

    public static String formatTicks(int ticks) {
        int seconds = (ticks + 19) / 20;
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }
}
