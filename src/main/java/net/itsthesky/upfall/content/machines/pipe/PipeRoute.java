package net.itsthesky.upfall.content.machines.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record PipeRoute(List<BlockPos> blocks, Direction exitFacing) {
    private static final double TUBE_FEET_OFFSET = 0.3;

    public BlockPos exit() {
        return blocks.get(blocks.size() - 1);
    }

    public int length() {
        return blocks.size() - 1;
    }

    public Vec3 ejectPoint() {
        Vec3 center = Vec3.atCenterOf(exit());
        return switch (exitFacing) {
            case UP -> new Vec3(center.x, exit().getY() + 1.0, center.z);
            case DOWN -> new Vec3(center.x, exit().getY() - 0.6, center.z);
            default -> new Vec3(center.x + exitFacing.getStepX(), exit().getY(), center.z + exitFacing.getStepZ());
        };
    }

    public Vec3 ejectPush() {
        return switch (exitFacing) {
            case UP -> new Vec3(0, 0.75, 0);
            case DOWN -> new Vec3(0, -0.3, 0);
            default -> new Vec3(exitFacing.getStepX() * 0.6, 0.25, exitFacing.getStepZ() * 0.6);
        };
    }

    public List<Vec3> playerPoints(Vec3 start) {
        List<Vec3> points = new ArrayList<>();
        points.add(start);
        for (BlockPos pos : blocks) {
            points.add(Vec3.atCenterOf(pos).subtract(0, TUBE_FEET_OFFSET, 0));
        }
        points.add(ejectPoint());
        return PipePath.simplify(points);
    }
}
