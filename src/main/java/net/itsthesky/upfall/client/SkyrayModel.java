package net.itsthesky.upfall.client;

import net.itsthesky.upfall.Upfall;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
//? if >=26.1 {
/*import net.minecraft.client.model.EntityModel;
*///?} else {
import net.itsthesky.upfall.entity.Skyray;
import net.minecraft.client.model.HierarchicalModel;
//?}

//? if >=26.1 {
/*public class SkyrayModel extends EntityModel<SkyrayRenderState> {
*///?} else {
public class SkyrayModel extends HierarchicalModel<Skyray> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("skyray"), "main");

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;

    public SkyrayModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
        ModelPart body = root.getChild("body");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.tail = body.getChild("tail");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -1.5F, -6.0F, 8.0F, 3.0F, 12.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 15).addBox(0.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F),
                PartPose.offset(4.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(20, 15).addBox(-10.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F),
                PartPose.offset(-4.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 6.0F));
        body.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(40, 11).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offset(2.5F, 0.0F, -6.0F));
        body.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(40, 11).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offset(-2.5F, 0.0F, -6.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    //? if >=26.1 {
    /*@Override
    public void setupAnim(SkyrayRenderState state) {
        super.setupAnim(state);
        pose(state.ageInTicks, state.entityId);
    }
    *///?} else {
    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Skyray entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        pose(ageInTicks, entity.getId());
    }
    //?}

    private void pose(float ageInTicks, int entityId) {
        float flap = Mth.sin(ageInTicks * 0.13F + entityId) * 0.32F;
        leftWing.zRot = flap;
        rightWing.zRot = -flap;
        tail.yRot = Mth.sin(ageInTicks * 0.09F + entityId) * 0.25F;
        tail.xRot = Mth.cos(ageInTicks * 0.07F) * 0.08F;
    }
}
