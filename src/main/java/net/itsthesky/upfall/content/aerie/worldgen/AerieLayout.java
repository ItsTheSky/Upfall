package net.itsthesky.upfall.content.aerie.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AerieLayout {
    private AerieLayout() {}

    public static final int FLOOR_HEIGHT = 5;
    public static final int MAX_EXTENT = 56;
    private static final int MAX_GAP = 26;
    private static final int MAX_EXTRA_TOWERS = 7;

    public enum Kind { KEEP, MEDIUM, SMALL }

    public record Tower(Kind kind, BlockPos base, int radius, int floors, boolean terrace) {
        public int level(int floor) {
            return base.getY() + floor * FLOOR_HEIGHT;
        }

        public int roofY() {
            return level(floors);
        }

        public int underDepth() {
            return radius + radius / 2 + 2;
        }

        public int top() {
            return roofY() + (terrace ? (kind == Kind.KEEP ? 6 : 2) : radius + 3);
        }

        public int bottom() {
            return base.getY() - underDepth();
        }

        double cx() {
            return base.getX() + 0.5;
        }

        double cz() {
            return base.getZ() + 0.5;
        }
    }

    public record Bridge(int from, int to, int fromY, int toY, boolean broken) {}

    public record Plan(List<Tower> towers, List<Bridge> bridges) {}

    public static boolean inDisc(int dx, int dz, int radius) {
        return radius >= 0 && dx * dx + dz * dz <= radius * radius + radius;
    }

    public static boolean onRing(int dx, int dz, int radius) {
        return inDisc(dx, dz, radius) && !inDisc(dx, dz, radius - 1);
    }

    public static Plan plan(long seed, BlockPos origin) {
        RandomSource random = RandomSource.create(seed);
        List<Tower> towers = new ArrayList<>();
        towers.add(new Tower(Kind.KEEP, origin, 7 + random.nextInt(2), 6 + random.nextInt(2), true));

        int wanted = 3 + random.nextInt(MAX_EXTRA_TOWERS - 2);
        for (int attempt = 0; attempt < 80 && towers.size() <= wanted; attempt++) {
            Tower parent = towers.get(random.nextInt(towers.size()));
            boolean medium = random.nextInt(5) < 2;
            int radius = medium ? 5 + random.nextInt(2) : 3 + random.nextInt(2);
            int floors = medium ? 3 + random.nextInt(2) : 2 + random.nextInt(2);
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = parent.radius() + radius + 9 + random.nextInt(14);
            int x = parent.base().getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = parent.base().getZ() + (int) Math.round(Math.sin(angle) * distance);
            int step = random.nextInt(parent.floors() + floors - 1) - (floors - 1);
            int y = parent.base().getY() + step * FLOOR_HEIGHT + random.nextInt(3) - 1;
            if (y < origin.getY() - 12 || y > origin.getY() + 20) continue;
            Tower candidate = new Tower(medium ? Kind.MEDIUM : Kind.SMALL, new BlockPos(x, y, z), radius, floors,
                    medium && random.nextBoolean());
            if (Math.hypot(x - origin.getX(), z - origin.getZ()) + radius + 3 > MAX_EXTENT) continue;
            if (fits(towers, candidate)) {
                towers.add(candidate);
            }
        }

        List<Bridge> bridges = connect(towers, random);
        return prune(towers, bridges);
    }

    private static boolean fits(List<Tower> towers, Tower candidate) {
        for (Tower other : towers) {
            double min = other.radius() + candidate.radius() + 6;
            if (horizontalDistance(other, candidate) < min) {
                return false;
            }
        }
        return true;
    }

    static double horizontalDistance(Tower a, Tower b) {
        return Math.hypot(a.base().getX() - b.base().getX(), a.base().getZ() - b.base().getZ());
    }

    private record Candidate(int a, int b, int ya, int yb, double gap) {}

    private static List<Bridge> connect(List<Tower> towers, RandomSource random) {
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < towers.size(); i++) {
            for (int j = i + 1; j < towers.size(); j++) {
                Candidate candidate = candidate(towers, i, j);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::gap));

        int[] parent = new int[towers.size()];
        for (int i = 0; i < parent.length; i++) parent[i] = i;
        List<Candidate> accepted = new ArrayList<>();
        List<Candidate> spare = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (crosses(towers, accepted, candidate)) continue;
            int ra = find(parent, candidate.a());
            int rb = find(parent, candidate.b());
            if (ra != rb) {
                parent[ra] = rb;
                accepted.add(candidate);
            } else {
                spare.add(candidate);
            }
        }
        if (!spare.isEmpty() && random.nextBoolean()) {
            Candidate extra = spare.get(random.nextInt(spare.size()));
            if (!crosses(towers, accepted, extra)) {
                accepted.add(extra);
            }
        }

        List<Bridge> bridges = new ArrayList<>();
        for (Candidate c : accepted) {
            boolean broken = c.gap() >= 9 && random.nextFloat() < 0.3F;
            bridges.add(new Bridge(c.a(), c.b(), c.ya(), c.yb(), broken));
        }
        return bridges;
    }

    private static Candidate candidate(List<Tower> towers, int i, int j) {
        Tower a = towers.get(i);
        Tower b = towers.get(j);
        double gap = horizontalDistance(a, b) - a.radius() - b.radius();
        if (gap > MAX_GAP) return null;
        int bestA = 0;
        int bestB = 0;
        int bestDy = Integer.MAX_VALUE;
        for (int fa = 0; fa < a.floors(); fa++) {
            for (int fb = 0; fb < b.floors(); fb++) {
                int dy = Math.abs(a.level(fa) - b.level(fb));
                if (dy < bestDy) {
                    bestDy = dy;
                    bestA = a.level(fa);
                    bestB = b.level(fb);
                }
            }
        }
        if (bestDy > Math.max(1.0, gap) * 0.5) return null;
        for (int k = 0; k < towers.size(); k++) {
            if (k == i || k == j) continue;
            Tower other = towers.get(k);
            if (segmentDistance(other.cx(), other.cz(), a.cx(), a.cz(), b.cx(), b.cz()) < other.radius() + 3) {
                return null;
            }
        }
        return new Candidate(i, j, bestA, bestB, gap);
    }

    private static boolean crosses(List<Tower> towers, List<Candidate> accepted, Candidate candidate) {
        Tower a = towers.get(candidate.a());
        Tower b = towers.get(candidate.b());
        for (Candidate other : accepted) {
            if (other.a() == candidate.a() || other.a() == candidate.b() || other.b() == candidate.a() || other.b() == candidate.b()) continue;
            Tower c = towers.get(other.a());
            Tower d = towers.get(other.b());
            if (Math.abs((candidate.ya() + candidate.yb()) - (other.ya() + other.yb())) / 2 >= 6) continue;
            if (segmentsIntersect(a.cx(), a.cz(), b.cx(), b.cz(), c.cx(), c.cz(), d.cx(), d.cz())) {
                return true;
            }
        }
        return false;
    }

    private static Plan prune(List<Tower> towers, List<Bridge> bridges) {
        boolean[] reached = new boolean[towers.size()];
        reached[0] = true;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Bridge bridge : bridges) {
                if (reached[bridge.from()] != reached[bridge.to()]) {
                    reached[bridge.from()] = reached[bridge.to()] = true;
                    changed = true;
                }
            }
        }
        int[] remap = new int[towers.size()];
        List<Tower> kept = new ArrayList<>();
        for (int i = 0; i < towers.size(); i++) {
            remap[i] = reached[i] ? kept.size() : -1;
            if (reached[i]) kept.add(towers.get(i));
        }
        List<Bridge> keptBridges = new ArrayList<>();
        for (Bridge bridge : bridges) {
            if (reached[bridge.from()] && reached[bridge.to()]) {
                keptBridges.add(new Bridge(remap[bridge.from()], remap[bridge.to()], bridge.fromY(), bridge.toY(), bridge.broken()));
            }
        }
        return new Plan(List.copyOf(kept), List.copyOf(keptBridges));
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    static double segmentDistance(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double t = Mth.clamp(((px - ax) * dx + (pz - az) * dz) / (dx * dx + dz * dz), 0.0, 1.0);
        return Math.hypot(px - (ax + t * dx), pz - (az + t * dz));
    }

    private static boolean segmentsIntersect(double ax, double az, double bx, double bz, double cx, double cz, double dx, double dz) {
        double d1 = cross(cx, cz, dx, dz, ax, az);
        double d2 = cross(cx, cz, dx, dz, bx, bz);
        double d3 = cross(ax, az, bx, bz, cx, cz);
        double d4 = cross(ax, az, bx, bz, dx, dz);
        return d1 * d2 < 0 && d3 * d4 < 0;
    }

    private static double cross(double ax, double az, double bx, double bz, double px, double pz) {
        return (bx - ax) * (pz - az) - (bz - az) * (px - ax);
    }

    public record Span(double ax, double az, double ux, double uz, double start, double end, int startY, int endY) {
        public static Span of(Plan plan, Bridge bridge) {
            Tower a = plan.towers().get(bridge.from());
            Tower b = plan.towers().get(bridge.to());
            double dx = b.cx() - a.cx();
            double dz = b.cz() - a.cz();
            double length = Math.hypot(dx, dz);
            return new Span(a.cx(), a.cz(), dx / length, dz / length, a.radius() - 0.5, length - b.radius() + 0.5,
                    bridge.fromY(), bridge.toY());
        }

        public double along(int x, int z) {
            return (x + 0.5 - ax) * ux + (z + 0.5 - az) * uz;
        }

        public double across(int x, int z) {
            return -(x + 0.5 - ax) * uz + (z + 0.5 - az) * ux;
        }

        public double height(double along) {
            double t = Mth.clamp((along - start) / (end - start), 0.0, 1.0);
            return startY + (endY - startY) * t;
        }

        public double middle() {
            return (start + end) / 2.0;
        }
    }
}
