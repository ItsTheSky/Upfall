package net.itsthesky.upfall.content.reliquary.worldgen;

import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.content.mobs.MobEntities;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Beam;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Cube;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Exit;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Fragment;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Interior;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Link;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Plan;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Route;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Segment;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Shape;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Stone;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Style;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Tilt;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryLayout.Trail;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ReliquaryBlueprint {
    private record Cache(BlockPos pos, ResourceKey<LootTable> lootTable) {}

    private record Spawner(BlockPos pos, Supplier<? extends EntityType<?>> type) {}

    private record Tether(BlockPos anchor, BlockPos other) {}

    private record Column(int x, int z, int top) {}

    private static final double SQRT2 = ReliquaryLayout.SQRT2;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final long seed;
    private final BlockPos origin;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Cache> caches = new ArrayList<>();
    private final List<Spawner> spawners = new ArrayList<>();
    private final List<Tether> tethers = new ArrayList<>();
    private Column column;
    private Map<Long, List<BlockPos>> byChunk;

    private ReliquaryBlueprint(long seed, BlockPos origin) {
        this.seed = seed;
        this.origin = origin;
    }

    public static ReliquaryBlueprint cube(Plan plan, int index, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        bp.buildCube(plan, index, RandomSource.create(seed));
        return bp;
    }

    public static ReliquaryBlueprint link(Plan plan, int index, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        bp.buildLink(plan.links().get(index));
        return bp;
    }

    public static ReliquaryBlueprint debris(Plan plan, int index, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        for (Beam beam : plan.debris().get(index)) {
            bp.beam(beam, List.of());
        }
        return bp;
    }

    public static ReliquaryBlueprint fragments(Plan plan, int index, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        bp.buildFragments(plan, plan.fragments().get(index));
        return bp;
    }

    public static ReliquaryBlueprint approach(Plan plan, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        if (plan.approach() != null) bp.buildApproach(plan.approach());
        return bp;
    }

    public static ReliquaryBlueprint trail(Plan plan, int index, long seed) {
        ReliquaryBlueprint bp = new ReliquaryBlueprint(seed, plan.origin());
        Trail trail = plan.trails().get(index);
        for (Stone stone : trail.stones()) {
            bp.stone(stone);
        }
        return bp;
    }

    public Map<BlockPos, BlockState> blocks() {
        return blocks;
    }

    public int cacheCount() {
        return caches.size();
    }

    public boolean hasCache(ResourceKey<LootTable> lootTable) {
        return caches.stream().anyMatch(cache -> cache.lootTable() == lootTable);
    }

    public int spawnerCount() {
        return spawners.size();
    }

    public int tetherCount() {
        return tethers.size();
    }

    private void set(int x, int y, int z, BlockState state) {
        if (!ReliquaryLayout.withinReach(origin, x, z, ReliquaryLayout.MAX_REACH) || y < UpfallTerrain.MIN_Y + 1
                || y >= UpfallTerrain.MIN_Y + UpfallTerrain.HEIGHT) return;
        blocks.put(new BlockPos(x, y, z), state);
    }

    private void set(BlockPos pos, BlockState state) {
        set(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    private @Nullable BlockState get(int x, int y, int z) {
        return blocks.get(new BlockPos(x, y, z));
    }

    private double noise(int x, int y, int z) {
        long h = Mth.getSeed(x, y, z) ^ seed * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 31)) * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h >>> 11) * 0x1.0p-53;
    }

    private static BlockState beamState(Direction.Axis axis) {
        return ReliquaryBlocks.LATTICE_BEAM.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState lattice() {
        return ReliquaryBlocks.LATTICE_STONE.get().defaultBlockState();
    }

    private static BlockState carved() {
        return ReliquaryBlocks.CARVED_LATTICE_STONE.get().defaultBlockState();
    }

    private BlockState edgeStone(int x, int y, int z, boolean mossy) {
        double n = noise(x, y, z);
        if (n < 0.1) return ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
        if (n < 0.15) return Blocks.CALCITE.defaultBlockState();
        if (n < 0.18) return Blocks.SMOOTH_QUARTZ.defaultBlockState();
        if (mossy && n < 0.45) return ModBlocks.MOSSY_PEARLSTONE_BRICKS.get().defaultBlockState();
        return lattice();
    }

    private static boolean cut(List<Segment> cuts, int x, int y, int z) {
        for (Segment segment : cuts) {
            if (segment.near(x, y, z, 0.0)) return true;
        }
        return false;
    }

    private static List<Segment> cutsNear(Plan plan, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<Segment> cuts = new ArrayList<>();
        for (Segment s : plan.corridors()) {
            double r = s.radius();
            if (Math.max(s.ax(), s.bx()) + r < minX || Math.min(s.ax(), s.bx()) - r > maxX) continue;
            if (Math.max(s.ay(), s.by()) + r < minY || Math.min(s.ay(), s.by()) - r > maxY) continue;
            if (Math.max(s.az(), s.bz()) + r < minZ || Math.min(s.az(), s.bz()) - r > maxZ) continue;
            cuts.add(s);
        }
        return cuts;
    }

    private void buildCube(Plan plan, int index, RandomSource random) {
        Cube c = plan.cubes().get(index);
        int reach = (int) Math.ceil(c.reach()) + 1;
        int span = c.half() + 7;
        List<Segment> cuts = cutsNear(plan, c.cx() - Math.max(span, reach), c.cy() - reach, c.cz() - Math.max(span, reach),
                c.cx() + Math.max(span, reach), c.cy() + reach, c.cz() + Math.max(span, reach));
        frame(c, cuts);
        deck(c, plan);
        switch (c.interior()) {
            case CORE -> core(c, plan, random);
            case ARCHIVE -> archive(c, plan);
            case HANGING_GARDEN -> garden(c, plan, random);
            case ECHO_HALL -> echo(c);
            case COLLAPSED -> rubble(c, plan);
            case SHARD_VAULT -> vault(c, plan);
        }
        if (c.shard()) {
            cache(c.cx(), c.deckY() + 1, c.cz(), ReliquaryStructures.SATELLITE_LOOT);
        }
        for (Beam beam : plan.protrusions().get(index)) {
            beam(beam, cuts);
        }
        lanterns(c, random);
    }

    private void frame(Cube c, List<Segment> cuts) {
        int h = c.half();
        double t = c.thickness();
        int thick = c.frameBlocks();
        int reach = (int) Math.ceil(h * SQRT2);
        int deck = c.deckY();
        boolean ribbed = c.style() == Style.RIBBED;
        boolean collapsed = c.interior() == Interior.COLLAPSED;
        boolean mossy = c.interior() == Interior.HANGING_GARDEN;
        double cu = Math.cos(c.collapse());
        double cv = Math.sin(c.collapse());
        int hi = c.innerHalf();
        int echoHalf = c.interior() == Interior.ECHO_HALL ? Math.max(3, (int) Math.round(hi * 0.55)) : 0;
        int extendTop = 2 + (int) Math.floor(noise(c.cx(), c.cy(), c.cz()) * 4);
        int extendBottom = 2 + (int) Math.floor(noise(c.cz(), c.cx(), c.cy()) * 4);
        int topSide = noise(c.cx(), c.cz(), c.cy()) < 0.5 ? 1 : -1;
        Direction.Axis axis = c.axis();
        for (int a = -h - 6; a <= h + 6; a++) {
            for (int dy = -reach; dy <= reach; dy++) {
                int y = c.cy() + dy;
                for (int p = -reach; p <= reach; p++) {
                    int x = c.worldX(a, p);
                    int z = c.worldZ(a, p);
                    BlockState state = null;
                    if (Math.abs(a) <= h) {
                        state = outer(a, dy, p, h, t, thick, x, y, z, deck, ribbed, collapsed, mossy, cu, cv, axis);
                    }
                    if (state == null && hi > 0 && (y < deck + 1 || y > deck + 4)) {
                        state = inner(a - c.innerA(), dy - c.innerDy(), p - c.innerP(), hi, a, h, extendTop, extendBottom, topSide, axis, mossy);
                    }
                    if (state == null && echoHalf > 0 && (y < deck + 1 || y > deck + 4)) {
                        state = inner(a + c.innerA(), dy + c.innerDy() + 2, p + c.innerP(), echoHalf, a, -1, 0, 0, 0, axis, false);
                    }
                    if (state == null || cut(cuts, x, y, z)) continue;
                    set(x, y, z, state);
                }
            }
        }
    }

    private @Nullable BlockState outer(int a, int dy, int p, int h, double t, int thick, int x, int y, int z, int deck, boolean ribbed,
                                       boolean collapsed, boolean mossy, double cu, double cv, Direction.Axis axis) {
        double u = (dy + p) / SQRT2;
        double v = (dy - p) / SQRT2;
        if (Math.abs(u) > h || Math.abs(v) > h) return null;
        boolean ea = Math.abs(a) > h - thick;
        boolean eu = Math.abs(u) > h - t;
        boolean ev = Math.abs(v) > h - t;
        int count = (ea ? 1 : 0) + (eu ? 1 : 0) + (ev ? 1 : 0);
        boolean gone = collapsed && (u * cu + v * cv) > 0.2 * h && noise(x, y, z) > 0.15;
        if (count >= 2) {
            if (gone) return null;
            if (count == 3) return carved();
            if (noise(x, y + 3, z) < 0.05) return null;
            if (eu && ev) return beamState(axis);
            return edgeStone(x, y, z, mossy);
        }
        if (!ribbed || gone || (y >= deck - 1 && y <= deck + 5)) return null;
        if (noise(x, y + 7, z) < 0.08) return null;
        if (Math.abs(u) > h - 1.2 && Math.floorMod((int) Math.floor(v + h), 3) == 0) return beamState(axis);
        if (Math.abs(v) > h - 1.2 && Math.floorMod((int) Math.floor(u + h), 3) == 0) return beamState(axis);
        if (Math.abs(a) == h && Math.floorMod((int) Math.floor(u + h), 4) == 0 && Math.abs(v) < h - t) return lattice();
        return null;
    }

    private @Nullable BlockState inner(int a, int dy, int p, int hi, int outerA, int outerHalf, int extendTop, int extendBottom, int topSide,
                                       Direction.Axis axis, boolean mossy) {
        double u = (dy + p) / SQRT2;
        double v = (dy - p) / SQRT2;
        if (Math.abs(u) > hi || Math.abs(v) > hi) return null;
        boolean eu = Math.abs(u) > hi - 1.8;
        boolean ev = Math.abs(v) > hi - 1.8;
        if (Math.abs(a) > hi) {
            if (!eu || !ev || outerHalf < 0) return null;
            boolean top = u > 0 && v > 0 && Integer.signum(a) == topSide && Math.abs(outerA) <= outerHalf + extendTop;
            boolean bottom = u < 0 && v < 0 && Integer.signum(a) == -topSide && Math.abs(outerA) <= outerHalf + extendBottom;
            return top || bottom ? beamState(axis) : null;
        }
        boolean ea = Math.abs(a) > hi - 2;
        int count = (ea ? 1 : 0) + (eu ? 1 : 0) + (ev ? 1 : 0);
        if (count < 2) return null;
        if (count == 3) return ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState();
        if (eu && ev) return beamState(axis);
        return mossy ? ModBlocks.MOSSY_PEARLSTONE_BRICKS.get().defaultBlockState() : ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
    }

    private void deck(Cube c, Plan plan) {
        int h = c.half();
        int w = c.deckWidth();
        int y = c.deckY();
        boolean garden = c.interior() == Interior.HANGING_GARDEN;
        boolean collapsed = c.interior() == Interior.COLLAPSED;
        for (int a = -h; a <= h; a++) {
            for (int p = -w; p <= w; p++) {
                int x = c.worldX(a, p);
                int z = c.worldZ(a, p);
                boolean border = Math.abs(a) == h || Math.abs(p) == w;
                double n = noise(x, y, z);
                if (collapsed && !border && n < 0.12 && Math.abs(p) > 2 && !walkway(plan, x, y, z)) continue;
                BlockState state;
                if (border) state = ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
                else if (p == 0) state = Blocks.SMOOTH_QUARTZ.defaultBlockState();
                else if (a % 6 == 0 && p % 6 == 0) state = carved();
                else if (garden && n < 0.4) state = ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState();
                else state = lattice();
                set(x, y, z, state);
                if (p % 5 == 0 && Math.abs(p) < w && noise(x, y - 1, z) > 0.1) {
                    set(x, y - 1, z, beamState(c.axis()));
                }
            }
        }
    }

    private static boolean walkway(Plan plan, int x, int y, int z) {
        for (Segment segment : plan.corridors()) {
            if (segment.near(x + 0.5, y + 1.5, z + 0.5, 0.0) || segment.near(x, y + 2.5, z, 0.0)) return true;
        }
        return false;
    }

    private boolean freeSpot(Cube c, Plan plan, int a, int p) {
        if (Math.abs(a) > c.half() - 1 || Math.abs(p) > c.deckWidth() - 1) return false;
        int x = c.worldX(a, p);
        int z = c.worldZ(a, p);
        int y = c.deckY();
        BlockState floor = get(x, y, z);
        BlockState here = get(x, y + 1, z);
        return floor != null && !floor.isAir() && (here == null || here.isAir()) && !walkway(plan, x, y, z);
    }

    private void cache(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        set(x, y, z, ModBlocks.SKY_CACHE.get().defaultBlockState());
        caches.add(new Cache(new BlockPos(x, y, z), lootTable));
    }

    private void core(Cube c, Plan plan, RandomSource random) {
        int cx = c.cx();
        int cz = c.cz();
        int y0 = c.deckY();
        BlockState seal = ReliquaryBlocks.FOLD_SEAL.get().defaultBlockState();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                for (int dy = 0; dy <= 8; dy++) {
                    boolean shell = Math.abs(dx) == 5 || Math.abs(dz) == 5 || dy == 0 || dy == 8;
                    set(cx + dx, y0 + dy, cz + dz, shell ? seal : AIR);
                }
            }
        }
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                for (int dy = 1; dy <= 7; dy++) {
                    set(cx + sx * 4, y0 + dy, cz + sz * 4, dy == 7 ? carved() : beamState(Direction.Axis.Y));
                }
            }
        }
        cache(cx, y0 + 1, cz, ReliquaryStructures.CORE_LOOT);
        set(cx, y0 + 7, cz, HamletBlocks.CLOUD_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int x = cx + dir.getStepX() * 8;
            int z = cz + dir.getStepZ() * 8;
            set(x, y0, z, ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
            set(x, y0 + 1, z, ReliquaryBlocks.FOLD_SOCKET.get().defaultBlockState());
            Direction side = dir.getClockWise();
            for (int s = -1; s <= 1; s += 2) {
                set(x + side.getStepX() * s, y0, z + side.getStepZ() * s, Blocks.SMOOTH_QUARTZ.defaultBlockState());
            }
        }
        int placed = 0;
        int h = c.half();
        for (int attempt = 0; attempt < 40 && placed < 2; attempt++) {
            int a = random.nextInt(2 * h - 3) - (h - 2);
            int p = random.nextInt(2 * c.deckWidth() - 3) - (c.deckWidth() - 2);
            if (Math.abs(a) < 10 && Math.abs(p) < 10 || !freeSpot(c, plan, a, p)) continue;
            cache(c.worldX(a, p), y0 + 1, c.worldZ(a, p), ReliquaryStructures.ARCHIVE_LOOT);
            placed++;
        }
    }

    private void archive(Cube c, Plan plan) {
        int y = c.deckY();
        int h = c.half();
        for (int p = 3; p <= c.deckWidth() - 2; p += 3) {
            for (int sign = -1; sign <= 1; sign += 2) {
                for (int a = -(h - 3); a <= h - 3; a++) {
                    int sp = sign * p;
                    if (!freeSpot(c, plan, a, sp)) continue;
                    int x = c.worldX(a, sp);
                    int z = c.worldZ(a, sp);
                    if (Math.floorMod(a, 4) == 0) {
                        set(x, y + 1, z, lattice());
                        set(x, y + 2, z, lattice());
                        set(x, y + 3, z, carved());
                    } else {
                        double n = noise(x, y + 1, z);
                        if (n < 0.12) continue;
                        set(x, y + 1, z, n > 0.9 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
                        if (noise(x, y + 2, z) > 0.25) set(x, y + 2, z, Blocks.BOOKSHELF.defaultBlockState());
                        if (noise(x, y + 3, z) > 0.6) set(x, y + 3, z, lattice());
                    }
                }
            }
        }
        Direction facing = c.axisDirection(1);
        set(c.worldX(-2, 0), y + 1, c.worldZ(-2, 0), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, facing));
        if (h > 9) {
            set(c.worldX(-5, 0), y + 1, c.worldZ(-5, 0), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, facing.getOpposite()));
        }
        cache(c.worldX(2, 0), y + 1, c.worldZ(2, 0), ReliquaryStructures.ARCHIVE_LOOT);
    }

    private void garden(Cube c, Plan plan, RandomSource random) {
        int y = c.deckY();
        int h = c.half();
        int w = c.deckWidth();
        for (int a = -h + 1; a <= h - 1; a++) {
            for (int p = -w + 1; p <= w - 1; p++) {
                int x = c.worldX(a, p);
                int z = c.worldZ(a, p);
                BlockState floor = get(x, y, z);
                if (floor == null || !floor.is(ModBlocks.SKYGRASS_BLOCK.get()) || !freeSpot(c, plan, a, p)) continue;
                double n = noise(x, y + 1, z);
                if (n < 0.15) set(x, y + 1, z, ModBlocks.DUSKBLOOM.get().defaultBlockState());
                else if (n < 0.4) set(x, y + 1, z, ModBlocks.CLOUD_FERN.get().defaultBlockState());
                else if (n < 0.55) set(x, y + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
                else if (n > 0.97) {
                    BlockState leaves = ModBlocks.SKYREACH.leaves().get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    set(x, y + 1, z, leaves);
                    if (noise(x, y + 2, z) > 0.5) set(x, y + 2, z, leaves);
                }
            }
        }
        int vines = 8 + random.nextInt(8);
        for (int i = 0; i < vines; i++) {
            int a = random.nextInt(2 * h - 1) - (h - 1);
            int p = random.nextInt(2 * w - 1) - (w - 1);
            int x = c.worldX(a, p);
            int z = c.worldZ(a, p);
            BlockState above = get(x, y, z);
            if (above == null || above.isAir()) continue;
            int top = get(x, y - 1, z) == null ? y - 1 : y - 2;
            hangGlowvine(x, top, z, 2 + random.nextInt(4));
        }
    }

    private void hangGlowvine(int x, int top, int z, int length) {
        for (int i = 0; i < length; i++) {
            if (get(x, top - i, z) != null) return;
        }
        for (int i = 0; i < length - 1; i++) {
            set(x, top - i, z, ModBlocks.GLOWVINE_PLANT.get().defaultBlockState());
        }
        set(x, top - length + 1, z, ModBlocks.GLOWVINE.get().defaultBlockState());
    }

    private void echo(Cube c) {
        int y = c.deckY();
        int x = c.worldX(0, 3);
        int z = c.worldZ(0, 3);
        set(x, y, z, carved());
        set(x, y + 1, z, Blocks.SPAWNER.defaultBlockState());
        spawners.add(new Spawner(new BlockPos(x, y + 1, z), MobEntities.DUSKWING));
    }

    private void rubble(Cube c, Plan plan) {
        int y = c.deckY();
        for (int a = -c.half() + 1; a <= c.half() - 1; a++) {
            for (int p = -c.deckWidth() + 1; p <= c.deckWidth() - 1; p++) {
                int x = c.worldX(a, p);
                int z = c.worldZ(a, p);
                double n = noise(x, y + 1, z);
                if (n < 0.9 || !freeSpot(c, plan, a, p)) continue;
                set(x, y + 1, z, n > 0.97 ? ModBlocks.CRACKED_PEARLSTONE_BRICKS.get().defaultBlockState()
                        : n > 0.94 ? beamState(c.axis()) : ModBlocks.COBBLED_PEARLSTONE.get().defaultBlockState());
            }
        }
    }

    private void vault(Cube c, Plan plan) {
        int y = c.deckY();
        for (int a = -3; a <= 3; a += 3) {
            for (int p = -3; p <= 3; p += 3) {
                if (a == 0 && p == 0 || !freeSpot(c, plan, a, p)) continue;
                int x = c.worldX(a, p);
                int z = c.worldZ(a, p);
                set(x, y + 1, z, Blocks.QUARTZ_PILLAR.defaultBlockState());
                set(x, y + 2, z, Blocks.QUARTZ_PILLAR.defaultBlockState());
                set(x, y + 3, z, carved());
            }
        }
        set(c.cx(), y, c.cz(), ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
    }

    private void lanterns(Cube c, RandomSource random) {
        int y = c.deckY();
        int count = 2 + c.half() / 6;
        for (int i = 0; i < count; i++) {
            int a = random.nextInt(2 * c.half() - 1) - (c.half() - 1);
            int p = random.nextInt(2 * c.deckWidth() - 1) - (c.deckWidth() - 1);
            int x = c.worldX(a, p);
            int z = c.worldZ(a, p);
            BlockState above = get(x, y, z);
            if (above == null || above.isAir() || get(x, y - 1, z) != null) continue;
            set(x, y - 1, z, HamletBlocks.CLOUD_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }

    private void beam(Beam beam, List<Segment> cuts) {
        List<BlockPos> voxels = beam.voxels();
        boolean capped = noise(beam.start().getX(), beam.start().getY(), beam.start().getZ()) < 0.3;
        BlockState body = beamState(beam.direction().getAxis());
        for (int i = 0; i < voxels.size(); i++) {
            BlockPos pos = voxels.get(i);
            if (cut(cuts, pos.getX(), pos.getY(), pos.getZ())) continue;
            boolean end = i >= voxels.size() - beam.width();
            set(pos, capped && end ? carved() : body);
        }
    }

    private void buildFragments(Plan plan, List<Fragment> group) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Fragment f : group) {
            int r = f.radius();
            minX = Math.min(minX, f.center().getX() - r);
            minY = Math.min(minY, f.center().getY() - r);
            minZ = Math.min(minZ, f.center().getZ() - r);
            maxX = Math.max(maxX, f.center().getX() + r);
            maxY = Math.max(maxY, f.center().getY() + r);
            maxZ = Math.max(maxZ, f.center().getZ() + r);
        }
        List<Segment> cuts = cutsNear(plan, minX, minY, minZ, maxX, maxY, maxZ);
        for (Fragment f : group) {
            fragment(f, cuts);
        }
    }

    private static double[] local(Tilt tilt, Direction.Axis axis, int dx, int dy, int dz) {
        double a0 = axis == Direction.Axis.X ? dx : dz;
        double p0 = axis == Direction.Axis.X ? dz : dx;
        return switch (tilt) {
            case EDGE -> new double[]{a0, (dy + p0) / SQRT2, (dy - p0) / SQRT2};
            case FLAT -> new double[]{a0, dy, p0};
            case DIAMOND -> new double[]{(a0 + p0) / SQRT2, dy, (a0 - p0) / SQRT2};
        };
    }

    private static int mask(Tilt tilt, double[] l, double h) {
        if (Math.abs(l[0]) > h || Math.abs(l[1]) > h || Math.abs(l[2]) > h) return -1;
        double ta = tilt == Tilt.DIAMOND ? 1.45 : 1.0;
        double tu = tilt == Tilt.EDGE ? 1.45 : 1.0;
        double tv = tilt == Tilt.FLAT ? 1.0 : 1.45;
        return (Math.abs(l[0]) > h - ta ? 1 : 0) | (Math.abs(l[1]) > h - tu ? 2 : 0) | (Math.abs(l[2]) > h - tv ? 4 : 0);
    }

    private static @Nullable Direction.Axis lineAxis(Tilt tilt, Direction.Axis axis, int mask) {
        return switch (mask) {
            case 6 -> tilt == Tilt.DIAMOND ? null : axis;
            case 5 -> tilt == Tilt.EDGE ? null : Direction.Axis.Y;
            case 3 -> tilt == Tilt.FLAT ? (axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X) : null;
            default -> null;
        };
    }

    private BlockState fragmentEdge(int palette, @Nullable Direction.Axis axis, int x, int y, int z) {
        if (axis != null && palette != 2) return beamState(axis);
        double n = noise(x, y + 11, z);
        return switch (palette) {
            case 0 -> edgeStone(x, y, z, false);
            case 1 -> n < 0.25 ? ModBlocks.CRACKED_PEARLSTONE_BRICKS.get().defaultBlockState() : ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
            case 2 -> n < 0.5 ? Blocks.CALCITE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            default -> n < 0.3 ? carved() : ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
        };
    }

    private static BlockState fragmentCorner(int palette) {
        return switch (palette) {
            case 0 -> carved();
            case 1 -> ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState();
            case 2 -> ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
            default -> lattice();
        };
    }

    private static BlockState fragmentFill(int palette) {
        return switch (palette) {
            case 0 -> lattice();
            case 1 -> ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
            case 2 -> Blocks.CALCITE.defaultBlockState();
            default -> ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
        };
    }

    private void fragment(Fragment f, List<Segment> cuts) {
        int r = f.radius();
        int h = f.half();
        int v = f.variant();
        int palette = v & 3;
        int cx = f.center().getX();
        int cy = f.center().getY();
        int cz = f.center().getZ();
        int face = Math.floorMod(v >> 2, 3);
        double faceSign = (v >> 5 & 1) == 0 ? 1.0 : -1.0;
        double cutoff = h * (-0.1 + ((v >> 7) & 3) * 0.15);
        int spikes = ((v >> 9) & 15) | (1 << ((v >> 13) & 3));
        Tilt innerTilt = f.tilt() == Tilt.FLAT ? Tilt.EDGE : Tilt.FLAT;
        int innerHalf = Math.max(1, (int) Math.round(h * (innerTilt == Tilt.FLAT ? 0.45 : 0.4)));
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    double[] l = local(f.tilt(), f.axis(), dx, dy, dz);
                    BlockState state = fragmentState(f, l, x, y, z, palette, face, faceSign, cutoff, spikes);
                    if (state == null && f.shape() == Shape.NESTED) {
                        int m = mask(innerTilt, local(innerTilt, f.axis(), dx, dy, dz), innerHalf);
                        if (Integer.bitCount(Math.max(m, 0)) >= 2) {
                            state = m == 7 ? fragmentCorner((palette + 1) & 3) : fragmentFill((palette + 2) & 3);
                        }
                    }
                    if (state == null || cut(cuts, x, y, z)) continue;
                    set(x, y, z, state);
                }
            }
        }
    }

    private @Nullable BlockState fragmentState(Fragment f, double[] l, int x, int y, int z, int palette, int face, double faceSign,
                                               double cutoff, int spikes) {
        int h = f.half();
        int m = mask(f.tilt(), l, h);
        if (m < 0) {
            int s = f.spike();
            if (s == 0 || Math.abs(l[0]) > h + s || Math.abs(l[1]) > h || Math.abs(l[2]) > h) return null;
            int edge = mask(f.tilt(), new double[]{0.0, l[1], l[2]}, h);
            int id = (l[1] > 0 ? 1 : 0) | (l[2] > 0 ? 2 : 0);
            if (edge != 6 || (spikes >> id & 1) == 0) return null;
            Direction.Axis axis = lineAxis(f.tilt(), f.axis(), 6);
            return axis != null ? beamState(axis) : lattice();
        }
        int bits = Integer.bitCount(m);
        if (f.shape() == Shape.BROKEN) {
            double plane = l[face] * faceSign + (noise(x, y, z) - 0.5) * 2.0;
            if (plane > cutoff) return null;
            if (bits >= 2 && plane > cutoff - 1.2) {
                return noise(x, y + 5, z) < 0.5 ? ModBlocks.CRACKED_PEARLSTONE_BRICKS.get().defaultBlockState()
                        : ModBlocks.COBBLED_PEARLSTONE.get().defaultBlockState();
            }
        }
        if (bits == 3) return fragmentCorner(palette);
        if (bits == 2) {
            if (f.shape() != Shape.SOLID && noise(x, y + 3, z) < 0.04) return null;
            return fragmentEdge(palette, lineAxis(f.tilt(), f.axis(), m), x, y, z);
        }
        if (bits == 1 && f.shape() == Shape.SOLID) {
            int on = Integer.numberOfTrailingZeros(m);
            if (on == face && l[on] * faceSign > 0) {
                int i = (int) Math.floor(l[(on + 1) % 3] + 0.5);
                int j = (int) Math.floor(l[(on + 2) % 3] + 0.5);
                return Math.floorMod(i + j, 2) == 0 ? carved() : ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState();
            }
            return fragmentFill(palette);
        }
        if (bits == 1 && f.shape() == Shape.RIBBED && (m & 6) != 0 && Math.floorMod((int) Math.floor(l[0] + 0.5), 2) == 0) {
            return palette == 0 ? ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState() : fragmentFill(palette);
        }
        return null;
    }

    private void stone(Stone stone) {
        BlockPos center = stone.center();
        Direction along = stone.axis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction side = along.getClockWise();
        BlockState body = beamState(stone.axis());
        if (stone.fragment()) {
            int shift = (int) Math.floor(noise(center.getX(), center.getY(), center.getZ()) * 3) - 1;
            for (int l = -1; l <= 1; l++) {
                set(center.relative(along, l).relative(side, shift), body);
            }
            return;
        }
        for (int o = -1; o <= 1; o++) {
            for (int l = -1; l <= 1; l++) {
                BlockPos pos = center.relative(side, o).relative(along, l);
                set(pos, o != 0 ? body : l == 0 ? carved() : lattice());
            }
        }
        double n = noise(center.getX(), center.getY() - 1, center.getZ());
        if (n > 0.55) {
            set(center.below(), beamState(Direction.Axis.Y));
            if (n > 0.8) set(center.below(2), beamState(Direction.Axis.Y));
        }
    }

    private void ledge(Exit exit) {
        BlockPos center = exit.out(1);
        Direction side = exit.normal().getClockWise();
        set(center, carved());
        set(center.relative(side), lattice());
        set(center.relative(side, -1), lattice());
    }

    private void buildLink(Link link) {
        for (Stone stone : link.stones()) {
            stone(stone);
        }
        if (link.route() == Route.UPDRAFT) {
            BlockPos vent = link.vent();
            set(vent, ModBlocks.UPDRAFT_VENT.get().defaultBlockState());
            for (int y = vent.getY() + 1; y <= link.columnTop(); y++) {
                set(vent.getX(), y, vent.getZ(), ModBlocks.UPDRAFT_MIST.get().defaultBlockState());
            }
            ledge(link.high());
        }
        if (link.anchorLow() != null && link.anchorHigh() != null) {
            post(link.anchorLow(), link.anchorHigh());
            post(link.anchorHigh(), link.anchorLow());
        }
    }

    private void post(BlockPos anchor, BlockPos other) {
        set(anchor.below(), carved());
        set(anchor.below(2), beamState(Direction.Axis.Y));
        Direction along = Direction.getNearest(other.getX() - anchor.getX(), 0, other.getZ() - anchor.getZ());
        set(anchor, HamletBlocks.ZIPLINE_ANCHOR.get().defaultBlockState().setValue(ZiplineAnchorBlock.FACING, along));
        tethers.add(new Tether(anchor, other));
    }

    private void buildApproach(ReliquaryLayout.Approach approach) {
        Exit exit = approach.exit();
        ledge(exit);
        BlockPos rope = exit.out(1).below();
        BlockState ropeState = HamletBlocks.ROPE.get().defaultBlockState();
        for (int i = 0; i < ReliquaryLayout.ROPE_LENGTH; i++) {
            set(rope.below(i), ropeState.setValue(RopeBlock.END, i == ReliquaryLayout.ROPE_LENGTH - 1));
        }
        column = new Column(approach.column().getX(), approach.column().getZ(), approach.top());
    }

    public BoundingBox bounds() {
        List<BlockPos> corners = new ArrayList<>(blocks.keySet());
        if (column != null) {
            corners.add(new BlockPos(column.x() - 1, UpfallTerrain.CLOUD_BAND_MIN, column.z() - 1));
            corners.add(new BlockPos(column.x() + 1, column.top(), column.z() + 1));
        }
        if (corners.isEmpty()) {
            return new BoundingBox(origin);
        }
        return BoundingBox.encapsulatingPositions(corners).orElseThrow();
    }

    private synchronized Map<Long, List<BlockPos>> index() {
        if (byChunk == null) {
            Map<Long, List<BlockPos>> map = new HashMap<>();
            for (BlockPos pos : blocks.keySet()) {
                map.computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), k -> new ArrayList<>()).add(pos);
            }
            byChunk = map;
        }
        return byChunk;
    }

    public void place(WorldGenLevel level, BoundingBox box, RandomSource random) {
        Map<Long, List<BlockPos>> index = index();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                List<BlockPos> positions = index.get(ChunkPos.asLong(cx, cz));
                if (positions == null) continue;
                for (BlockPos pos : positions) {
                    if (box.isInside(pos)) {
                        level.setBlock(pos, blocks.get(pos), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        for (Cache cache : caches) {
            if (box.isInside(cache.pos())) {
                RandomizableContainer.setBlockEntityLootTable(level, random, cache.pos(), cache.lootTable());
            }
        }
        for (Spawner spawner : spawners) {
            if (box.isInside(spawner.pos()) && level.getBlockEntity(spawner.pos()) instanceof SpawnerBlockEntity entity) {
                entity.setEntityId(spawner.type().get(), random);
            }
        }
        for (Tether tether : tethers) {
            if (box.isInside(tether.anchor()) && level.getBlockEntity(tether.anchor()) instanceof RopeAnchorBlockEntity anchor) {
                anchor.addLinkSilently(tether.other());
            }
        }
        if (column != null) {
            placeColumn(level, box);
        }
    }

    private void placeColumn(WorldGenLevel level, BoundingBox box) {
        int x = column.x();
        int z = column.z();
        if (x < box.minX() - 1 || x > box.maxX() + 1 || z < box.minZ() - 1 || z > box.maxZ() + 1) return;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int surface = -1;
        for (int y = Math.min(column.top() - 4, UpfallTerrain.CLOUD_BAND_MAX + 14); y >= UpfallTerrain.CLOUD_BAND_MIN; y--) {
            if (!level.getBlockState(cursor.set(x, y, z)).isAir()) {
                surface = y;
                break;
            }
        }
        if (surface < 0) {
            surface = UpfallTerrain.CLOUD_BAND_MAX - 6;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                cursor.set(x + dx, surface, z + dz);
                if (!box.isInside(cursor)) continue;
                if (dx == 0 && dz == 0) {
                    level.setBlock(cursor, ModBlocks.UPDRAFT_VENT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    for (int y = surface + 1; y <= column.top(); y++) {
                        level.setBlock(cursor.set(x, y, z), ModBlocks.UPDRAFT_MIST.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                } else {
                    level.setBlock(cursor, dx != 0 && dz != 0 ? carved() : lattice(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
