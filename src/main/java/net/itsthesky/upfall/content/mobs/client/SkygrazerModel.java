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
import net.itsthesky.upfall.content.mobs.entity.Skygrazer;
import net.minecraft.client.model.HierarchicalModel;
//?}

//? if >=26.1 {
/*public class SkygrazerModel extends EntityModel<SkygrazerRenderState> {
*///?} else {
public class SkygrazerModel extends HierarchicalModel<Skygrazer> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("skygrazer"), "main");
    public static final float SHADOW_RADIUS = 0.6F;
    public static final float BABY_SCALE = 0.55F;
    public static final float BABY_SHADOW_FACTOR = 0.5F;

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart tail;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart hindRight;
    private final ModelPart hindLeft;

    public SkygrazerModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
        ModelPart body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.tail = body.getChild("tail");
        this.frontRight = root.getChild("front_right");
        this.frontLeft = root.getChild("front_left");
        this.hindRight = root.getChild("hind_right");
        this.hindLeft = root.getChild("hind_left");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.0F, -7.0F, 9.0F, 8.0F, 14.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        body.addOrReplaceChild("fleece", CubeListBuilder.create().texOffs(0, 24).addBox(-6.0F, -6.0F, -8.5F, 12.0F, 11.0F, 17.0F), PartPose.ZERO);
        body.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(64, 0).addBox(-3.0F, -8.0F, -4.0F, 6.0F, 2.0F, 8.0F), PartPose.ZERO);
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(64, 12).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -3.0F, 8.5F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(96, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 7.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, -7.5F, 0.35F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(64, 24).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.35F, 0.0F, 0.0F));
        head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(64, 36).addBox(-1.5F, -1.0F, -3.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 0.5F, -5.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(88, 24).addBox(-4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -2.0F, -1.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(88, 24).mirror().addBox(0.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -2.0F, -1.0F, 0.0F, 0.0F, 0.4F));
        PartDefinition rightHorn = head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(96, 12).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, -3.0F, -1.5F, 0.0F, 0.0F, -0.35F));
        rightHorn.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(100, 12).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.8F));
        PartDefinition leftHorn = head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(96, 12).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, -3.0F, -1.5F, 0.0F, 0.0F, 0.35F));
        leftHorn.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(100, 12).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.8F));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(104, 12).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F);
        root.addOrReplaceChild("front_right", leg, PartPose.offset(-3.0F, 16.0F, -5.0F));
        root.addOrReplaceChild("front_left", leg, PartPose.offset(3.0F, 16.0F, -5.0F));
        root.addOrReplaceChild("hind_right", leg, PartPose.offset(-3.0F, 16.0F, 5.5F));
        root.addOrReplaceChild("hind_left", leg, PartPose.offset(3.0F, 16.0F, 5.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    //? if >=26.1 {
    /*@Override
    public void setupAnim(SkygrazerRenderState state) {
        super.setupAnim(state);
        pose(state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot, state.entityId);
    }
    *///?} else {
    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Skygrazer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        pose(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, entity.getId());
    }
    //?}

    private void pose(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, int entityId) {
        float swing = limbSwing * 0.6662F;
        frontRight.xRot = Mth.cos(swing) * 1.2F * limbSwingAmount;
        hindLeft.xRot = Mth.cos(swing) * 1.2F * limbSwingAmount;
        frontLeft.xRot = Mth.cos(swing + Mth.PI) * 1.2F * limbSwingAmount;
        hindRight.xRot = Mth.cos(swing + Mth.PI) * 1.2F * limbSwingAmount;

        neck.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.4F;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6F;
        head.xRot = -0.35F + headPitch * Mth.DEG_TO_RAD;
        float flop = Mth.sin(ageInTicks * 0.15F + entityId) * 0.05F + limbSwingAmount * Mth.cos(ageInTicks * 0.9F) * 0.15F;
        rightEar.zRot = -0.4F - flop;
        leftEar.zRot = 0.4F + flop;
        tail.xRot = 0.2F + Mth.sin(ageInTicks * 0.12F) * 0.1F;
    }
}
