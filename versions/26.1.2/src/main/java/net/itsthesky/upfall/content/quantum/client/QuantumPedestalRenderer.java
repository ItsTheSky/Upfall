package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class QuantumPedestalRenderer implements BlockEntityRenderer<QuantumPedestalBlockEntity, QuantumPedestalRenderer.State> {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final PedestalRenderer<QuantumPedestalBlockEntity> items;

    public QuantumPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.items = new PedestalRenderer<>(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(QuantumPedestalBlockEntity pedestal, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        items.extractRenderState(pedestal, state, partialTick, cameraPos, crumbling);
        state.ghosts.clear();
        state.parts.clear();
        Level level = pedestal.getLevel();
        if (level == null) return;
        BlockPos center = pedestal.getBlockPos();
        for (BlockPos pos : PedestalPoses.ghostRing(level, center)) {
            state.ghosts.add(pos.subtract(center));
        }
        BlockState ghost = QuantumBlocks.PEDESTAL.get().defaultBlockState();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(ghost)
                .collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, ghost, RandomSource.create(42L), state.parts);
        state.alpha = PedestalPoses.ghostAlpha(level.getGameTime() + partialTick);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        items.submit(state, poseStack, collector, camera);
        for (BlockPos offset : state.ghosts) {
            poseStack.pushPose();
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
            collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, consumer) -> putGhost(state, pose, consumer));
            poseStack.popPose();
        }
    }

    private static void putGhost(State state, PoseStack.Pose pose, VertexConsumer consumer) {
        QuadInstance instance = new QuadInstance();
        instance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
        instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        int tinted = ARGB.colorFromFloat(state.alpha / 255.0F, PedestalPoses.GHOST_RED, PedestalPoses.GHOST_GREEN, PedestalPoses.GHOST_BLUE);
        int plain = ARGB.color(state.alpha, 255, 255, 255);
        for (BlockStateModelPart part : state.parts) {
            for (Direction direction : DIRECTIONS) {
                putQuads(part.getQuads(direction), pose, consumer, instance, tinted, plain);
            }
            putQuads(part.getQuads(null), pose, consumer, instance, tinted, plain);
        }
    }

    private static void putQuads(List<BakedQuad> quads, PoseStack.Pose pose, VertexConsumer consumer, QuadInstance instance, int tinted, int plain) {
        for (BakedQuad quad : quads) {
            instance.setColor(quad.materialInfo().isTinted() ? tinted : plain);
            consumer.putBakedQuad(pose, quad, instance);
        }
    }

    @Override
    public AABB getRenderBoundingBox(QuantumPedestalBlockEntity pedestal) {
        int d = UpfallConfig.getInt(UpfallConfig.PEDESTAL_RING_DISTANCE) + 1;
        return new AABB(pedestal.getBlockPos()).inflate(d, 2, d);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class State extends PedestalRenderer.State {
        final List<BlockPos> ghosts = new ArrayList<>();
        final List<BlockStateModelPart> parts = new ArrayList<>();
        int alpha;
    }
}
