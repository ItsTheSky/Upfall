package net.itsthesky.upfall.content.quantum.cave;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumLoot;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class GasCaveNetwork {
    public static final GasCaveNetwork EMPTY = new GasCaveNetwork();

    public enum DeadEnd { NONE, CAMP, VEIN, CAVE_IN, SKELETON }

    public record Settings(double margin, int minY, int maxY, int maxLength, double branchChance, double entranceChance,
                           double dugChance, double deadEndFeatureChance, double clusterChance) {}

    private final Long2ObjectMap<LongList> gas = new Long2ObjectOpenHashMap<>();
    private final LongSet gasSet = new LongOpenHashSet();
    private final LongSet entranceSet = new LongOpenHashSet();
    private final Long2ObjectMap<List<Prop>> props = new Long2ObjectOpenHashMap<>();
    private final List<BlockPos> deadEnds = new ArrayList<>();
    private final List<DeadEnd> deadEndKinds = new ArrayList<>();
    private BlockPos entrance;
    private int length;

    public record Prop(long pos, BlockState state, ResourceKey<LootTable> loot, boolean solidOnly) {}

    private GasCaveNetwork() {}

    public boolean isEmpty() {
        return gasSet.isEmpty();
    }

    public LongList gasIn(ChunkPos chunk) {
        LongList list = gas.get(chunk.toLong());
        return list == null ? LongList.of() : list;
    }

    public List<Prop> propsIn(ChunkPos chunk) {
        List<Prop> list = props.get(chunk.toLong());
        return list == null ? List.of() : list;
    }

    public boolean isGas(BlockPos pos) {
        return gasSet.contains(pos.asLong());
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

    public List<BlockPos> deadEnds() {
        return deadEnds;
    }

    public List<DeadEnd> deadEndKinds() {
        return deadEndKinds;
    }

    public BlockPos entrance() {
        return entrance;
    }

    public int length() {
        return length;
    }

    public Map<Long, List<Prop>> allProps() {
        return props;
    }


    public static GasCaveNetwork build(ChunkPos source, RandomSource random, ToDoubleFunction<BlockPos> density, Settings settings) {
        Builder builder = new Builder(new GasCaveNetwork(), source, random, density, settings);
        if (!builder.start()) return EMPTY;
        return builder.network.isEmpty() ? EMPTY : builder.network;
    }

    private static final class Builder {
        private static final int MAX_REACH = 100;
        private static final int MAX_DEPTH = 4;
        private static final int CHECK_EVERY = 3;
        private static final double SKIPPED_MARGIN = 2.0;

        final GasCaveNetwork network;
        final ChunkPos source;
        final RandomSource random;
        final ToDoubleFunction<BlockPos> density;
        final Settings settings;
        final List<BlockPos> nodes = new ArrayList<>();
        int budget;
        boolean carvingEntrance;

        Builder(GasCaveNetwork network, ChunkPos source, RandomSource random, ToDoubleFunction<BlockPos> density, Settings settings) {
            this.network = network;
            this.source = source;
            this.random = random;
            this.density = memoize(density);
            this.settings = settings;
            this.budget = settings.maxLength();
        }

        private static ToDoubleFunction<BlockPos> memoize(ToDoubleFunction<BlockPos> density) {
            it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap cache = new it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap();
            return pos -> {
                long key = pos.asLong();
                if (cache.containsKey(key)) return cache.get(key);
                double value = density.applyAsDouble(pos);
                cache.put(key, value);
                return value;
            };
        }

        boolean start() {
            BlockPos start = null;
            for (int attempt = 0; attempt < 2 && start == null; attempt++) {
                int x = source.getMinBlockX() + random.nextInt(4) * 4;
                int z = source.getMinBlockZ() + random.nextInt(4) * 4;
                List<Integer> candidates = new ArrayList<>();
                for (int y = Math.floorDiv(settings.maxY(), 8) * 8; y >= settings.minY(); y -= 8) {
                    if (density.applyAsDouble(new BlockPos(x, y, z)) >= settings.margin() + 2.0) candidates.add(y - 1);
                }
                if (!candidates.isEmpty()) start = new BlockPos(x, candidates.get(random.nextInt(candidates.size())), z);
            }
            if (start == null) return false;
            room(start, 2 + random.nextInt(2));
            List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
            shuffle(dirs);
            int arms = 2 + random.nextInt(2);
            for (int i = 0; i < arms && budget > 0; i++) {
                tunnel(start, dirs.get(i), 0);
            }
            if (!nodes.isEmpty() && random.nextDouble() < settings.entranceChance()) {
                entrance(nodes.get(random.nextInt(nodes.size())));
            }
            clusters();
            network.length = settings.maxLength() - budget;
            return true;
        }

        void tunnel(BlockPos from, Direction dir, int depth) {
            if (depth > 0 && random.nextDouble() < settings.dugChance()) {
                dugTunnel(from, dir, depth);
            } else {
                wormTunnel(from, dir.toYRot() * Mth.DEG_TO_RAD + Mth.HALF_PI + (random.nextFloat() - 0.5F) * 0.8F, depth);
            }
        }

        void wormTunnel(BlockPos from, float yaw, int depth) {
            float radius = 1.6F + random.nextFloat() * 0.9F;
            float vertical = 1.35F + random.nextFloat() * 0.5F;
            int length = 18 + random.nextInt(26);
            double x = from.getX() + 0.5;
            double y = from.getY() + vertical * 0.7;
            double z = from.getZ() + 0.5;
            float pitch = 0.0F;
            float yawDrift = 0.0F;
            float pitchDrift = 0.0F;
            BlockPos feet = from;
            for (int i = 0; i < length; i++) {
                if (budget-- <= 0) break;
                yawDrift = yawDrift * 0.8F + (random.nextFloat() - random.nextFloat()) * 0.35F;
                pitchDrift = pitchDrift * 0.7F + (random.nextFloat() - random.nextFloat()) * 0.12F;
                yaw += yawDrift * 0.45F;
                pitch = Mth.clamp(pitch * 0.85F + pitchDrift, -0.45F, 0.45F);
                boolean moved = false;
                for (float turn : new float[]{0.0F, 0.5F, -0.5F, 1.0F, -1.0F}) {
                    float candidate = yaw + (random.nextBoolean() ? turn : -turn);
                    double nx = x + Mth.cos(candidate) * Mth.cos(pitch);
                    double ny = y + Mth.sin(pitch);
                    double nz = z + Mth.sin(candidate) * Mth.cos(pitch);
                    if (validCenter(nx, ny, nz, vertical, i % CHECK_EVERY == 0 || turn != 0.0F)) {
                        yaw = candidate;
                        x = nx;
                        y = ny;
                        z = nz;
                        moved = true;
                        break;
                    }
                }
                if (!moved) break;
                feet = ellipsoid(x, y, z, radius + (random.nextFloat() - 0.5F) * 0.3F, vertical);
                nodes.add(feet);
            }
            Direction heading = Direction.getNearest(Mth.cos(yaw), 0.0F, Mth.sin(yaw));
            if (depth >= MAX_DEPTH || budget <= 0 || random.nextDouble() >= settings.branchChance()) {
                deadEnd(feet, heading);
                return;
            }
            room(feet, 2 + random.nextInt(2));
            int branches = 1 + (random.nextFloat() < 0.45F ? 1 : 0);
            float[] offsets = {0.0F, 1.1F, -1.1F};
            int first = random.nextInt(offsets.length);
            for (int i = 0; i < branches && budget > 0; i++) {
                wormTunnel(feet, yaw + offsets[(first + i) % offsets.length] + (random.nextFloat() - 0.5F) * 0.5F, depth + 1);
            }
        }

        boolean validCenter(double x, double y, double z, float vertical, boolean sample) {
            BlockPos center = BlockPos.containing(x, y, z);
            if (center.getY() < settings.minY() || center.getY() > settings.maxY()) return false;
            if (Math.abs(center.getX() - source.getMiddleBlockX()) > MAX_REACH || Math.abs(center.getZ() - source.getMiddleBlockZ()) > MAX_REACH) return false;
            return !sample || density.applyAsDouble(center) >= settings.margin() + vertical + SKIPPED_MARGIN;
        }

        BlockPos ellipsoid(double cx, double cy, double cz, float radius, float vertical) {
            int floor = Mth.floor(cy - vertical * 0.7);
            int r = Mth.ceil(radius);
            int v = Mth.ceil(vertical);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -v; dy <= v; dy++) {
                        int bx = Mth.floor(cx) + dx;
                        int by = Mth.floor(cy) + dy;
                        int bz = Mth.floor(cz) + dz;
                        if (by < floor) continue;
                        double ex = (bx + 0.5 - cx) / radius;
                        double ey = (by + 0.5 - cy) / vertical;
                        double ez = (bz + 0.5 - cz) / radius;
                        if (ex * ex + ey * ey + ez * ez <= 1.0) gas(new BlockPos(bx, by, bz));
                    }
                }
            }
            return new BlockPos(Mth.floor(cx), floor, Mth.floor(cz));
        }

        void dugTunnel(BlockPos from, Direction dir, int depth) {
            int width = 3;
            int height = 3;
            int length = 10 + random.nextInt(12);
            BlockPos pos = from;
            for (int i = 0; i < length; i++) {
                if (budget-- <= 0) {
                    deadEnd(pos, dir);
                    return;
                }
                BlockPos next = pos.relative(dir);
                if (!valid(next, height, i % CHECK_EVERY == 0)) {
                    deadEnd(pos, dir);
                    return;
                }
                BlockPos corner = next.relative(dir.getClockWise(), -(width / 2));
                carveColumn(corner, dir, width, height);
                if (i % 4 == 2) supports(corner, dir, width, height);
                pos = next;
                nodes.add(pos);
            }
            if (depth >= MAX_DEPTH || budget <= 0 || random.nextDouble() >= settings.branchChance()) {
                deadEnd(pos, dir);
                return;
            }
            room(pos, 2);
            List<Direction> exits = new ArrayList<>(List.of(dir, dir.getClockWise(), dir.getCounterClockWise()));
            shuffle(exits);
            int branches = 1 + (random.nextFloat() < 0.45F ? 1 : 0);
            for (int i = 0; i < branches && budget > 0; i++) {
                tunnel(pos, exits.get(i), depth + 1);
            }
        }

        boolean valid(BlockPos pos, int height, boolean sample) {
            if (pos.getY() < settings.minY() || pos.getY() > settings.maxY()) return false;
            if (Math.abs(pos.getX() - source.getMiddleBlockX()) > MAX_REACH || Math.abs(pos.getZ() - source.getMiddleBlockZ()) > MAX_REACH) return false;
            return !sample || density.applyAsDouble(pos.above(height / 2)) >= settings.margin() + SKIPPED_MARGIN;
        }

        void carveColumn(BlockPos pos, Direction dir, int width, int height) {
            Direction side = dir.getClockWise();
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    gas(pos.relative(side, w).above(h));
                }
            }
        }

        void room(BlockPos center, int radius) {
            if (density.applyAsDouble(center.above()) < settings.margin() + radius) return;
            int height = 3 + random.nextInt(2);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > radius + 0.5) continue;
                    int top = r > radius - 0.5 ? height - 1 : height;
                    for (int dy = 0; dy < top; dy++) {
                        gas(center.offset(dx, dy, dz));
                    }
                }
            }
        }

        void supports(BlockPos pos, Direction dir, int width, int height) {
            Direction side = dir.getClockWise();
            BlockState post = ModBlocks.IRONBARK.log().get().defaultBlockState();
            BlockState beam = post.setValue(RotatedPillarBlock.AXIS, side.getAxis());
            BlockPos left = pos.relative(side, -1);
            BlockPos right = pos.relative(side, width);
            for (int h = 0; h < height; h++) {
                prop(left.above(h), post, null, true);
                prop(right.above(h), post, null, true);
            }
            for (int w = -1; w <= width; w++) {
                prop(pos.relative(side, w).above(height), beam, null, true);
            }
        }

        void clusters() {
            Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.NORTH,
                    Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};
            for (BlockPos node : nodes) {
                if (random.nextDouble() >= settings.clusterChance() * 4.0) continue;
                Direction dir = directions[random.nextInt(directions.length)];
                BlockPos cursor = node.above(dir.getAxis() == Direction.Axis.Y ? 1 : random.nextInt(2));
                if (!network.gasSet.contains(cursor.asLong())) continue;
                for (int i = 0; i < 6 && network.gasSet.contains(cursor.relative(dir).asLong()); i++) cursor = cursor.relative(dir);
                BlockPos wall = cursor.relative(dir);
                if (network.gasSet.contains(wall.asLong())) continue;
                prop(cursor, QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState().setValue(FlickerShardClusterBlock.FACING, dir.getOpposite()),
                        null, false);
            }
        }
        void entrance(BlockPos from) {
            BlockPos pos = from;
            Direction dir = towardsEdge(pos);
            boolean opened = false;
            carvingEntrance = true;
            for (int i = 0; i < 90 && !opened; i++) {
                if (i % 6 == 5) dir = towardsEdge(pos);
                BlockPos next = pos.relative(dir);
                if (next.getY() < settings.minY() - 8) break;
                ellipsoid(next.getX() + 0.5, next.getY() + 1.2, next.getZ() + 0.5, 1.5F, 1.4F);
                if (density.applyAsDouble(next.above()) < 0.0) opened = true;
                pos = next;
            }
            carvingEntrance = false;
            if (opened) network.entrance = pos;
        }

        Direction towardsEdge(BlockPos pos) {
            Direction best = Direction.NORTH;
            double lowest = Double.MAX_VALUE;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                double d = density.applyAsDouble(pos.relative(dir, 12).above());
                if (d < lowest) {
                    lowest = d;
                    best = dir;
                }
            }
            return best;
        }


        void deadEnd(BlockPos pos, Direction dir) {
            DeadEnd kind = DeadEnd.NONE;
            if (random.nextDouble() < settings.deadEndFeatureChance()
                    && density.applyAsDouble(pos.relative(dir, 2).above()) >= settings.margin() + 2.0
                    && density.applyAsDouble(pos.relative(dir, 5).above()) >= settings.margin() + 1.0) {
                kind = DeadEnd.values()[1 + random.nextInt(DeadEnd.values().length - 1)];
            }
            network.deadEnds.add(pos);
            network.deadEndKinds.add(kind);
            switch (kind) {
                case CAMP -> camp(pos, dir);
                case VEIN -> vein(pos, dir);
                case CAVE_IN -> caveIn(pos, dir);
                case SKELETON -> skeleton(pos, dir);
                case NONE -> {}
            }
        }

        void camp(BlockPos pos, Direction dir) {
            Direction side = dir.getClockWise();
            for (int f = 0; f <= 2; f++) {
                for (int w = -1; w <= 1; w++) {
                    for (int h = 0; h < 3; h++) gas(pos.relative(dir, f).relative(side, w).above(h));
                }
            }
            BlockPos end = pos.relative(dir, 2);
            prop(end.relative(side, -1), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), QuantumLoot.GAS_CAVE_SUPPLY, false);
            if (random.nextBoolean()) prop(end.relative(side, -1).above(), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, dir.getOpposite()), null, false);
            prop(end.relative(side, 1), QuantumBlocks.SPENT_LANTERN.get().defaultBlockState(), null, false);
            prop(end, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, dir.getOpposite()), QuantumLoot.GAS_CAVE_SUPPLY, false);
            prop(pos.relative(dir), QuantumBlocks.ABANDONED_PICKAXE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random)), null, false);
            RailShape shape = dir.getAxis() == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
            int rails = 2 + random.nextInt(4);
            for (int i = 1; i <= rails; i++) {
                BlockPos rail = pos.relative(dir, -i);
                if (network.gasSet.contains(rail.asLong()) && random.nextFloat() < 0.85F) {
                    prop(rail, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape), null, false);
                }
            }
        }

        void vein(BlockPos pos, Direction dir) {
            Direction side = dir.getClockWise();
            int holes = 4 + random.nextInt(4);
            for (int i = 0; i < holes; i++) {
                Direction wall = switch (random.nextInt(3)) {
                    case 0 -> side;
                    case 1 -> side.getOpposite();
                    default -> dir;
                };
                BlockPos hole = pos.relative(dir, -random.nextInt(3)).relative(wall).above(random.nextInt(2));
                gas(hole);
                if (random.nextFloat() < 0.5F) gas(hole.relative(wall));
                if (random.nextFloat() < 0.45F) {
                    BlockPos back = random.nextFloat() < 0.5F && network.gasSet.contains(hole.relative(wall).asLong()) ? hole.relative(wall) : hole;
                    prop(back, QuantumBlocks.FLICKER_SHARD_CLUSTER.get().defaultBlockState()
                            .setValue(FlickerShardClusterBlock.FACING, wall.getOpposite()), null, false);
                }
            }
            if (random.nextFloat() < 0.4F) {
                prop(pos, QuantumBlocks.ABANDONED_PICKAXE.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, side), null, false);
            }
        }

        void caveIn(BlockPos pos, Direction dir) {
            Direction side = dir.getClockWise();
            for (int f = 1; f <= 5; f++) {
                for (int h = 0; h < 2; h++) gas(pos.relative(dir, f).above(h));
            }
            BlockState[] rubble = {ModBlocks.COBBLED_PEARLSTONE.get().defaultBlockState(), ModBlocks.COBBLED_DUSKSLATE.get().defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState(), ModBlocks.PEARLSTONE.get().defaultBlockState()};
            for (int f = 2; f <= 3; f++) {
                for (int h = 0; h < 2; h++) prop(pos.relative(dir, f).above(h), rubble[random.nextInt(rubble.length)], null, false);
            }
            prop(pos.relative(dir).above(), rubble[random.nextInt(2)], null, false);
            BlockState brokenBeam = ModBlocks.IRONBARK.strippedLog().get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, dir.getAxis());
            prop(pos, brokenBeam, null, false);
            prop(pos.relative(dir, -1).above(2), ModBlocks.IRONBARK.fence().get().defaultBlockState()
                    .setValue(FenceBlock.WATERLOGGED, false), null, false);
            prop(pos.relative(side, -1), ModBlocks.IRONBARK.log().get().defaultBlockState(), null, true);
            prop(pos.relative(side, -1).above(), ModBlocks.IRONBARK.log().get().defaultBlockState(), null, true);
            if (random.nextFloat() < 0.55F) {
                prop(pos.relative(dir, 5), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, dir.getOpposite()), QuantumLoot.GAS_CAVE_SUPPLY, false);
            }
        }

        void skeleton(BlockPos pos, Direction dir) {
            prop(pos, QuantumBlocks.SCATTERED_BONES.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dir), null, false);
            BlockPos skull = pos.relative(dir, -1);
            prop(skull, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION,
                    RotationSegment.convertToSegment(random.nextFloat() * 360.0F)), null, false);
            prop(pos.relative(dir, -2), QuantumBlocks.CRACKED_VEIL_MASK.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, dir.getOpposite()), null, false);
            if (random.nextFloat() < 0.5F) {
                prop(pos.relative(dir, -3), QuantumBlocks.SPENT_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, false), null, false);
            }
        }


        void gas(BlockPos pos) {
            long key = pos.asLong();
            if (network.gasSet.contains(key)) return;
            if (!carvingEntrance && !(density.applyAsDouble(pos) > 1.0 && density.applyAsDouble(pos.above(3)) > 0.5
                    && density.applyAsDouble(pos.below(2)) > 0.5)) return;
            if (carvingEntrance) network.entranceSet.add(key);
            if (network.gasSet.add(key)) {
                network.gas.computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), k -> new LongArrayList()).add(key);
            }
        }

        void prop(BlockPos pos, BlockState state, ResourceKey<LootTable> loot, boolean solidOnly) {
            network.props.computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), k -> new ArrayList<>())
                    .add(new Prop(pos.asLong(), state, loot, solidOnly));
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
