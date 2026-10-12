package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class QuantumParticles {
    private QuantumParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Upfall.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VEIL_MOTE = PARTICLE_TYPES.register("veil_mote", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QUANTUM_SPARK = PARTICLE_TYPES.register("quantum_spark", () -> new SimpleParticleType(false));
}
