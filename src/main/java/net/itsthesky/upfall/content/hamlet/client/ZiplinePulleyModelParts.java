package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.Upfall;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

final class ZiplinePulleyModelParts {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("zipline_pulley"), "main");
    static final ResourceLocation TEXTURE = Upfall.id("textures/entity/zipline_pulley.png");

    private ZiplinePulleyModelParts() {}

    static LayerDefinition createLayer() {
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

    static float wheelAngle(double travelled) {
        return (float) (-travelled / (5.0 / 16.0 * Mth.PI) * Mth.TWO_PI);
    }
}
