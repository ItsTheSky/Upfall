package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.reliquary.boss.FoldFragment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class FoldFragmentRenderer extends EntityRenderer<FoldFragment> {
    private final FoldWyrmSegmentModel model;

    public FoldFragmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FoldWyrmSegmentModel(context.bakeLayer(FoldWyrmSegmentModel.LAYER));
    }

    @Override
    public void render(FoldFragment entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        FoldWyrmPass.fragmentTransform(poseStack, entity.tickCount + partialTick, entity.getBbHeight());
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
