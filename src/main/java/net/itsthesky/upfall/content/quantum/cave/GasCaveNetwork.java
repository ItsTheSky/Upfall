package net.itsthesky.upfall.content.quantum.cave;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumLoot;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlock;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class GasCaveNetwork {
    public static final GasCaveNetwork EMPTY = new GasCaveNetwork();

    public enum Entrance { CLIFF, CHIMNEY, SPIRAL }

    public enum RoomType { PLAIN, CAMP, LUMEN, QUANTUM, KAIRITE }

    public record Settings(int minY, int maxY, int gridSize, int rooms, double roomRadius, int shell, double loops,
                           double sinuosity, double corridorRadius, double fissureChance, double entranceRadius, double markerChance,
                           double clusterChance, double oreChance) {}

    public record Stats(int islandBlocks, int carved, int roomBlocks, int corridorBlocks, int entranceBlocks, int linkBlocks, int rooms,
                        Map<RoomType, Integer> roomTypes, double accessible, int closedPockets, int openBlocks, int breaches,
                        Map<Entrance, Integer> openByEntrance) {}

    public record Prop(long pos, BlockState state, ResourceKey<LootTable> loot, boolean solidOnly, long oreSeed, Predicate<BlockState> host) {
        public boolean isOre() {
            return state == null;
        }
    }

    private final Long2ObjectMap<LongList> gas = new Long2ObjectOpenHashMap<>();
    private final LongSet gasSet = new LongOpenHashSet();
    private final LongSet entranceSet = new LongOpenHashSet();
    private final Long2ObjectMap<List<Prop>> props = new Long2ObjectOpenHashMap<>();
    private Stats stats;

    private GasCaveNetwork() {}

    public boolean isEmpty() {
        return gasSet.isEmpty();
    }

    public LongList gasIn(ChunkPos chunk) {
        LongList list = gas.get(McChunks.key(chunk));
        return list == null ? LongList.of() : list;
    }

    public List<Prop> propsIn(ChunkPos chunk) {
        List<Prop> list = props.get(McChunks.key(chunk));
        return list == null ? List.of() : list;
    }

    public boolean isGas(BlockPos pos) {
        return gasSet.contains(pos.asLong());
    }

    public boolean isGas(long pos) {
        return gasSet.contains(pos);
    }

    public boolean isEntrance(long pos) {
        return entranceSet.contains(pos);
    }

    public int gasCount() {
        return gasSet.size();
    }

    public LongSet gasBlocks() {
        return gasSet;
    }

    public LongSet entranceBlocks() {
        return entranceSet;
    }

    public Map<Long, List<Prop>> allProps() {
        return props;
    }

    public Stats stats() {
        return stats;
    }

    public static GasCaveNetwork build(ChunkPos source, RandomSource random, NoiseRouter router, Settings settings) {
        Builder builder = new Builder(new GasCaveNetwork(), source, random, router, settings);
        if (!builder.start()) return EMPTY;
        return builder.network.isEmpty() ? EMPTY : builder.network;
    }

    private static final class Builder {
        private static final byte AIR = 0;
        private static final byte ISLAND = 1;
        private static final byte FOREIGN = 2;
        private static final byte SOLID = 3;

        private static final byte NONE = 0;
        private static final byte ROOM = 1;
        private static final byte LINK = 2;
        private static final byte ENTRANCE = 3;
        private static final byte CORRIDOR = 4;

        private static final int MAX_HEIGHT = 128;
        private static final int MIN_RUN = 14;
        private static final int MIN_ISLAND = 2000;
        private static final int MIN_POCKET = 8;
        private static final int CELL_WIDTH = 4;
        private static final int CELL_HEIGHT = 8;
        private static final int[][] SIDES = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        private static final Direction[] SIDE_DIRS = {Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        private static final int[] SECTOR = {1, 3, 4};
        private static final double FISSURE_WIDTH = 1.05;
        private static final double FISSURE_HEIGHT = 2.4;
        private static final double CAMP_CHANCE = 0.6;
        private static final double QUANTUM_CHANCE = 0.5;
        private static final double KAIRITE_CHANCE = 0.7;
        private static final double LUMEN_CHANCE = 0.6;
        private static final double QUANTUM_CLUSTER_CHANCE = 0.08;
        private static final Direction[] HORIZONTAL = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};

        final GasCaveNetwork network;
        final ChunkPos source;
        final RandomSource random;
        final DensityFunction density;
        final DensityFunction rustworks;
        final Settings settings;
        final ImprovedNoise noise;

        int size;
        int height;
        int ox;
        int oy;
        int oz;
        byte[] kind;
        byte[] depth;
        byte[] carve;
        byte[] entranceOf;
        int[] scratch;
        byte[] roomOf;
        boolean[] reserved;
        boolean[] used;
        int[] island;
        int islandCount;
        int[] top;
        int[] bottom;
        double[] rn;
        double centerX;
        double centerZ;
        boolean hasEdge;
        final List<Room> rooms = new ArrayList<>();
        final List<Shape> entrances = new ArrayList<>();
        final IntArrayList dripBlocks = new IntArrayList();
        final List<int[]> dripTips = new ArrayList<>();

        enum RoomShape { PLAIN, DOME, CATHEDRAL, LOBED }

        static final class Room {
            final double x, y, z, rx, ry, rz, reach, floor;
            final RoomShape shape;
            final List<double[]> lobes;
            final int index;
            final IntArrayList cells = new IntArrayList();
            final IntArrayList crumble = new IntArrayList();
            final IntArrayList pitBottom = new IntArrayList();
            RoomType type = RoomType.PLAIN;
            int degree;

            Room(double x, double y, double z, double rx, double ry, double rz, double reach, double floor, RoomShape shape,
                 List<double[]> lobes, int index) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.rx = rx;
                this.ry = ry;
                this.rz = rz;
                this.reach = reach;
                this.floor = floor;
                this.shape = shape;
                this.lobes = lobes;
                this.index = index;
            }
        }

        static final class Shape {
            final Entrance type;
            final List<double[]> blobs = new ArrayList<>();
            double[] tip;

            Shape(Entrance type) {
                this.type = type;
            }

            void blob(double x, double y, double z, double rx, double ry, double rz) {
                blobs.add(new double[]{x, y, z, rx, ry, rz});
            }
        }

        Builder(GasCaveNetwork network, ChunkPos source, RandomSource random, NoiseRouter router, Settings settings) {
            this.network = network;
            this.source = source;
            this.random = random;
            this.density = GasCaveNetworks.columnCached(router.finalDensity());
            this.rustworks = router.vegetation();
            this.settings = settings;
            this.noise = new ImprovedNoise(RandomSource.create(random.nextLong()));
        }

        boolean start() {
            int[] anchor = anchor();
            if (anchor == null) return false;
            size = settings.gridSize();
            ox = anchor[0] - size / 2;
            oz = anchor[2] - size / 2;
            int floor = settings.minY() - 1;
            height = Math.min(MAX_HEIGHT, settings.maxY() - floor + 1);
            oy = Mth.clamp(anchor[1] - height / 2, floor, settings.maxY() + 1 - height);
            sample();
            if (!flood(anchor[0] - ox, anchor[1] - oy, anchor[2] - oz)) return false;
            columns();
            distances();
            carve = new byte[kind.length];
            entranceOf = new byte[kind.length];
            roomOf = new byte[kind.length];
            planEntrances();
            if (entrances.isEmpty()) return false;
            placeRooms();
            if (rooms.isEmpty()) return false;
            for (Room room : rooms) carveRoom(room);
            corridors();
            for (Shape shape : entrances) carveEntrance(shape);
            connect();
            linkPockets();
            sculpt();
            assignTypes();
            for (Room room : rooms) {
                if (room.type == RoomType.QUANTUM) pit(room);
            }
            Stats stats = stats();
            if (stats.openBlocks() == 0) return false;
            network.stats = stats;
            reserved = new boolean[kind.length];
            used = new boolean[kind.length];
            markers();
            dripstone();
            for (Room room : rooms) decorate(room);
            clusters();
            details();
            ores();
            emit();
            return true;
        }

        int[] anchor() {
            ToDoubleFunction<BlockPos> column = GasCaveNetworks.interpolated(density);
            for (int attempt = 0; attempt < 4; attempt++) {
                int x = source.getMinBlockX() + random.nextInt(16);
                int z = source.getMinBlockZ() + random.nextInt(16);
                List<int[]> runs = new ArrayList<>();
                int runTop = Integer.MIN_VALUE;
                for (int y = settings.maxY(); y >= settings.minY() - 1; y--) {
                    boolean solid = y >= settings.minY() && column.applyAsDouble(new BlockPos(x, y, z)) > 0.0;
                    if (solid && runTop == Integer.MIN_VALUE) runTop = y;
                    if (!solid && runTop != Integer.MIN_VALUE) {
                        if (runTop - y >= MIN_RUN) runs.add(new int[]{y + 1, runTop});
                        runTop = Integer.MIN_VALUE;
                    }
                }
                if (runs.isEmpty()) continue;
                int[] run = runs.get(random.nextInt(runs.size()));
                return new int[]{x, (run[0] + run[1]) / 2, z};
            }
            return null;
        }

        int index(int x, int y, int z) {
            return (y * size + x) * size + z;
        }

        boolean inside(int x, int y, int z) {
            return x >= 0 && y >= 0 && z >= 0 && x < size && y < height && z < size;
        }

        void sample() {
            int cx0 = Math.floorDiv(ox, CELL_WIDTH);
            int cy0 = Math.floorDiv(oy, CELL_HEIGHT);
            int cz0 = Math.floorDiv(oz, CELL_WIDTH);
            int nx = Math.floorDiv(ox + size - 1, CELL_WIDTH) - cx0 + 2;
            int ny = Math.floorDiv(oy + height - 1, CELL_HEIGHT) - cy0 + 2;
            int nz = Math.floorDiv(oz + size - 1, CELL_WIDTH) - cz0 + 2;
            double[] corners = new double[nx * ny * nz];
            for (int i = 0; i < nx; i++) {
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny; j++) {
                        corners[(i * ny + j) * nz + k] = density.compute(new DensityFunction.SinglePointContext(
                                (cx0 + i) * CELL_WIDTH, (cy0 + j) * CELL_HEIGHT, (cz0 + k) * CELL_WIDTH));
                    }
                }
            }
            kind = new byte[size * height * size];
            for (int y = 0; y < height; y++) {
                int wy = oy + y;
                int j = Math.floorDiv(wy, CELL_HEIGHT) - cy0;
                double fy = (wy - (cy0 + j) * CELL_HEIGHT) / (double) CELL_HEIGHT;
                for (int x = 0; x < size; x++) {
                    int wx = ox + x;
                    int i = Math.floorDiv(wx, CELL_WIDTH) - cx0;
                    double fx = (wx - (cx0 + i) * CELL_WIDTH) / (double) CELL_WIDTH;
                    for (int z = 0; z < size; z++) {
                        int wz = oz + z;
                        int k = Math.floorDiv(wz, CELL_WIDTH) - cz0;
                        double fz = (wz - (cz0 + k) * CELL_WIDTH) / (double) CELL_WIDTH;
                        double d = Mth.lerp3(fx, fy, fz,
                                corners[(i * ny + j) * nz + k], corners[((i + 1) * ny + j) * nz + k],
                                corners[(i * ny + j + 1) * nz + k], corners[((i + 1) * ny + j + 1) * nz + k],
                                corners[(i * ny + j) * nz + k + 1], corners[((i + 1) * ny + j) * nz + k + 1],
                                corners[(i * ny + j + 1) * nz + k + 1], corners[((i + 1) * ny + j + 1) * nz + k + 1]);
                        if (d > 0.0) kind[index(x, y, z)] = wy >= settings.minY() ? SOLID : FOREIGN;
                    }
                }
            }
        }

        boolean flood(int ax, int ay, int az) {
            if (!inside(ax, ay, az) || kind[index(ax, ay, az)] != SOLID) return false;
            island = new int[kind.length];
            islandCount = 0;
            int start = index(ax, ay, az);
            kind[start] = ISLAND;
            island[islandCount++] = start;
            for (int head = 0; head < islandCount; head++) {
                int cell = island[head];
                int x = x(cell), y = y(cell), z = z(cell);
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (!inside(nx, ny, nz)) continue;
                    int n = index(nx, ny, nz);
                    if (kind[n] != SOLID) continue;
                    kind[n] = ISLAND;
                    island[islandCount++] = n;
                }
            }
            for (int i = 0; i < kind.length; i++) {
                if (kind[i] == SOLID) kind[i] = FOREIGN;
            }
            return islandCount >= MIN_ISLAND;
        }

        int x(int cell) {
            return cell / size % size;
        }

        int y(int cell) {
            return cell / (size * size);
        }

        int z(int cell) {
            return cell % size;
        }

        void columns() {
            int columns = size * size;
            top = new int[columns];
            bottom = new int[columns];
            Arrays.fill(top, -1);
            Arrays.fill(bottom, Integer.MAX_VALUE);
            for (int i = 0; i < islandCount; i++) {
                int cell = island[i];
                int col = x(cell) * size + z(cell);
                top[col] = Math.max(top[col], y(cell));
                bottom[col] = Math.min(bottom[col], y(cell));
            }
            int[] edge = new int[columns];
            Arrays.fill(edge, Integer.MAX_VALUE);
            IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
            double sumX = 0.0, sumZ = 0.0;
            int count = 0;
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int col = x * size + z;
                    if (top[col] < 0) continue;
                    sumX += x + 0.5;
                    sumZ += z + 0.5;
                    count++;
                    if (open2d(x + 1, z) || open2d(x - 1, z) || open2d(x, z + 1) || open2d(x, z - 1)) {
                        edge[col] = 0;
                        queue.enqueue(col);
                    }
                }
            }
            centerX = sumX / count;
            centerZ = sumZ / count;
            hasEdge = !queue.isEmpty();
            int maxEdge = 1;
            while (!queue.isEmpty()) {
                int col = queue.dequeueInt();
                int x = col / size, z = col % size;
                maxEdge = Math.max(maxEdge, edge[col]);
                int[][] around = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};
                for (int[] n : around) {
                    if (n[0] < 0 || n[1] < 0 || n[0] >= size || n[1] >= size) continue;
                    int next = n[0] * size + n[1];
                    if (top[next] < 0 || edge[next] != Integer.MAX_VALUE) continue;
                    edge[next] = edge[col] + 1;
                    queue.enqueue(next);
                }
            }
            rn = new double[columns];
            for (int col = 0; col < columns; col++) {
                if (top[col] < 0) continue;
                if (hasEdge) {
                    rn[col] = 1.0 - edge[col] / (double) maxEdge;
                } else {
                    rn[col] = Math.min(1.0, Math.hypot(col / size + 0.5 - centerX, col % size + 0.5 - centerZ) / (size / 2.0));
                }
            }
        }

        boolean open2d(int x, int z) {
            return x >= 0 && z >= 0 && x < size && z < size && top[x * size + z] < 0;
        }

        void distances() {
            depth = new byte[kind.length];
            int[] queue = new int[islandCount];
            int tail = 0;
            for (int i = 0; i < islandCount; i++) {
                int cell = island[i];
                int x = x(cell), y = y(cell), z = z(cell);
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (!inside(nx, ny, nz) || kind[index(nx, ny, nz)] == AIR) {
                        depth[cell] = 1;
                        queue[tail++] = cell;
                        break;
                    }
                }
            }
            for (int head = 0; head < tail; head++) {
                int cell = queue[head];
                int x = x(cell), y = y(cell), z = z(cell);
                int next = Math.min(Byte.MAX_VALUE, depth[cell] + 1);
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (!inside(nx, ny, nz)) continue;
                    int n = index(nx, ny, nz);
                    if (kind[n] != ISLAND || depth[n] != 0) continue;
                    depth[n] = (byte) next;
                    queue[tail++] = n;
                }
            }
        }

        void planEntrances() {
            List<Entrance> order = new ArrayList<>(List.of(Entrance.values()));
            shuffle(order);
            int wanted = 1 + random.nextInt(2);
            double base = random.nextDouble() * Mth.TWO_PI;
            for (Entrance type : order) {
                if (entrances.size() >= wanted) break;
                double angle = base + SECTOR[type.ordinal()] * Math.PI / 3.0 + (random.nextDouble() * 2.0 - 1.0) * 0.2;
                Shape shape = switch (type) {
                    case CLIFF -> cliff(angle);
                    case CHIMNEY -> chimney(angle);
                    case SPIRAL -> spiral(angle);
                };
                if (shape != null) entrances.add(shape);
            }
        }

        int column(double angle, double target) {
            double dx = Math.cos(angle), dz = Math.sin(angle);
            int best = -1;
            double bestError = Double.MAX_VALUE;
            for (double r = 0.0; r < size; r += 0.5) {
                int x = Mth.floor(centerX + dx * r), z = Mth.floor(centerZ + dz * r);
                if (x < 0 || z < 0 || x >= size || z >= size) break;
                int col = x * size + z;
                if (top[col] < 0) continue;
                double error = Math.abs(rn[col] - target);
                if (error < bestError) {
                    bestError = error;
                    best = col;
                }
            }
            return best;
        }

        Shape cliff(double angle) {
            if (!hasEdge) return null;
            int col = column(angle, 0.74);
            if (col < 0) return null;
            double dx = Math.cos(angle), dz = Math.sin(angle);
            double x = col / size + 0.5, z = col % size + 0.5;
            double y = Math.max(bottom[col] + 3, top[col] - 5) + 0.5;
            while (true) {
                int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
                if (!inside(bx, by, bz)) return null;
                byte k = kind[index(bx, by, bz)];
                if (k == FOREIGN) return null;
                if (k == AIR) break;
                x += dx * 0.5;
                z += dz * 0.5;
            }
            Shape shape = new Shape(Entrance.CLIFF);
            double r0 = settings.entranceRadius();
            for (double t = 0.0; t <= 14.0; t += 0.5) {
                double flare = t < 3.0 ? 1.35 : 1.0;
                shape.blob(x - dx * t, y - 0.25 * Math.min(t, 13.0), z - dz * t, r0 * 1.36 * flare, r0 * flare, r0 * 1.36 * flare);
            }
            shape.tip = new double[]{x - dx * 13.0, y - 3.25, z - dz * 13.0};
            return shape;
        }

        Shape chimney(double angle) {
            int col = column(angle, 0.16);
            if (col < 0) return null;
            int thickness = top[col] - bottom[col] + 1;
            if (thickness < 12) return null;
            int h = Mth.clamp((int) Math.round(0.45 * thickness), 6, 18);
            double x = col / size + 0.5, z = col % size + 0.5;
            double r0 = settings.entranceRadius();
            Shape shape = new Shape(Entrance.CHIMNEY);
            for (int y = bottom[col] - 3; y <= bottom[col] + h; y++) {
                double r = r0 + 0.9 * Math.max(0, bottom[col] + 2 - y) + 0.5 * noise.noise((ox + x) * 0.3, (oy + y) * 0.3, (oz + z) * 0.3);
                shape.blob(x, y + 0.5, z, r, 0.5, r);
            }
            shape.tip = new double[]{x, bottom[col] + h + 0.5, z};
            return shape;
        }

        Shape spiral(double angle) {
            int col = column(angle, 0.30);
            if (col < 0) return null;
            int thickness = top[col] - bottom[col] + 1;
            int depthTarget = Mth.clamp((int) Math.round(0.45 * thickness), 8, 16);
            if (thickness < depthTarget + 4) return null;
            double axisX = col / size + 0.5, axisZ = col % size + 0.5;
            double radius = settings.entranceRadius() + 2.2;
            double startY = top[col] + 1.5;
            double phase = random.nextDouble() * Mth.TWO_PI;
            double turns = depthTarget / 6.0;
            Shape shape = new Shape(Entrance.SPIRAL);
            double end = turns * Mth.TWO_PI;
            double[] last = null;
            for (double t = 0.0; t <= end; t += 0.06) {
                double px = axisX + Math.cos(phase + t) * radius;
                double pz = axisZ + Math.sin(phase + t) * radius;
                double py = startY - t / Mth.TWO_PI * 6.0;
                shape.blob(px, py, pz, 2.0, 2.0, 2.0);
                last = new double[]{px, py, pz};
            }
            shape.tip = last;
            return shape;
        }

        void placeRooms() {
            int attempts = (int) Math.round(1500.0 * (size / 64.0) * (size / 64.0));
            int shell = settings.shell();
            for (int attempt = 0; attempt < attempts && rooms.size() < settings.rooms(); attempt++) {
                int cell = island[random.nextInt(islandCount)];
                if (depth[cell] <= shell) continue;
                Room room = shapeRoom(x(cell) + 0.5, y(cell) + 0.5, z(cell) + 0.5);
                if (!fits(room)) continue;
                boolean clear = true;
                for (Room other : rooms) {
                    if (Math.sqrt(sq(room.x - other.x) + sq(1.4 * (room.y - other.y)) + sq(room.z - other.z)) < 1.2 * (room.reach + other.reach)) {
                        clear = false;
                        break;
                    }
                }
                if (clear) rooms.add(room);
            }
        }

        Room shapeRoom(double x, double y, double z) {
            double base = settings.roomRadius();
            int roll = random.nextInt(100);
            RoomShape shape = roll < 40 ? RoomShape.PLAIN : roll < 60 ? RoomShape.DOME : roll < 75 ? RoomShape.CATHEDRAL : RoomShape.LOBED;
            double rx, ry, rz;
            switch (shape) {
                case DOME -> {
                    rx = base * (1.0 + random.nextDouble() * 0.4);
                    ry = Math.max(2.2, rx * 0.42);
                    rz = rx * (0.85 + random.nextDouble() * 0.3);
                }
                case CATHEDRAL -> {
                    rx = base * (0.6 + random.nextDouble() * 0.25);
                    ry = rx * (1.2 + random.nextDouble() * 0.3);
                    rz = rx * (0.85 + random.nextDouble() * 0.3);
                }
                case LOBED -> {
                    rx = base * (0.65 + random.nextDouble() * 0.35);
                    ry = Math.max(1.6, rx * 0.6);
                    rz = rx * (0.8 + random.nextDouble() * 0.4);
                }
                default -> {
                    rx = base * (0.75 + random.nextDouble() * 0.5);
                    ry = Math.max(1.6, rx * 0.6);
                    rz = rx * (0.8 + random.nextDouble() * 0.4);
                }
            }
            double cut = ry * 1.6 >= 3.0 ? 0.6 : 1.0;
            double floor = y - ry * cut;
            List<double[]> lobes = new ArrayList<>();
            lobes.add(new double[]{x, y, z, rx, ry, rz});
            double reach = Math.max(rx, rz);
            if (shape == RoomShape.LOBED) {
                int extra = 1 + random.nextInt(2);
                double angle = random.nextDouble() * Mth.TWO_PI;
                for (int i = 0; i < extra; i++) {
                    double a = angle + i * Math.PI * (0.6 + random.nextDouble() * 0.8);
                    double scale = 0.6 + random.nextDouble() * 0.2;
                    double lrx = rx * scale, lry = ry * scale, lrz = rz * scale;
                    double distance = rx * (0.7 + random.nextDouble() * 0.3);
                    lobes.add(new double[]{x + Math.cos(a) * distance, floor + lry * cut, z + Math.sin(a) * distance, lrx, lry, lrz});
                    reach = Math.max(reach, distance + Math.max(lrx, lrz));
                }
            }
            return new Room(x, y, z, rx, ry, rz, reach, floor, shape, lobes, rooms.size());
        }

        boolean fits(Room room) {
            for (double[] l : room.lobes) {
                if (!deep(l[0], l[1], l[2]) || !deep(l[0] + l[3], l[1], l[2]) || !deep(l[0] - l[3], l[1], l[2])
                        || !deep(l[0], l[1] + l[4], l[2]) || !deep(l[0], room.floor - 1.0, l[2])
                        || !deep(l[0], l[1], l[2] + l[5]) || !deep(l[0], l[1], l[2] - l[5])) return false;
            }
            return true;
        }

        boolean deep(double x, double y, double z) {
            int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
            if (!inside(bx, by, bz)) return false;
            int cell = index(bx, by, bz);
            return kind[cell] == ISLAND && depth[cell] > settings.shell();
        }

        void carveRoom(Room room) {
            byte id = (byte) (room.index + 1);
            for (double[] lobe : room.lobes) {
                int rx = Mth.ceil(lobe[3] * 1.3), ry = Mth.ceil(lobe[4] * 1.3), rz = Mth.ceil(lobe[5] * 1.3);
                int cx = Mth.floor(lobe[0]), cy = Mth.floor(lobe[1]), cz = Mth.floor(lobe[2]);
                for (int x = cx - rx; x <= cx + rx; x++) {
                    for (int z = cz - rz; z <= cz + rz; z++) {
                        double floor = room.floor + 0.7 * noise.noise((ox + x) * 0.12, 91.3, (oz + z) * 0.12);
                        for (int y = cy - ry; y <= cy + ry; y++) {
                            if (!inside(x, y, z) || y + 0.5 < floor) continue;
                            int cell = index(x, y, z);
                            if (kind[cell] != ISLAND || depth[cell] <= settings.shell() || carve[cell] != NONE) continue;
                            double e = Math.sqrt(sq((x + 0.5 - lobe[0]) / lobe[3]) + sq((y + 0.5 - lobe[1]) / lobe[4]) + sq((z + 0.5 - lobe[2]) / lobe[5]));
                            double wobble = noise.noise((ox + x) * 0.22, (oy + y) * 0.22, (oz + z) * 0.22);
                            if (e >= 1.0 + 0.28 * wobble) continue;
                            carve[cell] = ROOM;
                            roomOf[cell] = id;
                            room.cells.add(cell);
                        }
                    }
                }
            }
        }

        void corridors() {
            int n = rooms.size();
            boolean[] tree = new boolean[n];
            double[] best = new double[n];
            int[] parent = new int[n];
            boolean[][] linked = new boolean[n][n];
            Arrays.fill(best, Double.MAX_VALUE);
            best[0] = 0.0;
            parent[0] = -1;
            for (int step = 0; step < n; step++) {
                int next = -1;
                for (int i = 0; i < n; i++) {
                    if (!tree[i] && (next < 0 || best[i] < best[next])) next = i;
                }
                tree[next] = true;
                if (parent[next] >= 0) {
                    linked[next][parent[next]] = linked[parent[next]][next] = true;
                    corridor(rooms.get(parent[next]), rooms.get(next));
                }
                for (int i = 0; i < n; i++) {
                    if (tree[i]) continue;
                    double d = roomDistance(rooms.get(next), rooms.get(i));
                    if (d < best[i]) {
                        best[i] = d;
                        parent[i] = next;
                    }
                }
            }
            int loops = (int) Math.round(settings.loops() * n * 0.8);
            for (int loop = 0; loop < loops; loop++) {
                int a = -1, b = -1;
                double shortest = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        if (linked[i][j]) continue;
                        double d = roomDistance(rooms.get(i), rooms.get(j));
                        if (d < shortest) {
                            shortest = d;
                            a = i;
                            b = j;
                        }
                    }
                }
                if (a < 0) break;
                linked[a][b] = linked[b][a] = true;
                corridor(rooms.get(a), rooms.get(b));
            }
        }

        double roomDistance(Room a, Room b) {
            return Math.sqrt(sq(a.x - b.x) + sq(1.3 * (a.y - b.y)) + sq(a.z - b.z));
        }

        void corridor(Room a, Room b) {
            a.degree++;
            b.degree++;
            if (random.nextDouble() < settings.fissureChance()) {
                tube(a.x, a.floor + FISSURE_HEIGHT, a.z, b.x, b.floor + FISSURE_HEIGHT, b.z, FISSURE_WIDTH, FISSURE_HEIGHT,
                        Math.max(0.15, settings.sinuosity()), CORRIDOR);
            } else {
                double r = settings.corridorRadius();
                tube(a.x, a.floor + r, a.z, b.x, b.floor + r, b.z, r, r, settings.sinuosity(), CORRIDOR);
            }
        }

        void tube(double ax, double ay, double az, double bx, double by, double bz, double rh, double rv, double sinuosity, byte type) {
            double dx = bx - ax, dy = by - ay, dz = bz - az;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0E-3) {
                ellipsoid(ax, ay, az, rh, rv, type);
                return;
            }
            double px = -dz, pz = dx;
            double norm = Math.sqrt(px * px + pz * pz);
            if (norm < 1.0E-3) {
                px = 1.0;
                pz = 0.0;
            } else {
                px /= norm;
                pz /= norm;
            }
            double phase = random.nextDouble() * 64.0 + 0.37;
            for (double t = 0.0; t <= length + 1.0E-6; t += 0.6) {
                double f = t / length;
                double offset = sinuosity * length * 0.35 * noise.noise(phase + f * 2.0, phase * 0.7, 0.5) * Math.sin(Math.PI * f);
                ellipsoid(ax + dx * f + px * offset, ay + dy * f, az + dz * f + pz * offset, rh, rv, type);
            }
            ellipsoid(bx, by, bz, rh, rv, type);
        }

        void sphere(double cx, double cy, double cz, double radius, byte type) {
            ellipsoid(cx, cy, cz, radius, radius, type);
        }

        void ellipsoid(double cx, double cy, double cz, double rh, double rv, byte type) {
            int r = Mth.ceil(rh), v = Mth.ceil(rv);
            int bx = Mth.floor(cx), by = Mth.floor(cy), bz = Mth.floor(cz);
            for (int x = bx - r; x <= bx + r; x++) {
                for (int y = by - v; y <= by + v; y++) {
                    for (int z = bz - r; z <= bz + r; z++) {
                        if (!inside(x, y, z)) continue;
                        int cell = index(x, y, z);
                        if (kind[cell] != ISLAND || depth[cell] < 2 || carve[cell] != NONE) continue;
                        if ((sq(x + 0.5 - cx) + sq(z + 0.5 - cz)) / (rh * rh) + sq(y + 0.5 - cy) / (rv * rv) <= 1.0) carve[cell] = type;
                    }
                }
            }
        }

        void carveEntrance(Shape shape) {
            for (double[] blob : shape.blobs) {
                int rx = Mth.ceil(blob[3]), ry = Mth.ceil(blob[4]), rz = Mth.ceil(blob[5]);
                int bx = Mth.floor(blob[0]), by = Mth.floor(blob[1]), bz = Mth.floor(blob[2]);
                for (int x = bx - rx; x <= bx + rx; x++) {
                    for (int y = by - ry; y <= by + ry; y++) {
                        for (int z = bz - rz; z <= bz + rz; z++) {
                            if (!inside(x, y, z)) continue;
                            int cell = index(x, y, z);
                            if (kind[cell] != ISLAND || carve[cell] == ENTRANCE) continue;
                            double e = sq((x + 0.5 - blob[0]) / blob[3]) + sq((y + 0.5 - blob[1]) / blob[4]) + sq((z + 0.5 - blob[2]) / blob[5]);
                            if (e > 1.0) continue;
                            carve[cell] = ENTRANCE;
                            entranceOf[cell] = (byte) shape.type.ordinal();
                        }
                    }
                }
            }
        }

        int[] components(int[] labels) {
            Arrays.fill(labels, -1);
            List<Integer> sizes = new ArrayList<>();
            if (scratch == null) scratch = new int[carve.length];
            int[] queue = scratch;
            for (int start = 0; start < carve.length; start++) {
                if (carve[start] == NONE || labels[start] >= 0) continue;
                int label = sizes.size();
                int tail = 0;
                queue[tail++] = start;
                labels[start] = label;
                for (int head = 0; head < tail; head++) {
                    int cell = queue[head];
                    int x = x(cell), y = y(cell), z = z(cell);
                    for (int[] side : SIDES) {
                        int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                        if (!inside(nx, ny, nz)) continue;
                        int n = index(nx, ny, nz);
                        if (carve[n] == NONE || labels[n] >= 0) continue;
                        labels[n] = label;
                        queue[tail++] = n;
                    }
                }
                sizes.add(tail);
            }
            return sizes.stream().mapToInt(Integer::intValue).toArray();
        }

        boolean touchesAir(int cell) {
            int x = x(cell), y = y(cell), z = z(cell);
            for (int[] side : SIDES) {
                int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                if (inside(nx, ny, nz) && kind[index(nx, ny, nz)] == AIR) return true;
            }
            return false;
        }

        void connect() {
            int[] labels = new int[carve.length];
            int largest = largest(components(labels));
            for (Shape shape : entrances) {
                if (shape.tip == null) continue;
                int tx = Mth.floor(shape.tip[0]), ty = Mth.floor(shape.tip[1]), tz = Mth.floor(shape.tip[2]);
                if (!inside(tx, ty, tz)) continue;
                int from = index(tx, ty, tz);
                if (carve[from] == NONE || labels[from] == largest) continue;
                link(from, labels, largest, Math.max(1.375, settings.corridorRadius()), 0.3);
                largest = largest(components(labels));
            }
        }

        static int largest(int[] sizes) {
            int largest = 0;
            for (int i = 1; i < sizes.length; i++) {
                if (sizes[i] > sizes[largest]) largest = i;
            }
            return largest;
        }

        void linkPockets() {
            int[] labels = new int[carve.length];
            int[] sizes = components(labels);
            boolean[] open = openComponents(labels, sizes.length);
            for (int label = 0; label < sizes.length; label++) {
                if (open[label] || sizes[label] < MIN_POCKET) continue;
                int from = -1;
                for (int cell = 0; cell < carve.length && from < 0; cell++) {
                    if (labels[cell] == label) from = cell;
                }
                linkToOpen(from, labels, open);
            }
            sizes = components(labels);
            open = openComponents(labels, sizes.length);
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] != NONE && !open[labels[cell]] && sizes[labels[cell]] < MIN_POCKET) carve[cell] = NONE;
            }
        }

        boolean[] openComponents(int[] labels, int count) {
            boolean[] open = new boolean[count];
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] != NONE && !open[labels[cell]] && touchesAir(cell)) open[labels[cell]] = true;
            }
            return open;
        }

        void linkToOpen(int from, int[] labels, boolean[] open) {
            int target = nearest(from, cell -> labels[cell] >= 0 && open[labels[cell]]);
            if (target < 0) return;
            double radius = Math.max(1.4, settings.corridorRadius());
            tube(x(from) + 0.5, y(from) + 0.5, z(from) + 0.5, x(target) + 0.5, y(target) + 0.5, z(target) + 0.5, radius, radius, 0.3, LINK);
            path(from, cell -> carve[cell] != NONE && labels[cell] >= 0 && open[labels[cell]]);
        }

        void link(int from, int[] labels, int target, double radius, double sinuosity) {
            int to = nearest(from, cell -> labels[cell] == target);
            if (to < 0) return;
            tube(x(from) + 0.5, y(from) + 0.5, z(from) + 0.5, x(to) + 0.5, y(to) + 0.5, z(to) + 0.5, radius, radius, sinuosity, LINK);
            path(from, cell -> labels[cell] == target);
        }

        int nearest(int from, java.util.function.IntPredicate accept) {
            int fx = x(from), fy = y(from), fz = z(from);
            int best = -1;
            long bestDistance = Long.MAX_VALUE;
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] == NONE || !accept.test(cell)) continue;
                long d = (long) sq(x(cell) - fx) + sq(y(cell) - fy) + sq(z(cell) - fz);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = cell;
                }
            }
            return best;
        }

        void path(int from, java.util.function.IntPredicate goal) {
            int[] cost = new int[carve.length];
            int[] previous = new int[carve.length];
            Arrays.fill(cost, Integer.MAX_VALUE);
            IntArrayFIFOQueue deque = new IntArrayFIFOQueue();
            deque.enqueue(from);
            cost[from] = 0;
            previous[from] = -1;
            int reached = -1;
            while (!deque.isEmpty()) {
                int cell = deque.dequeueInt();
                if (goal.test(cell) && cell != from) {
                    reached = cell;
                    break;
                }
                int x = x(cell), y = y(cell), z = z(cell);
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (!inside(nx, ny, nz)) continue;
                    int n = index(nx, ny, nz);
                    boolean carved = carve[n] != NONE;
                    if (!carved && (kind[n] != ISLAND || depth[n] < 2)) continue;
                    int step = carved ? 0 : 1;
                    if (cost[cell] + step >= cost[n]) continue;
                    cost[n] = cost[cell] + step;
                    previous[n] = cell;
                    if (step == 0) {
                        deque.enqueueFirst(n);
                    } else {
                        deque.enqueue(n);
                    }
                }
            }
            if (reached < 0) return;
            for (int cell = reached; cell >= 0; cell = previous[cell]) {
                if (carve[cell] == NONE) sphere(x(cell) + 0.5, y(cell) + 0.5, z(cell) + 0.5, settings.corridorRadius(), LINK);
            }
        }

        Stats stats() {
            int[] labels = new int[carve.length];
            int[] sizes = components(labels);
            boolean[] open = openComponents(labels, sizes.length);
            int carved = 0, roomBlocks = 0, corridorBlocks = 0, entranceBlocks = 0, linkBlocks = 0, accessible = 0, openBlocks = 0, breaches = 0;
            Map<Entrance, Integer> byEntrance = new EnumMap<>(Entrance.class);
            for (int cell = 0; cell < carve.length; cell++) {
                byte type = carve[cell];
                if (type == NONE) continue;
                carved++;
                switch (type) {
                    case ROOM -> roomBlocks++;
                    case CORRIDOR -> corridorBlocks++;
                    case LINK -> linkBlocks++;
                    default -> entranceBlocks++;
                }
                if (open[labels[cell]]) accessible++;
                if (touchesAir(cell)) {
                    openBlocks++;
                    if (type == ENTRANCE) {
                        byEntrance.merge(Entrance.values()[entranceOf[cell]], 1, Integer::sum);
                    } else {
                        breaches++;
                    }
                }
            }
            int closed = 0;
            for (int label = 0; label < sizes.length; label++) {
                if (!open[label] && sizes[label] >= MIN_POCKET) closed++;
            }
            Map<RoomType, Integer> types = new EnumMap<>(RoomType.class);
            for (Room room : rooms) types.merge(room.type, 1, Integer::sum);
            return new Stats(islandCount, carved, roomBlocks, corridorBlocks, entranceBlocks, linkBlocks, rooms.size(), types,
                    carved == 0 ? 0.0 : accessible / (double) carved, closed, openBlocks, breaches, byEntrance);
        }

        void markers() {
            BlockState mistglow = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
            for (int i = 0; i < islandCount; i++) {
                int cell = island[i];
                if (carve[cell] != NONE || depth[cell] > 2) continue;
                int x = x(cell), y = y(cell), z = z(cell);
                boolean touching = false;
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (inside(nx, ny, nz) && carve[index(nx, ny, nz)] == ENTRANCE) {
                        touching = true;
                        break;
                    }
                }
                if (touching && random.nextDouble() < settings.markerChance()) {
                    prop(world(cell), mistglow, true);
                    reserved[cell] = true;
                }
            }
        }

        void clusters() {
            BlockState cluster = QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState();
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] == NONE || carve[cell] == ENTRANCE || used[cell]) continue;
                double chance = typeOf(cell) == RoomType.QUANTUM ? QUANTUM_CLUSTER_CHANCE : settings.clusterChance();
                if (random.nextDouble() >= chance) continue;
                int x = x(cell), y = y(cell), z = z(cell);
                int first = random.nextInt(SIDES.length);
                for (int i = 0; i < SIDES.length; i++) {
                    int s = (first + i) % SIDES.length;
                    int nx = x + SIDES[s][0], ny = y + SIDES[s][1], nz = z + SIDES[s][2];
                    if (!inside(nx, ny, nz)) continue;
                    int wall = index(nx, ny, nz);
                    if (!rock(wall) || reserved[wall]) continue;
                    prop(world(cell), cluster.setValue(FlickerShardClusterBlock.FACING, SIDE_DIRS[s].getOpposite()), false);
                    reserved[wall] = true;
                    used[cell] = true;
                    break;
                }
            }
        }

        RoomType typeOf(int cell) {
            return roomOf[cell] > 0 ? rooms.get(roomOf[cell] - 1).type : RoomType.PLAIN;
        }

        boolean rock(int cell) {
            return kind[cell] == ISLAND && carve[cell] == NONE;
        }

        boolean isRoomCell(int cell, Room room) {
            return carve[cell] == ROOM && roomOf[cell] == room.index + 1;
        }

        int below(int cell) {
            return y(cell) > 0 ? cell - size * size : -1;
        }

        int above(int cell) {
            return y(cell) + 1 < height ? cell + size * size : -1;
        }

        boolean floor(int cell) {
            int below = below(cell);
            return below >= 0 && rock(below);
        }

        boolean ceiling(int cell) {
            int above = above(cell);
            return above >= 0 && rock(above);
        }

        boolean nearPassage(int cell, Room room) {
            int x = x(cell), y = y(cell), z = z(cell);
            for (int[] side : SIDES) {
                int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                if (!inside(nx, ny, nz)) continue;
                int n = index(nx, ny, nz);
                if (carve[n] != NONE && !isRoomCell(n, room)) return true;
            }
            return false;
        }

        void uncarve(int cell) {
            carve[cell] = NONE;
            roomOf[cell] = 0;
        }

        void sculpt() {
            for (Room room : rooms) {
                if (room.shape != RoomShape.CATHEDRAL && room.rx >= 5.0) {
                    int pillars = room.rx >= 6.0 ? 1 + random.nextInt(2) : random.nextInt(2);
                    for (int i = 0; i < pillars; i++) {
                        pillar(room);
                        refresh(room);
                    }
                }
                int spikes = room.cells.size() / 90;
                for (int i = 0; i < spikes && !room.cells.isEmpty(); i++) spike(room);
                refresh(room);
            }
        }

        void refresh(Room room) {
            IntArrayList kept = new IntArrayList();
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                if (isRoomCell(cell, room)) kept.add(cell);
            }
            room.cells.clear();
            room.cells.addAll(kept);
        }

        void pillar(Room room) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double distance = 0.3 + random.nextDouble() * 0.25;
            double px = room.x + Math.cos(angle) * room.rx * distance;
            double pz = room.z + Math.sin(angle) * room.rz * distance;
            double radius = 0.7 + random.nextDouble() * 0.6;
            IntArrayList candidates = new IntArrayList();
            int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                if (sq(x(cell) + 0.5 - px) + sq(z(cell) + 0.5 - pz) > sq(radius + 1.0)) continue;
                candidates.add(cell);
                low = Math.min(low, y(cell));
                high = Math.max(high, y(cell));
            }
            if (high - low < 2) return;
            IntArrayList solid = new IntArrayList();
            for (int i = 0; i < candidates.size(); i++) {
                int cell = candidates.getInt(i);
                int y = y(cell);
                double flare = Math.max(0.0, 1.5 - Math.min(y - low, high - y)) * 0.6;
                if (sq(x(cell) + 0.5 - px) + sq(z(cell) + 0.5 - pz) > sq(radius + flare)) continue;
                if (nearPassage(cell, room)) return;
                solid.add(cell);
            }
            for (int i = 0; i < solid.size(); i++) uncarve(solid.getInt(i));
        }

        void spike(Room room) {
            int cell = room.cells.getInt(random.nextInt(room.cells.size()));
            if (!isRoomCell(cell, room) || sq(x(cell) + 0.5 - room.x) + sq(z(cell) + 0.5 - room.z) < 4.0) return;
            boolean hanging = random.nextFloat() < 0.6F;
            int step = hanging ? 1 : -1;
            int x = x(cell), y = y(cell), z = z(cell);
            while (inside(x, y + step, z) && isRoomCell(index(x, y + step, z), room)) y += step;
            int base = y + step;
            if (!inside(x, base, z) || !rock(index(x, base, z))) return;
            int length = hanging ? 1 + random.nextInt(3) : 1 + random.nextInt(2);
            for (int i = 0; i < length + 2; i++) {
                int yy = y - step * i;
                if (!inside(x, yy, z) || !isRoomCell(index(x, yy, z), room) || nearPassage(index(x, yy, z), room)) return;
            }
            for (int i = 0; i < length; i++) {
                uncarve(index(x, y - step * i, z));
                dripBlocks.add(index(x, y - step * i, z));
            }
            int tipY = y - step * length;
            int extra = tipY - step * 2;
            boolean doubleTip = inside(x, extra, z) && isRoomCell(index(x, extra, z), room) && random.nextFloat() < 0.5F;
            dripTips.add(new int[]{index(x, tipY, z), step, doubleTip ? 2 : 1});
            if (length < 3) return;
            for (Direction dir : HORIZONTAL) {
                int nx = x + dir.getStepX(), nz = z + dir.getStepZ();
                if (!inside(nx, y, nz) || !inside(nx, base, nz)) continue;
                int n = index(nx, y, nz);
                if (isRoomCell(n, room) && rock(index(nx, base, nz)) && !nearPassage(n, room)) {
                    uncarve(n);
                    dripBlocks.add(n);
                }
            }
        }

        void dripstone() {
            BlockState block = Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            for (int i = 0; i < dripBlocks.size(); i++) {
                int cell = dripBlocks.getInt(i);
                if (rock(cell) && !reserved[cell]) solid(cell, block);
            }
            for (int[] tip : dripTips) {
                Direction direction = tip[1] > 0 ? Direction.DOWN : Direction.UP;
                int cell = tip[0];
                int stepCells = -tip[1] * size * size;
                for (int i = 0; i < tip[2]; i++, cell += stepCells) {
                    if (cell < 0 || cell >= carve.length || carve[cell] == NONE || used[cell]) break;
                    DripstoneThickness thickness = i == tip[2] - 1 ? DripstoneThickness.TIP : DripstoneThickness.FRUSTUM;
                    put(cell, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
                            .setValue(PointedDripstoneBlock.THICKNESS, thickness));
                }
            }
        }

        void assignTypes() {
            List<Room> pool = new ArrayList<>(rooms);
            shuffle(pool);
            pool.sort(Comparator.comparingInt(room -> room.degree <= 1 ? 0 : 1));
            if (random.nextDouble() < CAMP_CHANCE) take(pool, RoomType.CAMP, room -> true);
            if (random.nextDouble() < QUANTUM_CHANCE) take(pool, RoomType.QUANTUM, room -> room.rx >= 4.5);
            if (random.nextDouble() < KAIRITE_CHANCE) take(pool, RoomType.KAIRITE, this::inRustworks);
            int lumen = random.nextDouble() < LUMEN_CHANCE ? 1 + random.nextInt(2) : 0;
            for (int i = 0; i < lumen; i++) take(pool, RoomType.LUMEN, room -> true);
        }

        void take(List<Room> pool, RoomType type, Predicate<Room> accept) {
            for (Iterator<Room> it = pool.iterator(); it.hasNext(); ) {
                Room room = it.next();
                if (!accept.test(room)) continue;
                room.type = type;
                it.remove();
                return;
            }
        }

        boolean inRustworks(Room room) {
            return rustworks.compute(new DensityFunction.SinglePointContext(ox + Mth.floor(room.x), oy + Mth.floor(room.y), oz + Mth.floor(room.z)))
                    > UpfallTerrain.BiomeThresholds.RUSTWORKS;
        }

        int heart(Room room) {
            int best = -1;
            double bestDistance = Double.MAX_VALUE;
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                if (!floor(cell)) continue;
                double d = sq(x(cell) + 0.5 - room.x) + sq(z(cell) + 0.5 - room.z);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = cell;
                }
            }
            return best;
        }

        void pit(Room room) {
            int heart = heart(room);
            if (heart < 0) return;
            int bottom = y(heart) - 3 - random.nextInt(2);
            double radius = 1.5 + random.nextDouble() * 0.7;
            int r = Mth.ceil(radius);
            IntArrayList cells = new IntArrayList();
            IntArrayList crumble = new IntArrayList();
            IntArrayList floorCells = new IntArrayList();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    int x = x(heart) + dx, z = z(heart) + dz;
                    int top = -1;
                    for (int y = y(heart) + 1; y >= y(heart) - 1; y--) {
                        if (inside(x, y, z) && isRoomCell(index(x, y, z), room) && floor(index(x, y, z))) top = y - 1;
                    }
                    if (top < 0 || top <= bottom) return;
                    for (int y = top; y >= bottom; y--) {
                        if (!inside(x, y, z)) return;
                        int cell = index(x, y, z);
                        if (!rock(cell) || depth[cell] < 3) return;
                        cells.add(cell);
                    }
                    crumble.add(index(x, top, z));
                    floorCells.add(index(x, bottom, z));
                }
            }
            byte id = (byte) (room.index + 1);
            for (int i = 0; i < cells.size(); i++) {
                int cell = cells.getInt(i);
                carve[cell] = ROOM;
                roomOf[cell] = id;
                room.cells.add(cell);
            }
            room.crumble.addAll(crumble);
            room.pitBottom.addAll(floorCells);
        }

        void decorate(Room room) {
            switch (room.type) {
                case CAMP -> camp(room);
                case LUMEN -> lumen(room);
                case QUANTUM -> quantum(room);
                case KAIRITE -> kairite(room);
                case PLAIN -> {}
            }
        }

        IntArrayList floorCells(Room room) {
            IntArrayList cells = new IntArrayList();
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                if (!used[cell] && floor(cell)) cells.add(cell);
            }
            return cells;
        }

        IntArrayList ceilingCells(Room room) {
            IntArrayList cells = new IntArrayList();
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                if (!used[cell] && ceiling(cell)) cells.add(cell);
            }
            return cells;
        }

        IntArrayList wallCells(Room room) {
            IntOpenHashSet seen = new IntOpenHashSet();
            IntArrayList walls = new IntArrayList();
            for (int i = 0; i < room.cells.size(); i++) {
                int cell = room.cells.getInt(i);
                int x = x(cell), y = y(cell), z = z(cell);
                for (int[] side : SIDES) {
                    int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                    if (!inside(nx, ny, nz)) continue;
                    int n = index(nx, ny, nz);
                    if (rock(n) && !reserved[n] && seen.add(n)) walls.add(n);
                }
            }
            return walls;
        }

        void sortByDistance(IntArrayList cells, int from) {
            int fx = x(from), fz = z(from);
            cells.sort((a, b) -> Integer.compare(sq(x(a) - fx) + sq(z(a) - fz), sq(x(b) - fx) + sq(z(b) - fz)));
        }

        void camp(Room room) {
            IntArrayList floor = floorCells(room);
            if (floor.size() < 6) return;
            int heart = heart(room);
            sortByDistance(floor, heart);
            int barrels = 1 + random.nextInt(2);
            for (int i = 0; i < floor.size() && barrels > 0; i++) {
                int cell = floor.getInt(i);
                Direction wall = wallSide(cell);
                if (used[cell] || wall == null) continue;
                propLoot(cell, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, wall.getOpposite()), QuantumLoot.GAS_CAVE_SUPPLY);
                barrels--;
                int top = above(cell);
                if (top >= 0 && carve[top] != NONE && !used[top] && random.nextFloat() < 0.4F) {
                    put(top, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
                }
            }
            BlockState[] scene = {
                    QuantumBlocks.SPENT_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, false),
                    QuantumBlocks.ABANDONED_PICKAXE.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, randomHorizontal()),
                    random.nextFloat() < 0.6F ? QuantumBlocks.CRACKED_VEIL_MASK.get().defaultBlockState()
                            .setValue(HorizontalDirectionalBlock.FACING, randomHorizontal()) : null,
                    random.nextFloat() < 0.7F ? QuantumBlocks.SCATTERED_BONES.get().defaultBlockState()
                            .setValue(HorizontalDirectionalBlock.FACING, randomHorizontal()) : null,
                    random.nextFloat() < 0.5F ? Blocks.SKELETON_SKULL.defaultBlockState()
                            .setValue(SkullBlock.ROTATION, RotationSegment.convertToSegment(random.nextFloat() * 360.0F)) : null};
            IntArrayList near = new IntArrayList();
            for (int i = 0; i < floor.size() && near.size() < 16; i++) {
                if (!used[floor.getInt(i)]) near.add(floor.getInt(i));
            }
            for (BlockState state : scene) {
                if (state == null || near.isEmpty()) continue;
                put(near.removeInt(random.nextInt(near.size())), state);
            }
            IntArrayList ceiling = ceilingCells(room);
            if (!ceiling.isEmpty()) {
                sortByDistance(ceiling, heart);
                put(ceiling.getInt(0), QuantumBlocks.SPENT_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        Direction wallSide(int cell) {
            int x = x(cell), y = y(cell), z = z(cell);
            for (Direction dir : HORIZONTAL) {
                int nx = x + dir.getStepX(), nz = z + dir.getStepZ();
                if (inside(nx, y, nz) && rock(index(nx, y, nz))) return dir;
            }
            return null;
        }

        Direction randomHorizontal() {
            return HORIZONTAL[random.nextInt(HORIZONTAL.length)];
        }

        void lumen(Room room) {
            BlockState moss = GrottoBlocks.LUMEN_MOSS.get().defaultBlockState();
            BlockState sprouts = GrottoBlocks.LUMEN_SPROUTS.get().defaultBlockState();
            IntArrayList floor = floorCells(room);
            for (int i = 0; i < floor.size(); i++) {
                int cell = floor.getInt(i);
                int below = below(cell);
                if (!reserved[below] && random.nextFloat() < 0.6F) solid(below, moss);
                if (random.nextFloat() < 0.35F) put(cell, sprouts);
            }
            IntArrayList walls = wallCells(room);
            for (int i = 0; i < walls.size(); i++) {
                float roll = random.nextFloat();
                if (roll < 0.3F) solid(walls.getInt(i), GrottoBlocks.LUMEN_ROCK.get().defaultBlockState());
                else if (roll < 0.34F) solid(walls.getInt(i), GrottoBlocks.LUMEN_DRUSE.get().defaultBlockState());
            }
            IntArrayList ceiling = ceilingCells(room);
            for (int i = 0; i < ceiling.size(); i++) {
                if (random.nextFloat() < 0.08F) vine(ceiling.getInt(i), 1 + random.nextInt(4));
            }
        }

        void quantum(Room room) {
            BlockState crumbling = QuantumBlocks.CRUMBLING_FLOOR.get().defaultBlockState();
            for (int i = 0; i < room.crumble.size(); i++) put(room.crumble.getInt(i), crumbling);
            BlockState cluster = QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState().setValue(FlickerShardClusterBlock.FACING, Direction.UP);
            for (int i = 0; i < room.pitBottom.size(); i++) {
                int cell = room.pitBottom.getInt(i);
                int below = below(cell);
                if (used[cell] || below < 0 || !rock(below) || random.nextFloat() >= 0.5F) continue;
                put(cell, cluster);
                reserved[below] = true;
            }
        }

        void kairite(Room room) {
            Predicate<BlockState> rustslate = state -> state.is(RustworksBlocks.RUSTSLATE.get());
            Predicate<BlockState> rustFloor = state -> state.is(RustworksBlocks.RUSTSLATE.get()) || state.is(RustworksBlocks.RUSTSOIL.get());
            IntArrayList floor = floorCells(room);
            for (int i = 0; i < floor.size(); i++) {
                int cell = floor.getInt(i);
                int below = below(cell);
                if (!reserved[below] && random.nextFloat() < 0.2F) {
                    propHost(below, RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get().defaultBlockState(), rustFloor);
                } else if (random.nextFloat() < 0.4F) {
                    put(cell, RustworksBlocks.VERDIGRIS_MOSS.get().defaultBlockState());
                }
            }
            IntArrayList walls = wallCells(room);
            for (int i = 0; i < walls.size(); i++) {
                if (random.nextFloat() < 0.15F) propHost(walls.getInt(i), RustworksBlocks.KAIRITE_ORE.get().defaultBlockState(), rustslate);
            }
        }

        void vine(int cell, int length) {
            IntArrayList strand = new IntArrayList();
            for (int current = cell; strand.size() < length && current >= 0 && carve[current] != NONE && !used[current]; current = below(current)) {
                strand.add(current);
            }
            if (strand.isEmpty()) return;
            BlockState body = ModBlocks.GLOWVINE_PLANT.get().defaultBlockState();
            for (int i = 0; i < strand.size() - 1; i++) put(strand.getInt(i), body);
            put(strand.getInt(strand.size() - 1), ModBlocks.GLOWVINE.get().defaultBlockState());
        }

        void details() {
            BlockState gravel = Blocks.GRAVEL.defaultBlockState();
            BlockState roots = Blocks.HANGING_ROOTS.defaultBlockState();
            for (int cell = 0; cell < carve.length; cell++) {
                byte type = carve[cell];
                if (type == NONE || type == ENTRANCE || used[cell]) continue;
                RoomType room = typeOf(cell);
                int x = x(cell), y = y(cell), z = z(cell);
                if (floor(cell)) {
                    int below = below(cell);
                    int under = below(below);
                    boolean bare = room == RoomType.PLAIN || room == RoomType.CAMP;
                    if (bare && !reserved[below] && under >= 0 && rock(under)
                            && noise.noise((ox + x) * 0.18, (oy + y) * 0.18 + 13.7, (oz + z) * 0.18) > 0.38) {
                        solid(below, gravel);
                    } else if (random.nextDouble() < 0.002) {
                        put(cell, QuantumBlocks.SCATTERED_BONES.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, randomHorizontal()));
                        continue;
                    }
                }
                if (ceiling(cell)) {
                    if (top[x * size + z] - y <= 14 && random.nextDouble() < 0.05) {
                        put(cell, roots);
                    } else if (type == CORRIDOR && random.nextDouble() < 0.006) {
                        vine(cell, 1 + random.nextInt(3));
                    }
                }
            }
        }

        void ores() {
            boolean[] taken = new boolean[carve.length];
            for (int i = 0; i < islandCount; i++) {
                int cell = island[i];
                if (carve[cell] != NONE || reserved[cell] || taken[cell] || !besideRoom(cell)) continue;
                if (random.nextDouble() >= settings.oreChance()) continue;
                long seed = random.nextLong();
                int size = 2 + random.nextInt(4);
                List<Integer> vein = new ArrayList<>(List.of(cell));
                taken[cell] = true;
                for (int grown = 1; grown < size; grown++) {
                    int from = vein.get(random.nextInt(vein.size()));
                    int s = random.nextInt(SIDES.length);
                    int nx = x(from) + SIDES[s][0], ny = y(from) + SIDES[s][1], nz = z(from) + SIDES[s][2];
                    if (!inside(nx, ny, nz)) continue;
                    int next = index(nx, ny, nz);
                    if (kind[next] != ISLAND || carve[next] != NONE || reserved[next] || taken[next]) continue;
                    taken[next] = true;
                    vein.add(next);
                }
                for (int block : vein) {
                    BlockPos pos = world(block);
                    network.props.computeIfAbsent(McChunks.key(pos.getX() >> 4, pos.getZ() >> 4), k -> new ArrayList<>())
                            .add(new Prop(pos.asLong(), null, null, true, seed, null));
                }
            }
        }

        boolean besideRoom(int cell) {
            int x = x(cell), y = y(cell), z = z(cell);
            for (int[] side : SIDES) {
                int nx = x + side[0], ny = y + side[1], nz = z + side[2];
                if (inside(nx, ny, nz) && carve[index(nx, ny, nz)] == ROOM) return true;
            }
            return false;
        }

        BlockPos world(int cell) {
            return new BlockPos(ox + x(cell), oy + y(cell), oz + z(cell));
        }

        void emit() {
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] == NONE) continue;
                BlockPos pos = world(cell);
                long key = pos.asLong();
                if (!network.gasSet.add(key)) continue;
                if (carve[cell] == ENTRANCE) network.entranceSet.add(key);
                network.gas.computeIfAbsent(McChunks.key(pos.getX() >> 4, pos.getZ() >> 4), k -> new LongArrayList()).add(key);
            }
        }

        void prop(BlockPos pos, BlockState state, boolean solidOnly) {
            add(new Prop(pos.asLong(), state, null, solidOnly, 0L, null));
        }

        void put(int cell, BlockState state) {
            prop(world(cell), state, false);
            used[cell] = true;
        }

        void solid(int cell, BlockState state) {
            prop(world(cell), state, true);
            reserved[cell] = true;
        }

        void propLoot(int cell, BlockState state, ResourceKey<LootTable> loot) {
            add(new Prop(world(cell).asLong(), state, loot, false, 0L, null));
            used[cell] = true;
        }

        void propHost(int cell, BlockState state, Predicate<BlockState> host) {
            add(new Prop(world(cell).asLong(), state, null, true, 0L, host));
            reserved[cell] = true;
        }

        void add(Prop prop) {
            BlockPos pos = BlockPos.of(prop.pos());
            network.props.computeIfAbsent(McChunks.key(pos.getX() >> 4, pos.getZ() >> 4), k -> new ArrayList<>()).add(prop);
        }

        static double sq(double v) {
            return v * v;
        }

        static int sq(int v) {
            return v * v;
        }

        <T> void shuffle(List<T> list) {
            for (int i = list.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                T t = list.get(i);
                list.set(i, list.get(j));
                list.set(j, t);
            }
        }
    }
}
