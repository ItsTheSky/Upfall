package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LoftkinRenderer extends MobRenderer<Loftkin, LoftkinModel> {
    public LoftkinRenderer(EntityRendererProvider.Context context) {
        super(context, new LoftkinModel(context.bakeLayer(LoftkinModel.LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(Loftkin entity) {
        return LoftkinModel.texture(entity.getVariant());
    }
}
