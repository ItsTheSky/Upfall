package net.itsthesky.upfall.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class UpfallSkyEffects extends DimensionSpecialEffects {
    private static final float[] DUSK_GLOW = new float[4];

    public UpfallSkyEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        float factor = Mth.clamp(brightness * 0.9F + 0.15F, 0.0F, 1.0F);
        return fogColor.multiply(factor, factor * 0.98F, factor * 0.97F);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return UpfallFog.isBlinded();
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY,
                                double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        return UpfallFog.isBlinded();
    }

    @Override
    public @Nullable float[] getSunriseColor(float timeOfDay, float partialTicks) {
        float[] vanilla = super.getSunriseColor(timeOfDay, partialTicks);
        if (vanilla == null) {
            return null;
        }
        DUSK_GLOW[0] = 0.91F;
        DUSK_GLOW[1] = 0.62F;
        DUSK_GLOW[2] = 0.66F;
        DUSK_GLOW[3] = Math.min(0.75F, vanilla[3] * 1.2F);
        return DUSK_GLOW;
    }
}
