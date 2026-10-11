package net.itsthesky.upfall.content.machines.pipe;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class PipePath {
    private final List<Vec3> points;
    private final double[] distances;

    public PipePath(List<Vec3> points) {
        this.points = List.copyOf(points);
        this.distances = new double[points.size()];
        for (int i = 1; i < points.size(); i++) {
            distances[i] = distances[i - 1] + points.get(i).distanceTo(points.get(i - 1));
        }
    }

    public List<Vec3> points() {
        return points;
    }

    public double length() {
        return distances[distances.length - 1];
    }

    public Vec3 end() {
        return points.get(points.size() - 1);
    }

    public int segmentAt(double progress) {
        for (int i = 1; i < points.size(); i++) {
            if (progress <= distances[i]) return i - 1;
        }
        return Math.max(0, points.size() - 2);
    }

    public Vec3 at(double progress) {
        if (points.size() == 1) return points.get(0);
        int i = segmentAt(Math.max(0, progress));
        double span = distances[i + 1] - distances[i];
        double t = span <= 1.0E-6 ? 1.0 : Math.min(1.0, Math.max(0.0, (progress - distances[i]) / span));
        return points.get(i).lerp(points.get(i + 1), t);
    }

    public Projection project(Vec3 pos) {
        double bestDistance = Double.MAX_VALUE;
        double bestProgress = 0;
        for (int i = 0; i + 1 < points.size(); i++) {
            Vec3 a = points.get(i);
            Vec3 ab = points.get(i + 1).subtract(a);
            double len2 = ab.lengthSqr();
            double t = len2 <= 1.0E-9 ? 0 : Math.min(1, Math.max(0, pos.subtract(a).dot(ab) / len2));
            double distance = a.add(ab.scale(t)).distanceTo(pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestProgress = distances[i] + t * Math.sqrt(len2);
            }
        }
        return new Projection(bestProgress, bestDistance);
    }

    public record Projection(double progress, double distance) {}

    public static List<Vec3> simplify(List<Vec3> points) {
        List<Vec3> out = new ArrayList<>();
        for (Vec3 point : points) {
            if (!out.isEmpty() && out.get(out.size() - 1).distanceToSqr(point) < 1.0E-6) continue;
            if (out.size() >= 2) {
                Vec3 a = out.get(out.size() - 2);
                Vec3 b = out.get(out.size() - 1);
                if (b.subtract(a).normalize().dot(point.subtract(b).normalize()) > 0.9999) {
                    out.set(out.size() - 1, point);
                    continue;
                }
            }
            out.add(point);
        }
        return out;
    }
}
