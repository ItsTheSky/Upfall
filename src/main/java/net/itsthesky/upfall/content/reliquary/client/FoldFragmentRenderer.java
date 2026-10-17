package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.reliquary.boss.FoldFragment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class FoldFragmentRenderer extends EntityRenderer<FoldFragment> {
    private static final float SCALE = 0.7F;

    private final FoldWyrmSegmentModel model;

    public FoldFragmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FoldWyrmSegmentModel(context.bakeLayer(FoldWyrmSegmentModel.LAYER));
    }

    @Override
    public void render(FoldFragment entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float spin = (entity.tickCount + partialTick) * 18.0F;
        poseStack.pushPose();
        poseStack.translate(0.0, entity.getBbHeight() / 2.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.XP.rotationDegrees(spin * 0.7F));
        poseStack.scale(-SCALE, -SCALE, SCALE);
        model.pose(0.0F, 0.0F);
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(FoldWardenRenderer.TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        model.render(poseStack, buffers.getBuffer(RenderType.eyes(FoldWardenRenderer.GLOW_TEXTURE)), 0xF000F0, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FoldFragment entity) {
        return FoldWardenRenderer.TEXTURE;
    }
}
