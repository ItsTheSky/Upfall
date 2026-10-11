package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class ZiplineSoundInstance extends AbstractTickableSoundInstance {
    private final ZiplinePulley pulley;

    public ZiplineSoundInstance(ZiplinePulley pulley) {
        super(HamletSounds.ZIPLINE_RIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.pulley = pulley;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = pulley.getX();
        this.y = pulley.getY();
        this.z = pulley.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (pulley.isRemoved()) {
            stop();
            return;
        }
        x = pulley.getX();
        y = pulley.getY();
        z = pulley.getZ();
        float speed = (float) ((pulley.getSpeed() - ZiplinePulley.MIN_SPEED) / (ZiplinePulley.MAX_SPEED - ZiplinePulley.MIN_SPEED));
        volume = Mth.lerp(Mth.clamp(speed, 0.0F, 1.0F), 0.25F, 0.8F);
        pitch = Mth.lerp(Mth.clamp(speed, 0.0F, 1.0F), 0.8F, 1.4F);
    }
}
