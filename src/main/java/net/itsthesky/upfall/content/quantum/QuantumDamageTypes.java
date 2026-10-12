package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public final class QuantumDamageTypes {
    private QuantumDamageTypes() {}

    public static final ResourceKey<DamageType> VEIL_GAS = ResourceKey.create(Registries.DAMAGE_TYPE, Upfall.id("veil_gas"));
    public static final ResourceKey<DamageType> ENTANGLEMENT = ResourceKey.create(Registries.DAMAGE_TYPE, Upfall.id("entanglement"));

    static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(VEIL_GAS, new DamageType("upfall.veil_gas", DamageScaling.NEVER, 0.0F));
        context.register(ENTANGLEMENT, new DamageType("upfall.entanglement", DamageScaling.NEVER, 0.1F));
    }
}
