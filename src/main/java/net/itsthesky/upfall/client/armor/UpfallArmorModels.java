package net.itsthesky.upfall.client.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UpfallArmorModels {
    public enum Style { LEVITITE, NIMBUSITE }

    private static final int OUTER_WIDTH = 64, OUTER_HEIGHT = 64;
    private static final int INNER_WIDTH = 64, INNER_HEIGHT = 32;

    private static final Map<Style, Map<EquipmentSlot, HumanoidModel<?>>> MODELS = new EnumMap<>(Style.class);

    private UpfallArmorModels() {}

    public static HumanoidModel<?> get(Style style, EquipmentSlot slot) {
        return MODELS.computeIfAbsent(style, s -> new EnumMap<>(EquipmentSlot.class)).computeIfAbsent(slot, s -> create(style, s));
    }

    private static HumanoidModel<?> create(Style style, EquipmentSlot slot) {
        Root root = slot == EquipmentSlot.LEGS ? new Root(INNER_WIDTH, INNER_HEIGHT) : new Root(OUTER_WIDTH, OUTER_HEIGHT);
        switch (slot) {
            case HEAD -> {
                root.head.box(32, 0, false, -4, -8, -4, 8, 8, 8, 0.8F);
                root.head.box(0, 0, false, -4, -8, -4, 8, 8, 8, 0.6F);
                root.head.box(0, 32, false, -4.5F, -5.6F, -4.5F, 9, 3, 7, 0.6F);
                if (style == Style.LEVITITE) {
                    root.head.child("wing_right", -5, -1.75F, 2.5F, 39.14867F, -20.7048F, -9.0072F)
                            .box(17, 39, true, 0, -7, 0, 0, 7, 11, 0);
                    root.head.child("wing_left", 5, -1.75F, 2.5F, 39.14867F, 20.70481F, 9.00717F)
                            .box(17, 39, true, 0, -7, 0, 0, 7, 11, 0);
                }
            }
            case CHEST -> {
                root.body.box(16, 16, false, -4, 0, -2, 8, 12, 4, 1.2F);
                root.body.box(0, 42, false, -4, 0.32F, -3.6F, 8, 2, 1, 0.61F);
                root.body.child("chest", 0, 6, -2.25F, 22.5F, 0, 0)
                        .box(0, 45, false, -4, -3, 0, 8, 2, 1, 0.61F);
                root.rightArm.box(40, 16, false, -3, -2, -2, 4, 12, 4, 0.5F);
                root.rightArm.child("right_arm_shoulder", -2.75F, -1.75F, 0, 0, 0, 22.5F)
                        .box(0, 52, true, -1, -1.5F, -3, 5, 6, 6, 0.5F);
                root.leftArm.box(40, 16, true, -1, -2, -2, 4, 12, 4, 0.5F);
                root.leftArm.child("left_arm_shoulder", 2.75F, -1.75F, 0, 0, 0, -22.5F)
                        .box(0, 52, false, -4, -1.5F, -3, 5, 6, 6, 0.5F);
            }
            case LEGS -> {
                Bone bodyUnder = root.body.child("body_under", 0, 24, 0, 0, 0, 0)
                        .box(16, 16, false, -4, -24, -2, 8, 12, 4, 0.5F)
                        .box(19, 6, false, -5, -15.25F, -3, 10, 3, 6, 0.1F);
                bodyUnder.child("body_flap", 0, -14.5F, 2.3F, 10, 0, 0)
                        .box(46, 4, false, -3, -0.09781F, -0.47921F, 6, 6, 1, 0);
                root.rightLeg.box(0, 16, false, -2, 0, -2, 4, 12, 4, 0.5F);
                root.rightLeg.child("right_leg_flap", -2.15F, 1.6F, 0, 0, 0, 17.5F)
                        .box(0, 0, true, -1.75F, -3.7748F, -2.5F, 4, 10, 5, 0.2F);
                root.leftLeg.box(0, 16, true, -2, 0, -2, 4, 12, 4, 0.5F);
                root.leftLeg.child("left_leg_flap", 2.15F, 1.1F, 0, 0, 0, -17.5F)
                        .box(0, 0, false, -2.26157F, -3.27482F, -2.5F, 4, 10, 5, 0.2F);
            }
            case FEET -> {
                root.rightLeg.box(0, 16, false, -2, 0, -2, 4, 12, 4, 0.6F);
                root.leftLeg.box(0, 16, true, -2, 0, -2, 4, 12, 4, 0.6F);
            }
            default -> {
            }
        }
        return new HumanoidModel<>(root.bake());
    }

    // Rotations are Blockbench group angles in degrees, already converted to model space (x and y negated).
    private static final class Bone {
        private final int texWidth, texHeight;
        private final PartPose pose;
        private final List<ModelPart.Cube> cubes = new ArrayList<>();
        private final Map<String, Bone> children = new HashMap<>();

        private Bone(int texWidth, int texHeight, PartPose pose) {
            this.texWidth = texWidth;
            this.texHeight = texHeight;
            this.pose = pose;
        }

        Bone box(int u, int v, boolean mirror, float x, float y, float z, float w, float h, float d, float grow) {
            cubes.add(CubeListBuilder.create().texOffs(u, v).mirror(mirror).addBox(x, y, z, w, h, d, new CubeDeformation(grow))
                    .getCubes().getFirst().bake(texWidth, texHeight));
            return this;
        }

        Bone child(String name, float x, float y, float z, float xDeg, float yDeg, float zDeg) {
            Bone child = new Bone(texWidth, texHeight, PartPose.offsetAndRotation(x, y, z,
                    (float) Math.toRadians(xDeg), (float) Math.toRadians(yDeg), (float) Math.toRadians(zDeg)));
            children.put(name, child);
            return child;
        }

        ModelPart bake(Map<String, ModelPart> extraChildren) {
            Map<String, ModelPart> baked = new HashMap<>(extraChildren);
            children.forEach((name, child) -> baked.put(name, child.bake(Map.of())));
            ModelPart part = new ModelPart(cubes, baked);
            part.setInitialPose(pose);
            part.loadPose(pose);
            return part;
        }
    }

    private static final class Root {
        final Bone head, body, rightArm, leftArm, rightLeg, leftLeg;
        private final Bone hat, root;

        private Root(int texWidth, int texHeight) {
            root = new Bone(texWidth, texHeight, PartPose.ZERO);
            head = new Bone(texWidth, texHeight, PartPose.ZERO);
            hat = new Bone(texWidth, texHeight, PartPose.ZERO);
            body = new Bone(texWidth, texHeight, PartPose.ZERO);
            rightArm = new Bone(texWidth, texHeight, PartPose.offset(-5.0F, 2.0F, 0.0F));
            leftArm = new Bone(texWidth, texHeight, PartPose.offset(5.0F, 2.0F, 0.0F));
            rightLeg = new Bone(texWidth, texHeight, PartPose.offset(-1.9F, 12.0F, 0.0F));
            leftLeg = new Bone(texWidth, texHeight, PartPose.offset(1.9F, 12.0F, 0.0F));
        }

        ModelPart bake() {
            Map<String, ModelPart> parts = new HashMap<>();
            //? if >=26.1 {
            /*parts.put("head", head.bake(Map.of("hat", hat.bake(Map.of()))));
            *///?} else {
            parts.put("hat", hat.bake(Map.of()));
            parts.put("head", head.bake(Map.of()));
            //?}
            parts.put("body", body.bake(Map.of()));
            parts.put("right_arm", rightArm.bake(Map.of()));
            parts.put("left_arm", leftArm.bake(Map.of()));
            parts.put("right_leg", rightLeg.bake(Map.of()));
            parts.put("left_leg", leftLeg.bake(Map.of()));
            return root.bake(parts);
        }
    }
}
