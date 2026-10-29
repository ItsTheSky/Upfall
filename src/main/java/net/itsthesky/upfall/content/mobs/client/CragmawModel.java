package net.itsthesky.upfall.content.mobs.client;

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
import net.itsthesky.upfall.content.mobs.entity.Cragmaw;
import net.minecraft.client.model.HierarchicalModel;
//?}

//? if >=26.1 {
/*public class CragmawModel extends EntityModel<CragmawRenderState> {
*///?} else {
public class CragmawModel extends HierarchicalModel<Cragmaw> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("cragmaw"), "main");

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart hindRight;
    private final ModelPart hindLeft;

    public CragmawModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail = body.getChild("tail");
        this.tailTip = tail.getChild("tail_tip");
        this.frontRight = root.getChild("front_right");
        this.frontLeft = root.getChild("front_left");
        this.hindRight = root.getChild("hind_right");
        this.hindLeft = root.getChild("hind_left");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -4.5F, -9.0F, 12.0F, 9.0F, 18.0F), PartPose.offset(0.0F, 12.0F, 1.0F));
        body.addOrReplaceChild("plate_front", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -4.0F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -4.5F, -5.0F));
        body.addOrReplaceChild("plate_mid", CubeListBuilder.create().texOffs(12, 30).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        body.addOrReplaceChild("plate_rear", CubeListBuilder.create().texOffs(24, 30).addBox(-1.0F, -2.0F, -1.5F, 2.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -4.5F, 5.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(64, 0).addBox(-4.5F, -3.5F, -6.0F, 9.0F, 7.0F, 6.0F), PartPose.offset(0.0F, 0.5F, -9.0F));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(64, 14).addBox(-3.5F, -1.5F, -4.0F, 7.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 0.5F, -6.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(64, 22).addBox(-3.0F, 0.0F, -7.0F, 6.0F, 2.0F, 7.0F),
                PartPose.offset(0.0F, 2.5F, -3.0F));
        jaw.addOrReplaceChild("right_tusk", CubeListBuilder.create().texOffs(92, 14).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-2.5F, 0.0F, -6.0F));
        jaw.addOrReplaceChild("left_tusk", CubeListBuilder.create().texOffs(92, 14).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(2.5F, 0.0F, -6.0F));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(96, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(-3.5F, -3.5F, -1.0F, 0.4F, 0.3F, 0.0F));
        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(3.5F, -3.5F, -1.0F, 0.4F, -0.3F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(16, 40).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, -1.5F, 9.0F));
        PartDefinition tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(40, 40).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        tip.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(60, 40).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 6.0F));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F);
        root.addOrReplaceChild("front_right", leg, PartPose.offset(-4.5F, 16.0F, -6.5F));
        root.addOrReplaceChild("front_left", leg, PartPose.offset(4.5F, 16.0F, -6.5F));
        root.addOrReplaceChild("hind_right", leg, PartPose.offset(-4.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("hind_left", leg, PartPose.offset(4.5F, 16.0F, 7.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    //? if >=26.1 {
    /*@Override
    public void setupAnim(CragmawRenderState state) {
        super.setupAnim(state);
        pose(state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot, state.aggressive, state.attackAnim);
    }
    *///?} else {
    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Cragmaw entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        pose(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, entity.isAggressive(), entity.attackAnim);
    }
    //?}

    private void pose(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, boolean aggressive, float attackAnim) {
        float swing = limbSwing * 0.6F;
        float amount = limbSwingAmount * 1.1F;
        frontRight.xRot = Mth.cos(swing) * amount;
        hindLeft.xRot = Mth.cos(swing) * amount;
        frontLeft.xRot = Mth.cos(swing + Mth.PI) * amount;
        hindRight.xRot = Mth.cos(swing + Mth.PI) * amount;

        body.xRot = aggressive ? -0.05F : 0.0F;
        body.zRot = Mth.cos(swing) * 0.03F * limbSwingAmount;
        head.xRot = headPitch * Mth.DEG_TO_RAD + (aggressive ? 0.15F : 0.0F);
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        float bite = Mth.sin(attackAnim * Mth.PI);
        jaw.xRot = (aggressive ? 0.35F + Mth.sin(ageInTicks * 0.4F) * 0.05F : 0.05F) + bite * 0.5F;

        tail.yRot = Mth.sin(ageInTicks * 0.08F) * 0.12F + Mth.cos(swing) * 0.25F * limbSwingAmount;
        tailTip.yRot = Mth.sin(ageInTicks * 0.08F - 0.9F) * 0.22F;
        tail.xRot = aggressive ? -0.3F : 0.1F;
    }
}
