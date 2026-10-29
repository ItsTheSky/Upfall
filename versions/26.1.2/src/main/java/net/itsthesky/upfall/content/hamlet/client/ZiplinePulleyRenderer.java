package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class ZiplinePulleyRenderer extends EntityRenderer<ZiplinePulley, ZiplinePulleyRenderer.State> {
    private final PulleyModel model;

    public ZiplinePulleyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PulleyModel(context.bakeLayer(ZiplinePulleyModelParts.LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ZiplinePulley pulley, State state, float partialTick) {
        super.extractRenderState(pulley, state, partialTick);
        state.yRot = Mth.lerp(partialTick, pulley.yRotO, pulley.getYRot());
        state.travelled = pulley.getProgress() + pulley.getSpeed() * partialTick;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        collector.submitModel(model, state, poseStack, ZiplinePulleyModelParts.TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        float yRot;
        double travelled;
    }

    private static final class PulleyModel extends EntityModel<State> {
        private final ModelPart wheel;

        PulleyModel(ModelPart root) {
            super(root);
            this.wheel = root.getChild("wheel");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            wheel.xRot = ZiplinePulleyModelParts.wheelAngle(state.travelled);
        }
    }
}
