package net.itsthesky.upfall.content.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.mobs.entity.Cragmaw;
import net.itsthesky.upfall.content.mobs.entity.Duskwing;
import net.itsthesky.upfall.content.mobs.entity.Mistjelly;
import net.itsthesky.upfall.content.mobs.entity.Skygrazer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public final class MobRenderers {
    private MobRenderers() {}

    private static Identifier skin(String name) {
        return Upfall.id("textures/entity/" + name + ".png");
    }

    private static <S extends LivingEntityRenderState, M extends EntityModel<S>> EyesLayer<S, M> glow(RenderLayerParent<S, M> parent, String name) {
        RenderType type = RenderTypes.eyes(skin(name + "_glow"));
        return new EyesLayer<>(parent) {
            @Override
            public RenderType renderType() {
                return type;
            }
        };
    }

    public static class DuskwingRenderer extends MobRenderer<Duskwing, DuskwingRenderState, DuskwingModel> {
        private static final Identifier TEXTURE = skin("duskwing");

        public DuskwingRenderer(EntityRendererProvider.Context context) {
            super(context, new DuskwingModel(context.bakeLayer(DuskwingModel.LAYER)), 0.3F);
            addLayer(glow(this, "duskwing"));
        }

        @Override
        public DuskwingRenderState createRenderState() {
            return new DuskwingRenderState();
        }

        @Override
        public void extractRenderState(Duskwing entity, DuskwingRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.entityId = entity.getId();
            state.diving = entity.isDiving();
            state.rise = (float) entity.getDeltaMovement().y;
        }

        @Override
        public Identifier getTextureLocation(DuskwingRenderState state) {
            return TEXTURE;
        }
    }

    public static class CragmawRenderer extends MobRenderer<Cragmaw, CragmawRenderState, CragmawModel> {
        private static final Identifier TEXTURE = skin("cragmaw");

        public CragmawRenderer(EntityRendererProvider.Context context) {
            super(context, new CragmawModel(context.bakeLayer(CragmawModel.LAYER)), 0.8F);
            addLayer(glow(this, "cragmaw"));
        }

        @Override
        public CragmawRenderState createRenderState() {
            return new CragmawRenderState();
        }

        @Override
        public void extractRenderState(Cragmaw entity, CragmawRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.aggressive = entity.isAggressive();
            state.attackAnim = entity.attackAnim;
        }

        @Override
        public Identifier getTextureLocation(CragmawRenderState state) {
            return TEXTURE;
        }
    }

    public static class MistjellyRenderer extends MobRenderer<Mistjelly, MistjellyRenderState, MistjellyModel> {
        private static final Identifier TEXTURE = skin("mistjelly");

        public MistjellyRenderer(EntityRendererProvider.Context context) {
            super(context, new MistjellyModel(context.bakeLayer(MistjellyModel.LAYER)), 0.0F);
            addLayer(glow(this, "mistjelly"));
        }

        @Override
        public MistjellyRenderState createRenderState() {
            return new MistjellyRenderState();
        }

        @Override
        public void extractRenderState(Mistjelly entity, MistjellyRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.entityId = entity.getId();
            state.aggressive = entity.isAggressive();
            state.horizontalSpeed = (float) entity.getDeltaMovement().horizontalDistance();
        }

        @Override
        protected void scale(MistjellyRenderState state, PoseStack poseStack) {
            poseStack.scale(MistjellyModel.RENDER_SCALE, MistjellyModel.RENDER_SCALE, MistjellyModel.RENDER_SCALE);
        }

        @Override
        protected int getBlockLightLevel(Mistjelly entity, BlockPos pos) {
            return MistjellyModel.BLOCK_LIGHT;
        }

        @Override
        public Identifier getTextureLocation(MistjellyRenderState state) {
            return TEXTURE;
        }
    }

    public static class SkygrazerRenderer extends MobRenderer<Skygrazer, SkygrazerRenderState, SkygrazerModel> {
        private static final Identifier TEXTURE = skin("skygrazer");

        public SkygrazerRenderer(EntityRendererProvider.Context context) {
            super(context, new SkygrazerModel(context.bakeLayer(SkygrazerModel.LAYER)), SkygrazerModel.SHADOW_RADIUS);
        }

        @Override
        public SkygrazerRenderState createRenderState() {
            return new SkygrazerRenderState();
        }

        @Override
        public void extractRenderState(Skygrazer entity, SkygrazerRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.entityId = entity.getId();
        }

        @Override
        protected void scale(SkygrazerRenderState state, PoseStack poseStack) {
            if (state.isBaby) {
                poseStack.scale(SkygrazerModel.BABY_SCALE, SkygrazerModel.BABY_SCALE, SkygrazerModel.BABY_SCALE);
            }
        }

        @Override
        protected float getShadowRadius(SkygrazerRenderState state) {
            return super.getShadowRadius(state) * (state.isBaby ? SkygrazerModel.BABY_SHADOW_FACTOR : 1.0F);
        }

        @Override
        public Identifier getTextureLocation(SkygrazerRenderState state) {
            return TEXTURE;
        }
    }
}
