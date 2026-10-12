package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class QuantumComponents {
    private QuantumComponents() {}

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Upfall.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<QuantumUpgrades>> UPGRADES = COMPONENTS.registerComponentType(
            "quantum_upgrades", builder -> builder.persistent(QuantumUpgrades.CODEC).networkSynchronized(QuantumUpgrades.STREAM_CODEC));
}
