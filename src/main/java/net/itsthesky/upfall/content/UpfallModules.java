package net.itsthesky.upfall.content;

import net.itsthesky.upfall.content.chronospire.ChronospireModule;
import net.itsthesky.upfall.content.enchanting.EnchantingModule;
import net.itsthesky.upfall.content.engine.EngineModule;
import net.itsthesky.upfall.content.gravity.GravityModule;
import net.itsthesky.upfall.content.grotto.GrottoModule;
import net.itsthesky.upfall.content.hamlet.HamletModule;
import net.itsthesky.upfall.content.machines.MachinesModule;
import net.itsthesky.upfall.content.mobs.MobsModule;
import net.itsthesky.upfall.content.rustworks.RustworksModule;
import net.itsthesky.upfall.content.world.WorldModule;
import net.itsthesky.upfall.datagen.DataContribution;

import java.util.List;
import java.util.function.Consumer;

public final class UpfallModules {
    private UpfallModules() {}

    public static final List<UpfallModule> ALL = List.of(
            new GravityModule(),
            new EnchantingModule(),
            new MobsModule(),
            new WorldModule(),
            new ChronospireModule(),
            new HamletModule(),
            new GrottoModule(),
            new RustworksModule(),
            new MachinesModule(),
            new EngineModule());

    public static void forEachData(Consumer<DataContribution> action) {
        for (UpfallModule module : ALL) {
            action.accept(module.data());
        }
    }
}
