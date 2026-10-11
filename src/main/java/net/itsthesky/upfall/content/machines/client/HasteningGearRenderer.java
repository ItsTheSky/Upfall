package net.itsthesky.upfall.content.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class HasteningGearRenderer implements BlockEntityRenderer<HasteningGearBlockEntity> {
    public HasteningGearRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HasteningGearBlockEntity gear, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (gear.getLevel() == null) {
            return;
        }
        BlockState state = gear.getBlockState();
        Direction facing = state.getValue(HasteningGearBlock.FACING);
        BakedModel model = ClockworkClientModule.cog();
        float angle = state.getValue(HasteningGearBlock.ACTIVE) ? (gear.getLevel().getGameTime() + partialTick) * 9.0F : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf().rotationTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), state, model,
                1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
