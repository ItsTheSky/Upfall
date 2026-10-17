package net.itsthesky.upfall.content.reliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.itsthesky.upfall.content.reliquary.boss.FoldWardenSegment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FoldWardenRenderer extends MobRenderer<FoldWarden, FoldWardenModel> {
    static final ResourceLocation TEXTURE = Upfall.id("textures/entity/fold_warden.png");
    static final ResourceLocation GLOW_TEXTURE = Upfall.id("textures/entity/fold_warden_glow.png");
    private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);
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
    public void render(FoldWarden entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
        FoldWarden.Phase phase = entity.phase();
        if (phase != FoldWarden.Phase.UNFOLD && phase != FoldWarden.Phase.WYRM) return;
        float grow = phase == FoldWarden.Phase.UNFOLD
                ? Mth.clamp(((entity.phaseTicks() + partialTick) / FoldWarden.UNFOLD_TICKS - 0.2F) / 0.4F, 0.0F, 1.0F)
                : 1.0F;
        if (grow <= 0.01F) return;
        Vec3 origin = lerp(entity.xo, entity.yo, entity.zo, entity.getX(), entity.getY(), entity.getZ(), partialTick);
        Vec3 lead = origin.add(0.0, entity.getBbHeight() / 2.0, 0.0);
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        for (FoldWardenSegment segment : entity.segments()) {
            Vec3 center = lerp(segment.xo, segment.yo, segment.zo, segment.getX(), segment.getY(), segment.getZ(), partialTick)
                    .add(0.0, segment.size / 2.0, 0.0);
            if (!entity.segmentVisible(segment.index)) break;
            Vec3 toLead = lead.subtract(center);
            double length = toLead.length();
            float yaw = length < 1.0E-4 ? entity.yBodyRot : (float) (Mth.atan2(toLead.z, toLead.x) * Mth.RAD_TO_DEG) - 90.0F;
            float pitch = length < 1.0E-4 ? 0.0F : (float) -Mth.atan2(toLead.y, toLead.horizontalDistance());
            float size = segment.size * grow;

            poseStack.pushPose();
            poseStack.translate(center.x - origin.x, center.y - origin.y, center.z - origin.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            poseStack.scale(-size, -size, size);
            segmentModel.pose(pitch, (float) (length / size));
            segmentModel.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay);
            segmentModel.render(poseStack, buffers.getBuffer(GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
            lead = center;
        }
    }

    private static Vec3 lerp(double xo, double yo, double zo, double x, double y, double z, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, xo, x), Mth.lerp(partialTick, yo, y), Mth.lerp(partialTick, zo, z));
    }

    @Override
    protected float getFlipDegrees(FoldWarden entity) {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(FoldWarden entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(FoldWarden entity) {
        return TEXTURE;
    }
}
