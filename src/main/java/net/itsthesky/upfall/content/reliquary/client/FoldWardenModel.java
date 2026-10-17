package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.Util;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class FoldWardenModel extends HierarchicalModel<FoldWarden> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Upfall.id("fold_warden"), "main");
    private static final float QUARTER = Mth.HALF_PI;
    private static final float EIGHTH = Mth.PI / 4.0F;
    private static final float EDGE = 20.5F;
    private static final float FACE = 21.0F;
    private static final float BODY_Y = -12.0F;
    private static final float HALO = 52.0F;
    private static final float PREVIEW_OPEN = 0.45F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart core;
    private final ModelPart leftShutter;
    private final ModelPart rightShutter;
    private final ModelPart halo;
    private final ModelPart[] debris = new ModelPart[3];
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart crest;

    public FoldWardenModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.core = body.getChild("core");
        this.leftShutter = core.getChild("left_shutter");
        this.rightShutter = core.getChild("right_shutter");
        this.halo = body.getChild("halo");
        for (int i = 0; i < debris.length; i++) {
            debris[i] = body.getChild("debris_" + i);
        }
        this.head = root.getChild("head");
        this.jaw = head.getChild("jaw");
        this.crest = head.getChild("crest");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, BODY_Y, 0.0F));

        PartDefinition core = body.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-20.0F, -20.0F, -20.0F, 40.0F, 40.0F, 40.0F),
                PartPose.rotation(0.0F, 0.0F, EIGHTH));

        CubeListBuilder edge = CubeListBuilder.create().texOffs(160, 0).addBox(-2.5F, -22.0F, -2.5F, 5.0F, 44.0F, 5.0F);
        int index = 0;
        for (int a = -1; a <= 1; a += 2) {
            for (int b = -1; b <= 1; b += 2) {
                core.addOrReplaceChild("edge_" + index++, edge, PartPose.offset(a * EDGE, 0.0F, b * EDGE));
                core.addOrReplaceChild("edge_" + index++, edge, PartPose.offsetAndRotation(0.0F, a * EDGE, b * EDGE, 0.0F, 0.0F, QUARTER));
                core.addOrReplaceChild("edge_" + index++, edge, PartPose.offsetAndRotation(a * EDGE, b * EDGE, 0.0F, QUARTER, 0.0F, 0.0F));
            }
        }
        CubeListBuilder cap = CubeListBuilder.create().texOffs(180, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        index = 0;
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    core.addOrReplaceChild("cap_" + index++, cap, PartPose.offset(x * FACE, y * FACE, z * FACE));
                }
            }
        }

        CubeListBuilder panel = CubeListBuilder.create().texOffs(180, 16).addBox(-16.0F, -16.0F, -2.0F, 32.0F, 32.0F, 4.0F);
        core.addOrReplaceChild("back_panel", panel, PartPose.offset(0.0F, 0.0F, FACE));
        core.addOrReplaceChild("left_panel", panel, PartPose.offsetAndRotation(-FACE, 0.0F, 0.0F, 0.0F, QUARTER, 0.0F));
        core.addOrReplaceChild("right_panel", panel, PartPose.offsetAndRotation(FACE, 0.0F, 0.0F, 0.0F, QUARTER, 0.0F));
        core.addOrReplaceChild("top_panel", panel, PartPose.offsetAndRotation(0.0F, -FACE, 0.0F, QUARTER, 0.0F, 0.0F));
        core.addOrReplaceChild("bottom_panel", panel, PartPose.offsetAndRotation(0.0F, FACE, 0.0F, QUARTER, 0.0F, 0.0F));
        core.addOrReplaceChild("left_shutter",
                CubeListBuilder.create().texOffs(200, 52).addBox(0.0F, -16.0F, -2.0F, 16.0F, 32.0F, 4.0F),
                PartPose.offset(-16.0F, 0.0F, -FACE));
        core.addOrReplaceChild("right_shutter",
                CubeListBuilder.create().texOffs(200, 52).mirror().addBox(-16.0F, -16.0F, -2.0F, 16.0F, 32.0F, 4.0F),
                PartPose.offset(16.0F, 0.0F, -FACE));
        core.addOrReplaceChild("eye", CubeListBuilder.create()
                        .texOffs(0, 89).addBox(-11.0F, -11.0F, -20.5F, 22.0F, 22.0F, 2.0F)
                        .texOffs(50, 89).addBox(-7.0F, -7.0F, -21.5F, 14.0F, 14.0F, 2.0F)
                        .texOffs(84, 89).addBox(-3.0F, -3.0F, -22.5F, 6.0F, 6.0F, 2.0F),
                PartPose.ZERO);

        PartDefinition halo = body.addOrReplaceChild("halo", CubeListBuilder.create(), PartPose.rotation(0.2F, 0.0F, 0.0F));
        CubeListBuilder longBar = CubeListBuilder.create().texOffs(0, 115).addBox(-HALO - 2.5F, -2.0F, -2.5F, HALO * 2 + 5.0F, 4.0F, 5.0F);
        CubeListBuilder shortBar = CubeListBuilder.create().texOffs(0, 124).addBox(-HALO + 2.5F, -2.0F, -2.5F, HALO * 2 - 5.0F, 4.0F, 5.0F);
        halo.addOrReplaceChild("bar_north", longBar, PartPose.offset(0.0F, 0.0F, -HALO));
        halo.addOrReplaceChild("bar_south", longBar, PartPose.offset(0.0F, 0.0F, HALO));
        halo.addOrReplaceChild("bar_east", shortBar, PartPose.offsetAndRotation(HALO, 0.0F, 0.0F, 0.0F, QUARTER, 0.0F));
        halo.addOrReplaceChild("bar_west", shortBar, PartPose.offsetAndRotation(-HALO, 0.0F, 0.0F, 0.0F, QUARTER, 0.0F));

        body.addOrReplaceChild("debris_0", CubeListBuilder.create().texOffs(100, 89).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 42.0F, 0.0F, EIGHTH, 0.0F, EIGHTH));
        body.addOrReplaceChild("debris_1", CubeListBuilder.create().texOffs(132, 89).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(2.0F, 50.0F, 0.0F, EIGHTH, 0.0F, EIGHTH));
        body.addOrReplaceChild("debris_2", CubeListBuilder.create().texOffs(156, 89).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-1.0F, 56.0F, 0.0F, EIGHTH, 0.0F, EIGHTH));
        body.addOrReplaceChild("plumb", CubeListBuilder.create().texOffs(172, 89).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 22.0F, 1.0F),
                PartPose.offset(0.0F, 37.0F, 0.0F));
        createHead(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void createHead(PartDefinition root) {
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 170).addBox(-14.0F, -8.0F, -14.0F, 28.0F, 16.0F, 30.0F)
                        .texOffs(116, 170).addBox(-15.0F, -11.0F, -10.0F, 30.0F, 5.0F, 12.0F)
                        .texOffs(116, 188).addBox(-10.0F, -4.0F, -24.0F, 20.0F, 10.0F, 10.0F)
                        .texOffs(176, 188).addBox(-8.0F, -2.0F, -28.0F, 16.0F, 6.0F, 4.0F)
                        .texOffs(216, 188).addBox(-14.5F, -4.0F, -12.0F, 1.0F, 3.0F, 5.0F)
                        .texOffs(216, 188).mirror().addBox(13.5F, -4.0F, -12.0F, 1.0F, 3.0F, 5.0F)
                        .texOffs(184, 216).addBox(-9.0F, 6.0F, -22.0F, 18.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        head.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(200, 170).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, -2.0F, EIGHTH, 0.0F, EIGHTH));
        for (int side = -1; side <= 1; side += 2) {
            String name = side < 0 ? "left" : "right";
            PartDefinition horn = head.addOrReplaceChild(name + "_horn", CubeListBuilder.create()
                            .texOffs(0, 216).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 22.0F, 6.0F)
                            .texOffs(24, 216).addBox(-2.0F, -27.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                    PartPose.offsetAndRotation(side * 9.0F, -8.0F, 6.0F, -0.7F, 0.0F, side * 0.3F));
            head.addOrReplaceChild(name + "_fin", CubeListBuilder.create().texOffs(40, 216).addBox(-1.0F, -7.0F, -8.0F, 2.0F, 14.0F, 16.0F),
                    PartPose.offsetAndRotation(side * 14.5F, 0.0F, 2.0F, 0.0F, side * 0.5F, 0.0F));
        }
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(76, 216).addBox(-12.0F, 0.0F, -38.0F, 24.0F, 6.0F, 30.0F)
                        .texOffs(184, 216).addBox(-9.0F, -2.0F, -36.0F, 18.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 8.2F, 10.0F));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FoldWarden entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean preview = !(entity.level() instanceof ClientLevel);
        if (preview) ageInTicks = (Util.getMillis() % 3_600_000L) / 50.0F;
        float partialTick = preview ? 0.0F : ageInTicks - entity.tickCount;
        FoldWarden.Phase phase = preview ? FoldWarden.Phase.CUBE : entity.phase();
        float unfold = phase == FoldWarden.Phase.UNFOLD
                ? Mth.clamp((entity.phaseTicks() + partialTick) / FoldWarden.UNFOLD_TICKS, 0.0F, 1.0F)
                : phase == FoldWarden.Phase.WYRM ? 1.0F : 0.0F;
        float cubeScale = 1.0F - Mth.clamp((unfold - 0.2F) / 0.5F, 0.0F, 1.0F);
        float headScale = Mth.clamp((unfold - 0.35F) / 0.4F, 0.0F, 1.0F);
        body.visible = cubeScale > 0.01F;
        head.visible = headScale > 0.01F;
        body.xScale = body.yScale = body.zScale = cubeScale;
        head.xScale = head.yScale = head.zScale = headScale;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jaw.xRot = jawOpen(entity, ageInTicks);
        crest.yRot = ageInTicks * 0.1F;

        boolean awakening = phase == FoldWarden.Phase.AWAKEN;
        float awaken = awakening ? Mth.clamp(entity.phaseTicks() / (float) FoldWarden.AWAKEN_TICKS, 0.0F, 1.0F) : 1.0F;
        float t = ageInTicks + entity.getId() * 3.1F;

        body.y = BODY_Y + Mth.sin(t * 0.08F) * 1.5F;
        float settle = (1.0F - awaken) * (1.0F - awaken);
        core.yRot = Mth.sin(t * 0.03F) * 0.15F + settle * t * 0.35F + unfold * unfold * 30.0F;
        core.xRot = Mth.sin(t * 0.021F) * 0.06F;
        body.zRot = preview ? 0.0F : tilt(entity, partialTick) * EIGHTH;

        float open = preview ? PREVIEW_OPEN : Mth.lerp(partialTick, entity.shutterO, entity.shutter);
        float swing = open * 1.35F + Mth.sin(t * 0.12F) * 0.04F * open;
        leftShutter.yRot = swing;
        rightShutter.yRot = -swing;

        halo.yRot = t * (0.02F + settle * 0.25F);
        halo.xRot = 0.2F + Mth.sin(t * 0.02F) * 0.1F;
        halo.zRot = Mth.cos(t * 0.017F) * 0.08F;

        for (int i = 0; i < debris.length; i++) {
            debris[i].yRot = t * (0.05F + i * 0.03F);
            debris[i].x = Mth.sin(t * 0.06F + i * 2.1F) * (1.0F + i);
        }
    }

    private static float jawOpen(FoldWarden entity, float ageInTicks) {
        return switch (entity.attack()) {
            case BREATH -> Math.min(0.75F, entity.attackTicks() * 0.05F);
            case COIL, DIVE -> 0.35F;
            case SHED -> 0.5F;
            default -> 0.08F + Mth.sin(ageInTicks * 0.1F) * 0.05F;
        };
    }

    private static float tilt(FoldWarden entity, float partialTick) {
        if (entity.attack() != FoldWarden.Attack.TILT) return 0.0F;
        float ticks = entity.attackTicks() + partialTick;
        float end = FoldWarden.TILT_WINDUP + FoldWarden.TILT_PUSH;
        if (ticks < FoldWarden.TILT_WINDUP) return Mth.clamp(ticks / FoldWarden.TILT_WINDUP, 0.0F, 1.0F);
        return Mth.clamp((end - ticks) / 10.0F, 0.0F, 1.0F);
    }
}
