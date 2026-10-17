package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class FoldSatelliteModel extends HierarchicalModel<FoldSatellite> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("fold_satellite"), "main");

    private final ModelPart root;
    private final ModelPart spin;

    public FoldSatelliteModel(ModelPart root) {
        this.root = root;
        this.spin = root.getChild("spin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition spin = root.addOrReplaceChild("spin", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        spin.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 14.0F, 14.0F)
                        .texOffs(0, 28).addBox(-8.0F, -2.5F, -8.0F, 16.0F, 5.0F, 16.0F)
                        .texOffs(0, 49).addBox(-2.0F, -2.0F, -8.5F, 4.0F, 4.0F, 1.0F)
                        .texOffs(0, 49).addBox(-2.0F, -2.0F, 7.5F, 4.0F, 4.0F, 1.0F)
                        .texOffs(10, 49).addBox(-8.5F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F)
                        .texOffs(10, 49).addBox(7.5F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F),
                PartPose.rotation(Mth.PI / 4.0F, 0.0F, 0.61F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FoldSatellite entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = switch (entity.mode()) {
            case ORBIT, RISE -> 0.04F;
            case MARK -> 0.25F;
            case DIVE -> 0.5F;
        };
        spin.yRot = ageInTicks * speed + entity.getId();
        spin.y = 15.0F + (entity.mode() == FoldSatellite.Mode.MARK ? Mth.sin(ageInTicks * 0.8F) * 0.6F : 0.0F);
    }
}
