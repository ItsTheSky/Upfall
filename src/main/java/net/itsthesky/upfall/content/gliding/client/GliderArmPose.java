package net.itsthesky.upfall.content.gliding.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
*///?} else {
import net.minecraft.world.entity.LivingEntity;
//?}

public final class GliderArmPose {
    private GliderArmPose() {}

    // Referenced from META-INF/enumextensions.json.
    public static final EnumProxy<HumanoidModel.ArmPose> PROXY = new EnumProxy<>(HumanoidModel.ArmPose.class,
            //? if >=26.1 {
            /*true, true, (IArmPoseTransformer) GliderArmPose::apply);
            *///?} else {
            true, (IArmPoseTransformer) GliderArmPose::apply);
            //?}

    //? if >=26.1 {
    /*private static void apply(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
    *///?} else {
    private static void apply(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
    //?}
        raise(model.rightArm);
        raise(model.leftArm);
    }

    private static void raise(ModelPart arm) {
        arm.xRot = (float) -Math.PI;
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
    }
}
