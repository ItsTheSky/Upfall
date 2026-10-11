package net.itsthesky.upfall.content.chronospire.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record PlatformPath(List<Vec3> points, double speed, int pause, int offset) {
    public PlatformPath {
        points = List.copyOf(points);
    }

    public double length() {
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            length += points.get(i).distanceTo(points.get(i - 1));
        }
        return length;
    }

    public int travelTicks() {
        return Math.max(1, Mth.ceil(length() / speed));
    }

    public int period() {
        return 2 * (travelTicks() + pause);
    }

    public Vec3 positionAt(long gameTime) {
        if (points.size() < 2) {
            return points.isEmpty() ? Vec3.ZERO : points.get(0);
        }
        int travel = travelTicks();
        long t = Math.floorMod(gameTime + offset, (long) period());
        double progress;
        if (t < pause) {
            progress = 0;
        } else if (t < pause + travel) {
            progress = (t - pause) / (double) travel;
        } else if (t < 2L * pause + travel) {
            progress = 1;
        } else {
            progress = 1 - (t - 2L * pause - travel) / (double) travel;
        }
        double eased = progress * progress * (3 - 2 * progress);
        return along(eased * length());
    }

    private Vec3 along(double distance) {
        for (int i = 1; i < points.size(); i++) {
            Vec3 a = points.get(i - 1);
            Vec3 b = points.get(i);
            double segment = a.distanceTo(b);
            if (distance <= segment || i == points.size() - 1) {
                return segment <= 1.0E-6 ? b : a.lerp(b, Math.min(1, distance / segment));
            }
            distance -= segment;
        }
        return points.get(points.size() - 1);
    }

    public PlatformPath offsetBy(Vec3 delta) {
        List<Vec3> moved = new ArrayList<>();
        for (Vec3 p : points) {
            moved.add(p.add(delta));
        }
        return new PlatformPath(moved, speed, pause, offset);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Vec3 p : points) {
            list.add(DoubleTag.valueOf(p.x));
            list.add(DoubleTag.valueOf(p.y));
            list.add(DoubleTag.valueOf(p.z));
        }
        tag.put("Points", list);
        tag.putDouble("Speed", speed);
        tag.putInt("Pause", pause);
        tag.putInt("Offset", offset);
        return tag;
    }

    public static PlatformPath load(CompoundTag tag) {
        ListTag list = tag.getList("Points", Tag.TAG_DOUBLE);
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i + 2 < list.size(); i += 3) {
            points.add(new Vec3(list.getDouble(i), list.getDouble(i + 1), list.getDouble(i + 2)));
        }
        double speed = tag.contains("Speed") ? tag.getDouble("Speed") : 0.1;
        return new PlatformPath(points, Math.max(0.01, speed), tag.getInt("Pause"), tag.getInt("Offset"));
    }
}
