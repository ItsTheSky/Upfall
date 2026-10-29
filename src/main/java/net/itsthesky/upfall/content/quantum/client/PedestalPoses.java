package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

final class PedestalPoses {
    static final float GHOST_RED = 0.75F;
    static final float GHOST_GREEN = 0.9F;
    static final float GHOST_BLUE = 1.0F;

    private PedestalPoses() {}

    static void item(PoseStack poseStack, float time, boolean ritual) {
        float spin = time * (ritual ? 12.0F : 2.0F);
        float lift = ritual ? 0.35F : 0.0F;
        poseStack.translate(0.5, 1.25 + lift + Mth.sin(time * 0.08F) * 0.06, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin % 360.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);
    }

    static int ghostAlpha(float time) {
        return (int) (90 + 50 * Mth.sin(time * 0.1F));
    }

    static List<BlockPos> ghostRing(Level level, BlockPos center) {
        List<BlockPos> ring = new ArrayList<>(QuantumPedestalBlockEntity.sides(center));
        ring.addAll(QuantumPedestalBlockEntity.corners(center));
        ring.removeIf(pos -> level.getBlockState(pos).is(QuantumBlocks.PEDESTAL.get()));
        return ring;
    }
}
