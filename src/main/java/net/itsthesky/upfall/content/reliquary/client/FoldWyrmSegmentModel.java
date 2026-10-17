package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.itsthesky.upfall.Upfall;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class FoldWyrmSegmentModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("fold_warden"), "segment");

    private final ModelPart root;
    private final ModelPart pitch;
    private final ModelPart spine;

    public FoldWyrmSegmentModel(ModelPart root) {
        this.root = root;
        this.pitch = root.getChild("pitch");
        this.spine = pitch.getChild("spine");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pitch = root.addOrReplaceChild("pitch", CubeListBuilder.create(), PartPose.ZERO);
        pitch.addOrReplaceChild("shell", CubeListBuilder.create()
                        .texOffs(0, 136).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                        .texOffs(64, 136).addBox(-9.0F, -9.0F, -2.0F, 18.0F, 18.0F, 4.0F)
                        .texOffs(136, 136).addBox(-2.0F, -11.0F, -6.0F, 4.0F, 3.5F, 12.0F),
                PartPose.rotation(0.0F, 0.0F, Mth.PI / 4.0F));
        pitch.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(168, 136).addBox(-3.0F, -3.0F, -16.0F, 6.0F, 6.0F, 16.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }

    public void pose(float pitchRadians, float spineLength) {
        pitch.xRot = pitchRadians;
        spine.visible = spineLength > 0.0F;
        spine.zScale = Math.max(spineLength, 0.01F);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        root.render(poseStack, consumer, light, overlay);
    }
}
