package net.itsthesky.upfall.content.chronospire.worldgen;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.content.chronospire.entity.PlatformPath;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class ChronospireBlueprint {
    public static final int RADIUS = 26;
    public static final int MIN_Y = -32;
    public static final int MAX_Y = 90;
    static final double TURN_RISE = 16.0;
    static final double STAIR_BOTTOM = 30.0;
    static final double STAIR_TOP = 53.0;
    static final double STAIR_MAX_ANGLE = 360.0 * (STAIR_TOP - STAIR_BOTTOM) / TURN_RISE;

    static final double[][] CLEARED = {{13.5, 30, 57}, {11.0, 65, 90}};

    private final BlockPos origin;
    private final int rotation;
    private final long seed;
    private final Long2ObjectLinkedOpenHashMap<BlockState> blocks = new Long2ObjectLinkedOpenHashMap<>();
    private final List<BlockPos> caches = new ArrayList<>();
    private final Map<BlockPos, List<BoundingBox>> gates = new LinkedHashMap<>();
    private final List<PlatformPath> platforms = new ArrayList<>();

    private final BlockState bricks = ChronoBlocks.CHRONOSTONE_BRICKS.get().defaultBlockState();
    private final BlockState stone = ChronoBlocks.CHRONOSTONE.get().defaultBlockState();
    private final BlockState chiseled = ChronoBlocks.CHISELED_CHRONOSTONE.get().defaultBlockState();
    private final BlockState gilded = ChronoBlocks.GILDED_CHRONOSTONE.get().defaultBlockState();
    private final BlockState warded = ChronoBlocks.WARDED_CHRONOSTONE.get().defaultBlockState();
    private final BlockState glass = ChronoBlocks.CHRONOGLASS.get().defaultBlockState();
    private final BlockState lantern = ChronoBlocks.PHASE_LANTERN.get().defaultBlockState();
    private final BlockState seal = ChronoBlocks.CHRONO_SEAL.get().defaultBlockState();
    private final BlockState current = ChronoBlocks.CHRONO_CURRENT.get().defaultBlockState();
    private final BlockState pearl = ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
    private final BlockState pearlSlab = ModBlocks.PEARLSTONE_BRICK_VARIANTS.slab().get().defaultBlockState();
    private final BlockState duskslate = ModBlocks.DUSKSLATE.get().defaultBlockState();
    private final BlockState cloud = ModBlocks.DENSE_CLOUD.get().defaultBlockState();
    private final BlockState air = Blocks.AIR.defaultBlockState();

    private ChronospireBlueprint(BlockPos origin, int rotation, long seed) {
        this.origin = origin;
        this.rotation = Math.floorMod(rotation, 4);
        this.seed = seed;
    }

    public static ChronospireBlueprint build(BlockPos origin, int rotation, long seed) {
        ChronospireBlueprint plan = new ChronospireBlueprint(origin, rotation, seed);
        plan.root();
        plan.plaza();
        plan.tower();
        plan.balcony();
        plan.needle();
        plan.stairs();
        plan.gallery();
        plan.crown();
        plan.hourStones();
        plan.buildPlatforms();
        return plan;
    }

    public static BoundingBox bounds(BlockPos origin) {
        return new BoundingBox(origin.getX() - RADIUS, origin.getY() + MIN_Y, origin.getZ() - RADIUS,
                origin.getX() + RADIUS, origin.getY() + MAX_Y, origin.getZ() + RADIUS);
    }

    public Long2ObjectLinkedOpenHashMap<BlockState> blocks() {
        return blocks;
    }

    public List<BlockPos> caches() {
        return caches;
    }

    public Map<BlockPos, List<BoundingBox>> gates() {
        return gates;
    }

    public List<PlatformPath> platforms() {
        return platforms;
    }

    public BlockPos origin() {
        return origin;
    }

    public BlockPos at(int x, int y, int z) {
        return switch (rotation) {
            case 1 -> origin.offset(-z, y, x);
            case 2 -> origin.offset(-x, y, -z);
            case 3 -> origin.offset(z, y, -x);
            default -> origin.offset(x, y, z);
        };
    }

    public Vec3 at(double x, double y, double z) {
        double u = x - 0.5;
        double v = z - 0.5;
        double ru = switch (rotation) {
            case 1 -> -v;
            case 2 -> -u;
            case 3 -> v;
            default -> u;
        };
        double rv = switch (rotation) {
            case 1 -> u;
            case 2 -> -v;
            case 3 -> -u;
            default -> v;
        };
        return new Vec3(origin.getX() + ru + 0.5, origin.getY() + y, origin.getZ() + rv + 0.5);
    }

    public boolean inClearedVolume(BlockPos pos) {
        double dx = pos.getX() - origin.getX();
        double dz = pos.getZ() - origin.getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        int y = pos.getY() - origin.getY();
        for (double[] cylinder : CLEARED) {
            if (d <= cylinder[0] && y >= cylinder[1] && y <= cylinder[2]) return true;
        }
        return false;
    }

    private void set(int x, int y, int z, BlockState state) {
        blocks.put(at(x, y, z).asLong(), state);
    }

    private BlockState get(int x, int y, int z) {
        return blocks.get(at(x, y, z).asLong());
    }

    private static double dist(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    private static double angle(double x, double z) {
        double a = Math.toDegrees(Math.atan2(z, x));
        return a < 0 ? a + 360 : a;
    }

    private static double angleDiff(double a, double b) {
        double d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }

    private static boolean hourMark(double angle, double halfWidth) {
        return angleDiff(angle, Math.round(angle / 30.0) * 30.0) <= halfWidth;
    }

    private double hash(int x, int y, int z) {
        long h = seed ^ x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    private double noise(double a, double b, int salt) {
        int ia = Mth.floor(a);
        int ib = Mth.floor(b);
        double fa = a - ia;
        double fb = b - ib;
        fa = fa * fa * (3 - 2 * fa);
        fb = fb * fb * (3 - 2 * fb);
        double n00 = hash(ia, ib, salt);
        double n10 = hash(ia + 1, ib, salt);
        double n01 = hash(ia, ib + 1, salt);
        double n11 = hash(ia + 1, ib + 1, salt);
        return Mth.lerp(fb, Mth.lerp(fa, n00, n10), Mth.lerp(fa, n01, n11));
    }

    private BlockState rock(int x, int y, int z) {
        double n = noise(x * 0.25 + y * 0.18, z * 0.25 - y * 0.1, 7) * 0.7 + hash(x, y, z) * 0.3;
        if (n < 0.45) return stone;
        if (n < 0.72) return duskslate;
        return ModBlocks.PEARLSTONE.get().defaultBlockState();
    }

    private static BlockState phaseStone(ChronoPhase phase, boolean slab) {
        return ChronoBlocks.PHASE_STONE.get().defaultBlockState().setValue(PhaseTrackingBlock.PHASE, phase)
                .setValue(PhaseStoneBlock.SLAB, slab);
    }

    private BoundingBox relativeBox(BlockPos controller, int x0, int y0, int z0, int x1, int y1, int z1) {
        BoundingBox box = BoundingBox.fromCorners(at(x0, y0, z0), at(x1, y1, z1));
        return box.moved(-controller.getX(), -controller.getY(), -controller.getZ());
    }

    private void root() {
        for (int y = -24; y <= -1; y++) {
            double t = -y / 24.0;
            for (int x = -16; x <= 16; x++) {
                for (int z = -16; z <= 16; z++) {
                    double d = dist(x, z);
                    double r = 13.5 * Math.pow(1 - t, 0.85) + (noise(angle(x, z) / 30.0, y * 0.22, 3) - 0.5) * 3.2 * (1 - t * 0.4);
                    if (d > r) continue;
                    boolean shell = d > r - 1.7;
                    BlockState state = shell ? rock(x, y, z) : duskslate;
                    if (shell && y < -3 && hash(x, y, z + 99) < 0.04) {
                        state = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
                    }
                    set(x, y, z, state);
                }
            }
        }
        for (int y = -27; y <= -18; y++) {
            set(0, y, 0, gilded);
        }
        for (int x = -1; x <= 1; x++) {
            for (int y = -31; y <= -29; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (dist(x, z) + Math.abs(y + 30) <= 2) {
                        set(x, y, z, lantern);
                    }
                }
            }
        }
        set(0, -28, 0, gilded);
        set(0, -32, 0, gilded);
    }

    private void plaza() {
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                double d = dist(x, z);
                if (d > 13.5) continue;
                double a = angle(x, z);
                BlockState state;
                if (d > 12.5) state = bricks;
                else if (d > 10.5 && hourMark(a, 4.5)) state = gilded;
                else if (d > 9.5) state = chiseled;
                else if (d > 7.5) state = pearl;
                else if (Math.abs(x) <= 2 && Math.abs(z) <= 2) state = warded;
                else state = (x == 0 || z == 0) ? gilded : pearl;
                set(x, 0, z, state);
                if (d > 12.5) {
                    boolean landing = Math.abs(x) <= 1 || Math.abs(z) <= 1;
                    if (!landing) set(x, 1, z, pearlSlab);
                }
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(x, -1, z, warded);
            }
        }
        for (int[] p : new int[][]{{8, 8}, {-8, 8}, {8, -8}, {-8, -8}}) {
            set(p[0], 1, p[1], lantern);
        }
        set(2, 1, 9, ChronoBlocks.HOUR_DIAL.get().defaultBlockState());
        set(-2, 1, 9, ChronoBlocks.HOUR_DIAL.get().defaultBlockState());
    }

    private void tower() {
        for (int y = 1; y <= 37; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    double d = dist(x, z);
                    double a = angle(x, z);
                    if (d <= 7.5) {
                        towerCell(x, y, z, d, a);
                    } else if (d <= 8.5 && y <= 27 && angleDiff(a, Math.round(a / 45.0) * 45.0) <= 4.0) {
                        set(x, y, z, y % 9 == 0 ? gilded : bricks);
                    }
                }
            }
        }

        ChronoPhase[] glyphs = {ChronoPhase.DAWN, ChronoPhase.NOON, ChronoPhase.DUSK, ChronoPhase.MIDNIGHT};
        int[][] glyphPos = {{4, -4}, {4, 4}, {-4, 4}, {-4, -4}};
        for (int i = 0; i < 4; i++) {
            int gx = glyphPos[i][0];
            int gz = glyphPos[i][1];
            set(gx, 3, gz, ChronoBlocks.CHRONOGLYPH.get().defaultBlockState()
                    .setValue(ChronoglyphBlock.GLYPH, glyphs[i]));
            set(gx, 2, gz, chiseled);
            set(gx, 4, gz, chiseled);
        }
        BlockPos pedestal = at(0, 1, 4);
        set(0, 1, 4, ChronoBlocks.GLYPH_PEDESTAL.get().defaultBlockState());
        gates.put(pedestal, List.of(relativeBox(pedestal, -2, 1, -2, 2, 7, 2)));
        for (int[] p : new int[][]{{6, 0}, {-6, 0}, {0, -6}}) {
            set(p[0], 5, p[1], lantern);
        }

        for (int y = 1; y <= 7; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    set(x, y, z, Math.max(Math.abs(x), Math.abs(z)) == 2 ? seal : current);
                }
            }
        }
        for (int y = 8; y <= 31; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    set(x, y, z, current);
                }
            }
        }
        for (int y : new int[]{14, 23}) {
            for (int[] p : new int[][]{{6, 0}, {-6, 0}, {0, 6}, {0, -6}}) {
                set(p[0], y, p[1], lantern);
            }
        }

        for (int k = 0; k < 5; k++) {
            double a = Math.toRadians(90 + 72 * k);
            int wx = (int) Math.round(4 * Math.cos(a));
            int wz = (int) Math.round(4 * Math.sin(a));
            set(wx, 30, wz, ChronoBlocks.PHASE_WHEEL.get().defaultBlockState()
                    .setValue(PhaseTrackingBlock.PHASE, ChronoPhase.byIndex((int) (hash(k, 30, 5) * 4))));
        }
        if (allWheelsEqual()) {
            double a = Math.toRadians(90);
            set((int) Math.round(4 * Math.cos(a)), 30, (int) Math.round(4 * Math.sin(a)),
                    ChronoBlocks.PHASE_WHEEL.get().defaultBlockState().setValue(PhaseTrackingBlock.PHASE,
                            get(0, 30, 4).getValue(PhaseTrackingBlock.PHASE).next()));
        }
        BlockPos gnomon = at(0, 34, 0);
        set(0, 34, 0, ChronoBlocks.CHRONO_GNOMON.get().defaultBlockState());
        set(0, 35, 0, gilded);
        set(0, 36, 0, gilded);
        gates.put(gnomon, List.of(relativeBox(gnomon, -7, 30, -7, 7, 32, 7), relativeBox(gnomon, -3, 65, -3, 3, 67, 3)));
        for (int[] p : new int[][]{{4, 4}, {-4, 4}, {4, -4}, {-4, -4}}) {
            set(p[0], 35, p[1], lantern);
        }
    }

    private boolean allWheelsEqual() {
        ChronoPhase first = null;
        for (int k = 0; k < 5; k++) {
            double a = Math.toRadians(90 + 72 * k);
            BlockState state = get((int) Math.round(4 * Math.cos(a)), 30, (int) Math.round(4 * Math.sin(a)));
            ChronoPhase phase = state.getValue(PhaseTrackingBlock.PHASE);
            if (first != null && first != phase) return false;
            first = phase;
        }
        return true;
    }

    private void towerCell(int x, int y, int z, double d, double a) {
        boolean wall = d > 5.5;
        boolean outer = d > 6.5;
        boolean lift = Math.abs(x) <= 1 && Math.abs(z) <= 1;
        if (y == 8 || y == 29 || y == 37) {
            if (lift && y != 37) return;
            set(x, y, z, outer && y != 29 ? gilded : y == 29 && !wall && d > 2.5 && d <= 3.5 ? gilded : warded);
            return;
        }
        if (y == 9 && !wall) {
            if (!lift) set(x, y, z, cloud);
            return;
        }
        if (!wall) {
            if (!lift) set(x, y, z, air);
            return;
        }
        BlockState state = warded;
        if (outer && (y == 1 || y == 36)) state = chiseled;
        if (y >= 12 && y <= 25 && angleDiff(a, 22.5 + Math.round((a - 22.5) / 45.0) * 45.0) <= 5.0) state = glass;
        if (z > 0 && Math.abs(x) <= 1 && y <= 4) state = air;
        if (z > 0 && Math.abs(x) <= 1 && y == 5) state = outer ? gilded : warded;
        if (z > 0 && Math.abs(x) == 2 && y <= 5 && outer) state = chiseled;
        boolean door = (Math.abs(x) <= 1 || Math.abs(z) <= 1) && y >= 30 && y <= 32;
        if (door) state = seal;
        if ((Math.abs(x) <= 1 || Math.abs(z) <= 1) && y == 33) state = outer ? gilded : warded;
        set(x, y, z, state);
    }

    private void balcony() {
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                double d = dist(x, z);
                if (d <= 7.5 || d > 13.5) continue;
                set(x, 29, z, d > 12.5 ? bricks : d > 8.5 && d <= 11.5 ? cloud : bricks);
                if (d > 12.5) {
                    set(x, 30, z, hourMark(angle(x, z) - 22.5, 3.0) && Math.round((angle(x, z) - 22.5) / 30.0) % 3 == 0 ? lantern : pearlSlab);
                }
                if (d <= 11.5) set(x, 28, z, bricks);
                if (d <= 10.0) set(x, 27, z, bricks);
                if (d <= 9.0) set(x, 26, z, chiseled);
                if (d <= 8.2) set(x, 25, z, chiseled);
            }
        }
    }

    private void needle() {
        for (int y = 38; y <= 63; y++) {
            double r = y == 38 ? 6.5 : y == 39 ? 5.5 : 4.5;
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    double d = dist(x, z);
                    if (d > r) continue;
                    BlockState state;
                    if (d <= r - 1) {
                        state = stone;
                    } else if (y == 44 || y == 51 || y == 58) {
                        state = gilded;
                    } else if (y >= 40 && y <= 60 && y % 2 == 0 && angleDiff(angle(x, z), 45 + Math.round((angle(x, z) - 45) / 90.0) * 90.0) <= 8) {
                        state = lantern;
                    } else {
                        state = bricks;
                    }
                    set(x, y, z, state);
                }
            }
        }
    }

    private void stairs() {
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                double d = dist(x, z);
                if (d <= 8.5 || d > 11.5) continue;
                double a = angle(x, z);
                for (int arm = 0; arm < 4; arm++) {
                    double phi = ((a - (90 + 90 * arm)) % 360 + 360) % 360;
                    for (int turn = 0; turn < 2; turn++) {
                        double travelled = phi + 360 * turn;
                        if (travelled > STAIR_MAX_ANGLE) continue;
                        double surface = Math.floor((STAIR_BOTTOM + TURN_RISE * travelled / 360.0) * 2) / 2;
                        if (surface < STAIR_BOTTOM + 0.5) continue;
                        boolean slab = surface != Math.floor(surface);
                        int y = slab ? (int) Math.floor(surface) : (int) surface - 1;
                        set(x, y, z, phaseStone(ChronoPhase.byIndex(arm), slab));
                    }
                }
            }
        }
    }

    private void gallery() {
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                double d = dist(x, z);
                if (d <= 11.5 || d > 15.5) continue;
                double a = angle(x, z);
                set(x, 52, z, d > 14.5 ? bricks : hourMark(a, 3.0) ? gilded : pearl);
                if (d <= 15.0 && d > 12.0) set(x, 51, z, bricks);
                if (d <= 14.5 && d > 12.5) set(x, 50, z, chiseled);
                boolean dock = x >= 14 && Math.abs(z) <= 1;
                if (d > 14.5 && !dock) {
                    set(x, 53, z, angleDiff(a, 45 + Math.round((a - 45) / 90.0) * 90.0) <= 2.5 ? lantern : pearlSlab);
                }
            }
        }
        set(13, 53, 3, ChronoBlocks.HOUR_DIAL.get().defaultBlockState());
    }

    private void crown() {
        for (int x = -11; x <= 11; x++) {
            for (int z = -11; z <= 11; z++) {
                double d = dist(x, z);
                if (d > 10.5) continue;
                double a = angle(x, z);
                BlockState state;
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) state = warded;
                else if (d > 9.5) state = bricks;
                else if (d > 7.5 && hourMark(a, 5.0)) state = gilded;
                else if (d > 6.5) state = chiseled;
                else state = pearl;
                set(x, 64, z, state);
                if (d <= 9.5) set(x, 63, z, bricks);
                if (d <= 8.0) set(x, 62, z, bricks);
                if (d <= 6.5) set(x, 61, z, chiseled);
                boolean dock = x >= 9 && Math.abs(z) <= 1;
                if (d > 9.5 && !dock) {
                    set(x, 65, z, angleDiff(a, 45 + Math.round((a - 45) / 90.0) * 90.0) <= 3.0 ? lantern : pearlSlab);
                }
            }
        }

        for (int y = 65; y <= 70; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    boolean edge = Math.max(Math.abs(x), Math.abs(z)) == 3;
                    if (y == 70) {
                        set(x, y, z, edge ? gilded : warded);
                    } else if (edge) {
                        boolean entrance = (Math.abs(x) <= 1 || Math.abs(z) <= 1) && y <= 67;
                        set(x, y, z, entrance ? seal : warded);
                    } else {
                        set(x, y, z, air);
                    }
                }
            }
        }
        set(0, 70, 0, warded);
        set(0, 65, 0, ModBlocks.SKY_CACHE.get().defaultBlockState());
        caches.add(at(0, 65, 0));
        for (int[] p : new int[][]{{2, 2}, {-2, 2}, {2, -2}, {-2, -2}}) {
            set(p[0], 68, p[1], lantern);
        }

        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for (int p = 0; p < 4; p++) {
            for (int r = 1; r <= 9; r++) {
                set(dirs[p][0] * r, 71, dirs[p][1] * r, phaseStone(ChronoPhase.byIndex(p), false));
            }
        }
        set(0, 71, 0, gilded);

        for (int y = 72; y <= 80; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    set(x, y, z, y % 3 == 0 ? gilded : chiseled);
                }
            }
        }
        for (int y = 81; y <= 84; y++) {
            set(0, y, 0, gilded);
        }
        for (int x = -2; x <= 2; x++) {
            for (int y = 84; y <= 88; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.sqrt(x * x + (y - 86) * (y - 86) + z * z) <= 1.8) set(x, y, z, lantern);
                }
            }
        }
        set(0, 89, 0, gilded);
        set(0, 90, 0, gilded);
        for (int step = 0; step < 360; step += 2) {
            double a = Math.toRadians(step);
            int u = (int) Math.round(7 * Math.cos(a));
            int y = (int) Math.round(78 + 7 * Math.sin(a));
            if (y > 71 && y < 84) {
                set(u, y, 0, gilded);
                set(0, y, u, gilded);
            }
            set((int) Math.round(7 * Math.cos(a)), 78, (int) Math.round(7 * Math.sin(a)), gilded);
        }
    }

    private void hourStones() {
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45 + 90 * k);
            int cx = (int) Math.round(21 * Math.cos(a));
            int cz = (int) Math.round(21 * Math.sin(a));
            int cy = 36 + 4 * k;
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    double d = dist(dx, dz);
                    double r = 4.6 + (noise(angle(dx, dz) / 40.0, k * 3.0, 11) - 0.5) * 1.4;
                    if (d > r) continue;
                    int top = d <= r - 1.6 ? 0 : -1;
                    int depth = (int) Math.round(Math.pow((r - d) / r, 0.7) * 11 + 1 + hash(dx, k, dz) * 1.5);
                    for (int dy = -depth; dy <= top; dy++) {
                        BlockState state = dy == top ? (d <= 2.2 ? bricks : stone) : rock(cx + dx, cy + dy, cz + dz);
                        if (dy == -depth && depth > 2 && hash(dx, dy, dz + k) < 0.2) {
                            state = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
                        }
                        set(cx + dx, cy + dy, cz + dz, state);
                    }
                }
            }
            ChronoPhase phase = ChronoPhase.byIndex(k);
            for (int dy = 1; dy <= 3; dy++) {
                set(cx, cy + dy, cz, phaseStone(phase, false));
            }
            set(cx, cy + 4, cz, gilded);
            set(cx, cy + 5, cz, lantern);
            for (int[] p : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                set(cx + p[0], cy + 1, cz + p[1], phaseStone(phase, true));
            }
        }
    }

    private void buildPlatforms() {
        platforms.add(new PlatformPath(List.of(at(0.5, 1.0, 0.5), at(0.5, 29.5, 0.5)), 0.12, 80,
                (int) (hash(1, 2, 3) * 400)));
        platforms.add(new PlatformPath(List.of(at(17.5, 52.5, 0.5), at(17.5, 64.5, 0.5), at(12.5, 64.5, 0.5)), 0.1, 80,
                (int) (hash(4, 5, 6) * 400)));
        for (int x = 16; x <= 18; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 53; y <= 66; y++) {
                    set(x, y, z, current);
                }
            }
        }
        for (int x = 11; x <= 15; x++) {
            for (int z = -1; z <= 1; z++) {
                set(x, 65, z, current);
                set(x, 66, z, current);
            }
        }
    }

    public List<BlockPos> find(Predicate<BlockState> predicate) {
        List<BlockPos> found = new ArrayList<>();
        for (Long2ObjectMap.Entry<BlockState> entry : blocks.long2ObjectEntrySet()) {
            if (predicate.test(entry.getValue())) {
                found.add(BlockPos.of(entry.getLongKey()));
            }
        }
        return found;
    }
}
