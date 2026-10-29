package net.itsthesky.upfall.content.quantum.client;

import net.itsthesky.upfall.compat.mc.client.McCamera;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.gas.VeilProtection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class VeilVision {
    private VeilVision() {}

    private static volatile boolean active;
    private static float haze;
    private static boolean inGas;

    public static boolean isActive() {
        return active;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean now = player != null && VeilProtection.isProtected(player);
        if (now != active) {
            active = now;
            if (mc.levelRenderer != null && mc.level != null) {
                mc.levelRenderer.allChanged();
            }
        }
        inGas = player != null && mc.level != null
                && mc.level.getBlockState(BlockPos.containing(player.getEyePosition())).is(QuantumBlocks.VEIL_GAS.get());
        if (inGas && !mc.isPaused()) motes(mc, player);
    }

    private static void motes(Minecraft mc, LocalPlayer player) {
        RandomSource random = player.getRandom();
        int count = active ? 1 : 4;
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5) * 6.0;
            double y = player.getEyeY() + (random.nextDouble() - 0.6) * 3.0;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 6.0;
            if (!mc.level.getBlockState(BlockPos.containing(x, y, z)).is(QuantumBlocks.VEIL_GAS.get())) continue;
            mc.level.addParticle(QuantumParticles.VEIL_MOTE.get(), x, y, z,
                    (random.nextDouble() - 0.5) * 0.015, (random.nextDouble() - 0.3) * 0.01, (random.nextDouble() - 0.5) * 0.015);
        }
    }

    private static boolean cameraInGas(ViewportEvent event) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getBlockState(McCamera.blockPosition(event.getCamera())).is(QuantumBlocks.VEIL_GAS.get());
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        //? if >=26.1 {
        /*if (event.getType() != FogType.ATMOSPHERIC) return;
        float target = cameraInGas(event) ? 1.0F : 0.0F;
        // 1.21.1 posts RenderFog three times per frame (sky, sky callback, terrain), 26.1 once: same fade speed per frame
        for (int i = 0; i < 3; i++) {
            haze = Mth.lerp(0.08F, haze, target);
        }
        *///?} else {
        if (event.getType() != FogType.NONE) return;
        haze = Mth.lerp(0.08F, haze, cameraInGas(event) ? 1.0F : 0.0F);
        //?}
        if (haze < 0.01F) return;
        float far = active ? 36.0F : 14.0F;
        //? if >=26.1 {
        /*if (event.getEnvironment() instanceof net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment) {
            event.getFogData().renderDistanceEnd = Mth.lerp(haze, event.getFogData().renderDistanceEnd, far);
            event.getFogData().renderDistanceStart = Mth.lerp(haze, event.getFogData().renderDistanceStart, 0.0F);
        } else {
            event.setFarPlaneDistance(Mth.lerp(haze, event.getFarPlaneDistance(), far));
            event.setNearPlaneDistance(Mth.lerp(haze, event.getNearPlaneDistance(), 0.0F));
        }
        event.getFogData().skyEnd = Mth.lerp(haze, event.getFogData().skyEnd, far);
        event.getFogData().cloudEnd = Mth.lerp(haze, event.getFogData().cloudEnd, far);
        *///?} else {
        event.setFarPlaneDistance(Mth.lerp(haze, event.getFarPlaneDistance(), far));
        event.setNearPlaneDistance(Mth.lerp(haze, event.getNearPlaneDistance(), 0.0F));
        event.setCanceled(true);
        //?}
    }

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (haze < 0.01F) return;
        float h = haze * (active ? 0.6F : 0.85F);
        event.setRed(Mth.lerp(h, event.getRed(), active ? 0.48F : 0.40F));
        event.setGreen(Mth.lerp(h, event.getGreen(), active ? 0.42F : 0.46F));
        event.setBlue(Mth.lerp(h, event.getBlue(), active ? 0.62F : 0.50F));
    }
}
