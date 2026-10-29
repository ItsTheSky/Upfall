package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ZiplinePulleyRenderer extends EntityRenderer<ZiplinePulley> {
    private final ModelPart root;
    private final ModelPart wheel;

    public ZiplinePulleyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.root = context.bakeLayer(ZiplinePulleyModelParts.LAYER);
        this.wheel = root.getChild("wheel");
    }

    @Override
    public void render(ZiplinePulley pulley, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        double travelled = pulley.getProgress() + pulley.getSpeed() * partialTick;
        wheel.xRot = ZiplinePulleyModelParts.wheelAngle(travelled);
        root.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(ZiplinePulleyModelParts.TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(pulley, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplinePulley pulley) {
        return ZiplinePulleyModelParts.TEXTURE;
    }
}
