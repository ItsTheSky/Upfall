package net.itsthesky.upfall.content.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RewinderRenderer implements BlockEntityRenderer<RewinderBlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public RewinderRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(RewinderBlockEntity rewinder, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = rewinder.stack();
        if (stack.isEmpty() || rewinder.getLevel() == null) {
            return;
        }
        float time = rewinder.getLevel().getGameTime() + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5, 1.15 + Math.sin(time * 0.08) * 0.04, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * 3.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        context.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, LevelRenderer.getLightColor(rewinder.getLevel(), rewinder.getBlockPos().above()),
                OverlayTexture.NO_OVERLAY, poseStack, buffer, rewinder.getLevel(), (int) rewinder.getBlockPos().asLong());
        poseStack.popPose();
    }
}
