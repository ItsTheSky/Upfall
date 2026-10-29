package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.ToIntFunction;

public class RopeLineRenderer implements BlockEntityRenderer<RopeAnchorBlockEntity> {
    public RopeLineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RopeAnchorBlockEntity anchor, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = anchor.getLevel();
        BlockState state = anchor.getBlockState();
        if (level == null || anchor.links().isEmpty() || !(state.getBlock() instanceof AbstractRopeAnchorBlock block)) {
            return;
        }
        boolean zipline = block.isZipline();
        BlockPos origin = anchor.getBlockPos();
        Vec3 from = block.attachPoint(origin, state);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(RopeLineMesh.TEXTURE));
        ToIntFunction<BlockPos> lightAt = pos -> LevelRenderer.getLightColor(level, pos);
        for (BlockPos other : anchor.links()) {
            Vec3 to = RopeLineMesh.otherEnd(level, other, zipline);
            if (to == null) continue;
            double sag = RopeLines.sag(from, to, zipline);
            RopeLineMesh.drawHalf(poseStack.last(), buffer, lightAt, origin, from, to, sag, RopeLineMesh.width(zipline));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RopeAnchorBlockEntity anchor) {
        return !anchor.links().isEmpty();
    }

    @Override
    public int getViewDistance() {
        return 192;
    }

    @Override
    public AABB getRenderBoundingBox(RopeAnchorBlockEntity anchor) {
        return RopeLineMesh.renderBox(anchor);
    }
}
