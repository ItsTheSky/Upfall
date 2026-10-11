package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WorldParticles {
    private WorldParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Upfall.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CLOUD_DRIP = PARTICLE_TYPES.register("cloud_drip",
            () -> new SimpleParticleType(false));
}
