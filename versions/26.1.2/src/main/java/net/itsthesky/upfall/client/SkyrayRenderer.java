package net.itsthesky.upfall.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.entity.Skyray;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class SkyrayRenderer extends MobRenderer<Skyray, SkyrayRenderState, SkyrayModel> {
    private static final Identifier TEXTURE = Upfall.id("textures/entity/skyray.png");

    public SkyrayRenderer(EntityRendererProvider.Context context) {
        super(context, new SkyrayModel(context.bakeLayer(SkyrayModel.LAYER)), 0.0F);
    }

    @Override
    public SkyrayRenderState createRenderState() {
        return new SkyrayRenderState();
    }

    @Override
    public void extractRenderState(Skyray entity, SkyrayRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entityId = entity.getId();
    }

    @Override
    public Identifier getTextureLocation(SkyrayRenderState state) {
        return TEXTURE;
    }
}
