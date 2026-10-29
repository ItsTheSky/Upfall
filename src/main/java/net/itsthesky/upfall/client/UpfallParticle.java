package net.itsthesky.upfall.client;

import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
//? if >=26.1 {
/*import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;
*///?} else {
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
//?}

//? if >=26.1 {
/*public class UpfallParticle extends SingleQuadParticle {
*///?} else {
public class UpfallParticle extends TextureSheetParticle {
//?}
    public enum Kind { PETAL, GILDED_DUST, RISING_DROPLET, MIST_MOTE, RUST_FLAKE, VEIL_MOTE, QUANTUM_SPARK }

    private final Kind kind;
    private float spinSpeed;
    private boolean spark;

    //? if >=26.1 {
    /*protected UpfallParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, Kind kind, SpriteSet sprites, RandomSource spriteRandom) {
        super(level, x, y, z, dx, dy, dz, sprites.get(spriteRandom));
        this.kind = kind;
    *///?} else {
    protected UpfallParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, Kind kind, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.kind = kind;
        this.pickSprite(sprites);
    //?}
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.hasPhysics = kind == Kind.PETAL;
        this.spinSpeed = (random.nextFloat() - 0.5F) * 0.15F;
        switch (kind) {
            case PETAL -> {
                this.lifetime = 80 + random.nextInt(60);
                this.gravity = 0.006F;
                this.quadSize = 0.045F + random.nextFloat() * 0.03F;
                this.roll = random.nextFloat() * Mth.TWO_PI;
            }
            case GILDED_DUST -> {
                this.lifetime = 30 + random.nextInt(30);
                this.gravity = 0.0F;
                this.quadSize = 0.04F + random.nextFloat() * 0.03F;
                this.friction = 0.92F;
            }
            case RISING_DROPLET -> {
                this.lifetime = 20 + random.nextInt(15);
                this.gravity = -0.01F;
                this.quadSize = 0.05F + random.nextFloat() * 0.02F;
                this.friction = 0.98F;
            }
            case MIST_MOTE -> {
                this.lifetime = 60 + random.nextInt(60);
                this.gravity = 0.0F;
                this.quadSize = 0.15F + random.nextFloat() * 0.1F;
                this.friction = 0.97F;
                this.alpha = 0.0F;
            }
            case VEIL_MOTE -> {
                this.lifetime = 50 + random.nextInt(50);
                this.gravity = 0.0F;
                this.quadSize = 0.08F + random.nextFloat() * 0.08F;
                this.friction = 0.96F;
                this.alpha = 0.0F;
            }
            case QUANTUM_SPARK -> {
                this.lifetime = 12 + random.nextInt(12);
                this.gravity = 0.0F;
                this.quadSize = 0.05F + random.nextFloat() * 0.04F;
                this.friction = 0.85F;
            }
            case RUST_FLAKE -> {
                this.spark = random.nextInt(9) == 0;
                this.alpha = 0.0F;
                if (spark) {
                    this.setSprite(sprites.get(3, 3));
                    this.lifetime = 12 + random.nextInt(14);
                    this.gravity = -0.002F;
                    this.quadSize = 0.025F + random.nextFloat() * 0.015F;
                    this.friction = 0.9F;
                    this.xd += (random.nextFloat() - 0.5F) * 0.03F;
                    this.zd += (random.nextFloat() - 0.5F) * 0.03F;
                } else {
                    this.setSprite(sprites.get(random.nextInt(3), 3));
                    this.lifetime = 140 + random.nextInt(100);
                    this.gravity = 0.0008F;
                    this.quadSize = 0.035F + random.nextFloat() * 0.025F;
                    this.friction = 0.99F;
                    this.xd += 0.004F + random.nextFloat() * 0.004F;
                    this.zd += (random.nextFloat() - 0.5F) * 0.006F;
                    this.roll = random.nextFloat() * Mth.TWO_PI;
                    this.oRoll = roll;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) age / lifetime;
        switch (kind) {
            case PETAL -> {
                oRoll = roll;
                roll += spinSpeed;
                xd += Math.sin(age * 0.12) * 0.002;
                if (onGround) {
                    xd = 0;
                    zd = 0;
                    spinSpeed = 0;
                }
            }
            case GILDED_DUST, RISING_DROPLET -> alpha = 1.0F - life * life;
            case MIST_MOTE -> alpha = Mth.sin(life * Mth.PI) * 0.7F;
            case VEIL_MOTE -> alpha = Mth.sin(life * Mth.PI) * 0.5F;
            case QUANTUM_SPARK -> alpha = (1.0F - life) * (age % 4 < 2 ? 1.0F : 0.55F);
            case RUST_FLAKE -> {
                if (spark) {
                    alpha = Math.min(1.0F, (1.0F - life) * 1.5F);
                } else {
                    alpha = Math.min(1.0F, Mth.sin(life * Mth.PI) * 3.0F) * 0.9F;
                    oRoll = roll;
                    roll += spinSpeed * 0.4F;
                    yd += Math.sin(age * 0.09 + spinSpeed * 40.0) * 0.0006;
                    zd += Math.cos(age * 0.07 + spinSpeed * 30.0) * 0.0004;
                }
            }
        }
    }

    private boolean fullBright() {
        return kind == Kind.GILDED_DUST || kind == Kind.PETAL || kind == Kind.QUANTUM_SPARK || kind == Kind.RUST_FLAKE && spark;
    }

    //? if >=26.1 {
    /*@Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return fullBright() ? 0xF000F0 : super.getLightCoords(partialTick);
    }
    *///?} else {
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return fullBright() ? 0xF000F0 : super.getLightColor(partialTick);
    }
    //?}

    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PETAL.get(), sprites -> new Provider(sprites, Kind.PETAL));
        event.registerSpriteSet(ModParticles.GILDED_DUST.get(), sprites -> new Provider(sprites, Kind.GILDED_DUST));
        event.registerSpriteSet(ModParticles.RISING_DROPLET.get(), sprites -> new Provider(sprites, Kind.RISING_DROPLET));
        event.registerSpriteSet(ModParticles.MIST_MOTE.get(), sprites -> new Provider(sprites, Kind.MIST_MOTE));
        event.registerSpriteSet(ModParticles.RUST_FLAKE.get(), sprites -> new Provider(sprites, Kind.RUST_FLAKE));
    }

    public record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<SimpleParticleType> {
        //? if >=26.1 {
        /*@Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
            return new UpfallParticle(level, x, y, z, dx, dy, dz, kind, sprites, random);
        }
        *///?} else {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new UpfallParticle(level, x, y, z, dx, dy, dz, kind, sprites);
        }
        //?}
    }
}
