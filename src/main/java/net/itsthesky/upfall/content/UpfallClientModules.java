package net.itsthesky.upfall.content;

import net.itsthesky.upfall.content.chronospire.client.ChronospireClientModule;
import net.itsthesky.upfall.content.enchanting.client.EnchantingClientModule;
import net.itsthesky.upfall.content.gravity.client.GravityClientModule;
import net.itsthesky.upfall.content.grotto.client.GrottoClientModule;
import net.itsthesky.upfall.content.hamlet.client.HamletClientModule;
import net.itsthesky.upfall.content.machines.client.ClockworkClientModule;
import net.itsthesky.upfall.content.machines.client.MachinesClientModule;
import net.itsthesky.upfall.content.mobs.client.MobsClientModule;
import net.itsthesky.upfall.content.quantum.client.QuantumClientModule;
import net.itsthesky.upfall.content.world.client.WorldClientModule;

import java.util.List;

public final class UpfallClientModules {
    private UpfallClientModules() {}

    public static final List<UpfallClientModule> ALL = List.of(
            new GravityClientModule(),
            new EnchantingClientModule(),
            new MobsClientModule(),
            new WorldClientModule(),
            new ChronospireClientModule(),
            new HamletClientModule(),
            new GrottoClientModule(),
            new MachinesClientModule(),
            new ClockworkClientModule(),
            new QuantumClientModule());
}
