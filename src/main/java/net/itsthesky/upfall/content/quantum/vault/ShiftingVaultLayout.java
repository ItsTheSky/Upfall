package net.itsthesky.upfall.content.quantum.vault;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public final class ShiftingVaultLayout {
    public static final int CELLS = 7;
    public static final int PITCH = 3;
    public static final int SIZE_X = 24;
    public static final int SIZE_Y = 10;
    public static final int SIZE_Z = 29;
    public static final int FLOOR = 4;
    public static final int DOOR_X = 11;
    public static final int ENTRANCE_CELL_X = 3;
    public static final int ENTRANCE_CELL_Z = CELLS - 1;
    private static final int[][] CHEST_CELLS = {{0, 3}, {3, 0}, {6, 3}};
    private static final Direction[] CHEST_FACINGS = {Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final int INITIAL_STATE = 1;

    private final BlockPos origin;
    private final long seed;
    private final Long2ObjectMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
    private final List<BlockPos> chestSpots = new ArrayList<>();
    private final List<BlockPos> frost = new ArrayList<>();
    private final List<BlockPos> lamps = new ArrayList<>();
    private final List<BoundingBox> darkZones = new ArrayList<>();
    private final List<int[]> crumblingCells = new ArrayList<>();
    private final boolean[][] h = new boolean[CELLS][CELLS + 1];
    private final boolean[][] v = new boolean[CELLS + 1][CELLS];

    public ShiftingVaultLayout(BlockPos origin, long seed) {
        this.origin = origin;
        this.seed = seed;
        RandomSource random = RandomSource.create(seed);
        maze(random);
        build(random);
    }

    public BlockPos origin() {
        return origin;
    }

    public long seed() {
        return seed;
    }

    public Long2ObjectMap<BlockState> blocks() {
        return blocks;
    }

    public BlockState stateAt(BlockPos pos) {
        return blocks.get(pos.asLong());
    }

    public BoundingBox bounds() {
        return new BoundingBox(origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + SIZE_X - 1, origin.getY() + SIZE_Y - 1, origin.getZ() + SIZE_Z - 1);
    }

    public BoundingBox interior() {
        return new BoundingBox(origin.getX() + 1, origin.getY() + 1, origin.getZ() + 1,
                origin.getX() + SIZE_X - 2, origin.getY() + 7, origin.getZ() + SIZE_Z - 2);
    }

    public BlockPos door() {
        return at(DOOR_X, FLOOR + 1, SIZE_Z);
    }

    public BlockPos core() {
        return at(12, 0, 25);
    }

    public List<BlockPos> chestSpots() {
        return chestSpots;
    }

    public List<Direction> chestFacings() {
        return List.of(CHEST_FACINGS);
    }

    public List<BlockPos> frost() {
        return frost;
    }

    public List<BlockPos> lamps() {
        return lamps;
    }

    public List<BoundingBox> darkZones() {
        return darkZones;
    }

    public List<int[]> crumblingCells() {
        return crumblingCells;
    }

    public BlockPos at(int x, int y, int z) {
        return origin.offset(x, y, z);
    }

    public static int cellMin(int c) {
        return 2 + c * PITCH;
    }

    public static int wallLine(int k) {
        return 1 + k * PITCH;
    }

    public boolean wallNorth(int cx, int row) {
        return h[cx][row];
    }

    public boolean wallWest(int column, int cz) {
        return v[column][cz];
    }


    private void maze(RandomSource random) {
        for (boolean[] row : h) java.util.Arrays.fill(row, true);
        for (boolean[] row : v) java.util.Arrays.fill(row, true);
        boolean[][] seen = new boolean[CELLS][CELLS];
        List<int[]> stack = new ArrayList<>();
        stack.add(new int[]{ENTRANCE_CELL_X, ENTRANCE_CELL_Z});
        seen[ENTRANCE_CELL_X][ENTRANCE_CELL_Z] = true;
        int[][] steps = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        while (!stack.isEmpty()) {
            int[] cell = stack.get(stack.size() - 1);
            List<int[]> options = new ArrayList<>();
            for (int[] step : steps) {
                int nx = cell[0] + step[0];
                int nz = cell[1] + step[1];
                if (nx >= 0 && nz >= 0 && nx < CELLS && nz < CELLS && !seen[nx][nz]) options.add(new int[]{nx, nz});
            }
            if (options.isEmpty()) {
                stack.remove(stack.size() - 1);
                continue;
            }
            int[] next = options.get(random.nextInt(options.size()));
            open(cell[0], cell[1], next[0], next[1]);
            seen[next[0]][next[1]] = true;
            stack.add(next);
        }
        for (int i = 0; i < 7; i++) {
            int cx = random.nextInt(CELLS - 1);
            int cz = random.nextInt(CELLS - 1);
            if (random.nextBoolean()) open(cx, cz, cx + 1, cz);
            else open(cx, cz, cx, cz + 1);
        }
        h[ENTRANCE_CELL_X][CELLS] = false;
    }

    private void open(int ax, int az, int bx, int bz) {
        if (ax == bx) h[ax][Math.max(az, bz)] = false;
        else v[Math.max(ax, bx)][az] = false;
    }


    private void build(RandomSource random) {
        BlockState bricks = QuantumBlocks.VAULT_BRICKS.get().defaultBlockState();
        BlockState tiles = QuantumBlocks.VAULT_TILES.get().defaultBlockState();
        BlockState glass = QuantumBlocks.VAULT_GLASS.get().defaultBlockState();
        BlockState frostGlass = QuantumBlocks.FROST_GLASS.get().defaultBlockState();
        BlockState gas = QuantumBlocks.VEIL_GAS.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        fill(0, 0, 0, SIZE_X - 1, SIZE_Y - 1, SIZE_Z - 1, bricks);
        fill(1, 1, 1, 22, 3, 22, gas);
        fill(8, 1, 23, 15, 3, 27, gas);
        for (int i = 0; i <= CELLS; i++) {
            for (int j = 0; j <= CELLS; j++) fill(wallLine(i), 1, wallLine(j), wallLine(i), 3, wallLine(j), bricks);
        }
        fill(1, FLOOR, 1, 22, FLOOR, 22, tiles);
        fill(8, FLOOR, 23, 15, FLOOR, 27, tiles);

        fill(1, 5, 1, 22, 7, 22, air);
        for (int k = 0; k <= CELLS; k++) {
            for (int c = 0; c < CELLS; c++) {
                boolean centralSightline = c == ENTRANCE_CELL_X;
                if (h[c][k]) segment(cellMin(c), wallLine(k), true, k > 0 && k < CELLS && !centralSightline && random.nextFloat() < 0.18F ? frostGlass : glass);
                if (v[k][c]) segment(wallLine(k), cellMin(c), false, k > 0 && k < CELLS && random.nextFloat() < 0.18F ? frostGlass : glass);
            }
            for (int j = 0; j <= CELLS; j++) fill(wallLine(k), 5, wallLine(j), wallLine(k), 7, wallLine(j), glass);
        }
        fill(cellMin(ENTRANCE_CELL_X), 5, 22, cellMin(ENTRANCE_CELL_X) + 1, 7, 22, air);

        fill(8, 5, 23, 15, 7, 27, air);
        fill(DOOR_X, 5, 28, DOOR_X + 1, 7, 28, air);
        for (int y = 1; y <= 5; y++) {
            set(15, y, 27, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }

        for (int cx = 0; cx < CELLS; cx++) {
            for (int cz = 0; cz < CELLS; cz++) {
                if ((cx + cz) % 2 == 0 && !isChestCell(cx, cz)) lamp(cellMin(cx), 8, cellMin(cz));
            }
        }
        lamp(11, 8, 25);

        for (int i = 0; i < CHEST_CELLS.length; i++) {
            int cx = CHEST_CELLS[i][0];
            int cz = CHEST_CELLS[i][1];
            int x = cx == 0 ? cellMin(cx) : cx == CELLS - 1 ? cellMin(cx) + 1 : cellMin(cx);
            int z = cz == 0 ? cellMin(cz) : cellMin(cz);
            chestSpots.add(at(x, 5, z));
        }
        for (BlockPos spot : chestSpots) {
            BlockPos lamp = spot.above(3);
            set(lamp.getX() - origin.getX(), lamp.getY() - origin.getY(), lamp.getZ() - origin.getZ(), QuantumBlocks.VAULT_LAMP.get().defaultBlockState());
            if (!lamps.contains(lamp)) lamps.add(lamp);
        }
        BlockState chest = QuantumBlocks.SHIFTING_CHEST.get().defaultBlockState().setValue(ShiftingChestBlock.FACING, CHEST_FACINGS[INITIAL_STATE]);
        blocks.put(chestSpots.get(INITIAL_STATE).asLong(), chest);

        BlockState crumbling = QuantumBlocks.CRUMBLING_FLOOR.get().defaultBlockState();
        int patches = 0;
        for (int attempt = 0; attempt < 40 && patches < 5; attempt++) {
            int cx = random.nextInt(CELLS);
            int cz = random.nextInt(CELLS);
            if (isChestCell(cx, cz) || cx == ENTRANCE_CELL_X && cz >= CELLS - 2 || isCrumbling(cx, cz)) continue;
            crumblingCells.add(new int[]{cx, cz});
            fill(cellMin(cx), FLOOR, cellMin(cz), cellMin(cx) + 1, FLOOR, cellMin(cz) + 1, crumbling);
            patches++;
        }

        int zones = 0;
        for (int attempt = 0; attempt < 40 && zones < 2; attempt++) {
            int cx = random.nextInt(CELLS);
            int cz = 1 + random.nextInt(CELLS - 2);
            if (isChestCell(cx, cz) || isCrumbling(cx, cz)) continue;
            BlockPos min = at(cellMin(cx), 5, cellMin(cz));
            darkZones.add(new BoundingBox(min.getX(), min.getY(), min.getZ(), min.getX() + 1, min.getY() + 2, min.getZ() + 1));
            zones++;
        }

        set(12, 0, 25, QuantumBlocks.VAULT_CORE.get().defaultBlockState());
    }

    private boolean isChestCell(int cx, int cz) {
        for (int[] cell : CHEST_CELLS) {
            if (cell[0] == cx && cell[1] == cz) return true;
        }
        return false;
    }

    private boolean isCrumbling(int cx, int cz) {
        for (int[] cell : crumblingCells) {
            if (cell[0] == cx && cell[1] == cz) return true;
        }
        return false;
    }

    private void segment(int x, int z, boolean alongX, BlockState state) {
        for (int i = 0; i < 2; i++) {
            for (int y = 5; y <= 7; y++) {
                BlockPos pos = alongX ? at(x + i, y, z) : at(x, y, z + i);
                blocks.put(pos.asLong(), state);
                if (state.getBlock() instanceof FrostGlassBlock) frost.add(pos);
            }
        }
    }

    private void lamp(int x, int y, int z) {
        set(x, y, z, QuantumBlocks.VAULT_LAMP.get().defaultBlockState());
        lamps.add(at(x, y, z));
    }

    private void set(int x, int y, int z, BlockState state) {
        blocks.put(at(x, y, z).asLong(), state);
    }

    private void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) set(x, y, z, state);
            }
        }
    }
}
