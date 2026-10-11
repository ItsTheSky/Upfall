package net.itsthesky.upfall.content.chronospire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class DriftPlatformRenderer extends EntityRenderer<DriftPlatform> {
    private final BlockRenderDispatcher blocks;

    public DriftPlatformRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blocks = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DriftPlatform platform, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState plate = ChronoBlocks.DRIFT_PLATE.get().defaultBlockState();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                poseStack.pushPose();
                poseStack.translate(i - 1.5, 0.0, j - 1.5);
                blocks.renderSingleBlock(plate, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
                poseStack.popPose();
            }
        }
        super.render(platform, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DriftPlatform platform) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
