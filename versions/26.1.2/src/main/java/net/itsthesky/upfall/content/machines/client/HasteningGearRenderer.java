package net.itsthesky.upfall.content.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HasteningGearRenderer implements BlockEntityRenderer<HasteningGearBlockEntity, HasteningGearRenderer.State> {
    public HasteningGearRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HasteningGearBlockEntity gear, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(gear, state, partialTick, cameraPos, crumbling);
        state.cog = null;
        Level level = gear.getLevel();
        if (level == null) {
            return;
        }
        BlockState blockState = gear.getBlockState();
        state.cog = ClockworkClientModule.cog();
        state.facing = blockState.getValue(HasteningGearBlock.FACING);
        state.angle = ClockworkPoses.gearAngle(blockState.getValue(HasteningGearBlock.ACTIVE), level.getGameTime(), partialTick);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.cog == null) {
            return;
        }
        poseStack.pushPose();
        ClockworkPoses.gear(poseStack, state.facing, state.angle);
        collector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), List.of(state.cog), BlockModelRenderState.EMPTY_TINTS,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        BlockStateModelPart cog;
        Direction facing = Direction.NORTH;
        float angle;
    }
}
