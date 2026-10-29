package net.itsthesky.upfall.content.hamlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.ToIntFunction;

final class RopeLineMesh {
    static final ResourceLocation TEXTURE = Upfall.id("textures/entity/rope_line.png");
    private static final int SEGMENTS = 12;

    private RopeLineMesh() {}

    static float width(boolean zipline) {
        return zipline ? 0.075F : 0.055F;
    }

    static Vec3 otherEnd(Level level, BlockPos other, boolean zipline) {
        if (level.isLoaded(other)) {
            BlockState state = level.getBlockState(other);
            return state.getBlock() instanceof AbstractRopeAnchorBlock block ? block.attachPoint(other, state) : null;
        }
        return zipline ? ZiplinePulley.attach(other) : Vec3.atCenterOf(other);
    }

    static void drawHalf(PoseStack.Pose pose, VertexConsumer buffer, ToIntFunction<BlockPos> lightAt, BlockPos origin, Vec3 from, Vec3 to, double sag, float width) {
        Vec3 base = Vec3.atLowerCornerOf(origin);
        Vec3 previous = RopeLines.point(from, to, sag, 0.0);
        float v = 0.0F;
        for (int i = 1; i <= SEGMENTS; i++) {
            Vec3 next = RopeLines.point(from, to, sag, 0.5 * i / SEGMENTS);
            Vec3 along = next.subtract(previous);
            double length = along.length();
            if (length < 1.0E-4) continue;
            Vec3 dir = along.scale(1.0 / length);
            Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
            side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
            Vec3 up = side.cross(dir).normalize();
            int light = lightAt.applyAsInt(BlockPos.containing(previous.add(next).scale(0.5)));
            float nextV = v + (float) length;
            ribbon(buffer, pose, previous.subtract(base), next.subtract(base), side.scale(width), v, nextV, light);
            ribbon(buffer, pose, previous.subtract(base), next.subtract(base), up.scale(width), v, nextV, light);
            v = nextV;
            previous = next;
        }
    }

    private static void ribbon(VertexConsumer buffer, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 half, float v0, float v1, int light) {
        vertex(buffer, pose, a.subtract(half), 0.0F, v0, light);
        vertex(buffer, pose, a.add(half), 1.0F, v0, light);
        vertex(buffer, pose, b.add(half), 1.0F, v1, light);
        vertex(buffer, pose, b.subtract(half), 0.0F, v1, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 pos, float u, float v, int light) {
        buffer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    static AABB renderBox(RopeAnchorBlockEntity anchor) {
        AABB box = new AABB(anchor.getBlockPos());
        Level level = anchor.getLevel();
        if (level == null) return box;
        Vec3 from = Vec3.atCenterOf(anchor.getBlockPos());
        for (BlockPos other : anchor.links()) {
            Vec3 middle = from.add(Vec3.atCenterOf(other)).scale(0.5);
            box = box.minmax(new AABB(middle, middle).inflate(0.5, 2.0, 0.5));
        }
        return box;
    }
}
