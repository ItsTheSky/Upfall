package net.itsthesky.upfall.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.entity.Skyray;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SkyrayRenderer extends MobRenderer<Skyray, SkyrayModel> {
    private static final ResourceLocation TEXTURE = Upfall.id("textures/entity/skyray.png");

    public SkyrayRenderer(EntityRendererProvider.Context context) {
        super(context, new SkyrayModel(context.bakeLayer(SkyrayModel.LAYER)), 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(Skyray entity) {
        return TEXTURE;
    }
}
