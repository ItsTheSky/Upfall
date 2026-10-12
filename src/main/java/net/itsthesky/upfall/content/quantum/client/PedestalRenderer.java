package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
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
        float spin = time * (ritual ? 12.0F : 2.0F);
        float lift = ritual ? 0.35F : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.5, 1.25 + lift + Mth.sin(time * 0.08F) * 0.06, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin % 360.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);
        int light = LevelRenderer.getLightColor(level, pedestal.getBlockPos().above());
        items.renderStatic(stack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, buffer, level, (int) pedestal.getBlockPos().asLong());
        poseStack.popPose();
    }
}
