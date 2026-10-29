package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class FoldWardenRenderer extends MobRenderer<FoldWarden, FoldWardenModel> {
    static final ResourceLocation TEXTURE = Upfall.id("textures/entity/fold_warden.png");
    static final ResourceLocation GLOW_TEXTURE = Upfall.id("textures/entity/fold_warden_glow.png");
    private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;

    private final FoldWyrmSegmentModel segmentModel;

    public FoldWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new FoldWardenModel(context.bakeLayer(FoldWardenModel.LAYER)), 0.0F);
        this.segmentModel = new FoldWyrmSegmentModel(context.bakeLayer(FoldWyrmSegmentModel.LAYER));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public void render(FoldWarden entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
        List<FoldWyrmPass.Piece> pieces = FoldWyrmPass.pieces(entity, partialTick);
        if (pieces.isEmpty()) return;
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        for (FoldWyrmPass.Piece piece : pieces) {
            poseStack.pushPose();
            piece.transform(poseStack);
            segmentModel.pose(piece.pitch(), piece.spine());
            segmentModel.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay);
            segmentModel.render(poseStack, buffers.getBuffer(GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    @Override
    protected float getFlipDegrees(FoldWarden entity) {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(FoldWarden entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(FoldWarden entity) {
        return TEXTURE;
    }
}
