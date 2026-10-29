package net.itsthesky.upfall.content.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RewinderRenderer implements BlockEntityRenderer<RewinderBlockEntity, RewinderRenderer.State> {
    private final ItemModelResolver itemModels;

    public RewinderRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModels = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RewinderBlockEntity rewinder, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(rewinder, state, partialTick, cameraPos, crumbling);
        state.visible = false;
        ItemStack stack = rewinder.stack();
        Level level = rewinder.getLevel();
        if (stack.isEmpty() || level == null) {
            return;
        }
        state.visible = true;
        state.time = level.getGameTime() + partialTick;
        state.itemLight = LevelRenderer.getLightCoords(level, rewinder.getBlockPos().above());
        itemModels.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, level, null, (int) rewinder.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) {
            return;
        }
        poseStack.pushPose();
        ClockworkPoses.rewinderItem(poseStack, state.time);
        state.item.submit(poseStack, collector, state.itemLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean visible;
        float time;
        int itemLight;
    }
}
