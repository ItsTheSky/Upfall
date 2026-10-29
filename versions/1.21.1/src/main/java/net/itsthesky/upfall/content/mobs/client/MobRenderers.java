package net.itsthesky.upfall.content.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.mobs.entity.Cragmaw;
import net.itsthesky.upfall.content.mobs.entity.Duskwing;
import net.itsthesky.upfall.content.mobs.entity.Mistjelly;
import net.itsthesky.upfall.content.mobs.entity.Skygrazer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class MobRenderers {
    private MobRenderers() {}

    private static ResourceLocation skin(String name) {
        return Upfall.id("textures/entity/" + name + ".png");
    }

    private static <T extends Mob, M extends EntityModel<T>> EyesLayer<T, M> glow(RenderLayerParent<T, M> parent, String name) {
        RenderType type = RenderType.eyes(skin(name + "_glow"));
        return new EyesLayer<>(parent) {
            @Override
            public RenderType renderType() {
                return type;
            }
        };
    }

    public static class DuskwingRenderer extends MobRenderer<Duskwing, DuskwingModel> {
        private static final ResourceLocation TEXTURE = skin("duskwing");

        public DuskwingRenderer(EntityRendererProvider.Context context) {
            super(context, new DuskwingModel(context.bakeLayer(DuskwingModel.LAYER)), 0.3F);
            addLayer(glow(this, "duskwing"));
        }

        @Override
        public ResourceLocation getTextureLocation(Duskwing entity) {
            return TEXTURE;
        }
    }

    public static class CragmawRenderer extends MobRenderer<Cragmaw, CragmawModel> {
        private static final ResourceLocation TEXTURE = skin("cragmaw");

        public CragmawRenderer(EntityRendererProvider.Context context) {
            super(context, new CragmawModel(context.bakeLayer(CragmawModel.LAYER)), 0.8F);
            addLayer(glow(this, "cragmaw"));
        }

        @Override
        public ResourceLocation getTextureLocation(Cragmaw entity) {
            return TEXTURE;
        }
    }

    public static class MistjellyRenderer extends MobRenderer<Mistjelly, MistjellyModel> {
        private static final ResourceLocation TEXTURE = skin("mistjelly");

        public MistjellyRenderer(EntityRendererProvider.Context context) {
            super(context, new MistjellyModel(context.bakeLayer(MistjellyModel.LAYER)), 0.0F);
            addLayer(glow(this, "mistjelly"));
        }

        @Override
        protected void scale(Mistjelly entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(MistjellyModel.RENDER_SCALE, MistjellyModel.RENDER_SCALE, MistjellyModel.RENDER_SCALE);
        }

        @Override
        protected int getBlockLightLevel(Mistjelly entity, BlockPos pos) {
            return MistjellyModel.BLOCK_LIGHT;
        }

        @Override
        public ResourceLocation getTextureLocation(Mistjelly entity) {
            return TEXTURE;
        }
    }

    public static class SkygrazerRenderer extends MobRenderer<Skygrazer, SkygrazerModel> {
        private static final ResourceLocation TEXTURE = skin("skygrazer");

        public SkygrazerRenderer(EntityRendererProvider.Context context) {
            super(context, new SkygrazerModel(context.bakeLayer(SkygrazerModel.LAYER)), SkygrazerModel.SHADOW_RADIUS);
        }

        @Override
        protected void scale(Skygrazer entity, PoseStack poseStack, float partialTick) {
            if (entity.isBaby()) {
                poseStack.scale(SkygrazerModel.BABY_SCALE, SkygrazerModel.BABY_SCALE, SkygrazerModel.BABY_SCALE);
                shadowRadius = SkygrazerModel.SHADOW_RADIUS * SkygrazerModel.BABY_SHADOW_FACTOR;
            } else {
                shadowRadius = SkygrazerModel.SHADOW_RADIUS;
            }
        }

        @Override
        public ResourceLocation getTextureLocation(Skygrazer entity) {
            return TEXTURE;
        }
    }
}
