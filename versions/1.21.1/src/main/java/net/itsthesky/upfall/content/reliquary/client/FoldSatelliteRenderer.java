package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class FoldSatelliteRenderer extends MobRenderer<FoldSatellite, FoldSatelliteModel> {
    private static final ResourceLocation TEXTURE = Upfall.id("textures/entity/fold_satellite.png");
    private static final RenderType GLOW = RenderType.eyes(Upfall.id("textures/entity/fold_satellite_glow.png"));

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
    protected float getFlipDegrees(FoldSatellite entity) {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(FoldSatellite entity) {
        return TEXTURE;
    }
}
