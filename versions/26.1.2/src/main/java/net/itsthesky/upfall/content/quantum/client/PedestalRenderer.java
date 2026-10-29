package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
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

public class PedestalRenderer<T extends PedestalBlockEntity> implements BlockEntityRenderer<T, PedestalRenderer.State> {
    private final ItemModelResolver itemModels;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModels = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T pedestal, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTick, cameraPos, crumbling);
        state.visible = false;
        ItemStack stack = pedestal.item();
        Level level = pedestal.getLevel();
        if (stack.isEmpty() || level == null) return;
        state.visible = true;
        state.ritual = pedestal instanceof QuantumPedestalBlockEntity quantum && quantum.active();
        state.time = level.getGameTime() + partialTick;
        state.itemLight = LevelRenderer.getLightCoords(level, pedestal.getBlockPos().above());
        itemModels.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, level, null, (int) pedestal.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;
        poseStack.pushPose();
        PedestalPoses.item(poseStack, state.time, state.ritual);
        state.item.submit(poseStack, collector, state.itemLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean visible;
        boolean ritual;
        float time;
        int itemLight;
    }
}
