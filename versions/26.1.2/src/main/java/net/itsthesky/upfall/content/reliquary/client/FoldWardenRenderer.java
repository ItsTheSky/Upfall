package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

public class FoldWardenRenderer extends MobRenderer<FoldWarden, FoldWardenRenderState, FoldWardenModel> {
    static final Identifier TEXTURE = Upfall.id("textures/entity/fold_warden.png");
    static final Identifier GLOW_TEXTURE = Upfall.id("textures/entity/fold_warden_glow.png");
    private static final RenderType BODY = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType OUTLINE = RenderTypes.outline(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(GLOW_TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;

    private final FoldWyrmSegmentModel segmentModel;

    public FoldWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new FoldWardenModel(context.bakeLayer(FoldWardenModel.LAYER)), 0.0F);
        this.segmentModel = new FoldWyrmSegmentModel(context.bakeLayer(FoldWyrmSegmentModel.LAYER));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public FoldWardenRenderState createRenderState() {
        return new FoldWardenRenderState();
    }

    @Override
    public void extractRenderState(FoldWarden entity, FoldWardenRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.preview = !(entity.level() instanceof ClientLevel);
        state.entityId = entity.getId();
        state.phase = entity.phase();
        state.phaseTicks = entity.phaseTicks();
        state.attack = entity.attack();
        state.attackTicks = entity.attackTicks();
        state.shutterO = entity.shutterO;
        state.shutter = entity.shutter;
        state.pieces = FoldWyrmPass.pieces(entity, partialTick);
    }

    @Override
    public void submit(FoldWardenRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.pieces.isEmpty()) return;
        int light = state.lightCoords;
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        int outline = state.outlineColor;
        for (FoldWyrmPass.Piece piece : state.pieces) {
            poseStack.pushPose();
            piece.transform(poseStack);
            collector.submitCustomGeometry(poseStack, BODY, (pose, consumer) -> segmentModel.draw(pose, consumer, piece.pitch(), piece.spine(), light, overlay, -1));
            if (outline != 0) {
                collector.submitCustomGeometry(poseStack, OUTLINE, (pose, consumer) -> segmentModel.draw(pose, consumer, piece.pitch(), piece.spine(), light, overlay, outline));
            }
            collector.submitCustomGeometry(poseStack, GLOW, (pose, consumer) -> segmentModel.draw(pose, consumer, piece.pitch(), piece.spine(), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1));
            poseStack.popPose();
        }
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(FoldWarden entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    protected AABB getBoundingBoxForCulling(FoldWarden entity) {
        return entity.cullingBox();
    }

    @Override
    public Identifier getTextureLocation(FoldWardenRenderState state) {
        return TEXTURE;
    }
}
