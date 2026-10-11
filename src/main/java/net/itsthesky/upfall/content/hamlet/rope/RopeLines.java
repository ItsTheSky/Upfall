package net.itsthesky.upfall.content.hamlet.rope;

import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RopeLines {
    private RopeLines() {}

    private static final double ROPE_SAG = 0.07;
    private static final double ZIPLINE_SAG = 0.025;
    private static final double MAX_SAG = 1.6;

    public enum LinkResult {
        LINKED, NOT_ANCHORS, MISMATCH, TOO_FAR, FULL, SAME;

        public String message() {
            return "message.upfall.rope." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static Vec3 attachPoint(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof AbstractRopeAnchorBlock anchor) {
            return anchor.attachPoint(pos, state);
        }
        return Vec3.atCenterOf(pos);
    }

    public static double sag(Vec3 from, Vec3 to, boolean zipline) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return Math.min(MAX_SAG, Math.sqrt(dx * dx + dz * dz) * (zipline ? ZIPLINE_SAG : ROPE_SAG));
    }

    public static Vec3 point(Vec3 from, Vec3 to, double sag, double t) {
        return new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t - sag * 4.0 * t * (1.0 - t),
                from.z + (to.z - from.z) * t);
    }

    public static double closestT(Vec3 from, Vec3 to, double sag, Vec3 target) {
        int steps = 48;
        double best = 0.0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double dist = point(from, to, sag, t).distanceToSqr(target);
            if (dist < bestDist) {
                bestDist = dist;
                best = t;
            }
        }
        double step = 1.0 / steps;
        for (int i = 0; i < 12; i++) {
            step *= 0.5;
            double lower = Math.max(0.0, best - step);
            double upper = Math.min(1.0, best + step);
            double dl = point(from, to, sag, lower).distanceToSqr(target);
            double du = point(from, to, sag, upper).distanceToSqr(target);
            if (dl < bestDist) {
                best = lower;
                bestDist = dl;
            } else if (du < bestDist) {
                best = upper;
                bestDist = du;
            }
        }
        return best;
    }

    public static LinkResult link(Level level, BlockPos a, BlockPos b) {
        if (a.equals(b)) {
            return LinkResult.SAME;
        }
        BlockState stateA = level.getBlockState(a);
        BlockState stateB = level.getBlockState(b);
        if (!(stateA.getBlock() instanceof AbstractRopeAnchorBlock anchorA) || !(stateB.getBlock() instanceof AbstractRopeAnchorBlock anchorB)
                || !(level.getBlockEntity(a) instanceof RopeAnchorBlockEntity beA) || !(level.getBlockEntity(b) instanceof RopeAnchorBlockEntity beB)) {
            return LinkResult.NOT_ANCHORS;
        }
        if (anchorA.isZipline() != anchorB.isZipline()) {
            return LinkResult.MISMATCH;
        }
        if (anchorA.attachPoint(a, stateA).distanceTo(anchorB.attachPoint(b, stateB)) > Math.min(anchorA.maxLength(), anchorB.maxLength())) {
            return LinkResult.TOO_FAR;
        }
        if (beA.isLinkedTo(b)) {
            return LinkResult.LINKED;
        }
        if (anchorA.isZipline()) {
            unlinkAll(level, a);
            unlinkAll(level, b);
            Direction along = Direction.getNearest(b.getX() - a.getX(), 0, b.getZ() - a.getZ());
            level.setBlock(a, stateA.setValue(ZiplineAnchorBlock.FACING, along), Block.UPDATE_ALL);
            level.setBlock(b, stateB.setValue(ZiplineAnchorBlock.FACING, along.getOpposite()), Block.UPDATE_ALL);
        } else if (beA.links().size() >= anchorA.maxLinks() || beB.links().size() >= anchorB.maxLinks()) {
            return LinkResult.FULL;
        }
        beA.addLink(b);
        beB.addLink(a);
        return LinkResult.LINKED;
    }

    public static void unlinkAll(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof RopeAnchorBlockEntity anchor)) {
            return;
        }
        for (BlockPos other : List.copyOf(anchor.links())) {
            if (level.isLoaded(other) && level.getBlockEntity(other) instanceof RopeAnchorBlockEntity otherAnchor) {
                otherAnchor.removeLink(pos);
            }
        }
        anchor.clearLinks();
    }

    public static BlockPos ziplinePartner(Level level, BlockPos anchor) {
        if (!(level.getBlockState(anchor).getBlock() instanceof ZiplineAnchorBlock)
                || !(level.getBlockEntity(anchor) instanceof RopeAnchorBlockEntity be) || be.links().isEmpty()) {
            return null;
        }
        BlockPos other = be.links().getFirst();
        if (!level.isLoaded(other) || !(level.getBlockState(other).getBlock() instanceof ZiplineAnchorBlock)
                || !(level.getBlockEntity(other) instanceof RopeAnchorBlockEntity otherBe) || !otherBe.isLinkedTo(anchor)) {
            return null;
        }
        return other;
    }
}
