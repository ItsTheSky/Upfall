package net.itsthesky.upfall.content.chronospire.client;

import com.mojang.blaze3d.vertex.PoseStack;

final class DriftPlatformPlates {
    private DriftPlatformPlates() {}

    static void forEach(PoseStack poseStack, Runnable plate) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                poseStack.pushPose();
                poseStack.translate(i - 1.5, 0.0, j - 1.5);
                plate.run();
                poseStack.popPose();
            }
        }
    }
}
