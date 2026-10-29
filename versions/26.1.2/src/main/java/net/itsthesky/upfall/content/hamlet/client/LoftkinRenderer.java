package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class LoftkinRenderer extends MobRenderer<Loftkin, LoftkinRenderState, LoftkinModel> {
    public LoftkinRenderer(EntityRendererProvider.Context context) {
        super(context, new LoftkinModel(context.bakeLayer(LoftkinModel.LAYER)), 0.45F);
    }

    @Override
    public LoftkinRenderState createRenderState() {
        return new LoftkinRenderState();
    }

    @Override
    public void extractRenderState(Loftkin entity, LoftkinRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entityId = entity.getId();
        state.unhappyCounter = entity.getUnhappyCounter();
        state.onZipline = entity.getVehicle() instanceof ZiplinePulley;
        state.hasParasol = entity.hasParasol();
        state.variant = entity.getVariant();
        state.waveAnimation.copyFrom(entity.waveAnimation);
    }

    @Override
    public Identifier getTextureLocation(LoftkinRenderState state) {
        return LoftkinModel.texture(state.variant);
    }
}
