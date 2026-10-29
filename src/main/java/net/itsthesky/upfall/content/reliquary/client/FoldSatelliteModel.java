package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
//? if >=26.1 {
/*import net.minecraft.client.model.EntityModel;
*///?} else {
import net.minecraft.client.model.HierarchicalModel;
//?}
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

//? if >=26.1 {
/*public class FoldSatelliteModel extends EntityModel<FoldSatelliteRenderState> {
*///?} else {
public class FoldSatelliteModel extends HierarchicalModel<FoldSatellite> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("fold_satellite"), "main");

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart spin;

    public FoldSatelliteModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
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

    //? if <26.1 {
    @Override
    public ModelPart root() {
        return root;
    }
    //?}

    //? if >=26.1 {
    /*@Override
    public void setupAnim(FoldSatelliteRenderState state) {
        super.setupAnim(state);
        pose(state.mode, state.ageInTicks, state.entityId);
    }
    *///?} else {
    @Override
    public void setupAnim(FoldSatellite entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        pose(entity.mode(), ageInTicks, entity.getId());
    }
    //?}

    private void pose(FoldSatellite.Mode mode, float ageInTicks, int entityId) {
        float speed = switch (mode) {
            case ORBIT, RISE -> 0.04F;
            case MARK -> 0.25F;
            case DIVE -> 0.5F;
        };
        spin.yRot = ageInTicks * speed + entityId;
        spin.y = 15.0F + (mode == FoldSatellite.Mode.MARK ? Mth.sin(ageInTicks * 0.8F) * 0.6F : 0.0F);
    }
}
