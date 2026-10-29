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
import net.itsthesky.upfall.content.mobs.entity.Mistjelly;
import net.minecraft.client.model.HierarchicalModel;
//?}

//? if >=26.1 {
/*public class MistjellyModel extends EntityModel<MistjellyRenderState> {
*///?} else {
public class MistjellyModel extends HierarchicalModel<Mistjelly> {
//?}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("mistjelly"), "main");
    public static final float RENDER_SCALE = 1.4F;
    public static final int BLOCK_LIGHT = 13;
    private static final float[][] TENTACLES = {{-3.5F, -3.5F}, {3.5F, -3.5F}, {-3.5F, 3.5F}, {3.5F, 3.5F}};
    private static final float[][] ARMS = {{-2.0F, -1.5F}, {2.0F, -1.5F}, {-2.0F, 1.5F}, {2.0F, 1.5F}};

    //? if <26.1 {
    private final ModelPart root;
    //?}
    private final ModelPart bell;
    private final ModelPart under;
    private final ModelPart[] upper = new ModelPart[4];
    private final ModelPart[] lower = new ModelPart[4];
    private final ModelPart[] arms = new ModelPart[4];

    public MistjellyModel(ModelPart root) {
        //? if >=26.1 {
        /*super(root);
        *///?} else {
        this.root = root;
        //?}
        this.bell = root.getChild("bell");
        this.under = root.getChild("under");
        for (int i = 0; i < 4; i++) {
            upper[i] = under.getChild("tentacle_" + i);
            lower[i] = upper[i].getChild("tentacle_end_" + i);
            arms[i] = under.getChild("arm_" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bell = root.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition dome = bell.addOrReplaceChild("dome", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        dome.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(26, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition under = root.addOrReplaceChild("under", CubeListBuilder.create().texOffs(0, 24).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 1.0F, 10.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        for (int i = 0; i < 4; i++) {
            PartDefinition upper = under.addOrReplaceChild("tentacle_" + i, CubeListBuilder.create().texOffs(52, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offset(TENTACLES[i][0], 1.0F, TENTACLES[i][1]));
            upper.addOrReplaceChild("tentacle_end_" + i, CubeListBuilder.create().texOffs(56, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offset(0.0F, 6.0F, 0.0F));
            under.addOrReplaceChild("arm_" + i, CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 8.0F, 1.0F),
                    PartPose.offset(ARMS[i][0], 1.0F, ARMS[i][1]));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    //? if >=26.1 {
    /*@Override
    public void setupAnim(MistjellyRenderState state) {
        super.setupAnim(state);
        pose(state.ageInTicks, state.entityId, state.aggressive, state.horizontalSpeed);
    }
    *///?} else {
    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Mistjelly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        pose(ageInTicks, entity.getId(), entity.isAggressive(), (float) entity.getDeltaMovement().horizontalDistance());
    }
    //?}

    private void pose(float ageInTicks, int entityId, boolean aggressive, float horizontalSpeed) {
        float t = ageInTicks + entityId * 13.0F;
        float rate = aggressive ? 0.28F : 0.12F;
        float pulse = Mth.sin(t * rate);
        float bob = Mth.sin(t * 0.07F) * 0.6F;
        bell.y = 9.0F + bob;
        under.y = 9.0F + bob + pulse * 0.5F;
        bell.yScale = 1.0F + pulse * 0.1F;
        bell.xScale = 1.0F - pulse * 0.06F;
        bell.zScale = bell.xScale;
        under.xScale = bell.xScale;
        under.zScale = bell.xScale;

        float drift = horizontalSpeed * 2.0F;
        for (int i = 0; i < 4; i++) {
            float phase = t * 0.09F + i * 1.3F;
            upper[i].xRot = Mth.sin(phase) * 0.22F + drift * 0.5F - pulse * 0.1F;
            upper[i].zRot = Mth.cos(phase * 0.9F) * 0.18F + (i % 2 == 0 ? -1 : 1) * (0.1F + pulse * 0.05F);
            lower[i].xRot = Mth.sin(phase - 0.9F) * 0.35F;
            lower[i].zRot = Mth.cos(phase * 0.9F - 0.9F) * 0.25F;
            arms[i].xRot = Mth.sin(t * 0.06F + i * 2.1F) * 0.15F;
            arms[i].zRot = Mth.cos(t * 0.06F + i * 2.1F) * 0.15F;
        }
    }
}
