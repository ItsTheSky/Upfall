package net.itsthesky.upfall.content.chronospire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class DriftPlatformRenderer extends EntityRenderer<DriftPlatform, DriftPlatformRenderer.State> {
    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelResolver blocks;

    public DriftPlatformRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blocks = context.getBlockModelResolver();
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DriftPlatform platform, State state, float partialTick) {
        super.extractRenderState(platform, state, partialTick);
        blocks.update(state.plate, ChronoBlocks.DRIFT_PLATE.get().defaultBlockState(), DISPLAY);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        DriftPlatformPlates.forEach(poseStack, () ->
                state.plate.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor));
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        final BlockModelRenderState plate = new BlockModelRenderState();
    }
}
