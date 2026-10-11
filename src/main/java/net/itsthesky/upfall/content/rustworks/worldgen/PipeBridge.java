package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PipeBridge(int x, int z, Direction dir, int a, int b, int ha, int hb, int radius, int sag, boolean hollow,
                         int breakStart, int breakEnd, int valveAt, int valveSide) {
    public static final int EMBED = 3;
    public static final int MIN_GAP = 8;
    public static final int MAX_GAP = 80;
    public static final int MAX_DY = 12;
    public static final int EDGE_SEARCH = 36;
    public static final int LANDING = 5;
    public static final int SEGMENT = 16;
    public static final int RING_STEP = 6;
    private static final int VOID_DROP = 8;
    private static final int LANDING_DROP = 6;

    public interface Heights {
        int at(int x, int z);
    }

    public interface Biomes {
        boolean valid(int x, int y, int z);
    }

    public interface Sink {
        boolean isOpen(BlockPos pos);

        void set(BlockPos pos, BlockState state);
    }

    public static Optional<PipeBridge> search(RandomSource random, int x, int z, Heights heights, Biomes biomes) {
        int radius = random.nextInt(3) == 0 ? 3 : 2;
        List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Util.shuffle(dirs, random);
        long seed = random.nextLong();
        for (Direction dir : dirs) {
            Optional<PipeBridge> bridge = scan(RandomSource.create(seed ^ dir.ordinal()), x, z, dir, radius, heights, biomes);
            if (bridge.isPresent()) return bridge;
        }
        return Optional.empty();
    }

    private static Optional<PipeBridge> scan(RandomSource random, int x, int z, Direction dir, int radius, Heights heights, Biomes biomes) {
        int sx = dir.getStepX();
        int sz = dir.getStepZ();
        int last = 0;
        int hl = heights.at(x, z);
        if (hl < RustFeature.MIN_LAND) return Optional.empty();
        int t = 1;
        for (; t <= EDGE_SEARCH; t++) {
            int h = heights.at(x + sx * t, z + sz * t);
            if (h < hl - VOID_DROP) break;
            last = t;
            hl = h;
        }
        if (t > EDGE_SEARCH) return Optional.empty();
        int a = last;
        int ha = hl;
        for (int k = 1; k <= LANDING; k++) {
            if (heights.at(x + sx * (a - k), z + sz * (a - k)) < ha - LANDING_DROP) return Optional.empty();
        }
        int b = -1;
        for (t = a + 1; t - a - 1 <= MAX_GAP; t++) {
            if (heights.at(x + sx * t, z + sz * t) >= ha - MAX_DY) {
                b = t;
                break;
            }
        }
        if (b < 0 || b - a - 1 < MIN_GAP) return Optional.empty();
        int hb = heights.at(x + sx * b, z + sz * b);
        if (hb > ha + MAX_DY) return Optional.empty();
        for (int k = 1; k <= LANDING; k++) {
            if (heights.at(x + sx * (b + k), z + sz * (b + k)) < hb - LANDING_DROP) return Optional.empty();
        }
        PipeBridge bridge = create(random, x, z, dir, a, b, ha, hb, radius);
        for (t = a + 1; t < b; t++) {
            if (heights.at(x + sx * t, z + sz * t) > bridge.deck(t) - 2 * radius - 2) return Optional.empty();
        }
        if (!biomes.valid(x + sx * a, ha, z + sz * a) || !biomes.valid(x + sx * b, hb, z + sz * b)) return Optional.empty();
        return Optional.of(bridge);
    }

    public static PipeBridge create(RandomSource random, int x, int z, Direction dir, int a, int b, int ha, int hb, int radius) {
        int gap = b - a - 1;
        int sag = gap >= 40 ? 1 + random.nextInt(3) : gap >= 14 ? random.nextInt(3) : random.nextInt(2);
        boolean hollow = random.nextFloat() < 0.6F;
        int breakStart = 0;
        int breakEnd = 0;
        if (gap >= 14 && random.nextInt(4) == 0) {
            breakStart = a + 1 + gap / 3 + random.nextInt(Math.max(1, gap / 3 - 2));
            breakEnd = breakStart + 2 + random.nextInt(3);
        }
        int valveAt = Integer.MIN_VALUE;
        if (random.nextBoolean()) {
            valveAt = a + 2 + random.nextInt(Math.max(1, gap - 2));
            if ((valveAt - a) % RING_STEP == 0) valveAt++;
            if (breakEnd > breakStart && valveAt >= breakStart - 2 && valveAt <= breakEnd + 1) valveAt = Integer.MIN_VALUE;
        }
        int valveSide = random.nextBoolean() ? 1 : -1;
        return new PipeBridge(x, z, dir, a, b, ha, hb, radius, sag, hollow, breakStart, breakEnd, valveAt, valveSide);
    }

    public int first() {
        return a - EMBED;
    }

    public int last() {
        return b + EMBED;
    }

    public boolean broken() {
        return breakEnd > breakStart;
    }

    public boolean missing(int t) {
        return t >= breakStart && t < breakEnd;
    }

    public int deck(int t) {
        int deck;
        if (t <= a) {
            deck = ha;
        } else if (t >= b) {
            deck = hb;
        } else {
            double f = (t - a) / (double) (b - a);
            deck = (int) Math.round(Mth.lerp(f, ha, hb) - sag * Math.sin(Math.PI * f));
        }
        if (broken()) {
            if (t == breakStart - 1 || t == breakEnd) {
                deck -= 2;
            } else if (t == breakStart - 2 || t == breakEnd + 1) {
                deck -= 1;
            }
        }
        return deck;
    }

    public BlockPos at(int t, int u, int y) {
        Direction side = dir.getClockWise();
        return new BlockPos(x + dir.getStepX() * t + side.getStepX() * u, y, z + dir.getStepZ() * t + side.getStepZ() * u);
    }

    public BoundingBox bounds() {
        return bounds(first(), last());
    }

    public BoundingBox bounds(int from, int to) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int t = from; t <= to; t++) {
            int deck = deck(t);
            minY = Math.min(minY, deck - 2 * radius - 1);
            maxY = Math.max(maxY, deck);
        }
        return BoundingBox.fromCorners(at(from, -radius - 2, minY), at(to, radius + 2, maxY));
    }

    public void place(int t, Sink sink) {
        if (missing(t)) return;
        boolean inGap = t > a && t < b;
        int center = deck(t) - radius;
        boolean ring = inGap && (t - a) % RING_STEP == 0;
        double inner = (radius - 0.5) * (radius - 0.5);
        double outer = (radius + 0.5) * (radius + 0.5);
        double flange = (radius + 1.3) * (radius + 1.3);
        BlockState plating = RustFeature.plating();
        for (int u = -radius - 1; u <= radius + 1; u++) {
            for (int v = -radius - 1; v <= radius + 1; v++) {
                int d2 = u * u + v * v;
                BlockPos pos = at(t, u, center + v);
                if (d2 <= inner) {
                    if (inGap) {
                        sink.set(pos, hollow ? Blocks.AIR.defaultBlockState() : plating);
                    } else if (sink.isOpen(pos)) {
                        sink.set(pos, plating);
                    }
                } else if (d2 <= outer) {
                    sink.set(pos, plating);
                } else if (ring && d2 <= flange && v < radius && sink.isOpen(pos)) {
                    sink.set(pos, (u + v) % 2 == 0 ? RustworksBlocks.RUSTSLATE.get().defaultBlockState() : plating);
                }
            }
        }
        if (t == valveAt) {
            BlockPos pos = at(t, valveSide * (radius + 1), center);
            if (sink.isOpen(pos)) {
                Direction side = dir.getClockWise();
                sink.set(pos, RustFeature.valve(valveSide > 0 ? side : side.getOpposite()));
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", x);
        tag.putInt("Z", z);
        tag.putInt("Dir", dir.get2DDataValue());
        tag.putInt("A", a);
        tag.putInt("B", b);
        tag.putInt("HA", ha);
        tag.putInt("HB", hb);
        tag.putInt("Radius", radius);
        tag.putInt("Sag", sag);
        tag.putBoolean("Hollow", hollow);
        tag.putInt("BreakStart", breakStart);
        tag.putInt("BreakEnd", breakEnd);
        tag.putInt("ValveAt", valveAt);
        tag.putInt("ValveSide", valveSide);
        return tag;
    }

    public static PipeBridge load(CompoundTag tag) {
        return new PipeBridge(tag.getInt("X"), tag.getInt("Z"), Direction.from2DDataValue(tag.getInt("Dir")), tag.getInt("A"), tag.getInt("B"),
                tag.getInt("HA"), tag.getInt("HB"), tag.getInt("Radius"), tag.getInt("Sag"), tag.getBoolean("Hollow"),
                tag.getInt("BreakStart"), tag.getInt("BreakEnd"), tag.getInt("ValveAt"), tag.getInt("ValveSide"));
    }
}
