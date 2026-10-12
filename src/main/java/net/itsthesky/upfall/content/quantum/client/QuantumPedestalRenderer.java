package net.itsthesky.upfall.content.quantum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class QuantumPedestalRenderer extends PedestalRenderer<QuantumPedestalBlockEntity> {
    private final BlockRenderDispatcher blocks;

    public QuantumPedestalRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.blocks = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(QuantumPedestalBlockEntity pedestal, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       int packedOverlay) {
        super.render(pedestal, partialTick, poseStack, buffer, packedLight, packedOverlay);
        Level level = pedestal.getLevel();
        if (level == null) return;
        BlockPos center = pedestal.getBlockPos();
        List<BlockPos> ring = new ArrayList<>(QuantumPedestalBlockEntity.sides(center));
        ring.addAll(QuantumPedestalBlockEntity.corners(center));
        BlockState ghost = QuantumBlocks.PEDESTAL.get().defaultBlockState();
        BakedModel model = blocks.getBlockModel(ghost);
        float time = level.getGameTime() + partialTick;
        int alpha = (int) (90 + 50 * Mth.sin(time * 0.1F));
        VertexConsumer consumer = new AlphaConsumer(buffer.getBuffer(Sheets.translucentCullBlockSheet()), alpha);
        for (BlockPos pos : ring) {
            if (level.getBlockState(pos).is(QuantumBlocks.PEDESTAL.get())) continue;
            poseStack.pushPose();
            poseStack.translate(pos.getX() - center.getX(), pos.getY() - center.getY(), pos.getZ() - center.getZ());
            blocks.getModelRenderer().renderModel(poseStack.last(), consumer, ghost, model, 0.75F, 0.9F, 1.0F,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(QuantumPedestalBlockEntity pedestal) {
        int d = UpfallConfig.getInt(UpfallConfig.PEDESTAL_RING_DISTANCE) + 1;
        return new AABB(pedestal.getBlockPos()).inflate(d, 2, d);
    }

    @Override
    public boolean shouldRenderOffScreen(QuantumPedestalBlockEntity pedestal) {
        return true;
    }

    private record AlphaConsumer(VertexConsumer delegate, int alpha) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int a) {
            delegate.setColor(red, green, blue, a * alpha / 255);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
