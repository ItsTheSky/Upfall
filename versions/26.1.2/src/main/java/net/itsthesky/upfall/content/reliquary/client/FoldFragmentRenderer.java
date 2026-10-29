package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.reliquary.boss.FoldFragment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class FoldFragmentRenderer extends EntityRenderer<FoldFragment, EntityRenderState> {
    private static final RenderType BODY = RenderTypes.entityCutout(FoldWardenRenderer.TEXTURE);
    private static final RenderType OUTLINE = RenderTypes.outline(FoldWardenRenderer.TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(FoldWardenRenderer.GLOW_TEXTURE);

    private final FoldWyrmSegmentModel model;

    public FoldFragmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FoldWyrmSegmentModel(context.bakeLayer(FoldWyrmSegmentModel.LAYER));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        int outline = state.outlineColor;
        poseStack.pushPose();
        FoldWyrmPass.fragmentTransform(poseStack, state.ageInTicks, state.boundingBoxHeight);
        collector.submitCustomGeometry(poseStack, BODY, (pose, consumer) -> model.draw(pose, consumer, 0.0F, 0.0F, light, OverlayTexture.NO_OVERLAY, -1));
        if (outline != 0) {
            collector.submitCustomGeometry(poseStack, OUTLINE, (pose, consumer) -> model.draw(pose, consumer, 0.0F, 0.0F, light, OverlayTexture.NO_OVERLAY, outline));
        }
        collector.submitCustomGeometry(poseStack, GLOW, (pose, consumer) -> model.draw(pose, consumer, 0.0F, 0.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, -1));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
