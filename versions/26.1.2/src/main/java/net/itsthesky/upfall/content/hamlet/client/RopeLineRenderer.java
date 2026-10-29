package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class RopeLineRenderer implements BlockEntityRenderer<RopeAnchorBlockEntity, RopeLineRenderer.State> {
    public RopeLineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RopeAnchorBlockEntity anchor, State state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(anchor, state, partialTick, cameraPosition, crumbling);
        state.lines.clear();
        state.level = null;
        Level level = anchor.getLevel();
        BlockState blockState = anchor.getBlockState();
        if (level == null || anchor.links().isEmpty() || !(blockState.getBlock() instanceof AbstractRopeAnchorBlock block)) {
            return;
        }
        boolean zipline = block.isZipline();
        BlockPos origin = anchor.getBlockPos();
        Vec3 from = block.attachPoint(origin, blockState);
        for (BlockPos other : anchor.links()) {
            Vec3 to = RopeLineMesh.otherEnd(level, other, zipline);
            if (to == null) continue;
            state.lines.add(new Line(from, to, RopeLines.sag(from, to, zipline), RopeLineMesh.width(zipline)));
        }
        state.level = level;
        state.origin = origin;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Level level = state.level;
        if (level == null || state.lines.isEmpty()) {
            return;
        }
        ToIntFunction<BlockPos> lightAt = pos -> LevelRenderer.getLightCoords(level, pos);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(RopeLineMesh.TEXTURE), (pose, buffer) -> {
            for (Line line : state.lines) {
                RopeLineMesh.drawHalf(pose, buffer, lightAt, state.origin, line.from(), line.to(), line.sag(), line.width());
            }
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 192;
    }

    @Override
    public AABB getRenderBoundingBox(RopeAnchorBlockEntity anchor) {
        return RopeLineMesh.renderBox(anchor);
    }

    public static class State extends BlockEntityRenderState {
        final List<Line> lines = new ArrayList<>();
        @Nullable Level level;
        BlockPos origin = BlockPos.ZERO;
    }

    record Line(Vec3 from, Vec3 to, double sag, float width) {}
}
