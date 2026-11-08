package net.itsthesky.upfall.content.quantum.cave;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.itsthesky.upfall.compat.mc.McChunks;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class GasCaveNetwork {
    public static final GasCaveNetwork EMPTY = new GasCaveNetwork();

    public enum Entrance { CLIFF, CHIMNEY, SPIRAL }

    public record Settings(int minY, int maxY, int gridSize, int rooms, double roomRadius, int shell, double loops,
                           double sinuosity, double corridorRadius, double entranceRadius, double markerChance, double clusterChance) {}

    public record Stats(int islandBlocks, int carved, int roomBlocks, int entranceBlocks, int linkBlocks, int rooms,
                        double accessible, int closedPockets, int openBlocks, int breaches, Map<Entrance, Integer> openByEntrance) {}

    public record Prop(long pos, BlockState state, ResourceKey<LootTable> loot, boolean solidOnly) {}

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

    public static GasCaveNetwork build(ChunkPos source, RandomSource random, DensityFunction density, Settings settings) {
        Builder builder = new Builder(new GasCaveNetwork(), source, random, density, settings);
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

        private static final int MAX_HEIGHT = 128;
        private static final int MIN_RUN = 14;
        private static final int MIN_ISLAND = 2000;
        private static final int MIN_POCKET = 8;
        private static final int CELL_WIDTH = 4;
        private static final int CELL_HEIGHT = 8;
        private static final int[][] SIDES = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        private static final Direction[] SIDE_DIRS = {Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        private static final int[] SECTOR = {1, 3, 4};

        final GasCaveNetwork network;
        final ChunkPos source;
        final RandomSource random;
        final DensityFunction density;
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

        record Room(double x, double y, double z, double rx, double ry, double rz) {}

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

        Builder(GasCaveNetwork network, ChunkPos source, RandomSource random, DensityFunction density, Settings settings) {
            this.network = network;
            this.source = source;
            this.random = random;
            this.density = GasCaveNetworks.columnCached(density);
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
            planEntrances();
            if (entrances.isEmpty()) return false;
            placeRooms();
            if (rooms.isEmpty()) return false;
            for (Room room : rooms) carveRoom(room);
            corridors();
            for (Shape shape : entrances) carveEntrance(shape);
            connect();
            linkPockets();
            Stats stats = stats();
            if (stats.openBlocks() == 0) return false;
            network.stats = stats;
            markers();
            clusters();
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
            double base = settings.roomRadius();
            int shell = settings.shell();
            for (int attempt = 0; attempt < attempts && rooms.size() < settings.rooms(); attempt++) {
                int cell = island[random.nextInt(islandCount)];
                if (depth[cell] <= shell) continue;
                double rx = base * (0.75 + random.nextDouble() * 0.5);
                double ry = Math.max(1.6, rx * 0.6);
                double rz = rx * (0.8 + random.nextDouble() * 0.4);
                double cx = x(cell) + 0.5, cy = y(cell) + 0.5, cz = z(cell) + 0.5;
                if (!deep(cx + rx, cy, cz) || !deep(cx - rx, cy, cz) || !deep(cx, cy + ry, cz) || !deep(cx, cy - ry, cz)
                        || !deep(cx, cy, cz + rz) || !deep(cx, cy, cz - rz)) continue;
                boolean clear = true;
                for (Room other : rooms) {
                    if (Math.sqrt(sq(cx - other.x) + sq(1.4 * (cy - other.y)) + sq(cz - other.z)) < 1.2 * (rx + other.rx)) {
                        clear = false;
                        break;
                    }
                }
                if (clear) rooms.add(new Room(cx, cy, cz, rx, ry, rz));
            }
        }

        boolean deep(double x, double y, double z) {
            int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
            if (!inside(bx, by, bz)) return false;
            int cell = index(bx, by, bz);
            return kind[cell] == ISLAND && depth[cell] > settings.shell();
        }

        void carveRoom(Room room) {
            int rx = Mth.ceil(room.rx * 1.3), ry = Mth.ceil(room.ry * 1.3), rz = Mth.ceil(room.rz * 1.3);
            int cx = Mth.floor(room.x), cy = Mth.floor(room.y), cz = Mth.floor(room.z);
            for (int x = cx - rx; x <= cx + rx; x++) {
                for (int y = cy - ry; y <= cy + ry; y++) {
                    for (int z = cz - rz; z <= cz + rz; z++) {
                        if (!inside(x, y, z)) continue;
                        int cell = index(x, y, z);
                        if (kind[cell] != ISLAND || depth[cell] <= settings.shell() || carve[cell] != NONE) continue;
                        double e = Math.sqrt(sq((x + 0.5 - room.x) / room.rx) + sq((y + 0.5 - room.y) / room.ry) + sq((z + 0.5 - room.z) / room.rz));
                        double wobble = noise.noise((ox + x) * 0.22, (oy + y) * 0.22, (oz + z) * 0.22);
                        if (e < 1.0 + 0.28 * wobble) carve[cell] = ROOM;
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
            tube(a.x, a.y, a.z, b.x, b.y, b.z, settings.corridorRadius(), settings.sinuosity(), ROOM);
        }

        void tube(double ax, double ay, double az, double bx, double by, double bz, double radius, double sinuosity, byte type) {
            double dx = bx - ax, dy = by - ay, dz = bz - az;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0E-3) {
                sphere(ax, ay, az, radius, type);
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
                sphere(ax + dx * f + px * offset, ay + dy * f, az + dz * f + pz * offset, radius, type);
            }
            sphere(bx, by, bz, radius, type);
        }

        void sphere(double cx, double cy, double cz, double radius, byte type) {
            int r = Mth.ceil(radius);
            int bx = Mth.floor(cx), by = Mth.floor(cy), bz = Mth.floor(cz);
            for (int x = bx - r; x <= bx + r; x++) {
                for (int y = by - r; y <= by + r; y++) {
                    for (int z = bz - r; z <= bz + r; z++) {
                        if (!inside(x, y, z)) continue;
                        int cell = index(x, y, z);
                        if (kind[cell] != ISLAND || depth[cell] < 2 || carve[cell] != NONE) continue;
                        if (sq(x + 0.5 - cx) + sq(y + 0.5 - cy) + sq(z + 0.5 - cz) <= radius * radius) carve[cell] = type;
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
                link(from, labels, largest, 1.375, 0.3);
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
            tube(x(from) + 0.5, y(from) + 0.5, z(from) + 0.5, x(target) + 0.5, y(target) + 0.5, z(target) + 0.5, 1.4, 0.3, LINK);
            path(from, cell -> carve[cell] != NONE && labels[cell] >= 0 && open[labels[cell]]);
        }

        void link(int from, int[] labels, int target, double radius, double sinuosity) {
            int to = nearest(from, cell -> labels[cell] == target);
            if (to < 0) return;
            tube(x(from) + 0.5, y(from) + 0.5, z(from) + 0.5, x(to) + 0.5, y(to) + 0.5, z(to) + 0.5, radius, sinuosity, LINK);
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
                if (carve[cell] == NONE) sphere(x(cell) + 0.5, y(cell) + 0.5, z(cell) + 0.5, 1.1, LINK);
            }
        }

        Stats stats() {
            int[] labels = new int[carve.length];
            int[] sizes = components(labels);
            boolean[] open = openComponents(labels, sizes.length);
            int carved = 0, roomBlocks = 0, entranceBlocks = 0, linkBlocks = 0, accessible = 0, openBlocks = 0, breaches = 0;
            Map<Entrance, Integer> byEntrance = new EnumMap<>(Entrance.class);
            for (int cell = 0; cell < carve.length; cell++) {
                byte type = carve[cell];
                if (type == NONE) continue;
                carved++;
                switch (type) {
                    case ROOM -> roomBlocks++;
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
            return new Stats(islandCount, carved, roomBlocks, entranceBlocks, linkBlocks, rooms.size(),
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
                if (touching && random.nextDouble() < settings.markerChance()) prop(world(cell), mistglow, true);
            }
        }

        void clusters() {
            BlockState cluster = QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState();
            for (int cell = 0; cell < carve.length; cell++) {
                if (carve[cell] == NONE || carve[cell] == ENTRANCE) continue;
                if (random.nextDouble() >= settings.clusterChance()) continue;
                int x = x(cell), y = y(cell), z = z(cell);
                int first = random.nextInt(SIDES.length);
                for (int i = 0; i < SIDES.length; i++) {
                    int s = (first + i) % SIDES.length;
                    int nx = x + SIDES[s][0], ny = y + SIDES[s][1], nz = z + SIDES[s][2];
                    if (!inside(nx, ny, nz)) continue;
                    int wall = index(nx, ny, nz);
                    if (kind[wall] != ISLAND || carve[wall] != NONE) continue;
                    prop(world(cell), cluster.setValue(FlickerShardClusterBlock.FACING, SIDE_DIRS[s].getOpposite()), false);
                    break;
                }
            }
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
            network.props.computeIfAbsent(McChunks.key(pos.getX() >> 4, pos.getZ() >> 4), k -> new ArrayList<>())
                    .add(new Prop(pos.asLong(), state, null, solidOnly));
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
