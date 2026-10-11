package net.itsthesky.upfall.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class UpfallParticle extends TextureSheetParticle {
    public enum Kind { PETAL, GILDED_DUST, RISING_DROPLET, MIST_MOTE }

    private final Kind kind;
    private float spinSpeed;

    protected UpfallParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, Kind kind, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.kind = kind;
        this.pickSprite(sprites);
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
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return kind == Kind.GILDED_DUST || kind == Kind.PETAL ? 0xF000F0 : super.getLightColor(partialTick);
    }

    public record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new UpfallParticle(level, x, y, z, dx, dy, dz, kind, sprites);
        }
    }
}
