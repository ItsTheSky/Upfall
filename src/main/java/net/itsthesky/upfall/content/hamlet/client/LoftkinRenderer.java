package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LoftkinRenderer extends MobRenderer<Loftkin, LoftkinModel> {
    private static final ResourceLocation[] TEXTURES = {
            Upfall.id("textures/entity/loftkin/loftkin_dusk.png"),
            Upfall.id("textures/entity/loftkin/loftkin_sky.png"),
            Upfall.id("textures/entity/loftkin/loftkin_pearl.png")};

    public LoftkinRenderer(EntityRendererProvider.Context context) {
        super(context, new LoftkinModel(context.bakeLayer(LoftkinModel.LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(Loftkin entity) {
        return TEXTURES[Math.floorMod(entity.getVariant(), TEXTURES.length)];
    }
}
