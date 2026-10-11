package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.engine.EngineBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksLoot;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StalledAutomatonFeature extends RustFeature {
    public enum Pose { KNEELING, TOPPLED, BURIED }

    private enum L { F, U, Y }

    private static final int FOOTPRINT = 4;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        FeatureRegion region = new FeatureRegion(context.level(), origin);
        if (!flat(region, origin)) return false;
        Pose pose = Pose.values()[random.nextInt(Pose.values().length)];
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        build(context.level(), random, origin, facing, pose, random.nextInt(5) < 2);
        return true;
    }

    @Nullable
    public static BlockPos build(WorldGenLevel level, RandomSource random, BlockPos origin, Direction facing, Pose pose, boolean withCache) {
        Frame frame = new Frame(new FeatureRegion(level, origin), random, origin, facing);
        BlockPos cache = switch (pose) {
            case KNEELING -> kneeling(frame, withCache);
            case TOPPLED -> toppled(frame, withCache);
            case BURIED -> buried(frame, withCache);
        };
        debris(frame);
        if (random.nextInt(5) < 2) {
            BlockPos plate = frame.at(4 + random.nextInt(2), random.nextInt(5) - 2, -1);
            if (frame.region.isOpen(plate.above()) && !frame.region.isOpen(plate)) {
                frame.region.set(plate, EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState());
            }
        }
        if (cache != null) {
            RandomizableContainer.setBlockEntityLootTable(level, random, cache, RustworksLoot.AUTOMATON);
        }
        return cache;
    }

    private static boolean flat(FeatureRegion region, BlockPos origin) {
        int good = 0;
        int total = 0;
        for (int dx = -FOOTPRINT; dx <= FOOTPRINT; dx++) {
            for (int dz = -FOOTPRINT; dz <= FOOTPRINT; dz++) {
                int ground = ground(region, origin.getX() + dx, origin.getZ() + dz, origin.getY());
                boolean ok = Math.abs(ground - origin.getY()) <= 1;
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2 && !ok) return false;
                total++;
                if (ok) good++;
            }
        }
        if (good * 10 < total * 8) return false;
        for (int y = 1; y <= 8; y++) {
            if (!region.isOpen(origin.above(y))) return false;
        }
        return true;
    }

    private static int ground(FeatureRegion region, int x, int z, int near) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, near + 3, z);
        for (int y = near + 3; y >= near - 4; y--) {
            cursor.setY(y);
            if (!region.isOpen(cursor) && region.get(cursor).isSolid()) {
                return y + 1;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Nullable
    private static BlockPos kneeling(Frame b, boolean withCache) {
        b.solid(0, 1, 0, cog(b.axis(L.U)));
        b.limb(-1, 1, 0, pipe(b.axis(L.F)));
        b.solid(-2, 1, 0, plating());
        b.solid(2, -1, 0, plating());
        b.limb(3, -1, 0, plating());
        b.solid(0, 1, 1, pipe(Direction.Axis.Y));
        b.solid(2, -1, 1, pipe(Direction.Axis.Y));
        b.solid(2, -1, 2, cog(b.axis(L.U)));
        b.limb(1, -1, 2, pipe(b.axis(L.F)));
        for (int u = -1; u <= 1; u++) {
            b.solid(0, u, 2, cogPlating());
        }
        b.solid(-1, 0, 2, plating());
        for (int y = 3; y <= 4; y++) {
            for (int u = -1; u <= 1; u++) {
                b.solid(0, u, y, plating());
                b.solid(-1, u, y, plating());
            }
        }
        b.solid(0, 0, 3, cog(b.axis(L.F)));
        b.solid(0, 0, 4, cogPlating());
        b.solid(-2, 0, 3, plating());
        b.solid(-2, 0, 4, pipe(Direction.Axis.Y));
        b.limb(-2, 0, 5, pipe(Direction.Axis.Y));
        b.limb(-2, 0, 6, leakingPipe(Direction.Axis.Y));
        b.solid(0, -2, 4, cog(b.axis(L.U)));
        b.solid(0, 2, 4, cog(b.axis(L.U)));
        b.solid(0, -2, 3, pipe(Direction.Axis.Y));
        b.solid(0, -2, 2, pipe(Direction.Axis.Y));
        b.limb(0, -2, 1, bars(b, L.F));
        b.solid(0, 2, 3, pipe(Direction.Axis.Y));
        b.solid(0, 2, 2, pipe(Direction.Axis.Y));
        b.solid(0, 2, 1, cog(b.axis(L.F)));
        b.solid(0, 2, 0, cogPlating());
        b.limb(1, 2, 0, bars(b, L.F));
        b.solid(0, 0, 5, plating());
        b.solid(1, 0, 5, eye());
        b.solid(0, 0, 6, cogPlating());
        b.limb(1, 0, 6, plating());
        b.limb(-1, 0, 5, chain(Direction.Axis.Y));
        b.support();
        return b.cache(withCache, -1, 0, 3);
    }

    @Nullable
    private static BlockPos toppled(Frame b, boolean withCache) {
        Direction.Axis along = b.axis(L.F);
        Direction.Axis depth = b.axis(L.U);
        int sink = -1;
        b.solid(-4, 0, sink, plating());
        b.solid(-4, 0, sink + 2, plating());
        for (int h = -3; h <= -2; h++) {
            b.limb(h, 0, sink, pipe(along));
            b.limb(h, 0, sink + 2, pipe(along));
        }
        b.solid(-1, 0, sink + 1, cogPlating());
        b.solid(-1, 0, sink, cog(Direction.Axis.Y));
        b.solid(-1, 0, sink + 2, cog(Direction.Axis.Y));
        for (int h = 0; h <= 1; h++) {
            for (int a = 0; a <= 2; a++) {
                for (int d = -1; d <= 1; d++) {
                    b.solid(h, d, sink + a, plating());
                }
            }
        }
        b.solid(0, 1, sink + 1, cog(depth));
        b.solid(1, 1, sink + 1, cogPlating());
        b.limb(0, -2, sink + 1, pipe(along));
        b.limb(1, -2, sink + 1, leakingPipe(along));
        b.solid(1, 0, sink + 3, cog(Direction.Axis.Y));
        b.solid(1, 0, sink + 4, pipe(Direction.Axis.Y));
        b.solid(1, 0, sink + 5, cog(depth));
        b.solid(2, 0, sink + 5, pipe(along));
        b.limb(3, 0, sink + 5, cogPlating());
        b.limb(3, 0, sink + 6, bars(b, L.U));
        b.limb(4, 0, sink + 5, bars(b, L.F));
        b.limb(2, 0, sink + 4, chain(Direction.Axis.Y));
        b.solid(2, 0, sink + 1, pipe(along));
        b.solid(3, 0, sink + 1, plating());
        b.solid(3, 1, sink + 1, eye());
        b.solid(4, 0, sink + 1, cogPlating());
        b.limb(3, 0, sink + 2, plating());
        b.support();
        return b.cache(withCache, 0, 0, sink + 1);
    }

    @Nullable
    private static BlockPos buried(Frame b, boolean withCache) {
        for (int y = -3; y <= 1; y++) {
            for (int f = -1; f <= 1; f++) {
                for (int u = -1; u <= 1; u++) {
                    if (y == 1 && Math.abs(f) == 1 && Math.abs(u) == 1) continue;
                    b.solid(f, u, y, y == 1 ? cogPlating() : plating());
                }
            }
        }
        b.solid(1, 0, 0, eye());
        b.limb(1, -1, 0, bars(b, L.U));
        b.limb(0, 0, 2, pipe(Direction.Axis.Y));
        b.limb(0, 0, 3, chain(Direction.Axis.Y));
        b.solid(0, 2, -1, cogPlating());
        b.limb(0, 2, 0, plating());
        b.solid(0, 3, -1, plating());
        for (int y = -1; y <= 3; y++) {
            b.solid(0, 4, y, pipe(Direction.Axis.Y));
        }
        b.solid(0, 4, 4, cog(b.axis(L.F)));
        b.solid(1, 4, 4, pipe(b.axis(L.F)));
        b.solid(2, 4, 4, pipe(b.axis(L.F)));
        b.limb(3, 4, 4, cogPlating());
        b.limb(3, 4, 5, bars(b, L.F));
        b.limb(4, 4, 4, bars(b, L.F));
        b.limb(2, 4, 3, chain(Direction.Axis.Y));
        return b.cache(withCache, 0, 0, -1);
    }

    private static void debris(Frame b) {
        int count = 2 + b.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            int f = b.random.nextInt(11) - 5;
            int u = b.random.nextInt(11) - 5;
            if (Math.abs(f) <= 2 && Math.abs(u) <= 2) continue;
            BlockPos pos = b.at(f, u, 0);
            int ground = ground(b.region, pos.getX(), pos.getZ(), pos.getY());
            if (ground == Integer.MIN_VALUE) continue;
            pos = new BlockPos(pos.getX(), ground, pos.getZ());
            BlockState state = switch (b.random.nextInt(5)) {
                case 0 -> cog(b.random.nextBoolean() ? b.axis(L.F) : b.axis(L.U));
                case 1 -> plating();
                case 2 -> pipe(b.random.nextBoolean() ? b.axis(L.F) : b.axis(L.U));
                case 3 -> chain(b.random.nextBoolean() ? b.axis(L.F) : b.axis(L.U));
                default -> RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState();
            };
            b.region.setIfOpen(pos, state);
        }
    }

    private static BlockState eye() {
        return Blocks.WAXED_OXIDIZED_COPPER_BULB.defaultBlockState();
    }

    private static BlockState chain(Direction.Axis axis) {
        return Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, axis);
    }

    private static BlockState bars(Frame b, L along) {
        BlockState state = Blocks.IRON_BARS.defaultBlockState();
        Direction dir = along == L.F ? b.facing : b.facing.getClockWise();
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true)
                .setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir.getOpposite()), true);
    }

    private static final class Frame {
        final FeatureRegion region;
        final RandomSource random;
        final BlockPos base;
        final Direction facing;
        final Direction right;
        final List<BlockPos> placed = new ArrayList<>();

        Frame(FeatureRegion region, RandomSource random, BlockPos base, Direction facing) {
            this.region = region;
            this.random = random;
            this.base = base;
            this.facing = facing;
            this.right = facing.getClockWise();
        }

        BlockPos at(int f, int u, int y) {
            return base.relative(facing, f).relative(right, u).above(y);
        }

        Direction.Axis axis(L local) {
            return switch (local) {
                case F -> facing.getAxis();
                case U -> right.getAxis();
                case Y -> Direction.Axis.Y;
            };
        }

        void solid(int f, int u, int y, BlockState state) {
            BlockPos pos = at(f, u, y);
            region.set(pos, state);
            placed.add(pos);
        }

        void limb(int f, int u, int y, BlockState state) {
            if (random.nextInt(10) != 0) {
                solid(f, u, y, state);
            }
        }

        void support() {
            for (BlockPos pos : placed) {
                if (pos.getY() > base.getY()) continue;
                BlockPos.MutableBlockPos cursor = pos.mutable();
                for (int i = 1; i <= 3; i++) {
                    cursor.setY(pos.getY() - i);
                    if (!region.isOpen(cursor)) break;
                    region.set(cursor, RustworksBlocks.RUSTSLATE.get().defaultBlockState());
                }
            }
        }

        @Nullable
        BlockPos cache(boolean withCache, int f, int u, int y) {
            if (!withCache) return null;
            BlockPos pos = at(f, u, y);
            if (!region.contains(pos)) return null;
            region.set(pos, ModBlocks.SKY_CACHE.get().defaultBlockState());
            return pos;
        }
    }
}
