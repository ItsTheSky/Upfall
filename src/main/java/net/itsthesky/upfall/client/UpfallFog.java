package net.itsthesky.upfall.client;

import net.itsthesky.upfall.compat.mc.client.McCamera;
import net.itsthesky.upfall.content.enchanting.EnchantingEffects;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgradeEffects;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ViewportEvent;
//? if >=26.1 {
/*import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributes;
*///?} else {
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
//?}

public final class UpfallFog {
    private UpfallFog() {}

    private static float mistseaWeight;
    private static float blindWeight;

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != ModDimensions.UPFALL) return;
        if (blindWeight > 0.001F) {
            event.setRed(Mth.lerp(blindWeight, event.getRed(), 0.95F));
            event.setGreen(Mth.lerp(blindWeight, event.getGreen(), 0.93F));
            event.setBlue(Mth.lerp(blindWeight, event.getBlue(), 0.96F));
            return;
        }
        float up = Mth.clamp(-McCamera.xRot(event.getCamera()) / 50.0F, 0.0F, 1.0F) * 0.7F;
        if (up <= 0.0F) return;
        //? if >=26.1 {
        /*int skyColor = event.getCamera().attributeProbe().getValue(EnvironmentAttributes.SKY_COLOR, (float) event.getPartialTick());
        float skyRed = ARGB.redFloat(skyColor);
        float skyGreen = ARGB.greenFloat(skyColor);
        float skyBlue = ARGB.blueFloat(skyColor);
        *///?} else {
        Vec3 sky = mc.level.getSkyColor(event.getCamera().getPosition(), (float) event.getPartialTick());
        float skyRed = (float) sky.x;
        float skyGreen = (float) sky.y;
        float skyBlue = (float) sky.z;
        //?}
        event.setRed(Mth.lerp(up, event.getRed(), skyRed));
        event.setGreen(Mth.lerp(up, event.getGreen(), skyGreen));
        event.setBlue(Mth.lerp(up, event.getBlue(), skyBlue));
    }

    public static boolean isBlinded() {
        return blindWeight > 0.5F;
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != ModDimensions.UPFALL) return;
        //? if >=26.1 {
        /*if (event.getType() != FogType.ATMOSPHERIC) return;
        if (blindWeight > 0.001F) {
            event.getFogData().skyEnd = Mth.lerp(blindWeight, event.getFogData().skyEnd, 0.0F);
        }
        *///?} else {
        if (event.getType() != FogType.NONE) return;
        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            if (blindWeight <= 0.001F) return;
            event.setFarPlaneDistance(Mth.lerp(blindWeight, event.getFarPlaneDistance(), 0.0F));
            event.setNearPlaneDistance(0.0F);
            event.setCanceled(true);
            return;
        }
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        //?}
        Camera camera = event.getCamera();
        double y = McCamera.position(camera).y;

        boolean inMistsea = mc.level.getBiome(McCamera.blockPosition(camera)).is(ModBiomes.MISTSEA);
        mistseaWeight = Mth.lerp(0.02F, mistseaWeight, inMistsea ? 1.0F : 0.0F);

        //? if >=26.1 {
        /*float far = event.getEnvironment() instanceof AtmosphericFogEnvironment ? event.getFogData().renderDistanceEnd : event.getFarPlaneDistance();
        *///?} else {
        float far = event.getFarPlaneDistance();
        //?}
        float altitude = (float) Mth.clamp((y - 50.0) / 80.0, 0.0, 1.0);
        float factor = Mth.lerp(altitude, 0.5F, 1.0F) * Mth.lerp(mistseaWeight, 1.0F, 0.6F);
        boolean blind = mc.player != null && !EnchantingEffects.hasCloudsight(mc.player) && !QuantumUpgradeEffects.ignoresMist(mc.player)
                && mc.level.getBlockState(McCamera.blockPosition(camera)).is(ModBlocks.MIST.get());
        blindWeight = Mth.lerp(0.15F, blindWeight, blind ? 1.0F : 0.0F);
        float end = Mth.lerp(blindWeight, far * factor, 6.0F);
        float start = Mth.lerp(blindWeight, far * factor * Mth.lerp(altitude, 0.05F, 0.55F), 0.0F);
        //? if >=26.1 {
        /*if (event.getEnvironment() instanceof AtmosphericFogEnvironment) {
            // 1.21.1 terrain fog is cylindrical; on 26.1 only the render-distance pair is, the environmental pair is spherical
            event.getFogData().renderDistanceStart = start;
            event.getFogData().renderDistanceEnd = end;
            event.setNearPlaneDistance(Float.MAX_VALUE);
            event.setFarPlaneDistance(Float.MAX_VALUE);
            return;
        }
        *///?}
        event.setFarPlaneDistance(end);
        event.setNearPlaneDistance(start);
        //? if <26.1 {
        event.setCanceled(true);
        //?}
    }
}
