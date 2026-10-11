package net.itsthesky.upfall.content.hamlet.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HamletLayout {
    private HamletLayout() {}

    public enum Kind { MARKET, HOME, WORKSHOP, LOOKOUT, GARDEN }

    public static final class Platform {
        public final Kind kind;
        public final BlockPos center;
        public final int radius;
        public final Direction front;
        public final List<BlockPos> openings = new ArrayList<>();
        public int bridgeMask;
        public int stallMask = 0b1111;

        Platform(Kind kind, BlockPos center, int radius, Direction front) {
            this.kind = kind;
            this.center = center;
            this.radius = radius;
            this.front = front;
        }
    }

    public record Bridge(int from, int to, Direction direction) {}

    public record Zipline(int from, int to, BlockPos anchorFrom, BlockPos anchorTo) {}

    public record Plan(List<Platform> platforms, List<Bridge> bridges, List<Zipline> ziplines) {}

    private static final Direction[] AXES = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    private static final int[][] DIAGONALS = {{1, -1}, {1, 1}, {-1, 1}, {-1, -1}};

    public static boolean inDeck(int dx, int dz, int radius) {
        return dx * dx + dz * dz <= radius * radius + radius;
    }

    public static Plan plan(RandomSource random, BlockPos center) {
        List<Platform> platforms = new ArrayList<>();
        List<Bridge> bridges = new ArrayList<>();
        List<Zipline> ziplines = new ArrayList<>();
        int hubRadius = 6 + random.nextInt(2);
        Platform hub = new Platform(Kind.MARKET, center, hubRadius, Direction.NORTH);
        platforms.add(hub);

        List<Direction> axes = new ArrayList<>(List.of(AXES));
        Collections.shuffle(axes, new java.util.Random(random.nextLong()));
        int bridged = 2 + random.nextInt(3);
        List<Kind> axisKinds = new ArrayList<>(List.of(Kind.WORKSHOP, Kind.HOME, Kind.HOME, Kind.HOME));
        for (int i = 0; i < bridged; i++) {
            Direction dir = axes.get(i);
            Kind kind = axisKinds.get(i);
            int radius = kind == Kind.WORKSHOP ? 5 : 4 + random.nextInt(2);
            int distance = hubRadius + 5 + random.nextInt(5) + radius;
            int offset = -random.nextInt(2);
            Direction perp = dir.getClockWise();
            BlockPos pos = center.relative(dir, distance).relative(perp.getAxis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH, offset)
                    .above(random.nextInt(5) - 2);
            Platform platform = new Platform(kind, pos, radius, dir.getOpposite());
            if (fits(platforms, platform)) {
                platforms.add(platform);
                bridges.add(new Bridge(0, platforms.size() - 1, dir));
                hub.bridgeMask |= 1 << dir.get2DDataValue();
            }
        }

        List<Integer> diagonals = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(diagonals, new java.util.Random(random.nextLong()));
        int zipped = random.nextInt(10) < 7 ? 2 : 1;
        for (int i = 0; i < zipped; i++) {
            int slot = diagonals.get(i);
            boolean lookout = i == 0;
            int radius = lookout ? 4 : 5;
            double distance = hubRadius + (lookout ? 9 : 8) + random.nextInt(5) + radius;
            int dx = (int) Math.round(DIAGONALS[slot][0] * distance / Math.sqrt(2.0));
            int dz = (int) Math.round(DIAGONALS[slot][1] * distance / Math.sqrt(2.0));
            int dy = lookout ? 9 + random.nextInt(5) : -5 - random.nextInt(3);
            Direction front = Direction.getNearest(-dx, 0, -dz);
            Platform platform = new Platform(lookout ? Kind.LOOKOUT : Kind.GARDEN, center.offset(dx, dy, dz), radius, front);
            if (fits(platforms, platform)) {
                platforms.add(platform);
                hub.stallMask &= ~(1 << slot);
                ziplines.add(zipline(platforms, 0, platforms.size() - 1));
            }
        }

        for (Bridge bridge : bridges) {
            addBridgeOpenings(platforms, bridge);
        }
        return new Plan(platforms, bridges, ziplines);
    }

    private static boolean fits(List<Platform> platforms, Platform candidate) {
        for (Platform other : platforms) {
            double min = other.radius + candidate.radius + 4;
            if (horizontalDistSqr(other.center, candidate.center) < min * min) {
                return false;
            }
        }
        return true;
    }

    static double horizontalDistSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    static int[] bridgeRows() {
        return new int[]{0, 1};
    }

    static BlockPos rimCell(Platform platform, Direction direction, int rowWorld) {
        boolean alongX = direction.getAxis() == Direction.Axis.X;
        int perp = rowWorld - (alongX ? platform.center.getZ() : platform.center.getX());
        int step = alongX ? direction.getStepX() : direction.getStepZ();
        int along = 0;
        while (inDeck(alongX ? along + step : perp, alongX ? perp : along + step, platform.radius)) {
            along += step;
        }
        return alongX ? new BlockPos(platform.center.getX() + along, platform.center.getY(), rowWorld)
                : new BlockPos(rowWorld, platform.center.getY(), platform.center.getZ() + along);
    }

    static int rowBase(Platform market, Direction direction) {
        return direction.getAxis() == Direction.Axis.X ? market.center.getZ() : market.center.getX();
    }

    private static void addBridgeOpenings(List<Platform> platforms, Bridge bridge) {
        Platform a = platforms.get(bridge.from());
        Platform b = platforms.get(bridge.to());
        int base = rowBase(a, bridge.direction());
        for (int row : bridgeRows()) {
            a.openings.add(rimCell(a, bridge.direction(), base + row));
            b.openings.add(rimCell(b, bridge.direction().getOpposite(), base + row));
        }
    }

    private static Zipline zipline(List<Platform> platforms, int from, int to) {
        Platform a = platforms.get(from);
        Platform b = platforms.get(to);
        double ux = b.center.getX() - a.center.getX();
        double uz = b.center.getZ() - a.center.getZ();
        double length = Math.sqrt(ux * ux + uz * uz);
        ux /= length;
        uz /= length;
        BlockPos postA = a.center.offset((int) Math.round(ux * (a.radius - 1)), 0, (int) Math.round(uz * (a.radius - 1)));
        BlockPos postB = b.center.offset((int) Math.round(-ux * (b.radius - 1)), 0, (int) Math.round(-uz * (b.radius - 1)));
        double span = Math.sqrt(horizontalDistSqr(postA, postB));
        double slope = Math.abs(a.center.getY() - b.center.getY()) / span + 0.1;
        int high = Mth.clamp((int) Math.ceil(2.6 + 2.8 * slope), 3, 7);
        boolean aHigher = a.center.getY() >= b.center.getY();
        BlockPos anchorA = postA.above(aHigher ? high : 3);
        BlockPos anchorB = postB.above(aHigher ? 3 : high);
        addRimOpenings(a, ux, uz);
        addRimOpenings(b, -ux, -uz);
        return new Zipline(from, to, anchorA, anchorB);
    }

    private static void addRimOpenings(Platform platform, double ux, double uz) {
        double t = 0.0;
        int rx = 0;
        int rz = 0;
        while (true) {
            int nx = (int) Math.round(ux * (t + 0.5));
            int nz = (int) Math.round(uz * (t + 0.5));
            if (!inDeck(nx, nz, platform.radius)) break;
            rx = nx;
            rz = nz;
            t += 0.5;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 2 && inDeck(rx + dx, rz + dz, platform.radius)) {
                    platform.openings.add(platform.center.offset(rx + dx, 0, rz + dz));
                }
            }
        }
    }
}
