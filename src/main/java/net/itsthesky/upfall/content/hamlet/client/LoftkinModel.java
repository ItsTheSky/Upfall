package net.itsthesky.upfall.content.hamlet.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
//? if >=26.1 {
/*import net.minecraft.client.model.EntityModel;
*///?} else {
import net.minecraft.client.model.HierarchicalModel;
//?}
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

//? if >=26.1 {
/*public class LoftkinModel extends EntityModel<LoftkinRenderState> {
*///?} else {
public class LoftkinModel extends HierarchicalModel<Loftkin> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("loftkin"), "main");
    private static final ResourceLocation[] TEXTURES = {
            Upfall.id("textures/entity/loftkin/loftkin_dusk.png"),
            Upfall.id("textures/entity/loftkin/loftkin_sky.png"),
            Upfall.id("textures/entity/loftkin/loftkin_pearl.png")};

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart head;
    private final ModelPart tassel;
    private final ModelPart body;
    private final ModelPart poncho;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart parasol;

    public LoftkinModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
        this.head = root.getChild("head");
        this.tassel = head.getChild("tassel");
        this.body = root.getChild("body");
        this.poncho = body.getChild("poncho");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.parasol = root.getChild("parasol");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)),
                PartPose.ZERO);
        head.addOrReplaceChild("tassel", CubeListBuilder.create().texOffs(44, 32).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -7.5F, 4.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("poncho", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-5.5F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(5.5F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(32, 32).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("parasol", CubeListBuilder.create()
                        .texOffs(48, 48).addBox(-0.5F, -12.0F, -0.5F, 1.0F, 12.0F, 1.0F)
                        .texOffs(0, 48).addBox(-6.0F, -13.0F, -6.0F, 12.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static ResourceLocation texture(int variant) {
        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }

    //? if >=26.1 {
    /*@Override
    public void setupAnim(LoftkinRenderState state) {
        super.setupAnim(state);
        pose(state.entityId, state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot,
                state.unhappyCounter, state.waveAnimation.isStarted(), state.waveAnimation.getTimeInMillis(state.ageInTicks) / 1000.0F,
                state.onZipline, state.hasParasol);
    }
    *///?} else {
    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Loftkin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        entity.waveAnimation.updateTime(ageInTicks, 1.0F);
        if (pose(entity.getId(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, entity.getUnhappyCounter(),
                entity.waveAnimation.isStarted(), entity.waveAnimation.getAccumulatedTime() / 1000.0F,
                entity.getVehicle() instanceof ZiplinePulley, entity.hasParasol())) {
            entity.waveAnimation.stop();
        }
    }
    //?}

    private boolean pose(int entityId, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                         int unhappyCounter, boolean waving, float seconds, boolean onZipline, boolean hasParasol) {
        float phase = entityId * 0.7F;
        float breath = Mth.sin(ageInTicks * 0.07F + phase);

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        head.zRot = Mth.sin(ageInTicks * 0.04F + phase) * 0.07F;
        head.y = breath * 0.25F;
        poncho.y = breath * 0.2F;
        tassel.xRot = -0.9F + Mth.sin(ageInTicks * 0.09F + phase) * 0.12F + limbSwingAmount * 0.5F;
        tassel.yRot = Mth.sin(ageInTicks * 0.05F + phase) * 0.15F;

        float walk = limbSwing * 0.6662F;
        rightLeg.xRot = Mth.cos(walk) * 1.1F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(walk + Mth.PI) * 1.1F * limbSwingAmount;
        rightArm.xRot = Mth.cos(walk + Mth.PI) * 0.7F * limbSwingAmount;
        leftArm.xRot = Mth.cos(walk) * 0.7F * limbSwingAmount;
        rightArm.zRot = 0.06F + Mth.sin(ageInTicks * 0.067F + phase) * 0.04F;
        leftArm.zRot = -0.06F - Mth.sin(ageInTicks * 0.067F + phase) * 0.04F;
        body.zRot = Mth.sin(ageInTicks * 0.035F + phase) * 0.025F;

        if (unhappyCounter > 0) {
            head.zRot = 0.3F * Mth.sin(ageInTicks * 0.45F);
            head.xRot = 0.35F;
        }

        boolean waveFinished = false;
        if (waving) {
            if (seconds < 2.2F) {
                float raise = Math.min(1.0F, seconds * 4.0F) * Math.min(1.0F, (2.2F - seconds) * 4.0F);
                rightArm.xRot = Mth.lerp(raise, rightArm.xRot, -0.2F);
                rightArm.zRot = Mth.lerp(raise, rightArm.zRot, 2.5F + Mth.sin(seconds * 11.0F) * 0.35F);
                head.zRot += raise * 0.12F;
            } else {
                waveFinished = true;
            }
        }

        if (onZipline) {
            rightArm.xRot = -3.0F;
            leftArm.xRot = -3.0F;
            rightArm.zRot = -0.12F;
            leftArm.zRot = 0.12F;
            rightLeg.xRot = Mth.sin(ageInTicks * 0.25F) * 0.35F;
            leftLeg.xRot = Mth.sin(ageInTicks * 0.25F + 1.2F) * 0.35F;
            tassel.xRot = -0.2F;
        }

        parasol.visible = hasParasol;
        if (parasol.visible) {
            rightArm.xRot = -2.95F;
            leftArm.xRot = -2.95F;
            rightArm.zRot = -0.28F;
            leftArm.zRot = 0.28F;
            parasol.zRot = Mth.sin(ageInTicks * 0.2F) * 0.08F;
            parasol.xRot = Mth.cos(ageInTicks * 0.17F) * 0.06F;
            rightLeg.xRot = Mth.sin(ageInTicks * 0.3F) * 0.25F;
            leftLeg.xRot = -rightLeg.xRot;
            tassel.xRot = -0.3F + Mth.sin(ageInTicks * 0.4F) * 0.2F;
        }
        return waveFinished;
    }
}
