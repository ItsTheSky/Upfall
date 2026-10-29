package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.datagen.ModLanguageProvider;

final class WatchLiftData {
    private WatchLiftData() {}

    static void lang(ModLanguageProvider lang) {
        lang.add("item.upfall.phase_watch.desc", "Shows the phase of the Chronospire and the time left before the next one while held.");
        lang.add("message.upfall.phase_watch", "%s - next in %s");
        lang.add("message.upfall.phase_watch.no_signal", "No signal");
        lang.add("block.upfall.gearlift.desc", "Jump to rise to the next Gearlift above, sneak to descend to the one below.");
    }

    static float phaseThreshold(ChronoPhase phase) {
        return 0.2F * (phase.ordinal() + 1) - 0.1F;
    }
}
