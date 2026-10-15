package net.itsthesky.upfall.content.reliquary.worldgen;

import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class ReliquaryLayout {
    private ReliquaryLayout() {}

    public static final int MAX_REACH = 112;
    static final int SAFE_REACH = 106;
    static final int DEBRIS_REACH = 110;
    static final int MIN_Y = 102;
    static final int MAX_Y = 298;
    static final double SQRT2 = Math.sqrt(2.0);
    static final int STONE_SPACING = 5;
    static final int MAX_STEPS_GAP = 72;
    static final double ZIPLINE_MIN_GAP = 26.0;
    static final double ZIPLINE_MAX_LENGTH = 62.0;
    static final int ROPE_LENGTH = 26;
    static final int CLOUD_GUESS = 58;
    public static final int MIN_SATELLITES = 6;
    public static final int MAX_SATELLITES = 10;
    public static final int REQUIRED_SHARDS = 4;

    public enum Interior { CORE, ARCHIVE, HANGING_GARDEN, ECHO_HALL, COLLAPSED, SHARD_VAULT }

    public enum Style { OPEN, RIBBED }

    public enum Route { STEPS, UPDRAFT }

    public enum Shape { FRAME, RIBBED, NESTED, BROKEN, SOLID, SPIKED, CLUSTER }

    public enum Tilt { EDGE, FLAT, DIAMOND }

    public record Cube(BlockPos center, int half, Direction.Axis axis, Interior interior, Style style, boolean shard,
                       int innerHalf, int innerA, int innerDy, int innerP, double collapse) {
        public int cx() {
            return center.getX();
        }

        public int cy() {
            return center.getY();
        }

        public int cz() {
            return center.getZ();
        }

        public double thickness() {
            return 1.6 + half * 0.08;
        }

        public int frameBlocks() {
            return (int) Math.round(thickness());
        }

        public int deckY() {
            return cy() - Math.max(3, (int) Math.round(half * 0.3));
        }

        public int deckWidth() {
            return (int) Math.floor(half * SQRT2 - (cy() - deckY()));
        }

        public double reach() {
            return half * SQRT2;
        }

        public int extentX() {
            return axis == Direction.Axis.X ? half : (int) Math.ceil(reach());
        }

        public int extentZ() {
            return axis == Direction.Axis.X ? (int) Math.ceil(reach()) : half;
        }

        public double localA(double x, double z) {
            return axis == Direction.Axis.X ? x - cx() : z - cz();
        }

        public double localP(double x, double z) {
            return axis == Direction.Axis.X ? z - cz() : x - cx();
        }

        public int worldX(int a, int p) {
            return axis == Direction.Axis.X ? cx() + a : cx() + p;
        }

        public int worldZ(int a, int p) {
            return axis == Direction.Axis.X ? cz() + p : cz() + a;
        }

        public Direction axisDirection(int sign) {
            return axis == Direction.Axis.X ? (sign > 0 ? Direction.EAST : Direction.WEST) : (sign > 0 ? Direction.SOUTH : Direction.NORTH);
        }

        public Direction sideDirection(int sign) {
            return axis == Direction.Axis.X ? (sign > 0 ? Direction.SOUTH : Direction.NORTH) : (sign > 0 ? Direction.EAST : Direction.WEST);
        }

        public boolean inHull(double x, double y, double z, double margin) {
            double h = half + margin;
            double a = localA(x, z);
            if (Math.abs(a) > h) return false;
            double p = localP(x, z);
            double dy = y - cy();
            return Math.abs(dy + p) / SQRT2 <= h && Math.abs(dy - p) / SQRT2 <= h;
        }

        public boolean onDeck(double x, double z, int inset) {
            return Math.abs(localA(x, z)) <= half - inset && Math.abs(localP(x, z)) <= deckWidth() - inset;
        }
    }

    public record Fragment(BlockPos center, int half, Shape shape, Tilt tilt, Direction.Axis axis, int variant) {
        public int spike() {
            return shape == Shape.SPIKED ? 2 + Math.floorMod(variant >> 4, 3) : 0;
        }

        public int radius() {
            return (int) Math.ceil(half * SQRT2) + 1 + spike();
        }

        public double extent() {
            return half * 1.75 + spike();
        }
    }

    public record Exit(int cube, BlockPos edge, Direction normal) {
        public BlockPos out(int distance) {
            return edge.relative(normal, distance);
        }
    }

    public record Stone(BlockPos center, Direction.Axis axis, boolean fragment) {}

    public record Link(int from, int to, Route route, Exit low, Exit high, List<Stone> stones,
                       @Nullable BlockPos anchorLow, @Nullable BlockPos anchorHigh) {
        public @Nullable BlockPos vent() {
            return route == Route.UPDRAFT ? stones.getLast().center() : null;
        }

        public int columnTop() {
            return high.edge().getY() + 3;
        }
    }

    public record Approach(Exit exit, BlockPos column, int top) {}

    public record Trail(Exit exit, List<Stone> stones) {}

    public record Beam(BlockPos start, Direction direction, int length, int width) {
        public List<BlockPos> voxels() {
            List<BlockPos> voxels = new ArrayList<>(length * width);
            Direction side = direction.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
            for (int i = 0; i < length; i++) {
                BlockPos pos = start.relative(direction, i);
                voxels.add(pos);
                if (width > 1) voxels.add(pos.relative(side));
            }
            return voxels;
        }
    }

    public record Segment(double ax, double ay, double az, double bx, double by, double bz, double radius) {
        public static Segment of(double ax, double ay, double az, double bx, double by, double bz, double radius) {
            return new Segment(ax, ay, az, bx, by, bz, radius);
        }

        public static Segment between(BlockPos a, double ya, BlockPos b, double yb, double radius) {
            return new Segment(a.getX(), ya, a.getZ(), b.getX(), yb, b.getZ(), radius);
        }

        public double distance(double x, double y, double z) {
            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            double lengthSqr = dx * dx + dy * dy + dz * dz;
            double t = lengthSqr == 0 ? 0.0 : Mth.clamp(((x - ax) * dx + (y - ay) * dy + (z - az) * dz) / lengthSqr, 0.0, 1.0);
            double px = ax + dx * t - x;
            double py = ay + dy * t - y;
            double pz = az + dz * t - z;
            return Math.sqrt(px * px + py * py + pz * pz);
        }

        public boolean near(double x, double y, double z, double pad) {
            if (x < Math.min(ax, bx) - radius - pad || x > Math.max(ax, bx) + radius + pad) return false;
            if (z < Math.min(az, bz) - radius - pad || z > Math.max(az, bz) + radius + pad) return false;
            if (y < Math.min(ay, by) - radius - pad || y > Math.max(ay, by) + radius + pad) return false;
            return distance(x, y, z) < radius + pad;
        }

        double length() {
            return Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay) + (bz - az) * (bz - az));
        }

        double gap(Segment other) {
            int steps = Math.max(1, (int) Math.ceil(length()));
            double best = Double.MAX_VALUE;
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                best = Math.min(best, other.distance(ax + (bx - ax) * t, ay + (by - ay) * t, az + (bz - az) * t));
            }
            return best - radius - other.radius;
        }
    }

    public record Plan(BlockPos origin, List<Cube> cubes, List<Link> links, @Nullable Approach approach, List<Trail> trails,
                       List<List<Beam>> protrusions, List<List<Beam>> debris, List<List<Fragment>> fragments, List<Segment> corridors,
                       List<List<Exit>> exits) {
        public int shardCount() {
            return (int) cubes.stream().filter(Cube::shard).count();
        }

        public int fragmentCount() {
            return fragments.stream().mapToInt(List::size).sum();
        }
    }

    public static Plan plan(long seed, BlockPos origin) {
        Plan best = null;
        for (int attempt = 0; attempt < 8; attempt++) {
            Plan plan = new Builder(RandomSource.create(seed + attempt * 0x9E3779B97F4A7C15L), origin).build();
            if (plan.cubes().size() - 1 >= MIN_SATELLITES && plan.shardCount() >= REQUIRED_SHARDS && plan.approach() != null) {
                return plan;
            }
            if (best == null || plan.shardCount() > best.shardCount()) {
                best = plan;
            }
        }
        return best;
    }

    static boolean withinReach(BlockPos origin, double x, double z, int reach) {
        return Math.abs(x - origin.getX()) <= reach && Math.abs(z - origin.getZ()) <= reach;
    }

    private static final class Builder {
        private final RandomSource random;
        private final BlockPos origin;
        private final List<Cube> cubes = new ArrayList<>();
        private final List<Segment> corridors = new ArrayList<>();

        Builder(RandomSource random, BlockPos origin) {
            this.random = random;
            this.origin = origin;
        }

        Plan build() {
            placeCubes();
            List<Link> links = connect();
            prune(links);
            assignShards();
            List<List<Exit>> exits = new ArrayList<>();
            for (int i = 0; i < cubes.size(); i++) exits.add(new ArrayList<>());
            for (Link link : links) {
                exits.get(link.low().cube()).add(link.low());
                exits.get(link.high().cube()).add(link.high());
                corridors.addAll(linkSegments(link));
            }
            links = ziplines(links);
            Approach approach = approach(exits);
            List<Trail> trails = trails(exits, approach);
            for (int i = 0; i < cubes.size(); i++) {
                for (Exit exit : exits.get(i)) {
                    corridors.add(walk(cubes.get(i), exit));
                }
            }
            List<List<Beam>> protrusions = new ArrayList<>();
            for (int i = 0; i < cubes.size(); i++) {
                protrusions.add(protrusions(i));
            }
            List<List<Beam>> debris = debris(protrusions);
            List<List<Fragment>> fragments = fragments(links);
            return new Plan(origin, List.copyOf(cubes), List.copyOf(links), approach, List.copyOf(trails), List.copyOf(protrusions),
                    debris, fragments, List.copyOf(corridors), exits.stream().map(List::copyOf).toList());
        }

        private void placeCubes() {
            int coreHalf = 21 + random.nextInt(7);
            cubes.add(new Cube(origin, coreHalf, randomAxis(), Interior.CORE, random.nextBoolean() ? Style.RIBBED : Style.OPEN, false,
                    (int) Math.round(coreHalf * (0.55 + random.nextDouble() * 0.08)), random.nextInt(5) - 2, random.nextInt(5) - 2,
                    random.nextInt(5) - 2, random.nextDouble() * Math.PI * 2.0));

            int wanted = MIN_SATELLITES + random.nextInt(MAX_SATELLITES - MIN_SATELLITES + 1);
            List<Interior> kinds = new ArrayList<>(List.of(Interior.SHARD_VAULT, Interior.SHARD_VAULT, Interior.ARCHIVE,
                    Interior.HANGING_GARDEN, Interior.ECHO_HALL, Interior.COLLAPSED));
            Interior[] pool = {Interior.ARCHIVE, Interior.HANGING_GARDEN, Interior.ECHO_HALL, Interior.COLLAPSED, Interior.SHARD_VAULT};
            while (kinds.size() < wanted) kinds.add(pool[random.nextInt(pool.length)]);
            Collections.shuffle(kinds, new Random(random.nextLong()));

            for (int attempt = 0; attempt < 400 && cubes.size() <= wanted; attempt++) {
                int half = 6 + random.nextInt(11);
                double angle = random.nextDouble() * Math.PI * 2.0;
                Cube core = cubes.getFirst();
                double min = core.reach() + half * SQRT2 + 4;
                double distance = min + random.nextDouble() * 60.0;
                int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
                int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
                int y = origin.getY() + random.nextInt(69) - 34;
                int reach = (int) Math.ceil(half * SQRT2);
                if (y - reach < MIN_Y + 4 || y + reach > MAX_Y - 4) continue;
                Direction.Axis axis = randomAxis();
                int innerHalf = (int) Math.round(half * (0.5 + random.nextDouble() * 0.12));
                int slack = Math.max(0, (half - innerHalf - 3) / 2);
                Interior interior = kinds.get(cubes.size() - 1);
                Style style = random.nextInt(5) < 2 ? Style.RIBBED : Style.OPEN;
                Cube candidate = new Cube(new BlockPos(x, y, z), half, axis, interior, style, false, innerHalf,
                        slack == 0 ? 0 : random.nextInt(2 * slack + 1) - slack, slack == 0 ? 0 : random.nextInt(2 * slack + 1) - slack,
                        slack == 0 ? 0 : random.nextInt(2 * slack + 1) - slack, random.nextDouble() * Math.PI * 2.0);
                if (Math.abs(x - origin.getX()) + candidate.extentX() > SAFE_REACH - 6
                        || Math.abs(z - origin.getZ()) + candidate.extentZ() > SAFE_REACH - 6) continue;
                if (fits(candidate)) {
                    cubes.add(candidate);
                }
            }
        }

        private Direction.Axis randomAxis() {
            return random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        }

        private boolean fits(Cube candidate) {
            for (Cube other : cubes) {
                double distance = Math.sqrt(candidate.center().distSqr(other.center()));
                double spacing = candidate.half() <= 10 && other.half() <= 10 ? 1.4 : 1.6;
                if (distance < spacing * (candidate.half() + other.half()) + 4) return false;
            }
            return true;
        }

        private Exit exit(int index, double tx, double tz) {
            Cube c = cubes.get(index);
            double da = c.localA(tx, tz);
            double dp = c.localP(tx, tz);
            int h = c.half();
            int w = c.deckWidth();
            int margin = c.frameBlocks() + 3;
            int a;
            int p;
            Direction normal;
            if (Math.abs(da) * w >= Math.abs(dp) * h) {
                int sign = da >= 0 ? 1 : -1;
                a = sign * h;
                int limit = Math.max(0, w - margin);
                p = da == 0 ? 0 : Mth.clamp((int) Math.round(dp * h / Math.abs(da)), -limit, limit);
                normal = c.axisDirection(sign);
            } else {
                int sign = dp >= 0 ? 1 : -1;
                p = sign * w;
                int limit = Math.max(0, h - margin);
                a = Mth.clamp((int) Math.round(da * w / Math.abs(dp)), -limit, limit);
                normal = c.sideDirection(sign);
            }
            return new Exit(index, new BlockPos(c.worldX(a, p), c.deckY(), c.worldZ(a, p)), normal);
        }

        private List<Link> connect() {
            record Candidate(Link link, double score) {}
            List<Candidate> candidates = new ArrayList<>();
            for (int i = 0; i < cubes.size(); i++) {
                for (int j = i + 1; j < cubes.size(); j++) {
                    Link link = route(i, j);
                    if (link != null) {
                        double gap = horizontal(link.low().out(2), link.high().out(2));
                        candidates.add(new Candidate(link, gap + 1.5 * (link.high().edge().getY() - link.low().edge().getY())));
                    }
                }
            }
            candidates.sort(Comparator.comparingDouble(Candidate::score));
            int[] parent = new int[cubes.size()];
            for (int i = 0; i < parent.length; i++) parent[i] = i;
            List<Link> accepted = new ArrayList<>();
            List<Link> spare = new ArrayList<>();
            for (Candidate candidate : candidates) {
                Link link = candidate.link();
                if (conflicts(accepted, link)) continue;
                int ra = find(parent, link.from());
                int rb = find(parent, link.to());
                if (ra != rb) {
                    parent[ra] = rb;
                    accepted.add(link);
                } else {
                    spare.add(link);
                }
            }
            int extras = 1 + random.nextInt(2);
            for (int i = 0; i < extras && !spare.isEmpty(); i++) {
                Link extra = spare.remove(random.nextInt(spare.size()));
                if (!conflicts(accepted, extra)) accepted.add(extra);
            }
            return accepted;
        }

        private boolean conflicts(List<Link> accepted, Link link) {
            List<Segment> mine = linkSegments(link);
            for (Link other : accepted) {
                boolean shares = other.from() == link.from() || other.to() == link.to() || other.from() == link.to() || other.to() == link.from();
                for (Segment a : mine) {
                    for (Segment b : linkSegments(other)) {
                        if (a.gap(b) < (shares ? -1.5 : 1.0)) return true;
                    }
                }
                if (shares && sameExit(other, link)) return true;
            }
            return false;
        }

        private static boolean sameExit(Link a, Link b) {
            for (Exit x : List.of(a.low(), a.high())) {
                for (Exit y : List.of(b.low(), b.high())) {
                    if (x.cube() == y.cube() && x.normal() == y.normal() && x.edge().distManhattan(y.edge()) < 7) return true;
                }
            }
            return false;
        }

        private @Nullable Link route(int i, int j) {
            Cube a = cubes.get(i);
            Cube b = cubes.get(j);
            Exit ea = exit(i, b.cx(), b.cz());
            Exit eb = exit(j, a.cx(), a.cz());
            boolean aLow = ea.edge().getY() <= eb.edge().getY();
            Exit low = aLow ? ea : eb;
            Exit high = aLow ? eb : ea;
            BlockPos from = low.out(2);
            BlockPos to = high.out(2);
            double gap = horizontal(from, to);
            if (gap > MAX_STEPS_GAP) return null;
            int rise = high.edge().getY() - low.edge().getY();
            int count = Math.max(2, (int) Math.ceil(gap / STONE_SPACING) + 1);
            Route route = rise <= count - 1 ? Route.STEPS : Route.UPDRAFT;
            Direction.Axis axis = Math.abs(to.getX() - from.getX()) >= Math.abs(to.getZ() - from.getZ()) ? Direction.Axis.X : Direction.Axis.Z;
            List<Stone> stones = new ArrayList<>();
            for (int k = 0; k < count; k++) {
                double t = (double) k / (count - 1);
                int x = (int) Math.round(Mth.lerp(t, from.getX(), to.getX()));
                int z = (int) Math.round(Mth.lerp(t, from.getZ(), to.getZ()));
                int y = low.edge().getY() + (route == Route.STEPS ? (int) Math.round(rise * t) : 0);
                if (!withinReach(origin, x, z, SAFE_REACH - 1)) return null;
                stones.add(new Stone(new BlockPos(x, y, z), axis, false));
            }
            Link link = new Link(Math.min(i, j), Math.max(i, j), route, low, high, List.copyOf(stones), null, null);
            for (Segment segment : linkSegments(link)) {
                if (blocked(segment, low, high)) return null;
            }
            return link;
        }

        private boolean blocked(Segment segment, @Nullable Exit low, @Nullable Exit high) {
            int steps = Math.max(1, (int) Math.ceil(segment.length()));
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                double x = Mth.lerp(t, segment.ax(), segment.bx());
                double y = Mth.lerp(t, segment.ay(), segment.by());
                double z = Mth.lerp(t, segment.az(), segment.bz());
                for (int k = 0; k < cubes.size(); k++) {
                    if (low != null && k == low.cube() && horizontal(x, z, low.out(2)) < 6) continue;
                    if (high != null && k == high.cube() && horizontal(x, z, high.out(2)) < 6) continue;
                    if (cubes.get(k).inHull(x, y, z, segment.radius() * 0.5) || cubes.get(k).inHull(x, y - 2.5, z, 1.0)) return true;
                }
            }
            return false;
        }

        List<Segment> linkSegments(Link link) {
            List<Segment> segments = new ArrayList<>();
            Stone first = link.stones().getFirst();
            Stone last = link.stones().getLast();
            segments.add(Segment.between(first.center(), first.center().getY() + 2.5, last.center(), last.center().getY() + 2.5, 2.4));
            if (link.route() == Route.UPDRAFT) {
                BlockPos column = last.center();
                segments.add(Segment.of(column.getX(), column.getY() + 1, column.getZ(), column.getX(), link.columnTop() + 2, column.getZ(), 1.6));
                segments.add(Segment.between(link.high().edge(), link.high().edge().getY() + 2.5, column, link.high().edge().getY() + 2.5, 2.0));
            }
            if (link.anchorLow() != null && link.anchorHigh() != null) {
                Vec3 a = Vec3.atCenterOf(link.anchorLow());
                Vec3 b = Vec3.atCenterOf(link.anchorHigh());
                double sag = RopeLines.sag(a, b, true);
                Vec3 previous = a;
                for (int i = 1; i <= 4; i++) {
                    Vec3 next = RopeLines.point(a, b, sag, i / 4.0);
                    segments.add(Segment.of(previous.x, previous.y - 1.2, previous.z, next.x, next.y - 1.2, next.z, 1.6));
                    previous = next;
                }
            }
            return segments;
        }

        private Segment walk(Cube cube, Exit exit) {
            BlockPos end = exit.out(1);
            return Segment.of(cube.cx(), cube.deckY() + 2.5, cube.cz(), end.getX(), cube.deckY() + 2.5, end.getZ(), 2.2);
        }

        private void prune(List<Link> links) {
            boolean[] reached = new boolean[cubes.size()];
            reached[0] = true;
            boolean changed = true;
            while (changed) {
                changed = false;
                for (Link link : links) {
                    if (reached[link.from()] != reached[link.to()]) {
                        reached[link.from()] = reached[link.to()] = true;
                        changed = true;
                    }
                }
            }
            int[] remap = new int[cubes.size()];
            List<Cube> kept = new ArrayList<>();
            for (int i = 0; i < cubes.size(); i++) {
                remap[i] = reached[i] ? kept.size() : -1;
                if (reached[i]) kept.add(cubes.get(i));
            }
            List<Link> keptLinks = new ArrayList<>();
            for (Link link : links) {
                if (!reached[link.from()]) continue;
                keptLinks.add(new Link(remap[link.from()], remap[link.to()], link.route(),
                        new Exit(remap[link.low().cube()], link.low().edge(), link.low().normal()),
                        new Exit(remap[link.high().cube()], link.high().edge(), link.high().normal()),
                        link.stones(), null, null));
            }
            cubes.clear();
            cubes.addAll(kept);
            links.clear();
            links.addAll(keptLinks);
        }

        private void assignShards() {
            List<Integer> order = new ArrayList<>();
            for (int i = 1; i < cubes.size(); i++) order.add(i);
            Collections.shuffle(order, new Random(random.nextLong()));
            order.sort(Comparator.comparingInt(i -> switch (cubes.get(i).interior()) {
                case SHARD_VAULT -> 0;
                case ECHO_HALL -> 2;
                default -> 1;
            }));
            int wanted = Math.max(REQUIRED_SHARDS, (int) order.stream().filter(i -> cubes.get(i).interior() == Interior.SHARD_VAULT).count());
            for (int k = 0; k < order.size() && k < wanted; k++) {
                int i = order.get(k);
                Cube c = cubes.get(i);
                cubes.set(i, new Cube(c.center(), c.half(), c.axis(), c.interior(), c.style(), true, c.innerHalf(), c.innerA(), c.innerDy(),
                        c.innerP(), c.collapse()));
            }
        }

        private List<Link> ziplines(List<Link> links) {
            List<Link> result = new ArrayList<>();
            for (Link link : links) {
                Link withLine = zipline(link);
                if (withLine != null) {
                    List<Segment> line = linkSegments(withLine).subList(linkSegments(link).size(), linkSegments(withLine).size());
                    boolean clear = true;
                    for (Segment segment : line) {
                        if (blockedLine(segment, link)) clear = false;
                        for (Segment other : corridors) {
                            if (!clear) break;
                            if (segment.gap(other) < -0.5 && !ownCorridor(other, link)) clear = false;
                        }
                    }
                    if (clear) {
                        corridors.addAll(line);
                        result.add(withLine);
                        continue;
                    }
                }
                result.add(link);
            }
            return result;
        }

        private boolean ownCorridor(Segment segment, Link link) {
            return linkSegments(link).contains(segment);
        }

        private boolean blockedLine(Segment segment, Link link) {
            int steps = Math.max(1, (int) Math.ceil(segment.length()));
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                double x = Mth.lerp(t, segment.ax(), segment.bx());
                double y = Mth.lerp(t, segment.ay(), segment.by());
                double z = Mth.lerp(t, segment.az(), segment.bz());
                for (int k = 0; k < cubes.size(); k++) {
                    if (k == link.low().cube() || k == link.high().cube()) continue;
                    if (cubes.get(k).inHull(x, y, z, 1.5)) return true;
                }
            }
            return false;
        }

        private @Nullable Link zipline(Link link) {
            BlockPos from = link.stones().getFirst().center();
            BlockPos to = link.stones().getLast().center();
            double gap = horizontal(from, to);
            if (gap < ZIPLINE_MIN_GAP) return null;
            double sx = -(to.getZ() - from.getZ()) / gap;
            double sz = (to.getX() - from.getX()) / gap;
            int side = random.nextBoolean() ? 3 : -3;
            BlockPos low = anchor(link.low(), sx * side, sz * side);
            BlockPos high = anchor(link.high(), sx * side, sz * side);
            if (low == null || high == null) return null;
            if (Vec3.atCenterOf(low).distanceTo(Vec3.atCenterOf(high)) > ZIPLINE_MAX_LENGTH) return null;
            return new Link(link.from(), link.to(), link.route(), link.low(), link.high(), link.stones(), low, high);
        }

        private @Nullable BlockPos anchor(Exit exit, double ox, double oz) {
            Cube cube = cubes.get(exit.cube());
            BlockPos inside = exit.out(-2);
            int x = inside.getX() + (int) Math.round(ox);
            int z = inside.getZ() + (int) Math.round(oz);
            if (!cube.onDeck(x, z, 1)) return null;
            return new BlockPos(x, cube.deckY() + 3, z);
        }

        private @Nullable Approach approach(List<List<Exit>> exits) {
            List<Integer> order = new ArrayList<>();
            for (int i = 1; i < cubes.size(); i++) order.add(i);
            order.sort(Comparator.comparingInt(i -> cubes.get(i).deckY()));
            for (int i : order) {
                Cube cube = cubes.get(i);
                double ox = cube.cx() - origin.getX();
                double oz = cube.cz() - origin.getZ();
                double length = Math.max(1.0, Math.sqrt(ox * ox + oz * oz));
                Exit exit = exit(i, cube.cx() + ox / length * 200.0, cube.cz() + oz / length * 200.0);
                BlockPos column = exit.out(2);
                if (!withinReach(origin, column.getX(), column.getZ(), SAFE_REACH - 1)) continue;
                int top = cube.deckY() + 3;
                List<Segment> segments = List.of(
                        Segment.of(column.getX(), CLOUD_GUESS, column.getZ(), column.getX(), top + 2, column.getZ(), 1.6),
                        Segment.between(exit.edge(), cube.deckY() + 2.5, column, cube.deckY() + 2.5, 2.0),
                        Segment.of(exit.out(1).getX(), cube.deckY() - ROPE_LENGTH, exit.out(1).getZ(), exit.out(1).getX(), cube.deckY() - 1,
                                exit.out(1).getZ(), 1.0));
                if (!clearOfEverything(segments, exit)) continue;
                corridors.addAll(segments);
                exits.get(i).add(exit);
                return new Approach(exit, column, top);
            }
            return null;
        }

        private boolean clearOfEverything(List<Segment> segments, Exit exit) {
            for (Segment segment : segments) {
                if (blocked(segment, null, exit)) return false;
                for (Segment other : corridors) {
                    if (segment.gap(other) < 0.5) return false;
                }
            }
            return true;
        }

        private List<Trail> trails(List<List<Exit>> exits, @Nullable Approach approach) {
            List<Integer> order = new ArrayList<>();
            for (int i = 1; i < cubes.size(); i++) {
                if (approach == null || approach.exit().cube() != i) order.add(i);
            }
            order.sort(Comparator.comparingDouble(i -> -horizontal(cubes.get(i).center(), origin)));
            List<Trail> trails = new ArrayList<>();
            for (int i : order) {
                if (trails.size() >= 2) break;
                Cube cube = cubes.get(i);
                double ox = cube.cx() - origin.getX();
                double oz = cube.cz() - origin.getZ();
                double length = Math.max(1.0, Math.sqrt(ox * ox + oz * oz));
                double ux = ox / length;
                double uz = oz / length;
                Exit exit = exit(i, cube.cx() + ux * 200.0, cube.cz() + uz * 200.0);
                BlockPos start = exit.out(2);
                Direction.Axis axis = Math.abs(ux) >= Math.abs(uz) ? Direction.Axis.X : Direction.Axis.Z;
                List<Stone> stones = new ArrayList<>();
                int y = start.getY();
                for (int k = 0; k < 12; k++) {
                    int x = start.getX() + (int) Math.round(ux * k * STONE_SPACING);
                    int z = start.getZ() + (int) Math.round(uz * k * STONE_SPACING);
                    if (!withinReach(origin, x, z, SAFE_REACH - 2)) break;
                    boolean fragment = k >= 3 && random.nextFloat() < 0.15F + k * 0.07F;
                    if (k >= 3 && random.nextFloat() < 0.35F) y -= 1 + random.nextInt(2);
                    if (k >= 2 && random.nextFloat() < 0.15F) continue;
                    BlockPos center = new BlockPos(x, y, z);
                    Segment probe = Segment.of(x, y + 1.5, z, x, y + 1.5, z, 2.0);
                    if (blocked(probe, null, exit) || corridors.stream().anyMatch(c -> c.gap(probe) < 0.0)) break;
                    stones.add(new Stone(center, axis, fragment));
                }
                if (stones.size() < 3) continue;
                corridors.add(Segment.between(stones.getFirst().center(), stones.getFirst().center().getY() + 1.5,
                        stones.getLast().center(), stones.getLast().center().getY() + 1.5, 2.0));
                exits.get(i).add(exit);
                trails.add(new Trail(exit, List.copyOf(stones)));
            }
            return trails;
        }

        private boolean clearBeam(Beam beam, int owner) {
            Cube own = owner >= 0 ? cubes.get(owner) : null;
            for (BlockPos pos : beam.voxels()) {
                double x = pos.getX();
                double y = pos.getY();
                double z = pos.getZ();
                if (!withinReach(origin, x, z, DEBRIS_REACH) || y < MIN_Y || y > MAX_Y) return false;
                for (int k = 0; k < cubes.size(); k++) {
                    if (k == owner) continue;
                    if (cubes.get(k).inHull(x, y, z, 1.5)) return false;
                }
                if (own != null && own.onDeck(x, z, -1) && y >= own.deckY() - 1 && y <= own.deckY() + 5) return false;
                for (Segment corridor : corridors) {
                    if (corridor.near(x, y, z, 1.5)) return false;
                }
            }
            return true;
        }

        private List<Beam> protrusions(int index) {
            Cube cube = cubes.get(index);
            int wanted = 3 + random.nextInt(5) + (cube.interior() == Interior.COLLAPSED ? 3 : 0) + (index == 0 ? 4 : 0);
            List<Beam> beams = new ArrayList<>();
            int h = cube.half();
            int corner = (int) Math.floor(cube.reach()) - 1;
            for (int attempt = 0; attempt < wanted * 6 && beams.size() < wanted; attempt++) {
                int length = 6 + random.nextInt(15);
                int width = random.nextInt(3) == 0 ? 2 : 1;
                int type = random.nextInt(4);
                int a = random.nextInt(2 * h - 3) - (h - 2);
                Direction dir;
                int x;
                int y;
                int z;
                switch (type) {
                    case 0 -> {
                        dir = Direction.UP;
                        x = cube.worldX(a, 0);
                        z = cube.worldZ(a, 0);
                        y = cube.cy() + corner - 2;
                    }
                    case 1 -> {
                        dir = Direction.DOWN;
                        x = cube.worldX(a, 0);
                        z = cube.worldZ(a, 0);
                        y = cube.cy() - corner + 2;
                    }
                    case 2 -> {
                        int sign = random.nextBoolean() ? 1 : -1;
                        dir = cube.sideDirection(sign);
                        x = cube.worldX(a, sign * (corner - 2));
                        z = cube.worldZ(a, sign * (corner - 2));
                        y = cube.cy();
                    }
                    default -> {
                        int sign = random.nextBoolean() ? 1 : -1;
                        dir = cube.axisDirection(sign);
                        int spread = Math.max(1, h - 3);
                        int dy = random.nextInt(2 * spread + 1) - spread;
                        int p = random.nextInt(2 * spread + 1) - spread;
                        if (Math.abs(dy + p) / SQRT2 > h - 3 || Math.abs(dy - p) / SQRT2 > h - 3) continue;
                        x = cube.worldX(sign * (h - 3), p);
                        z = cube.worldZ(sign * (h - 3), p);
                        y = cube.cy() + dy;
                        length += 3;
                    }
                }
                Beam beam = new Beam(new BlockPos(x, y, z), dir, length, width);
                if (clearBeam(beam, index)) beams.add(beam);
            }
            return List.copyOf(beams);
        }

        private List<List<Beam>> debris(List<List<Beam>> protrusions) {
            int wanted = 60 + cubes.size() * 12;
            int totalHalf = cubes.stream().mapToInt(c -> c.half() + (c.interior() == Interior.COLLAPSED ? 8 : 0)).sum();
            Map<Long, List<Beam>> groups = new LinkedHashMap<>();
            int placed = 0;
            for (int attempt = 0; attempt < wanted * 4 && placed < wanted; attempt++) {
                int roll = random.nextInt(totalHalf);
                Cube cube = cubes.getFirst();
                for (Cube c : cubes) {
                    roll -= c.half() + (c.interior() == Interior.COLLAPSED ? 8 : 0);
                    if (roll < 0) {
                        cube = c;
                        break;
                    }
                }
                double theta = random.nextDouble() * Math.PI * 2.0;
                double cos = random.nextDouble() * 2.0 - 1.0;
                double sin = Math.sqrt(1.0 - cos * cos);
                double distance = cube.reach() * (1.05 + random.nextDouble() * (cube.interior() == Interior.COLLAPSED ? 1.3 : 0.9));
                int length = 3 + random.nextInt(5);
                Direction dir = switch (random.nextInt(3)) {
                    case 0 -> Direction.EAST;
                    case 1 -> Direction.UP;
                    default -> Direction.SOUTH;
                };
                int x = cube.cx() + (int) Math.round(sin * Math.cos(theta) * distance) - dir.getStepX() * length / 2;
                int y = cube.cy() + (int) Math.round(cos * distance * 0.8) - dir.getStepY() * length / 2;
                int z = cube.cz() + (int) Math.round(sin * Math.sin(theta) * distance) - dir.getStepZ() * length / 2;
                Beam beam = new Beam(new BlockPos(x, y, z), dir, length, 1);
                if (!clearBeam(beam, -1)) continue;
                long key = ((long) Math.floorDiv(x - origin.getX(), 40) << 32) ^ (Math.floorDiv(z - origin.getZ(), 40) & 0xFFFFFFFFL);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(beam);
                placed++;
            }
            return groups.values().stream().map(List::copyOf).toList();
        }

        private List<List<Fragment>> fragments(List<Link> links) {
            int wanted = 36 + random.nextInt(25);
            List<Fragment> placed = new ArrayList<>();
            Map<Long, List<Fragment>> groups = new LinkedHashMap<>();
            for (int attempt = 0; attempt < wanted * 10 && placed.size() < wanted; attempt++) {
                Shape shape = shape();
                int half = switch (shape) {
                    case SOLID -> 2 + random.nextInt(3);
                    case NESTED -> 4 + random.nextInt(4);
                    case CLUSTER -> 2 + random.nextInt(2);
                    default -> random.nextInt(7) == 0 ? 7 + random.nextInt(2) : 2 + random.nextInt(5);
                };
                Tilt tilt = tilt();
                if (tilt != Tilt.FLAT) half = Math.max(3, half);
                BlockPos center = fragmentAnchor(links, placed, half);
                Fragment fragment = new Fragment(center, half, shape, tilt, randomAxis(), random.nextInt(1 << 16));
                List<Fragment> batch = new ArrayList<>(List.of(fragment));
                if (shape == Shape.CLUSTER) {
                    int extra = 1 + random.nextInt(2);
                    Fragment previous = fragment;
                    for (int k = 0; k < extra; k++) {
                        int next = 2 + random.nextInt(2);
                        Direction dir = Direction.getRandom(random);
                        int step = previous.half() + next + (random.nextBoolean() ? 0 : 1);
                        BlockPos pos = previous.center().relative(dir, step).offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
                        Tilt subTilt = random.nextInt(3) == 0 ? Tilt.EDGE : Tilt.FLAT;
                        previous = new Fragment(pos, subTilt == Tilt.FLAT ? next : 3, Shape.CLUSTER, subTilt, randomAxis(), random.nextInt(1 << 16));
                        batch.add(previous);
                    }
                }
                if (!batch.stream().allMatch(f -> clearFragment(f, placed))) continue;
                for (Fragment f : batch) {
                    placed.add(f);
                    long key = ((long) Math.floorDiv(f.center().getX() - origin.getX(), 32) << 32)
                            ^ (Math.floorDiv(f.center().getZ() - origin.getZ(), 32) & 0xFFFFFFFFL);
                    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(f);
                }
            }
            return groups.values().stream().map(List::copyOf).toList();
        }

        private Shape shape() {
            int roll = random.nextInt(100);
            if (roll < 24) return Shape.FRAME;
            if (roll < 39) return Shape.RIBBED;
            if (roll < 51) return Shape.NESTED;
            if (roll < 64) return Shape.BROKEN;
            if (roll < 74) return Shape.SOLID;
            if (roll < 86) return Shape.SPIKED;
            return Shape.CLUSTER;
        }

        private Tilt tilt() {
            int roll = random.nextInt(100);
            if (roll < 64) return Tilt.EDGE;
            if (roll < 82) return Tilt.DIAMOND;
            return Tilt.FLAT;
        }

        private BlockPos fragmentAnchor(List<Link> links, List<Fragment> placed, int half) {
            int roll = random.nextInt(10);
            if (roll >= 8 && !placed.isEmpty()) {
                Fragment base = placed.get(random.nextInt(placed.size()));
                return offset(base.center(), (base.half() + half) * (0.9 + random.nextDouble() * 0.5), 0.8);
            }
            if (roll >= 5 && !links.isEmpty()) {
                Link link = links.get(random.nextInt(links.size()));
                BlockPos stone = link.stones().get(random.nextInt(link.stones().size())).center();
                double lateral = (6 + half + random.nextDouble() * 10) * (random.nextBoolean() ? 1 : -1);
                int dx = link.stones().getLast().center().getX() - link.stones().getFirst().center().getX();
                int dz = link.stones().getLast().center().getZ() - link.stones().getFirst().center().getZ();
                double length = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
                return new BlockPos(stone.getX() + (int) Math.round(-dz / length * lateral), stone.getY() + random.nextInt(25) - 14,
                        stone.getZ() + (int) Math.round(dx / length * lateral));
            }
            Cube cube = roll < 3 ? cubes.getFirst() : cubes.get(random.nextInt(cubes.size()));
            return offset(cube.center(), cube.reach() * (0.8 + random.nextDouble() * 0.55) + half, 0.75);
        }

        private BlockPos offset(BlockPos center, double distance, double flatten) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double cos = random.nextDouble() * 2.0 - 1.0;
            double sin = Math.sqrt(1.0 - cos * cos);
            return center.offset((int) Math.round(sin * Math.cos(theta) * distance), (int) Math.round(cos * distance * flatten),
                    (int) Math.round(sin * Math.sin(theta) * distance));
        }

        private boolean clearFragment(Fragment fragment, List<Fragment> placed) {
            int x = fragment.center().getX();
            int y = fragment.center().getY();
            int z = fragment.center().getZ();
            int r = fragment.radius();
            double extent = fragment.extent();
            if (!withinReach(origin, x, z, DEBRIS_REACH - r) || y - r < MIN_Y || y + r > MAX_Y) return false;
            for (Segment corridor : corridors) {
                if (corridor.near(x, y, z, extent + 1.5)) return false;
            }
            for (int k = 0; k < cubes.size(); k++) {
                Cube cube = cubes.get(k);
                if (cube.inHull(x, y, z, -2.0)) return false;
                int top = cube.deckY() + (k == 0 ? 10 : 6);
                if (cube.onDeck(x, z, -(int) Math.ceil(extent) - 1) && y + extent >= cube.deckY() - 2 && y - extent <= top) return false;
            }
            for (Fragment other : placed) {
                double distance = Math.sqrt(fragment.center().distSqr(other.center()));
                if (distance < 0.75 * (fragment.half() + other.half())) return false;
            }
            return true;
        }

        private static int find(int[] parent, int i) {
            while (parent[i] != i) {
                parent[i] = parent[parent[i]];
                i = parent[i];
            }
            return i;
        }
    }

    static double horizontal(BlockPos a, BlockPos b) {
        return Math.hypot(a.getX() - b.getX(), a.getZ() - b.getZ());
    }

    static double horizontal(double x, double z, BlockPos b) {
        return Math.hypot(x - b.getX(), z - b.getZ());
    }
}
