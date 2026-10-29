package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class FoldSatelliteRenderer extends MobRenderer<FoldSatellite, FoldSatelliteRenderState, FoldSatelliteModel> {
    private static final Identifier TEXTURE = Upfall.id("textures/entity/fold_satellite.png");
    private static final RenderType GLOW = RenderTypes.eyes(Upfall.id("textures/entity/fold_satellite_glow.png"));

    public FoldSatelliteRenderer(EntityRendererProvider.Context context) {
        super(context, new FoldSatelliteModel(context.bakeLayer(FoldSatelliteModel.LAYER)), 0.0F);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public FoldSatelliteRenderState createRenderState() {
        return new FoldSatelliteRenderState();
    }

    @Override
    public void extractRenderState(FoldSatellite entity, FoldSatelliteRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entityId = entity.getId();
        state.mode = entity.mode();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public Identifier getTextureLocation(FoldSatelliteRenderState state) {
        return TEXTURE;
    }
}
