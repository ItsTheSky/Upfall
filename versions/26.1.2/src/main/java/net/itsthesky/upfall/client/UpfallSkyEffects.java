package net.itsthesky.upfall.client;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.joml.Matrix4fc;

public class UpfallSkyEffects implements CustomSkyboxRenderer, CustomCloudsRenderer {
    @Override
    public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
        if (UpfallFog.isBlinded()) {
            return true;
        }
        // Undoes the weather layer tint: 1.21.1 never darkens the dusk glow in rain.
        skyRenderState.sunriseAndSunsetColor = ARGB.colorFromFloat(ARGB.alphaFloat(skyRenderState.sunriseAndSunsetColor), DuskGlow.RED, DuskGlow.GREEN, DuskGlow.BLUE);
        return false;
    }

    @Override
    public boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor, float cloudHeight, int cloudRange, Matrix4fc modelViewMatrix) {
        return true;
    }
}
