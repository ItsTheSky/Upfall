package net.itsthesky.upfall.content.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

final class ClockworkPoses {
    private ClockworkPoses() {}

    static float gearAngle(boolean active, long gameTime, float partialTick) {
        return active ? (gameTime + partialTick) * 9.0F : 0.0F;
    }

    static void gear(PoseStack poseStack, Direction facing, float angle) {
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().rotationTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    static void rewinderItem(PoseStack poseStack, float time) {
        poseStack.translate(0.5, 1.15 + Math.sin(time * 0.08) * 0.04, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * 3.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
    }
}
