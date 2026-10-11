package net.itsthesky.upfall.content.engine.worldgen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.itsthesky.upfall.content.engine.EngineBlocks;
import net.itsthesky.upfall.content.engine.EngineBooks;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.block.PipeValveBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StalledEngineLayout {
    public static final int HX = 12;
    public static final int HZ = 9;
    public static final int WALL_H = 9;
    public static final int ROOF_Y = 10;
    public static final int PIPE_Y = 3;
    public static final int SPIRE_Y = 4;
    public static final int SPIRE_LENGTH = 38;
    public static final int FOUNDATION_DEPTH = 12;
    public static final int SUPPORT_DEPTH = 24;

    public enum ColumnKind { FOUNDATION, SUPPORT, OUTFLOW }

    public record Column(int x, int z, int topY, ColumnKind kind) {
    }

    private final BlockPos origin;
    private final int rotation;
    private final long seed;
    private final int outflowDir;
    private final int outflowLength;
    private final float spireAngle;
    private final Rotation rot;
    private final RandomSource random;

    private final Long2ObjectOpenHashMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
    private final List<Column> columns = new ArrayList<>();
    private final List<BlockPos> caches = new ArrayList<>();
    private final Map<BlockPos, EngineBooks.Book> lecterns = new LinkedHashMap<>();
    private final List<BlockPos> valves = new ArrayList<>();
    private final List<BoundingBox> flood = new ArrayList<>();
    private BlockPos vaultChest;
    private BlockPos regulator;
    private BlockPos heartgear;
    private final List<BlockPos> spirePipe = new ArrayList<>();
    private final BoundingBox bounds;

    public StalledEngineLayout(BlockPos origin, int rotation, long seed, int outflowDir, int outflowLength, float spireAngle) {
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        this.outflowDir = outflowDir;
        this.outflowLength = outflowLength;
        this.spireAngle = spireAngle;
        this.rot = Rotation.values()[Math.floorMod(rotation, 4)];
        this.random = new XoroshiroRandomSource(seed);
        build();
        this.bounds = computeBounds();
    }

    public BlockPos origin() {
        return origin;
    }

    public int rotation() {
        return rotation;
    }

    public long seed() {
        return seed;
    }

    public int outflowDir() {
        return outflowDir;
    }

    public int outflowLength() {
        return outflowLength;
    }

    public float spireAngle() {
        return spireAngle;
    }

    public Long2ObjectMap<BlockState> blocks() {
        return blocks;
    }

    public List<Column> columns() {
        return columns;
    }

    public List<BlockPos> caches() {
        return caches;
    }

    public Map<BlockPos, EngineBooks.Book> lecterns() {
        return lecterns;
    }

    public List<BlockPos> valves() {
        return valves;
    }

    public List<BoundingBox> flood() {
        return flood;
    }

    public BlockPos vaultChest() {
        return vaultChest;
    }

    public BlockPos regulator() {
        return regulator;
    }

    public BlockPos heartgear() {
        return heartgear;
    }

    public List<BlockPos> spirePipe() {
        return spirePipe;
    }

    public BoundingBox bounds() {
        return bounds;
    }

    public BlockState stateAt(BlockPos pos) {
        return blocks.get(pos.asLong());
    }

    private void build() {
        clearInterior();
        floor();
        walls();
        roof();
        rubble();
        catwalks();
        office();
        heartgearMachine();
        tanks();
        vault();
        wallPipes();
        foundations();
        outflow();
        spireMain();
    }

    private BlockPos at(int lx, int ly, int lz) {
        return switch (rot) {
            case CLOCKWISE_90 -> origin.offset(-lz, ly, lx);
            case CLOCKWISE_180 -> origin.offset(-lx, ly, -lz);
            case COUNTERCLOCKWISE_90 -> origin.offset(lz, ly, -lx);
            default -> origin.offset(lx, ly, lz);
        };
    }

    private void set(int lx, int ly, int lz, BlockState state) {
        blocks.put(at(lx, ly, lz).asLong(), state.rotate(rot));
    }

    private BlockState get(int lx, int ly, int lz) {
        return blocks.get(at(lx, ly, lz).asLong());
    }

    private boolean present(int lx, int ly, int lz) {
        BlockState state = get(lx, ly, lz);
        return state != null && !state.isAir();
    }

    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }

    private static BlockState plating() {
        return RustworksBlocks.RUSTY_PLATING.get().defaultBlockState();
    }

    private static BlockState pipe(Direction.Axis axis) {
        return RustworksBlocks.RUSTY_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState leaking(Direction.Axis axis) {
        return RustworksBlocks.LEAKING_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState cogPlating() {
        return RustworksBlocks.COG_PLATING.get().defaultBlockState();
    }

    private static BlockState cog(Direction.Axis axis) {
        return RustworksBlocks.RUSTY_COG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState bars(boolean alongX) {
        return Blocks.IRON_BARS.defaultBlockState().setValue(alongX ? IronBarsBlock.EAST : IronBarsBlock.NORTH, true)
                .setValue(alongX ? IronBarsBlock.WEST : IronBarsBlock.SOUTH, true);
    }

    private static BlockState grating() {
        return Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP);
    }

    private static BlockState facing(BlockState state, Direction direction) {
        return state.setValue(HorizontalDirectionalBlock.FACING, direction);
    }

    private static double collapse(int lx, int lz) {
        return Mth.clamp((lx / (double) HX - lz / (double) HZ) / 2.0, 0.0, 1.0);
    }

    private void clearInterior() {
        for (int lx = -HX + 1; lx <= HX - 1; lx++) {
            for (int lz = -HZ + 1; lz <= HZ - 1; lz++) {
                for (int ly = 1; ly <= ROOF_Y + 2; ly++) {
                    set(lx, ly, lz, air());
                }
            }
        }
    }

    private void floor() {
        for (int lx = -HX; lx <= HX; lx++) {
            for (int lz = -HZ; lz <= HZ; lz++) {
                BlockState state;
                float r = random.nextFloat();
                if (lx == 0 || lz == 0) {
                    state = plating();
                } else if (collapse(lx, lz) > 0.35 && r < 0.12) {
                    state = RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState();
                } else if (r < 0.7) {
                    state = RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState();
                } else if (r < 0.92) {
                    state = RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState();
                } else {
                    state = RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState();
                }
                set(lx, 0, lz, state);
            }
        }
    }

    private void walls() {
        for (int lx = -HX; lx <= HX; lx++) {
            for (int lz = -HZ; lz <= HZ; lz++) {
                boolean northSouth = Math.abs(lz) == HZ;
                if (!northSouth && Math.abs(lx) != HX) continue;
                boolean protect = lz == HZ && Math.abs(lx) <= 5;
                double c = collapse(lx, lz);
                int height = protect ? WALL_H : Mth.clamp(WALL_H - (int) Math.round(c * 7 + random.nextFloat() * 2.2 - 0.6), 2, WALL_H);
                boolean pilaster = northSouth ? Math.floorMod(lx, 4) == 0 : Math.floorMod(lz, 3) == 0;
                boolean window = northSouth ? Math.floorMod(lx, 4) == 2 : Math.floorMod(lz, 3) == 1;
                for (int ly = 1; ly <= height; ly++) {
                    float r = random.nextFloat();
                    BlockState state;
                    if (pilaster || Math.abs(lx) == HX && northSouth) {
                        state = plating();
                    } else if (ly == 1 || ly == 5) {
                        state = RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState();
                    } else if (window && !protect && (ly == 6 || ly == 7)) {
                        state = bars(northSouth);
                    } else if (!protect && ly >= 3 && r < 0.04) {
                        state = air();
                    } else if (r < 0.2) {
                        state = RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState();
                    } else {
                        state = RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState();
                    }
                    set(lx, ly, lz, state);
                }
                if (height < WALL_H && random.nextFloat() < 0.4F) {
                    set(lx, height + 1, lz, RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState());
                }
            }
        }
    }

    private void roof() {
        for (int lx = -HX + 1; lx <= HX - 1; lx++) {
            boolean beam = Math.floorMod(lx, 4) == 0 && Math.abs(lx) <= 8;
            for (int lz = -HZ; lz <= HZ; lz++) {
                double c = collapse(lx, lz);
                if (beam) {
                    if (Math.abs(lz) == HZ && !present(lx, WALL_H, lz)) continue;
                    if (random.nextFloat() > c * 1.1 + 0.08) {
                        set(lx, ROOF_Y, lz, plating());
                    }
                } else if (Math.abs(lz) < HZ && random.nextFloat() > 0.5 + c * 0.6) {
                    set(lx, ROOF_Y, lz, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.TOP));
                }
            }
        }
    }

    private void rubble() {
        BlockState[] pieces = {
                RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState(),
                RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState(),
                RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState(),
                plating()
        };
        for (int lx = -HX + 1; lx <= HX - 1; lx++) {
            for (int lz = -HZ + 1; lz <= HZ - 1; lz++) {
                double c = collapse(lx, lz);
                if (c < 0.25 || random.nextFloat() > c * 0.28) continue;
                if (random.nextInt(4) == 0) {
                    set(lx, 1, lz, facing(RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.stairs().get().defaultBlockState(),
                            Direction.Plane.HORIZONTAL.getRandomDirection(random)));
                } else {
                    set(lx, 1, lz, pieces[random.nextInt(pieces.length)]);
                    if (random.nextFloat() < c * 0.3) {
                        set(lx, 2, lz, RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState());
                    }
                }
            }
        }
    }

    private void catwalks() {
        for (int lx = -HX + 1; lx <= HX - 1; lx++) {
            boolean keep = lx >= -6 && lx <= 0 || lx <= -9;
            boolean walk = keep || random.nextFloat() > collapse(lx, -7) * 1.3 - 0.1;
            if (!walk) continue;
            for (int lz = -8; lz <= -6; lz++) {
                set(lx, 5, lz, grating());
            }
            if (lx > -10) {
                set(lx, 6, -6, bars(true));
            }
            if (Math.floorMod(lx, 4) == 1 && lx > -10) {
                for (int ly = 1; ly <= 4; ly++) {
                    set(lx, ly, -6, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS.wall().get().defaultBlockState());
                }
            }
            if (Math.floorMod(lx, 4) == 0 && Math.abs(lx) <= 8 && present(lx, ROOF_Y, -6)) {
                for (int ly = 7; ly < ROOF_Y; ly++) {
                    set(lx, ly, -6, Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y));
                }
            }
        }
        for (int ly = 1; ly <= 6; ly++) {
            set(-HX, ly, -5, RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState());
            set(-HX + 1, ly, -5, facing(Blocks.LADDER.defaultBlockState(), Direction.EAST));
        }

        for (int lz = -5; lz <= 7; lz++) {
            boolean walk = lz >= 2 || random.nextFloat() > collapse(10, lz) * 1.3 - 0.1;
            if (!walk) continue;
            for (int lx = 9; lx <= HX - 1; lx++) {
                set(lx, 5, lz, grating());
            }
            set(9, 6, lz, bars(false));
            if (Math.floorMod(lz, 4) == 1) {
                for (int ly = 1; ly <= 4; ly++) {
                    set(9, ly, lz, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS.wall().get().defaultBlockState());
                }
            }
        }
        for (int ly = 1; ly <= 6; ly++) {
            set(HX - 1, ly, HZ - 1, facing(Blocks.LADDER.defaultBlockState(), Direction.NORTH));
            set(HX - 1, ly, HZ, RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState());
        }
        set(HX - 1, 6, 4, ModBlocks.SKY_CACHE.get().defaultBlockState());
        caches.add(at(HX - 1, 6, 4));
        set(HX, 6, 4, plating());
        set(HX, 7, 4, plating());

        set(-3, 7, -HZ, RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState());
        set(-3, 7, -HZ + 1, valve(Direction.SOUTH));
        valves.add(at(-3, 7, -HZ + 1));
    }

    private static BlockState valve(Direction facing) {
        return EngineBlocks.DRAIN_VALVE.get().defaultBlockState().setValue(DrainValveBlock.FACING, facing);
    }

    private void office() {
        BlockState polished = RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState();
        for (int ly = 1; ly <= 4; ly++) {
            for (int lz = 2; lz <= 8; lz++) {
                set(-6, ly, lz, random.nextFloat() < 0.12F ? RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState() : polished);
            }
            for (int lx = -11; lx <= -7; lx++) {
                set(lx, ly, 2, random.nextFloat() < 0.12F ? RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState() : polished);
            }
        }
        for (int lx = -11; lx <= -6; lx++) {
            for (int lz = 2; lz <= 8; lz++) {
                set(lx, 5, lz, RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState());
            }
        }
        set(-9, 1, 2, air());
        set(-9, 2, 2, air());
        set(-10, 1, 7, lectern(Direction.NORTH));
        lecterns.put(at(-10, 1, 7), EngineBooks.FOREMAN_LOG);
        set(-8, 1, 8, ModBlocks.SKY_CACHE.get().defaultBlockState());
        caches.add(at(-8, 1, 8));
        set(-7, 1, 8, RustworksBlocks.RUSTY_COG.get().defaultBlockState());
        set(-HX, 2, 5, polished);
        set(-HX + 1, 2, 5, valve(Direction.EAST));
        valves.add(at(-HX + 1, 2, 5));
        set(-HX, 3, 7, EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState());
    }

    private static BlockState lectern(Direction direction) {
        return Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, direction).setValue(LecternBlock.HAS_BOOK, true);
    }

    private void heartgearMachine() {
        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                boolean side = (lx == 0) != (lz == 0) && (Math.abs(lx) == 2 || Math.abs(lz) == 2);
                set(lx, 1, lz, side ? EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState()
                        : RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState());
                if (Math.abs(lx) <= 1 && Math.abs(lz) <= 1) {
                    set(lx, 2, lz, plating());
                }
            }
        }
        double broken = random.nextDouble() * Math.PI * 2;
        List<int[]> fallen = new ArrayList<>();
        for (int gx = -4; gx <= 4; gx++) {
            for (int gy = 1; gy <= 9; gy++) {
                double d = Math.hypot(gx, gy - 5);
                if (d <= 2.5 || d > 3.5) continue;
                double angle = Math.atan2(gy - 5, gx);
                double diff = Math.abs(Mth.wrapDegrees(Math.toDegrees(angle - broken)));
                if (diff < 50) {
                    fallen.add(new int[]{gx, gy});
                    continue;
                }
                set(gx, gy, 0, cogPlating());
            }
        }
        int[][] spokes = {{1, 5}, {2, 5}, {-1, 5}, {-2, 5}, {0, 3}, {0, 4}, {0, 6}, {0, 7}};
        int leak = random.nextInt(spokes.length);
        for (int i = 0; i < spokes.length; i++) {
            Direction.Axis axis = spokes[i][1] == 5 ? Direction.Axis.X : Direction.Axis.Y;
            set(spokes[i][0], spokes[i][1], 0, i == leak ? leaking(axis) : pipe(axis));
        }
        set(0, 9, 0, pipe(Direction.Axis.Y));
        set(0, 5, 0, RustworksBlocks.HEARTGEAR.get().defaultBlockState());
        heartgear = at(0, 5, 0);
        for (int i = 0; i < fallen.size(); i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 3.2 + random.nextDouble() * 2.5;
            int fx = (int) Math.round(Math.cos(a) * r);
            int fz = (int) Math.round(Math.sin(a) * r);
            if (Math.abs(fx) <= 1 && fz >= 3 || Math.abs(fx) <= 2 && Math.abs(fz) <= 2) continue;
            set(fx, 1, fz, cog(Direction.Axis.values()[random.nextInt(3)]));
        }
        set(0, 1, 4, lectern(Direction.SOUTH));
        lecterns.put(at(0, 1, 4), EngineBooks.ENGINEER_NOTES);
    }

    private void tanks() {
        tank(-6, false);
        tank(6, true);
        for (int ly = 1; ly <= 2; ly++) {
            for (int lz = -11; lz <= -9; lz++) {
                set(-6, ly, lz, air());
            }
            set(9, ly, -14, air());
            set(10, ly, -14, air());
        }
        set(-6, 0, -10, plating());
        set(10, 0, -14, RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState());
        for (int ly = 1; ly <= 8; ly++) {
            set(-6, ly, -14, pipe(Direction.Axis.Y));
        }
        set(-6, 2, -13, valve(Direction.SOUTH));
        valves.add(at(-6, 2, -13));
        set(6, 1, -14, pipe(Direction.Axis.Y));
        set(6, 2, -14, leaking(Direction.Axis.Y));
        set(5, 1, -14, ModBlocks.SKY_CACHE.get().defaultBlockState());
        caches.add(at(5, 1, -14));
        set(-6, PIPE_Y, -10, pipe(Direction.Axis.Z));
        set(6, PIPE_Y, -10, pipe(Direction.Axis.Z));
        set(6, PIPE_Y + 1, -10, RustworksBlocks.PIPE_VALVE.get().defaultBlockState().setValue(PipeValveBlock.FACING, Direction.UP));
    }

    private void tank(int cx, boolean ruined) {
        int cz = -14;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > 3.61) continue;
                int lx = cx + dx;
                int lz = cz + dz;
                set(lx, 0, lz, plating());
                boolean shell = d > 2.5;
                int height = ruined ? 3 + random.nextInt(3) : 8;
                for (int ly = 1; ly <= 9; ly++) {
                    if (shell && ly <= height) {
                        set(lx, ly, lz, ly == 4 && !ruined ? RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState() : plating());
                    } else if (!shell && ly <= 8) {
                        set(lx, ly, lz, air());
                    }
                }
                if (!ruined) {
                    if (shell) {
                        set(lx, 9, lz, plating());
                    } else if (random.nextFloat() < 0.6F) {
                        set(lx, 9, lz, grating());
                    }
                } else if (!shell && random.nextFloat() < 0.15F) {
                    set(lx, 1, lz, random.nextBoolean() ? plating() : RustworksBlocks.COBBLED_RUSTSLATE_VARIANTS.slab().get().defaultBlockState());
                }
            }
        }
    }

    private void vault() {
        BlockState mist = EngineBlocks.STAGNANT_MIST.get().defaultBlockState();
        for (int lx = -4; lx <= 4; lx++) {
            for (int lz = HZ; lz <= 16; lz++) {
                if (lz > HZ) {
                    set(lx, 0, lz, plating());
                }
                set(lx, 6, lz, plating());
                for (int ly = 1; ly <= 5; ly++) {
                    boolean shell = Math.abs(lx) == 4 || lz == 16;
                    if (lz == HZ) {
                        if (!present(lx, ly, lz)) set(lx, ly, lz, RustworksBlocks.RUSTSLATE_BRICKS.get().defaultBlockState());
                    } else if (shell) {
                        set(lx, ly, lz, Math.abs(lx) == 4 && lz == 16 ? RustworksBlocks.POLISHED_RUSTSLATE.get().defaultBlockState() : plating());
                    } else {
                        set(lx, ly, lz, mist);
                    }
                }
            }
        }
        for (int lx = -1; lx <= 1; lx++) {
            for (int ly = 1; ly <= 3; ly++) {
                set(lx, ly, HZ, mist);
            }
        }
        set(0, 4, HZ, EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState());
        set(-3, 2, HZ, EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState());
        set(3, 2, HZ, EngineBlocks.DRAIN_REGULATOR.get().defaultBlockState().setValue(DrainRegulatorBlock.FACING, Direction.NORTH));
        regulator = at(3, 2, HZ);
        set(0, 1, 15, ModBlocks.SKY_CACHE.get().defaultBlockState());
        vaultChest = at(0, 1, 15);
        set(-2, 1, 15, lectern(Direction.NORTH));
        lecterns.put(at(-2, 1, 15), EngineBooks.VAULT_LETTER);
        set(2, 1, 15, cog(Direction.Axis.Y));
        set(3, 1, 15, pipe(Direction.Axis.Y));
        set(3, 2, 15, leaking(Direction.Axis.Y));
        flood.add(BoundingBox.fromCorners(at(-3, 1, HZ + 1), at(3, 5, 15)));
        flood.add(BoundingBox.fromCorners(at(-1, 1, HZ), at(1, 3, HZ)));
    }

    private void wallPipes() {
        for (int lz = -5; lz <= HZ - 1; lz++) {
            if (random.nextFloat() < 0.08F) continue;
            set(-HX + 1, 7, lz, random.nextFloat() < 0.12F ? leaking(Direction.Axis.Z) : pipe(Direction.Axis.Z));
        }
        set(-HX + 2, 7, -2, RustworksBlocks.PIPE_VALVE.get().defaultBlockState().setValue(PipeValveBlock.FACING, Direction.EAST));
        set(-HX + 2, 7, 5, RustworksBlocks.PIPE_VALVE.get().defaultBlockState().setValue(PipeValveBlock.FACING, Direction.EAST));
        for (int lx = -HX + 1; lx <= HX - 1; lx++) {
            if (random.nextFloat() < collapse(lx, -8) * 1.2) continue;
            set(lx, 8, -HZ + 1, random.nextFloat() < 0.1F ? leaking(Direction.Axis.X) : pipe(Direction.Axis.X));
        }
    }

    private void foundations() {
        for (Long2ObjectMap.Entry<BlockState> entry : blocks.long2ObjectEntrySet()) {
            long key = entry.getLongKey();
            if (BlockPos.getY(key) == origin.getY() && !entry.getValue().isAir()) {
                columns.add(new Column(BlockPos.getX(key), BlockPos.getZ(key), origin.getY() - 1, ColumnKind.FOUNDATION));
            }
        }
    }

    private void outflow() {
        if (outflowDir < 0) {
            for (int lz : new int[]{-2, 2}) {
                for (int lx = HX + 1; lx <= HX + 2; lx++) {
                    set(lx, PIPE_Y, lz, pipe(Direction.Axis.X));
                }
                set(HX + 3, PIPE_Y, lz, pipe(Direction.Axis.Y));
                BlockPos down = at(HX + 3, PIPE_Y - 1, lz);
                columns.add(new Column(down.getX(), down.getZ(), down.getY(), ColumnKind.OUTFLOW));
            }
            return;
        }
        Direction dir = Direction.from2DDataValue(outflowDir);
        Direction side = dir.getClockWise();
        int y = origin.getY() + PIPE_Y;
        for (int k : new int[]{-2, 2}) {
            int end = outflowLength + 2 + (k > 0 ? 1 : 0);
            for (int r = 0; r <= end; r++) {
                BlockPos pos = origin.relative(dir, r).relative(side, k).atY(y);
                if (blocks.containsKey(pos.asLong())) continue;
                blocks.put(pos.asLong(), r == end ? pipe(Direction.Axis.Y) : pipe(dir.getAxis()));
                if (r % 5 == 0 && r < outflowLength - 1) {
                    columns.add(new Column(pos.getX(), pos.getZ(), y - 1, ColumnKind.SUPPORT));
                }
            }
            BlockPos down = origin.relative(dir, end).relative(side, k);
            columns.add(new Column(down.getX(), down.getZ(), y - 1, ColumnKind.OUTFLOW));
        }
    }

    private void spireMain() {
        double angle = Float.isNaN(spireAngle) ? random.nextDouble() * Math.PI * 2 : spireAngle;
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        int y = origin.getY() + SPIRE_Y;
        List<int[]> cells = new ArrayList<>();
        int cx = 0;
        int cz = 0;
        cells.add(new int[]{0, 0, Math.abs(dx) >= Math.abs(dz) ? 0 : 1});
        double tx = dx == 0 ? Double.MAX_VALUE : 0.5 / Math.abs(dx);
        double tz = dz == 0 ? Double.MAX_VALUE : 0.5 / Math.abs(dz);
        double sx = dx == 0 ? Double.MAX_VALUE : 1.0 / Math.abs(dx);
        double sz = dz == 0 ? Double.MAX_VALUE : 1.0 / Math.abs(dz);
        while (Math.hypot(cx, cz) <= SPIRE_LENGTH) {
            if (tx < tz) {
                cx += dx > 0 ? 1 : -1;
                tx += sx;
                cells.add(new int[]{cx, cz, 0});
            } else {
                cz += dz > 0 ? 1 : -1;
                tz += sz;
                cells.add(new int[]{cx, cz, 1});
            }
        }
        int breakAt = SPIRE_LENGTH - 7;
        int start = 0;
        for (int i = 0; i < cells.size(); i++) {
            int[] cell = cells.get(i);
            if (Math.hypot(cell[0], cell[1]) <= breakAt
                    && blocks.containsKey(BlockPos.asLong(origin.getX() + cell[0], y, origin.getZ() + cell[1]))) {
                start = i + 1;
            }
        }
        int placed = 0;
        BlockPos last = null;
        for (int[] cell : cells.subList(start, cells.size())) {
            double r = Math.hypot(cell[0], cell[1]);
            if (r > breakAt) break;
            BlockPos pos = new BlockPos(origin.getX() + cell[0], y, origin.getZ() + cell[1]);
            Direction.Axis axis = cell[2] == 0 ? Direction.Axis.X : Direction.Axis.Z;
            blocks.put(pos.asLong(), pipe(axis));
            spirePipe.add(pos);
            if (placed == 0) {
                blocks.put(pos.below().asLong(), EngineBlocks.ENGRAVED_RUSTSLATE.get().defaultBlockState());
                columns.add(new Column(pos.getX(), pos.getZ(), y - 2, ColumnKind.SUPPORT));
            } else if (placed % 4 == 0) {
                columns.add(new Column(pos.getX(), pos.getZ(), y - 1, ColumnKind.SUPPORT));
            }
            placed++;
            last = pos;
        }
        if (last != null) {
            BlockState state = blocks.get(last.asLong());
            blocks.put(last.asLong(), leaking(state.getValue(RotatedPillarBlock.AXIS)));
            int fallen = 0;
            for (int[] cell : cells) {
                double r = Math.hypot(cell[0], cell[1]);
                if (r <= breakAt + 2.5 || fallen >= 3) continue;
                BlockPos pos = new BlockPos(origin.getX() + cell[0], origin.getY() + 1, origin.getZ() + cell[1]);
                if (blocks.containsKey(pos.asLong())) continue;
                blocks.put(pos.asLong(), pipe(cell[2] == 0 ? Direction.Axis.X : Direction.Axis.Z));
                columns.add(new Column(pos.getX(), pos.getZ(), pos.getY() - 1, ColumnKind.SUPPORT));
                fallen++;
            }
        }
    }

    private BoundingBox computeBounds() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (long key : blocks.keySet()) {
            int x = BlockPos.getX(key), y = BlockPos.getY(key), z = BlockPos.getZ(key);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        for (Column column : columns) {
            int bottom = bottomOf(column);
            minX = Math.min(minX, column.x());
            minZ = Math.min(minZ, column.z());
            maxX = Math.max(maxX, column.x());
            maxZ = Math.max(maxZ, column.z());
            minY = Math.min(minY, bottom);
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    static int bottomOf(Column column) {
        return switch (column.kind()) {
            case FOUNDATION -> column.topY() - FOUNDATION_DEPTH;
            case SUPPORT -> column.topY() - SUPPORT_DEPTH;
            case OUTFLOW -> UpfallTerrain.CLOUD_BAND_MIN;
        };
    }

    static BlockState columnState(Column column) {
        return switch (column.kind()) {
            case FOUNDATION -> RustworksBlocks.COBBLED_RUSTSLATE.get().defaultBlockState();
            case SUPPORT -> RustworksBlocks.POLISHED_RUSTSLATE_VARIANTS.wall().get().defaultBlockState();
            case OUTFLOW -> pipe(Direction.Axis.Y);
        };
    }
}
