package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.itsthesky.upfall.content.reliquary.boss.FoldWardenSegment;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

final class FoldWyrmPass {
    private static final float FRAGMENT_SCALE = 0.7F;

    private FoldWyrmPass() {}

    record Piece(Vec3 offset, float yaw, float pitch, float spine, float size) {
        void transform(PoseStack poseStack) {
            poseStack.translate(offset.x, offset.y, offset.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            poseStack.scale(-size, -size, size);
        }
    }

    static void fragmentTransform(PoseStack poseStack, float ageInTicks, float height) {
        float spin = ageInTicks * 18.0F;
        poseStack.translate(0.0, height / 2.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.XP.rotationDegrees(spin * 0.7F));
        poseStack.scale(-FRAGMENT_SCALE, -FRAGMENT_SCALE, FRAGMENT_SCALE);
    }

    static List<Piece> pieces(FoldWarden entity, float partialTick) {
        FoldWarden.Phase phase = entity.phase();
        if (phase != FoldWarden.Phase.UNFOLD && phase != FoldWarden.Phase.WYRM) return List.of();
        float grow = phase == FoldWarden.Phase.UNFOLD
                ? Mth.clamp(((entity.phaseTicks() + partialTick) / FoldWarden.UNFOLD_TICKS - 0.2F) / 0.4F, 0.0F, 1.0F)
                : 1.0F;
        if (grow <= 0.01F) return List.of();
        List<Piece> pieces = new ArrayList<>();
        Vec3 origin = lerp(entity.xo, entity.yo, entity.zo, entity.getX(), entity.getY(), entity.getZ(), partialTick);
        Vec3 lead = origin.add(0.0, entity.getBbHeight() / 2.0, 0.0);
        for (FoldWardenSegment segment : entity.segments()) {
            Vec3 center = lerp(segment.xo, segment.yo, segment.zo, segment.getX(), segment.getY(), segment.getZ(), partialTick)
                    .add(0.0, segment.size / 2.0, 0.0);
            if (!entity.segmentVisible(segment.index)) break;
            Vec3 toLead = lead.subtract(center);
            double length = toLead.length();
            float yaw = length < 1.0E-4 ? entity.yBodyRot : (float) (Mth.atan2(toLead.z, toLead.x) * Mth.RAD_TO_DEG) - 90.0F;
            float pitch = length < 1.0E-4 ? 0.0F : (float) -Mth.atan2(toLead.y, toLead.horizontalDistance());
            float size = segment.size * grow;
            pieces.add(new Piece(center.subtract(origin), yaw, pitch, (float) (length / size), size));
            lead = center;
        }
        return pieces;
    }

    private static Vec3 lerp(double xo, double yo, double zo, double x, double y, double z, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, xo, x), Mth.lerp(partialTick, yo, y), Mth.lerp(partialTick, zo, z));
    }
}
