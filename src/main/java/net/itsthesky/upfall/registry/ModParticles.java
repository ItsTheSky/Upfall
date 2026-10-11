package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Upfall.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PETAL = register("petal");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GILDED_DUST = register("gilded_dust");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RISING_DROPLET = register("rising_droplet");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MIST_MOTE = register("mist_mote");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUST_FLAKE = register("rust_flake");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
}
