package net.itsthesky.upfall.content.aerie.worldgen;

import net.itsthesky.upfall.content.aerie.AerieBlocks;
import net.itsthesky.upfall.content.aerie.AerieLoot;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout.Bridge;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout.Kind;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout.Plan;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout.Span;
import net.itsthesky.upfall.content.aerie.worldgen.AerieLayout.Tower;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.mobs.MobEntities;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class AerieBlueprint {
    private record Cache(BlockPos pos, ResourceKey<LootTable> lootTable) {}

    private record Spawner(BlockPos pos, Supplier<? extends EntityType<?>> type) {}

    private final long seed;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Cache> caches = new ArrayList<>();
    private final List<Spawner> spawners = new ArrayList<>();
    private final List<BlockPos> traps = new ArrayList<>();

    private AerieBlueprint(long seed) {
        this.seed = seed;
    }

    public Map<BlockPos, BlockState> blocks() {
        return blocks;
    }

    public int cacheCount() {
        return caches.size();
    }

    public int spawnerCount() {
        return spawners.size();
    }

    public int trapCount() {
        return traps.size();
    }

    public boolean hasCache(ResourceKey<LootTable> lootTable) {
        return caches.stream().anyMatch(cache -> cache.lootTable() == lootTable);
    }

    private void set(int x, int y, int z, BlockState state) {
        blocks.put(new BlockPos(x, y, z), state);
    }

    private BlockState get(int x, int y, int z) {
        return blocks.get(new BlockPos(x, y, z));
    }

    private double noise(int x, int y, int z) {
        long h = Mth.getSeed(x, y, z) ^ seed * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 31)) * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h >>> 11) * 0x1.0p-53;
    }

    private BlockState brick(int x, int y, int z) {
        double n = noise(x, y, z);
        if (n < 0.12) return ModBlocks.CRACKED_PEARLSTONE_BRICKS.get().defaultBlockState();
        if (n < 0.2) return ModBlocks.MOSSY_PEARLSTONE_BRICKS.get().defaultBlockState();
        return ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
    }

    private BlockState stone(int x, int y, int z) {
        double n = noise(x, y, z);
        if (n < 0.3) return ModBlocks.COBBLED_PEARLSTONE.get().defaultBlockState();
        return ModBlocks.PEARLSTONE.get().defaultBlockState();
    }

    private static BlockState wall() {
        return ModBlocks.PEARLSTONE_BRICK_VARIANTS.wall().get().defaultBlockState();
    }

    private static BlockState slab(SlabType type) {
        return ModBlocks.PEARLSTONE_BRICK_VARIANTS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    private static BlockState stairs(Direction facing, Half half) {
        return ModBlocks.PEARLSTONE_BRICK_VARIANTS.stairs().get().defaultBlockState()
                .setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public static AerieBlueprint tower(Plan plan, int index, long seed) {
        AerieBlueprint bp = new AerieBlueprint(seed);
        bp.buildTower(plan, index, RandomSource.create(seed));
        return bp;
    }

    public static AerieBlueprint bridge(Plan plan, int index, long seed) {
        AerieBlueprint bp = new AerieBlueprint(seed);
        bp.buildBridge(plan, plan.bridges().get(index), RandomSource.create(seed));
        return bp;
    }

    private record Door(Span span, int y, boolean start) {}

    private void buildTower(Plan plan, int index, RandomSource random) {
        Tower tower = plan.towers().get(index);
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int y0 = tower.base().getY();
        int r = tower.radius();
        List<Door> doors = new ArrayList<>();
        for (Bridge bridge : plan.bridges()) {
            if (bridge.from() == index) doors.add(new Door(Span.of(plan, bridge), bridge.fromY(), true));
            if (bridge.to() == index) doors.add(new Door(Span.of(plan, bridge), bridge.toY(), false));
        }

        underside(tower);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.inDisc(dx, dz, r)) continue;
                boolean ring = AerieLayout.onRing(dx, dz, r);
                int x = cx + dx;
                int z = cz + dz;
                set(x, y0, z, ring ? brick(x, y0, z) : dx == 0 && dz == 0
                        ? ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState()
                        : ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState());
                for (int floor = 0; floor < tower.floors(); floor++) {
                    int level = tower.level(floor);
                    if (floor > 0) {
                        set(x, level, z, ring ? brick(x, level, z) : ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState());
                    }
                    for (int y = level + 1; y < level + AerieLayout.FLOOR_HEIGHT; y++) {
                        set(x, y, z, ring ? brick(x, y, z) : AIR);
                    }
                }
                int roof = tower.roofY();
                set(x, roof, z, ring ? brick(x, roof, z) : ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState());
            }
        }
        for (int floor = 0; floor < tower.floors(); floor++) {
            windows(tower, tower.level(floor));
            if (floor > 0 && tower.kind() != Kind.SMALL) {
                ledge(tower, tower.level(floor));
            }
        }
        ledge(tower, tower.roofY());

        for (Door door : doors) {
            carveDoor(tower, door);
        }
        Direction ladder = ladderSide(tower, doors);
        ladder(tower, ladder);
        if (tower.terrace()) {
            terrace(tower, ladder);
        } else {
            cone(tower);
        }
        balcony(tower, doors, ladder, random);
        if (tower.kind() == Kind.KEEP) {
            updraftShaft(tower);
        }
        furnish(tower, ladder, doors, random);
        themes(tower, ladder, random);
        lights(tower, random);
        cobwebs(tower, random);
    }

    private void underside(Tower tower) {
        int r = tower.radius();
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int y0 = tower.base().getY();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.inDisc(dx, dz, r)) continue;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double shape = Math.pow(Math.max(0.0, 1.0 - dist / (r + 1.0)), 0.7);
                int depth = (int) Math.round(tower.underDepth() * shape * (0.75 + 0.5 * noise(cx + dx, 0, cz + dz)));
                for (int d = 1; d <= Math.max(1, depth); d++) {
                    int x = cx + dx;
                    int z = cz + dz;
                    set(x, y0 - d, z, stone(x, y0 - d, z));
                }
            }
        }
    }

    private void windows(Tower tower, int level) {
        int r = tower.radius();
        int count = tower.kind() == Kind.SMALL ? 4 : 8;
        double offset = noise(tower.base().getX(), level, tower.base().getZ()) * Math.PI;
        for (int i = 0; i < count; i++) {
            double angle = offset + Math.PI * 2.0 * i / count;
            int x = tower.base().getX() + (int) Math.round(Math.cos(angle) * r);
            int z = tower.base().getZ() + (int) Math.round(Math.sin(angle) * r);
            if (!AerieLayout.onRing(x - tower.base().getX(), z - tower.base().getZ(), r)) continue;
            set(x, level + 2, z, AIR);
            set(x, level + 3, z, AIR);
        }
    }

    private void ledge(Tower tower, int level) {
        int r = tower.radius() + 1;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (AerieLayout.onRing(dx, dz, r)) {
                    int x = tower.base().getX() + dx;
                    int z = tower.base().getZ() + dz;
                    if (noise(x, level + 7, z) > 0.08) {
                        set(x, level, z, slab(SlabType.BOTTOM));
                    }
                }
            }
        }
    }

    private void carveDoor(Tower tower, Door door) {
        int r = tower.radius();
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                if (!AerieLayout.inDisc(dx, dz, r + 1) || AerieLayout.inDisc(dx, dz, r - 1)) continue;
                int x = tower.base().getX() + dx;
                int z = tower.base().getZ() + dz;
                double along = door.span().along(x, z);
                boolean near = door.start() ? along < door.span().start() + 2.5 : along > door.span().end() - 2.5;
                if (!near || Math.abs(door.span().across(x, z)) > 1.5) continue;
                set(x, door.y(), z, brick(x, door.y(), z));
                for (int y = door.y() + 1; y <= door.y() + 3; y++) {
                    set(x, y, z, AIR);
                }
            }
        }
    }

    private static Direction ladderSide(Tower tower, List<Door> doors) {
        Direction best = Direction.NORTH;
        double bestScore = -1;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            double score = Math.PI;
            for (Door door : doors) {
                double ux = door.start() ? door.span().ux() : -door.span().ux();
                double uz = door.start() ? door.span().uz() : -door.span().uz();
                double cos = ux * dir.getStepX() + uz * dir.getStepZ();
                score = Math.min(score, Math.acos(Mth.clamp(cos, -1.0, 1.0)));
            }
            if (score > bestScore) {
                bestScore = score;
                best = dir;
            }
        }
        return best;
    }

    private void ladder(Tower tower, Direction side) {
        int r = tower.radius();
        int x = tower.base().getX() + side.getStepX() * (r - 1);
        int z = tower.base().getZ() + side.getStepZ() * (r - 1);
        int top = tower.terrace() ? tower.roofY() : tower.level(tower.floors() - 1);
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, side.getOpposite());
        for (int y = tower.base().getY() + 1; y <= top; y++) {
            set(x, y, z, ladder);
        }
        int wx = x + side.getStepX();
        int wz = z + side.getStepZ();
        for (int y = tower.base().getY() + 1; y <= top + 1; y++) {
            set(wx, y, wz, brick(wx, y, wz));
        }
    }

    private void terrace(Tower tower, Direction ladder) {
        int r = tower.radius();
        int roof = tower.roofY();
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int lx = cx + ladder.getStepX() * r;
        int lz = cz + ladder.getStepZ() * r;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.onRing(dx, dz, r)) continue;
                int x = cx + dx;
                int z = cz + dz;
                if (x == lx && z == lz) continue;
                boolean merlon = Math.floorMod(Math.round(Math.atan2(dz, dx) * r), 2) == 0;
                set(x, roof + 1, z, merlon ? brick(x, roof + 1, z) : wall());
                if (merlon && noise(x, roof + 3, z) > 0.88) {
                    set(x, roof + 2, z, lantern(x, roof + 2, z, false));
                } else if (merlon && noise(x, roof + 2, z) > 0.25) {
                    set(x, roof + 2, z, slab(SlabType.BOTTOM));
                }
            }
        }
        if (tower.kind() != Kind.KEEP) return;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                for (int y = roof + 1; y <= roof + 3; y++) {
                    set(cx + sx * 2, y, cz + sz * 2, wall());
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                set(cx + dx, roof + 4, cz + dz, edge ? slab(SlabType.BOTTOM) : brick(cx + dx, roof + 4, cz + dz));
            }
        }
        set(cx, roof + 5, cz, ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
        set(cx, roof + 6, cz, wall());
        set(cx, roof + 3, cz, hanging(GrottoBlocks.MISTGLOW_LANTERN.get().defaultBlockState()));
    }

    private void cone(Tower tower) {
        int r = tower.radius();
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int roof = tower.roofY();
        for (int i = 1; i <= r + 2; i++) {
            int ring = r + 2 - i;
            int y = roof + i;
            if (ring <= 0) {
                set(cx, y, cz, ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
                set(cx, y + 1, cz, wall());
                set(cx, y + 2, cz, wall());
                break;
            }
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (!AerieLayout.onRing(dx, dz, ring)) continue;
                    int x = cx + dx;
                    int z = cz + dz;
                    if (noise(x, y, z) < 0.07) continue;
                    set(x, y, z, stairs(Direction.getNearest(-dx, 0, -dz), Half.BOTTOM));
                }
            }
        }
    }

    private void balcony(Tower tower, List<Door> doors, Direction ladder, RandomSource random) {
        if (tower.kind() == Kind.SMALL || tower.floors() < 2) return;
        int floor = 1 + random.nextInt(tower.floors() - 1);
        int level = tower.level(floor);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double ux = Math.cos(angle);
        double uz = Math.sin(angle);
        if (ux * ladder.getStepX() + uz * ladder.getStepZ() > 0.5) return;
        for (Door door : doors) {
            double dx = door.start() ? door.span().ux() : -door.span().ux();
            double dz = door.start() ? door.span().uz() : -door.span().uz();
            if (dx * ux + dz * uz > 0.6) return;
        }
        int r = tower.radius();
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int reach = r + 3;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (!AerieLayout.inDisc(dx, dz, reach) || AerieLayout.inDisc(dx, dz, r)) continue;
                double side = -dx * uz + dz * ux;
                double forward = dx * ux + dz * uz;
                if (forward <= 0 || Math.abs(side) > 2.5) continue;
                int x = cx + dx;
                int z = cz + dz;
                set(x, level, z, brick(x, level, z));
                set(x, level - 1, z, slab(SlabType.TOP));
                boolean rim = Math.abs(side) > 1.5 || !AerieLayout.inDisc(dx, dz, reach - 1);
                set(x, level + 1, z, rim ? wall() : AIR);
                if (rim && noise(x, level + 2, z) > 0.8) {
                    set(x, level + 2, z, lantern(x, level + 2, z, false));
                }
            }
        }
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.onRing(dx, dz, r)) continue;
                if (dx * ux + dz * uz <= 0 || Math.abs(-dx * uz + dz * ux) > 0.9) continue;
                set(cx + dx, level + 1, cz + dz, AIR);
                set(cx + dx, level + 2, cz + dz, AIR);
            }
        }
    }

    private BlockPos wallSpot(Tower tower, int level, double angle) {
        int r = tower.radius() - 1;
        int dx = (int) Math.round(Math.cos(angle) * (r - 0.4));
        int dz = (int) Math.round(Math.sin(angle) * (r - 0.4));
        while (!AerieLayout.inDisc(dx, dz, r)) {
            dx -= Integer.signum(dx);
            dz -= Integer.signum(dz);
        }
        return new BlockPos(tower.base().getX() + dx, level + 1, tower.base().getZ() + dz);
    }

    private boolean freeFloorSpot(BlockPos pos) {
        BlockState here = get(pos.getX(), pos.getY(), pos.getZ());
        BlockState below = get(pos.getX(), pos.getY() - 1, pos.getZ());
        return (here == null || here.isAir()) && below != null && !below.isAir() && !below.is(Blocks.LADDER);
    }

    private BlockPos chest(Tower tower, int level, RandomSource random, ResourceKey<LootTable> lootTable) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos pos = wallSpot(tower, level, random.nextDouble() * Math.PI * 2.0);
            if (!freeFloorSpot(pos) || !clearOfOpenings(tower, pos)) continue;
            set(pos.getX(), pos.getY(), pos.getZ(), ModBlocks.SKY_CACHE.get().defaultBlockState());
            caches.add(new Cache(pos, lootTable));
            return pos;
        }
        return null;
    }

    private boolean clearOfOpenings(Tower tower, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(dir);
            BlockState state = get(side.getX(), side.getY(), side.getZ());
            if (state != null && state.is(Blocks.LADDER)) return false;
            int dx = side.getX() - tower.base().getX();
            int dz = side.getZ() - tower.base().getZ();
            if (AerieLayout.onRing(dx, dz, tower.radius()) && (state == null || state.isAir())) return false;
        }
        return true;
    }

    private void furnish(Tower tower, Direction ladder, List<Door> doors, RandomSource random) {
        int top = tower.floors() - 1;
        switch (tower.kind()) {
            case SMALL -> {
                if (random.nextFloat() < 0.6F) chest(tower, tower.level(top), random, AerieLoot.TOWER);
            }
            case MEDIUM -> {
                int armory = random.nextFloat() < 0.4F ? random.nextInt(tower.floors()) : -1;
                for (int floor = 0; floor < tower.floors(); floor++) {
                    if (floor == armory) {
                        chest(tower, tower.level(floor), random, AerieLoot.ARMORY);
                    } else if (random.nextBoolean()) {
                        chest(tower, tower.level(floor), random, AerieLoot.TOWER);
                    }
                }
                if (random.nextBoolean()) {
                    spawner(tower, tower.level(top), MobEntities.DUSKWING, null);
                }
            }
            case KEEP -> {
                spawner(tower, tower.level(0), MobEntities.CRAGMAW, ladder.getClockWise());
                chest(tower, tower.level(0), random, AerieLoot.TOWER);
                chest(tower, tower.level(1), random, AerieLoot.ARMORY);
                chest(tower, tower.level(1), random, AerieLoot.ARMORY);
                for (int floor = 2; floor < top; floor++) {
                    if (random.nextBoolean()) chest(tower, tower.level(floor), random, AerieLoot.TOWER);
                    if (random.nextFloat() < 0.6F) weatheredPatch(tower, tower.level(floor), random);
                    if (floor == top - 1) spawner(tower, tower.level(floor), MobEntities.DUSKWING, ladder.getClockWise());
                }
                treasure(tower, tower.level(top), random);
            }
        }
    }

    private void spawner(Tower tower, int level, Supplier<? extends EntityType<?>> type, Direction offset) {
        int x = tower.base().getX() + (offset == null ? 0 : offset.getStepX() * 2);
        int z = tower.base().getZ() + (offset == null ? 0 : offset.getStepZ() * 2);
        set(x, level + 1, z, Blocks.SPAWNER.defaultBlockState());
        spawners.add(new Spawner(new BlockPos(x, level + 1, z), type));
    }

    private void weatheredPatch(Tower tower, int level, RandomSource random) {
        int r = tower.radius() - 3;
        int px = tower.base().getX() + random.nextInt(2 * r + 1) - r;
        int pz = tower.base().getZ() + random.nextInt(2 * r + 1) - r;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                int x = px + dx;
                int z = pz + dz;
                BlockState floor = get(x, level, z);
                BlockState above = get(x, level + 1, z);
                if (floor != null && floor.is(ModBlocks.POLISHED_PEARLSTONE.get()) && above != null && above.isAir()) {
                    set(x, level, z, AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get().defaultBlockState());
                }
            }
        }
    }

    private void treasure(Tower tower, int level, RandomSource random) {
        BlockPos chest = chest(tower, level, random, AerieLoot.KEEP);
        if (chest == null) {
            int x = tower.base().getX() + 1;
            int z = tower.base().getZ();
            set(x, level + 1, z, ModBlocks.SKY_CACHE.get().defaultBlockState());
            caches.add(new Cache(new BlockPos(x, level + 1, z), AerieLoot.KEEP));
            chest = new BlockPos(x, level + 1, z);
        }
        Direction facing = Direction.getNearest(tower.base().getX() - chest.getX(), 0, tower.base().getZ() - chest.getZ());
        BlockPos front = chest.relative(facing);
        for (BlockPos plate : List.of(front, front.relative(facing.getClockWise()), front.relative(facing.getCounterClockWise()))) {
            BlockState here = get(plate.getX(), plate.getY(), plate.getZ());
            BlockState below = get(plate.getX(), plate.getY() - 1, plate.getZ());
            if (here == null || !here.isAir() || below == null || !below.is(ModBlocks.POLISHED_PEARLSTONE.get())) continue;
            set(plate.getX(), plate.getY(), plate.getZ(), Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE.defaultBlockState());
            set(plate.getX(), plate.getY() - 1, plate.getZ(), Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
            traps.add(plate.below());
        }
    }

    private BlockState lantern(int x, int y, int z, boolean hanging) {
        double n = noise(x, y + 11, z);
        BlockState state = n < 0.45 ? HamletBlocks.CLOUD_LANTERN.get().defaultBlockState()
                : n < 0.75 ? GrottoBlocks.MISTGLOW_LANTERN.get().defaultBlockState()
                : QuantumBlocks.SPENT_LANTERN.get().defaultBlockState();
        return state.setValue(LanternBlock.HANGING, hanging);
    }

    private static BlockState hanging(BlockState lantern) {
        return lantern.setValue(LanternBlock.HANGING, true);
    }

    private void setIfAir(int x, int y, int z, BlockState state) {
        BlockState current = get(x, y, z);
        if (current != null && current.isAir()) {
            set(x, y, z, state);
        }
    }

    private void updraftShaft(Tower tower) {
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        int y0 = tower.base().getY();
        set(cx, y0, cz, ModBlocks.UPDRAFT_VENT.get().defaultBlockState());
        for (int y = y0 + 1; y < tower.roofY(); y++) {
            set(cx, y, cz, ModBlocks.UPDRAFT_MIST.get().defaultBlockState());
        }
        for (int floor = 1; floor < tower.floors(); floor++) {
            int level = tower.level(floor);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) {
                        set(cx + dx, level, cz + dz, ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
                    }
                }
            }
        }
    }

    private enum Theme { NONE, LIBRARY, GARDEN }

    private void themes(Tower tower, Direction ladder, RandomSource random) {
        Theme previous = Theme.NONE;
        for (int floor = 0; floor < tower.floors(); floor++) {
            float roll = random.nextFloat();
            Theme theme = switch (tower.kind()) {
                case SMALL -> roll < 0.3F ? Theme.GARDEN : Theme.NONE;
                case MEDIUM -> roll < 0.3F ? Theme.LIBRARY : roll < 0.55F ? Theme.GARDEN : Theme.NONE;
                case KEEP -> floor == 0 || floor == tower.floors() - 1 ? Theme.NONE
                        : roll < 0.4F ? Theme.LIBRARY : roll < 0.7F ? Theme.GARDEN : Theme.NONE;
            };
            if (theme != Theme.NONE && theme == previous) {
                theme = tower.kind() == Kind.SMALL ? Theme.NONE : theme == Theme.LIBRARY ? Theme.GARDEN : Theme.LIBRARY;
            }
            switch (theme) {
                case LIBRARY -> library(tower, tower.level(floor), ladder, random);
                case GARDEN -> garden(tower, tower.level(floor), random);
                case NONE -> {}
            }
            previous = theme;
        }
    }

    private boolean besideOpening(Tower tower, int x, int y, int z) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int nx = x + dir.getStepX();
            int nz = z + dir.getStepZ();
            BlockState state = get(nx, y, nz);
            if (state != null && state.is(Blocks.LADDER)) return true;
            if (AerieLayout.onRing(nx - tower.base().getX(), nz - tower.base().getZ(), tower.radius()) && (state == null || state.isAir())) {
                return true;
            }
        }
        return false;
    }

    private void library(Tower tower, int level, Direction ladder, RandomSource random) {
        int r = tower.radius() - 1;
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.onRing(dx, dz, r)) continue;
                int x = cx + dx;
                int z = cz + dz;
                if (besideOpening(tower, x, level + 1, z) || besideOpening(tower, x, level + 2, z)) continue;
                double n = noise(x, level + 1, z);
                if (n < 0.2) continue;
                BlockState shelf = n > 0.93 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState();
                setIfAir(x, level + 1, z, shelf);
                if (noise(x, level + 2, z) > 0.3) {
                    setIfAir(x, level + 2, z, Blocks.BOOKSHELF.defaultBlockState());
                }
            }
        }
        Direction side = ladder.getCounterClockWise();
        setIfAir(cx + side.getStepX() * 2, level + 1, cz + side.getStepZ() * 2,
                Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, side.getOpposite()));
        if (random.nextBoolean()) {
            setIfAir(cx - side.getStepX() * 2, level + 1, cz - side.getStepZ() * 2, Blocks.CANDLE.defaultBlockState()
                    .setValue(CandleBlock.CANDLES, 1 + random.nextInt(4)));
        }
    }

    private void garden(Tower tower, int level, RandomSource random) {
        int r = tower.radius();
        int cx = tower.base().getX();
        int cz = tower.base().getZ();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int x = cx + dx;
                int z = cz + dz;
                if (AerieLayout.inDisc(dx, dz, r - 2) && !AerieLayout.inDisc(dx, dz, r - 3)) {
                    BlockState floor = get(x, level, z);
                    BlockState above = get(x, level + 1, z);
                    if (floor == null || !floor.is(ModBlocks.POLISHED_PEARLSTONE.get()) || above == null || !above.isAir()) continue;
                    set(x, level, z, ModBlocks.SKYSOIL.get().defaultBlockState());
                    double n = noise(x, level + 1, z);
                    if (n < 0.25) set(x, level + 1, z, ModBlocks.DUSKBLOOM.get().defaultBlockState());
                    else if (n < 0.6) set(x, level + 1, z, ModBlocks.CLOUD_FERN.get().defaultBlockState());
                }
                if (AerieLayout.onRing(dx, dz, r)) {
                    for (int y = level + 2; y <= level + 3; y++) {
                        if (noise(x, y, z) > 0.35) {
                            setIfAir(x, y, z, ModBlocks.SKYREACH.leaves().get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                        }
                    }
                }
            }
        }
        int vines = 2 + random.nextInt(3);
        for (int i = 0; i < vines; i++) {
            int x = cx + random.nextInt(2 * r - 1) - (r - 1);
            int z = cz + random.nextInt(2 * r - 1) - (r - 1);
            int top = level + AerieLayout.FLOOR_HEIGHT - 1;
            BlockState ceiling = get(x, top + 1, z);
            if (ceiling == null || ceiling.isAir() || !AerieLayout.inDisc(x - cx, z - cz, r - 1)) continue;
            hangGlowvine(x, top, z, 1 + random.nextInt(2));
        }
    }

    private void hangGlowvine(int x, int top, int z, int length) {
        for (int i = 0; i < length; i++) {
            BlockState current = get(x, top - i, z);
            if (current != null && !current.isAir()) return;
        }
        for (int i = 0; i < length - 1; i++) {
            set(x, top - i, z, ModBlocks.GLOWVINE_PLANT.get().defaultBlockState());
        }
        set(x, top - length + 1, z, ModBlocks.GLOWVINE.get().defaultBlockState());
    }

    private void lights(Tower tower, RandomSource random) {
        int r = tower.radius();
        int count = Math.max(1, r / 3);
        for (int floor = 0; floor < tower.floors(); floor++) {
            int y = tower.level(floor) + AerieLayout.FLOOR_HEIGHT - 1;
            double offset = random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < count; i++) {
                double angle = offset + Math.PI * 2.0 * i / count;
                double distance = count == 1 ? 0.0 : (r - 1) / 2.0;
                int x = tower.base().getX() + (int) Math.round(Math.cos(angle) * distance);
                int z = tower.base().getZ() + (int) Math.round(Math.sin(angle) * distance);
                BlockState here = get(x, y, z);
                BlockState above = get(x, y + 1, z);
                if (here != null && here.isAir() && above != null && !above.isAir() && !above.is(Blocks.LADDER)) {
                    set(x, y, z, lantern(x, y, z, true));
                }
            }
        }
    }

    private void cobwebs(Tower tower, RandomSource random) {
        int r = tower.radius() - 1;
        for (int floor = 0; floor < tower.floors(); floor++) {
            int y = tower.level(floor) + AerieLayout.FLOOR_HEIGHT - 1;
            for (int i = 0; i < r; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                int x = tower.base().getX() + (int) Math.round(Math.cos(angle) * r);
                int z = tower.base().getZ() + (int) Math.round(Math.sin(angle) * r);
                BlockState state = get(x, y, z);
                if (state != null && state.isAir()) {
                    set(x, y, z, Blocks.COBWEB.defaultBlockState());
                }
            }
        }
    }

    private void buildBridge(Plan plan, Bridge bridge, RandomSource random) {
        Tower a = plan.towers().get(bridge.from());
        Tower b = plan.towers().get(bridge.to());
        Span span = Span.of(plan, bridge);
        double gapHalf = bridge.broken() ? (3 + random.nextInt(4)) / 2.0 : -1;
        double trap = !bridge.broken() && random.nextFloat() < 0.4F
                ? span.start() + 3 + random.nextDouble() * Math.max(0.0, span.end() - span.start() - 6) : Double.NaN;
        BlockPos landing = landingSpot(plan, bridge, span);
        if (landing != null && Math.abs(trap - span.middle()) < LANDING_RADIUS + 2) {
            trap = Double.NaN;
        }
        int minX = (int) Math.floor(Math.min(a.cx(), b.cx())) - 4;
        int maxX = (int) Math.ceil(Math.max(a.cx(), b.cx())) + 4;
        int minZ = (int) Math.floor(Math.min(a.cz(), b.cz())) - 4;
        int maxZ = (int) Math.ceil(Math.max(a.cz(), b.cz())) + 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double along = span.along(x, z);
                double across = Math.abs(span.across(x, z));
                if (along < span.start() - 0.5 || along > span.end() + 0.5 || across > 2.5) continue;
                boolean insideA = AerieLayout.inDisc(x - a.base().getX(), z - a.base().getZ(), a.radius());
                boolean insideB = AerieLayout.inDisc(x - b.base().getX(), z - b.base().getZ(), b.radius());
                double fromMiddle = Math.abs(along - span.middle());
                if (fromMiddle < gapHalf) continue;
                if (landing != null && AerieLayout.inDisc(x - landing.getX(), z - landing.getZ(), LANDING_RADIUS)) continue;
                double height = span.height(along);
                int y = (int) Math.floor(height);
                boolean half = height - y >= 0.5;
                if (across <= 1.5) {
                    if (insideA && AerieLayout.inDisc(x - a.base().getX(), z - a.base().getZ(), a.radius() - 1)) continue;
                    if (insideB && AerieLayout.inDisc(x - b.base().getX(), z - b.base().getZ(), b.radius() - 1)) continue;
                    boolean crumbling = !Double.isNaN(trap) && Math.abs(along - trap) < 1.5 && !insideA && !insideB;
                    boolean frayed = gapHalf > 0 && fromMiddle < gapHalf + 1.5;
                    BlockState deck = crumbling ? AerieBlocks.WEATHERED_PEARLSTONE_BRICKS.get().defaultBlockState()
                            : frayed ? ModBlocks.CRACKED_PEARLSTONE_BRICKS.get().defaultBlockState() : brick(x, y, z);
                    set(x, y, z, deck);
                    if (half && !crumbling) set(x, y + 1, z, slab(SlabType.BOTTOM));
                    boolean edge = across > 0.5 && !crumbling && !insideA && !insideB;
                    if (edge && noise(x, y - 5, z) > 0.86) {
                        hangGlowvine(x, y - 1, z, noise(x, y - 6, z) > 0.5 ? 3 : 2);
                    } else if (edge && !half && noise(x, y + 1, z) > 0.9) {
                        set(x, y + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
                    }
                    if (across <= 0.5 && !crumbling && !insideA && !insideB) {
                        set(x, y - 1, z, stone(x, y - 1, z));
                        if (Math.floorMod((int) Math.floor(along), 7) == 3 && !frayed) {
                            set(x, y - 2, z, wall());
                            if (noise(x, y - 3, z) > 0.5) {
                                set(x, y - 3, z, hanging(GrottoBlocks.MISTGLOW_LANTERN.get().defaultBlockState()));
                            }
                        }
                    }
                } else if (!insideA && !insideB) {
                    set(x, y, z, brick(x, y, z));
                    boolean intact = gapHalf < 0 || fromMiddle > gapHalf + 1.5;
                    if (intact || noise(x, y, z) > 0.5) {
                        set(x, y + 1, z, wall());
                    }
                    if (noise(x, y - 7, z) > 0.8) {
                        drapeVine(a, b, x, y, z, Direction.getNearest(-span.uz() * Math.signum(span.across(x, z)), 0,
                                span.ux() * Math.signum(span.across(x, z))));
                    }
                    int post = (int) Math.floor(along);
                    if (intact && Math.floorMod(post, 8) == 4 && (Math.floorMod(post / 8, 2) == 0) == (span.across(x, z) > 0)) {
                        set(x, y + 2, z, wall());
                        set(x, y + 3, z, lantern(x, y + 3, z, false));
                    }
                }
            }
        }
        if (landing != null) {
            landing(span, landing, random);
        }
    }

    private static final int LANDING_RADIUS = 4;

    private static BlockPos landingSpot(Plan plan, Bridge bridge, Span span) {
        if (bridge.broken() || span.end() - span.start() < 16 || Math.abs(span.endY() - span.startY()) > 1) return null;
        double middle = span.middle();
        int x = Mth.floor(span.ax() + span.ux() * middle);
        int z = Mth.floor(span.az() + span.uz() * middle);
        for (Tower tower : plan.towers()) {
            if (Math.hypot(x - tower.base().getX(), z - tower.base().getZ()) < tower.radius() + LANDING_RADIUS + 3) return null;
        }
        return new BlockPos(x, Mth.floor(span.height(middle)), z);
    }

    private void landing(Span span, BlockPos center, RandomSource random) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int r = LANDING_RADIUS;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!AerieLayout.inDisc(dx, dz, r)) continue;
                int x = cx + dx;
                int z = cz + dz;
                set(x, cy, z, brick(x, cy, z));
                double dist = Math.sqrt(dx * dx + dz * dz);
                int depth = 1 + (int) Math.round(4 * Math.pow(Math.max(0.0, 1.0 - dist / (r + 1.0)), 0.7) * (0.7 + 0.6 * noise(x, cy - 1, z)));
                for (int d = 1; d <= depth; d++) {
                    set(x, cy - d, z, stone(x, cy - d, z));
                }
                boolean onPath = Math.abs(span.across(x, z)) <= 1.5;
                if (AerieLayout.onRing(dx, dz, r)) {
                    if (!onPath) {
                        set(x, cy + 1, z, wall());
                        if (noise(x, cy + 2, z) > 0.86) {
                            set(x, cy + 2, z, wall());
                            set(x, cy + 3, z, lantern(x, cy + 3, z, false));
                        }
                    }
                    if (noise(x, cy - 9, z) > 0.7) {
                        hangGlowvine(x, cy - depth - 1, z, 1 + random.nextInt(3));
                    }
                }
            }
        }
        if (random.nextBoolean()) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean rim = Math.max(Math.abs(dx), Math.abs(dz)) == 2;
                    set(cx + dx, cy + 1, cz + dz, rim
                            ? ModBlocks.POLISHED_PEARLSTONE_VARIANTS.slab().get().defaultBlockState()
                            : ModFluids.SKYWATER_BLOCK.get().defaultBlockState());
                    if (!rim) set(cx + dx, cy, cz + dz, ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState());
                }
            }
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) {
                        set(cx + dx, cy + 1, cz + dz, ModBlocks.POLISHED_PEARLSTONE_VARIANTS.slab().get().defaultBlockState());
                    }
                }
            }
            for (int y = cy + 1; y <= cy + 3; y++) {
                set(cx, y, cz, ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState());
            }
            set(cx, cy + 4, cz, ModBlocks.CHISELED_PEARLSTONE_BRICKS.get().defaultBlockState());
            set(cx, cy + 5, cz, wall());
            set(cx, cy + 6, cz, HamletBlocks.CLOUD_LANTERN.get().defaultBlockState());
        }
    }

    private void drapeVine(Tower a, Tower b, int x, int y, int z, Direction outward) {
        int vx = x + outward.getStepX();
        int vz = z + outward.getStepZ();
        if (AerieLayout.inDisc(vx - a.base().getX(), vz - a.base().getZ(), a.radius() + 1)
                || AerieLayout.inDisc(vx - b.base().getX(), vz - b.base().getZ(), b.radius() + 1)) return;
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(outward.getOpposite()), true);
        int length = 1 + (int) Math.floor(noise(vx, y, vz) * 3);
        for (int i = 0; i < length; i++) {
            if (get(vx, y - i, vz) != null) return;
            set(vx, y - i, vz, vine);
        }
    }

    public BoundingBox bounds() {
        if (blocks.isEmpty()) {
            return new BoundingBox(BlockPos.ZERO);
        }
        return BoundingBox.encapsulatingPositions(blocks.keySet()).orElseThrow();
    }

    public void place(WorldGenLevel level, BoundingBox box, RandomSource random) {
        blocks.forEach((pos, state) -> {
            if (!box.isInside(pos)) return;
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (state.getBlock() instanceof WallBlock || state.getBlock() instanceof StairBlock) {
                level.getChunk(pos).markPosForPostprocessing(pos);
            }
        });
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
        for (BlockPos trap : traps) {
            if (box.isInside(trap) && level.getBlockEntity(trap) instanceof DispenserBlockEntity dispenser) {
                dispenser.setItem(0, new ItemStack(Items.ARROW, 8 + random.nextInt(9)));
                dispenser.setItem(1, new ItemStack(Items.ARROW, 8 + random.nextInt(9)));
            }
        }
    }
}
