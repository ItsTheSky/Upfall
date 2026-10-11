package net.itsthesky.upfall.content.mobs.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.mobs.entity.Duskwing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class DuskwingModel extends HierarchicalModel<Duskwing> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("duskwing"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart crest;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftTip;
    private final ModelPart rightTip;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public DuskwingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.crest = head.getChild("crest");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.leftTip = leftWing.getChild("left_tip");
        this.rightTip = rightWing.getChild("right_tip");
        this.tail = body.getChild("tail");
        this.leftLeg = body.getChild("left_leg");
        this.rightLeg = body.getChild("right_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -5.0F, 6.0F, 5.0F, 10.0F), PartPose.offset(0.0F, 18.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -3.0F, -4.0F, 5.0F, 5.0F, 4.0F), PartPose.offset(0.0F, -0.5F, -5.0F));
        head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(20, 16).addBox(-1.0F, -0.5F, -3.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        head.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(32, 16).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, 0.3F, 0.0F, 0.0F));

        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 26).addBox(-8.0F, -0.5F, -3.5F, 8.0F, 1.0F, 8.0F), PartPose.offset(-3.0F, -1.5F, -1.0F));
        rightWing.addOrReplaceChild("right_tip", CubeListBuilder.create().texOffs(0, 36).addBox(-9.0F, -0.5F, -3.0F, 9.0F, 1.0F, 6.0F),
                PartPose.offset(-8.0F, 0.0F, 0.0F));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 26).mirror().addBox(0.0F, -0.5F, -3.5F, 8.0F, 1.0F, 8.0F), PartPose.offset(3.0F, -1.5F, -1.0F));
        leftWing.addOrReplaceChild("left_tip", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(0.0F, -0.5F, -3.0F, 9.0F, 1.0F, 6.0F),
                PartPose.offset(8.0F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(34, 26).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 8.0F), PartPose.offset(0.0F, 0.5F, 5.0F));
        tail.addOrReplaceChild("right_feather", CubeListBuilder.create().texOffs(34, 36).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 8.0F, 0.0F, 0.2F, 0.0F));
        tail.addOrReplaceChild("left_feather", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(0.0F, -0.5F, 0.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.5F, 0.0F, 8.0F, 0.0F, -0.2F, 0.0F));

        PartDefinition rightLeg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-1.5F, 2.5F, 1.0F));
        rightLeg.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(52, 36).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition leftLeg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(1.5F, 2.5F, 1.0F));
        leftLeg.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(52, 36).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Duskwing entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean diving = entity.isDiving();
        float phase = ageInTicks * 0.5F + entity.getId() * 1.7F;
        float flap = diving ? 0.15F : Mth.sin(phase) * 0.6F;
        float sweep = diving ? 1.1F : 0.0F;
        float rise = (float) entity.getDeltaMovement().y;
        body.xRot = diving ? 0.8F : Mth.clamp(-rise * 1.2F, -0.4F, 0.4F) + 0.05F;
        body.y = 18.0F + (diving ? 0.0F : Mth.sin(phase) * 0.8F);

        rightWing.zRot = flap;
        leftWing.zRot = -flap;
        rightWing.yRot = sweep;
        leftWing.yRot = -sweep;
        rightTip.zRot = diving ? 0.0F : flap * 0.8F + 0.1F;
        leftTip.zRot = -rightTip.zRot;
        rightTip.yRot = diving ? 0.5F : 0.0F;
        leftTip.yRot = -rightTip.yRot;

        head.xRot = -body.xRot * 0.7F + headPitch * Mth.DEG_TO_RAD;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.5F;
        crest.xRot = 0.3F + (diving ? 0.5F : Mth.sin(phase * 0.5F) * 0.08F);
        tail.xRot = diving ? -0.3F : Mth.sin(phase * 0.5F) * 0.1F;
        float legPose = diving ? -1.0F : 0.5F;
        leftLeg.xRot = legPose;
        rightLeg.xRot = legPose;
    }
}
