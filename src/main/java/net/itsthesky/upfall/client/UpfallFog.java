package net.itsthesky.upfall.client;

import net.itsthesky.upfall.content.enchanting.EnchantingEffects;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;

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
        float up = Mth.clamp(-event.getCamera().getXRot() / 50.0F, 0.0F, 1.0F) * 0.7F;
        if (up <= 0.0F) return;
        Vec3 sky = mc.level.getSkyColor(event.getCamera().getPosition(), (float) event.getPartialTick());
        event.setRed(Mth.lerp(up, event.getRed(), (float) sky.x));
        event.setGreen(Mth.lerp(up, event.getGreen(), (float) sky.y));
        event.setBlue(Mth.lerp(up, event.getBlue(), (float) sky.z));
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != ModDimensions.UPFALL) return;
        if (event.getType() != FogType.NONE || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        Camera camera = event.getCamera();
        double y = camera.getPosition().y;

        boolean inMistsea = mc.level.getBiome(camera.getBlockPosition()).is(ModBiomes.MISTSEA);
        mistseaWeight = Mth.lerp(0.02F, mistseaWeight, inMistsea ? 1.0F : 0.0F);

        float far = event.getFarPlaneDistance();
        float altitude = (float) Mth.clamp((y - 50.0) / 80.0, 0.0, 1.0);
        float factor = Mth.lerp(altitude, 0.5F, 1.0F) * Mth.lerp(mistseaWeight, 1.0F, 0.6F);
        boolean blind = mc.player != null && !EnchantingEffects.hasCloudsight(mc.player)
                && mc.level.getBlockState(camera.getBlockPosition()).is(ModBlocks.MIST.get());
        blindWeight = Mth.lerp(0.15F, blindWeight, blind ? 1.0F : 0.0F);
        event.setFarPlaneDistance(Mth.lerp(blindWeight, far * factor, 6.0F));
        event.setNearPlaneDistance(Mth.lerp(blindWeight, far * factor * Mth.lerp(altitude, 0.05F, 0.55F), 0.0F));
        event.setCanceled(true);
    }
}
