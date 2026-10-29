package net.itsthesky.upfall.content.world.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
//? if >=26.1 {
/*import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;
*///?} else {
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
//?}

//? if >=26.1 {
/*public class CloudDripParticle extends SingleQuadParticle {
*///?} else {
public class CloudDripParticle extends TextureSheetParticle {
//?}
    private static final int HANG_TICKS = 12;

    //? if >=26.1 {
    /*protected CloudDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource spriteRandom) {
        super(level, x, y, z, sprites.get(spriteRandom));
    *///?} else {
    protected CloudDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);
    //?}
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.lifetime = HANG_TICKS + 50 + random.nextInt(30);
        this.quadSize = 0.08F + random.nextFloat() * 0.04F;
        this.hasPhysics = false;
        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (age < HANG_TICKS) {
            alpha = (float) age / HANG_TICKS * 0.9F;
        } else {
            gravity = 0.06F;
            alpha = 0.9F * (1.0F - (float) (age - HANG_TICKS) / (lifetime - HANG_TICKS));
        }
    }

    //? if >=26.1 {
    /*@Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }
    *///?} else {
    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    //?}

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        //? if >=26.1 {
        /*@Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
            return new CloudDripParticle(level, x, y, z, sprites, random);
        }
        *///?} else {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new CloudDripParticle(level, x, y, z, sprites);
        }
        //?}
    }
}
