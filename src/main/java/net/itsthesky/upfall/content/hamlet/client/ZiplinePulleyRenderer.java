package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ZiplinePulleyRenderer extends EntityRenderer<ZiplinePulley> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("zipline_pulley"), "main");
    private static final ResourceLocation TEXTURE = Upfall.id("textures/entity/zipline_pulley.png");

    private final ModelPart root;
    private final ModelPart wheel;

    public ZiplinePulleyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.root = context.bakeLayer(LAYER);
        this.wheel = root.getChild("wheel");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.5F, -2.5F, 2.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("plates", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-2.0F, 0.0F, -2.0F, 1.0F, 6.0F, 4.0F)
                        .texOffs(10, 10).addBox(1.0F, 0.0F, -2.0F, 1.0F, 6.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("strap", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 6.0F, -0.5F, 2.0F, 5.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(ZiplinePulley pulley, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        double travelled = pulley.getProgress() + pulley.getSpeed() * partialTick;
        wheel.xRot = (float) (-travelled / (5.0 / 16.0 * Mth.PI) * Mth.TWO_PI);
        root.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(pulley, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplinePulley pulley) {
        return TEXTURE;
    }
}
