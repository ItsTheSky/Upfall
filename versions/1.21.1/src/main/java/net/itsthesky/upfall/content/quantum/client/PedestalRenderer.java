package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PedestalRenderer<T extends PedestalBlockEntity> implements BlockEntityRenderer<T> {
    private final ItemRenderer items;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(T pedestal, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = pedestal.item();
        Level level = pedestal.getLevel();
        if (stack.isEmpty() || level == null) return;
        boolean ritual = pedestal instanceof QuantumPedestalBlockEntity quantum && quantum.active();
        float time = level.getGameTime() + partialTick;
        poseStack.pushPose();
        PedestalPoses.item(poseStack, time, ritual);
        int light = LevelRenderer.getLightColor(level, pedestal.getBlockPos().above());
        items.renderStatic(stack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, buffer, level, (int) pedestal.getBlockPos().asLong());
        poseStack.popPose();
    }
}
