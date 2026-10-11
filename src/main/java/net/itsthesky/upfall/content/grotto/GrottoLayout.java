package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public final class GrottoLayout {
    public static final int CENTER_Y = 29;
    public static final int RADIUS = 26;
    private static final int TOP = 15;
    private static final int BOTTOM = 34;
    private static final double INNER = 0.68;
    private static final double MID = 0.77;
    private static final double OUTER = 0.86;
    private static final double SHAFT = 3.5;
    private static final double SHAFT_LINING = 4.7;
    private static final int NECK_BOTTOM = CENTER_Y + 6;
    private static final int MIST_ABOVE_TOP = 8;
    private static final double REACH = 1.4;
    private static final double SHELL = 0.07;
    private static final double SHELL_PUFF = 0.08;
    private static final double SHELL_MIST = 0.05;
    private static final int SHELL_TOP = 46;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState ROCK = GrottoBlocks.LUMEN_ROCK.get().defaultBlockState();
    private static final BlockState MOONSHELL = GrottoBlocks.MOONSHELL.get().defaultBlockState();
    private static final BlockState DRUSE = GrottoBlocks.LUMEN_DRUSE.get().defaultBlockState();
    private static final BlockState MOSS = GrottoBlocks.LUMEN_MOSS.get().defaultBlockState();
    private static final BlockState POOL = GrottoBlocks.PRISM_POOL.get().defaultBlockState();
    private static final BlockState SPROUTS = GrottoBlocks.LUMEN_SPROUTS.get().defaultBlockState();
    private static final BlockState VENT = ModBlocks.UPDRAFT_VENT.get().defaultBlockState();
    private static final BlockState MIST = ModBlocks.UPDRAFT_MIST.get().defaultBlockState();
    private static final BlockState CACHE = ModBlocks.SKY_CACHE.get().defaultBlockState();
    private static final BlockState CLOUD = ModBlocks.DENSE_CLOUD.get().defaultBlockState();
    private static final BlockState CLOUD_MIST = ModBlocks.MIST.get().defaultBlockState();

    private record Spire(int x, int z, int baseY, int dir, int length, double radius, BlockState block, BlockState tip) {
        BlockState at(int px, int py, int pz) {
            int along = (py - baseY) * dir;
            if (along < 0 || along > length) return null;
            int dx = px - x;
            int dz = pz - z;
            if (along == length) return dx == 0 && dz == 0 ? tip : null;
            double r = radius * Math.pow(1.0 - along / (double) length, 0.9) + 0.4;
            return dx * dx + dz * dz <= r * r ? block : null;
        }
    }

    private final long seed;
    private final int cx;
    private final int cz;
    private final int topY;
    private final int floorCenter;
    private final double stairAngle;
    private final List<Spire> spires = new ArrayList<>();
    private final List<BlockPos> caches = new ArrayList<>();
    private final int minY;
    private final int maxY;

    public GrottoLayout(long seed, int cx, int cz, int topY) {
        this.seed = seed;
        this.cx = cx;
        this.cz = cz;
        this.topY = topY;
        this.floorCenter = floorAt(cx, cz);
        this.stairAngle = hash(91, 0, 0, 0) * Math.PI * 2;

        RandomSource random = RandomSource.create(seed ^ 0x5DEECE66DL);
        double heartAngle = random.nextDouble() * Math.PI * 2;
        double heartDistance = 9.0 + random.nextDouble() * 3.0;
        int hx = cx + (int) Math.round(Math.cos(heartAngle) * heartDistance);
        int hz = cz + (int) Math.round(Math.sin(heartAngle) * heartDistance);
        addHeart(random, hx, hz);
        for (int side : new int[]{-1, 1}) {
            double angle = heartAngle + side * 0.62;
            int px = cx + (int) Math.round(Math.cos(angle) * 12.5);
            int pz = cz + (int) Math.round(Math.sin(angle) * 12.5);
            caches.add(new BlockPos(px, floorAt(px, pz) + 1, pz));
        }

        for (int i = 0; i < 30; i++) {
            int[] p = cavityPoint(random);
            if (tooClose(p[0], p[1], hx, hz, 7) || nearCache(p[0], p[1], 3)) continue;
            int fy = floorAt(p[0], p[1]);
            if (f(p[0], fy + 2, p[1]) > INNER - 0.05) continue;
            addSpire(p[0], p[1], fy, 1, 2 + random.nextInt(6), 0.6 + random.nextDouble() * 0.6, random.nextBoolean());
        }
        for (int i = 0; i < 28; i++) {
            int[] p = cavityPoint(random);
            int ceiling = ceilingAt(p[0], p[1]);
            if (ceiling < 0) continue;
            addSpire(p[0], p[1], ceiling, -1, 3 + random.nextInt(7), 0.5 + random.nextDouble() * 0.5, random.nextBoolean());
        }
        for (int i = 0; i < 3; i++) {
            double angle = random.nextDouble() * 2.0 + i * 2.09;
            addSpire(cx + (int) Math.round(Math.cos(angle) * 5.4), cz + (int) Math.round(Math.sin(angle) * 5.4), topY - 3, 1,
                    7 + random.nextInt(5), 1.5, true);
        }
        int tipY = tipY();
        addSpire(cx, cz, tipY, -1, 8, 1.7, true);

        int lo = Math.min(tipY - 9, CENTER_Y - BOTTOM - 18);
        this.minY = lo;
        this.maxY = topY + MIST_ABOVE_TOP + 2;
    }

    public BoundingBox bounds() {
        int reach = (int) Math.ceil(RADIUS * REACH) + 2;
        return new BoundingBox(cx - reach, minY, cz - reach, cx + reach, maxY, cz + reach);
    }

    public List<BlockPos> caches() {
        return caches;
    }

    public BlockPos vent() {
        return new BlockPos(cx, floorCenter + 1, cz);
    }

    public int topY() {
        return topY;
    }

    double f(int x, int y, int z) {
        double dx = x - cx;
        double dy = y - CENTER_Y;
        double dz = z - cz;
        double h = (dx * dx + dz * dz) / ((double) RADIUS * RADIUS);
        double v = dy >= 0 ? dy / TOP : Math.pow(-dy / BOTTOM, 0.8);
        double wobble = (noise3(x * 0.09, y * 0.09, z * 0.09, 1) - 0.5) * 0.17 + (noise3(x * 0.23, y * 0.23, z * 0.23, 2) - 0.5) * 0.08;
        return Math.sqrt(h + v * v) / (1.0 + wobble);
    }

    int floorAt(int x, int z) {
        return CENTER_Y - 5 + (int) Math.round((noise2(x * 0.07, z * 0.07, 3) - 0.5) * 3.2);
    }

    private int ceilingAt(int x, int z) {
        for (int y = CENTER_Y + TOP; y > floorAt(x, z) + 3; y--) {
            if (f(x, y, z) <= INNER) return y + 1;
        }
        return -1;
    }

    private int tipY() {
        for (int y = CENTER_Y - BOTTOM - 8; y < CENTER_Y; y++) {
            if (f(cx, y, cz) < 1.0) return y;
        }
        return CENTER_Y - BOTTOM;
    }

    private int[] cavityPoint(RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 6.0 + Math.sqrt(random.nextDouble()) * 11.0;
        return new int[]{cx + (int) Math.round(Math.cos(angle) * radius), cz + (int) Math.round(Math.sin(angle) * radius)};
    }

    private static boolean tooClose(int x, int z, int ox, int oz, int distance) {
        return (x - ox) * (x - ox) + (z - oz) * (z - oz) < distance * distance;
    }

    private boolean nearCache(int x, int z, int distance) {
        for (BlockPos cache : caches) {
            if (tooClose(x, z, cache.getX(), cache.getZ(), distance)) return true;
        }
        return false;
    }

    private void addHeart(RandomSource random, int hx, int hz) {
        int base = floorAt(hx, hz);
        addSpire(hx, hz, base, 1, 9 + random.nextInt(4), 2.1, true);
        for (int i = 0; i < 5; i++) {
            double angle = i * 1.256 + random.nextDouble() * 0.5;
            double distance = 3.3 + random.nextDouble() * 1.5;
            int sx = hx + (int) Math.round(Math.cos(angle) * distance);
            int sz = hz + (int) Math.round(Math.sin(angle) * distance);
            addSpire(sx, sz, floorAt(sx, sz), 1, 3 + random.nextInt(5), 0.7 + random.nextDouble() * 0.4, false);
        }
    }

    private void addSpire(int x, int z, int baseY, int dir, int length, double radius, boolean bigTip) {
        GrottoBlocks.CrystalSet set = crystalSet(x, baseY + dir * length / 2, z);
        Direction facing = dir > 0 ? Direction.UP : Direction.DOWN;
        BlockState tip = (bigTip ? set.cluster() : set.large()).get().defaultBlockState().setValue(AmethystClusterBlock.FACING, facing);
        spires.add(new Spire(x, z, baseY, dir, length, radius, set.block().get().defaultBlockState(), tip));
    }

    private GrottoBlocks.CrystalSet crystalSet(int x, int y, int z) {
        double n = noise3(x * 0.05, y * 0.05, z * 0.05, 7);
        return n < 0.49 ? GrottoBlocks.AZURE : n < 0.57 ? GrottoBlocks.ROSE : GrottoBlocks.VERDANT;
    }

    public BlockState stateAt(int x, int y, int z) {
        if (y < minY || y > maxY) return null;
        int dx = x - cx;
        int dz = z - cz;
        int reach = (int) Math.ceil(RADIUS * REACH) + 1;
        if (Math.abs(dx) > reach || Math.abs(dz) > reach) return null;

        for (Spire spire : spires) {
            BlockState state = spire.at(x, y, z);
            if (state != null) return state;
        }
        for (BlockPos cache : caches) {
            if (cache.getX() == x && cache.getY() == y && cache.getZ() == z) return CACHE;
        }
        double r = Math.sqrt((double) dx * dx + (double) dz * dz);
        if (dx == 0 && dz == 0) {
            if (y == floorCenter + 1) return VENT;
            if (y > floorCenter + 1 && y <= topY + MIST_ABOVE_TOP) return MIST;
        }
        if (r <= SHAFT + 0.6) {
            BlockState step = stairAt(x, y, z);
            if (step != null) return step;
        }

        BlockState base = baseAt(x, y, z, r);
        if (base != null) {
            if (base.isAir() && r > SHAFT_LINING + 0.2) return decorate(x, y, z, base);
            return base;
        }
        return shellAt(x, y, z);
    }

    private BlockState stairAt(int x, int y, int z) {
        int n = y - (floorCenter + 1);
        if (n < 0 || y > topY - 2) return null;
        double angle = stairAngle + n * (Math.PI * 2 / 12.0);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        for (double radius : new double[]{3.2, 2.3}) {
            if (cx + (int) Math.round(cos * radius) == x && cz + (int) Math.round(sin * radius) == z) {
                return n % 4 == 3 ? DRUSE : MOONSHELL;
            }
        }
        return null;
    }

    private BlockState baseAt(int x, int y, int z, double r) {
        if (y >= NECK_BOTTOM && y <= topY + 8 && r <= 8.5) {
            BlockState neck = neckAt(x, y, z, r);
            if (neck != null) return neck;
        }
        double f = f(x, y, z);
        if (f >= 1.0) return null;
        int floor = floorAt(x, z);
        if (f <= INNER) {
            if (y > floor) return AIR;
            if (y == floor) return floorSurface(x, z);
            if (y == floor - 1 && isPool(x, z)) return DRUSE;
            return hash(11, x, y, z) < 0.035 ? crystalSet(x, y, z).block().get().defaultBlockState() : ROCK;
        }
        if (f > OUTER) return hash(12, x, y, z) < 0.05 ? crystalSet(x, y, z).block().get().defaultBlockState() : ROCK;
        if (f > MID) return hash(13, x, y, z) < 0.08 ? DRUSE : MOONSHELL;
        return DRUSE;
    }

    private BlockState floorSurface(int x, int z) {
        if (isPool(x, z)) return POOL;
        double patch = noise2(x * 0.16, z * 0.16, 5);
        if (patch > 0.68) return MOONSHELL;
        if (patch < 0.2) return DRUSE;
        return MOSS;
    }

    private boolean isPool(int x, int z) {
        if (tooClose(x, z, cx, cz, 7) || noise2(x * 0.09, z * 0.09, 4) < 0.6) return false;
        for (BlockPos cache : caches) {
            if (tooClose(x, z, cache.getX(), cache.getZ(), 4)) return false;
        }
        for (Spire spire : spires) {
            if (spire.dir > 0 && tooClose(x, z, spire.x, spire.z, 3)) return false;
        }
        return f(x, floorAt(x, z) + 1, z) <= INNER - 0.06;
    }

    private BlockState neckAt(int x, int y, int z, double r) {
        if (y >= topY) {
            int top = topY + crownHeight(x, z);
            if (r <= SHAFT) return y <= topY + 4 ? AIR : null;
            if (r > 6.4 || y >= top) return null;
            return crystalSet(x, topY, z).block().get().defaultBlockState();
        }
        if (r <= SHAFT) return AIR;
        if (r <= SHAFT_LINING) return DRUSE;
        double outer = 6.0 + (noise3(x * 0.2, y * 0.2, z * 0.2, 6) - 0.5) * 2.0;
        if (r > outer) return null;
        return r < 5.6 ? MOONSHELL : ROCK;
    }

    private int crownHeight(int x, int z) {
        int bucket = (int) Math.floor((Math.atan2(z - cz, x - cx) + Math.PI) * 3.0);
        return 1 + (int) (hash(21, bucket, 0, 0) * 3.5);
    }

    private BlockState decorate(int x, int y, int z, BlockState air) {
        double roll = hash(31, x, y, z);
        if (roll > 0.09) return air;
        BlockPos pos = new BlockPos(x, y, z);
        int offset = (int) (hash(32, x, y, z) * 6);
        for (int i = 0; i < 6; i++) {
            Direction toSupport = Direction.values()[(i + offset) % 6];
            if (!solidBase(pos.relative(toSupport))) continue;
            if (toSupport == Direction.DOWN && roll < 0.03 && baseIs(pos.below(), MOSS)) return SPROUTS;
            if (roll < 0.05) return cluster(x, y, z, toSupport.getOpposite());
            return air;
        }
        return air;
    }

    private BlockState shellAt(int x, int y, int z) {
        if (y > SHELL_TOP) return null;
        double f = f(x, y, z);
        if (f < 1.0) return null;
        double cloud = 1.0 + SHELL + noise3(x * 0.11, y * 0.11, z * 0.11, 8) * SHELL_PUFF;
        if (f <= cloud) return CLOUD;
        return f <= cloud + SHELL_MIST ? CLOUD_MIST : null;
    }

    private BlockState cluster(int x, int y, int z, Direction facing) {
        GrottoBlocks.CrystalSet set = crystalSet(x, y, z);
        double size = hash(33, x, y, z);
        var block = size < 0.4 ? set.small() : size < 0.7 ? set.medium() : size < 0.92 ? set.large() : set.cluster();
        return block.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, facing);
    }

    private boolean solidBase(BlockPos pos) {
        double dx = pos.getX() - cx;
        double dz = pos.getZ() - cz;
        BlockState state = baseAt(pos.getX(), pos.getY(), pos.getZ(), Math.sqrt(dx * dx + dz * dz));
        return state != null && !state.isAir() && !state.is(POOL.getBlock());
    }

    private boolean baseIs(BlockPos pos, BlockState expected) {
        double dx = pos.getX() - cx;
        double dz = pos.getZ() - cz;
        return baseAt(pos.getX(), pos.getY(), pos.getZ(), Math.sqrt(dx * dx + dz * dz)) == expected;
    }

    private static long mix(long h) {
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private double hash(int salt, int x, int y, int z) {
        long h = mix(seed + salt * 0x9E3779B97F4A7C15L);
        h = mix(h ^ (x * 0x632BE59BD9B4E019L));
        h = mix(h ^ (y * 0x8CB92BA72F3D8DD7L));
        h = mix(h ^ (z * 0xD1B54A32D192ED03L));
        return (h >>> 11) * 0x1.0p-53;
    }

    private double noise3(double x, double y, double z, int salt) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        double fx = smooth(x - x0);
        double fy = smooth(y - y0);
        double fz = smooth(z - z0);
        double c00 = Mth.lerp(fx, hash(salt, x0, y0, z0), hash(salt, x0 + 1, y0, z0));
        double c10 = Mth.lerp(fx, hash(salt, x0, y0 + 1, z0), hash(salt, x0 + 1, y0 + 1, z0));
        double c01 = Mth.lerp(fx, hash(salt, x0, y0, z0 + 1), hash(salt, x0 + 1, y0, z0 + 1));
        double c11 = Mth.lerp(fx, hash(salt, x0, y0 + 1, z0 + 1), hash(salt, x0 + 1, y0 + 1, z0 + 1));
        return Mth.lerp(fz, Mth.lerp(fy, c00, c10), Mth.lerp(fy, c01, c11));
    }

    private double noise2(double x, double z, int salt) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double fx = smooth(x - x0);
        double fz = smooth(z - z0);
        return Mth.lerp(fz, Mth.lerp(fx, hash(salt, x0, 0, z0), hash(salt, x0 + 1, 0, z0)),
                Mth.lerp(fx, hash(salt, x0, 0, z0 + 1), hash(salt, x0 + 1, 0, z0 + 1)));
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }
}
