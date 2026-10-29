package net.itsthesky.upfall.content.hamlet.world;

import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.HamletLoot;
import net.itsthesky.upfall.content.hamlet.HamletStructures;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBridgeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HamletPieces {
    private HamletPieces() {}

    public static void addPieces(StructurePiecesBuilder builder, HamletLayout.Plan plan, RandomSource random) {
        for (HamletLayout.Platform platform : plan.platforms()) {
            builder.addPiece(new PlatformPiece(platform, random.nextLong()));
        }
        for (HamletLayout.Bridge bridge : plan.bridges()) {
            builder.addPiece(new BridgePiece(plan.platforms().get(bridge.from()), plan.platforms().get(bridge.to()), bridge.direction()));
        }
        for (HamletLayout.Zipline zipline : plan.ziplines()) {
            builder.addPiece(new ZiplinePiece(zipline.anchorFrom(), plan.platforms().get(zipline.from()).center.getY(),
                    zipline.anchorTo(), plan.platforms().get(zipline.to()).center.getY()));
        }
    }

    public record Preview(Map<BlockPos, BlockState> blocks, int inhabitants, int caches, int links) {}

    public static Preview preview(HamletLayout.Plan plan, long seed) {
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        addPieces(builder, plan, RandomSource.create(seed));
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int inhabitants = 0;
        int caches = 0;
        int links = 0;
        for (StructurePiece piece : builder.build().pieces()) {
            Blueprint bp = ((HamletPiece) piece).blueprint();
            blocks.putAll(bp.blocks());
            inhabitants += bp.inhabitantCount();
            caches += bp.cacheCount();
            links += bp.linkCount();
        }
        return new Preview(blocks, inhabitants, caches, links);
    }

    abstract static class HamletPiece extends StructurePiece {
        private Blueprint blueprint;

        protected HamletPiece(StructurePieceType type) {
            super(type, 0, new BoundingBox(BlockPos.ZERO));
            setOrientation(null);
        }

        protected HamletPiece(StructurePieceType type, CompoundTag tag) {
            super(type, tag);
        }

        protected abstract void design(Blueprint bp);

        Blueprint blueprint() {
            if (blueprint == null) {
                blueprint = new Blueprint();
                design(blueprint);
            }
            return blueprint;
        }

        protected void computeBounds() {
            boundingBox = blueprint().bounds(0);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                                BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
            blueprint().place(level, box, random);
        }
    }

    private record Frame(BlockPos origin, Rotation rotation) {
        static Frame facing(BlockPos origin, Direction front) {
            Rotation rotation = switch (front) {
                case EAST -> Rotation.CLOCKWISE_90;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case WEST -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE;
            };
            return new Frame(origin, rotation);
        }

        BlockPos at(int x, int y, int z) {
            return switch (rotation) {
                case CLOCKWISE_90 -> origin.offset(-z, y, x);
                case CLOCKWISE_180 -> origin.offset(-x, y, -z);
                case COUNTERCLOCKWISE_90 -> origin.offset(z, y, -x);
                default -> origin.offset(x, y, z);
            };
        }

        @SuppressWarnings("deprecation")
        BlockState turn(BlockState state) {
            return state.rotate(rotation);
        }
    }

    private static BlockState deck() {
        return HamletBlocks.WOVEN_PLANKS.get().defaultBlockState();
    }

    private static BlockState frame() {
        return ModBlocks.SKYREACH.planks().get().defaultBlockState();
    }

    private static BlockState log(Direction.Axis axis) {
        return ModBlocks.SKYREACH.log().get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState fence() {
        return ModBlocks.SKYREACH.fence().get().defaultBlockState();
    }

    private static BlockState lantern(boolean hanging) {
        return HamletBlocks.CLOUD_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, hanging);
    }

    private static BlockState spool(Direction.Axis axis) {
        return HamletBlocks.ROPE_SPOOL.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState stairs(BlockState base, Direction facing) {
        return base.setValue(StairBlock.FACING, facing);
    }

    private static BlockState slab(SlabBlock block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    private static HamletBlocks.CanopySet canopy(int color) {
        return HamletBlocks.CANOPIES.get(Math.floorMod(color, HamletBlocks.CANOPIES.size()));
    }

    private static BlockState peg(Direction facing) {
        return HamletBlocks.ROPE_ANCHOR.get().defaultBlockState().setValue(RopeAnchorBlock.FACING, facing);
    }

    public static class PlatformPiece extends HamletPiece {
        private final HamletLayout.Kind kind;
        private final BlockPos center;
        private final int radius;
        private final Direction front;
        private final List<BlockPos> openings;
        private final int bridgeMask;
        private final int stallMask;
        private final long seed;

        PlatformPiece(HamletLayout.Platform platform, long seed) {
            super(HamletStructures.PLATFORM.get());
            this.kind = platform.kind;
            this.center = platform.center;
            this.radius = platform.radius;
            this.front = platform.front;
            this.openings = List.copyOf(platform.openings);
            this.bridgeMask = platform.bridgeMask;
            this.stallMask = platform.stallMask;
            this.seed = seed;
            computeBounds();
        }

        public PlatformPiece(CompoundTag tag) {
            super(HamletStructures.PLATFORM.get(), tag);
            this.kind = HamletLayout.Kind.valueOf(McNbt.getString(tag, "Kind"));
            this.center = BlockPos.of(McNbt.getLong(tag, "Center"));
            this.radius = McNbt.getInt(tag, "Radius");
            this.front = Direction.from2DDataValue(McNbt.getInt(tag, "Front"));
            List<BlockPos> list = new ArrayList<>();
            for (long packed : McNbt.getLongArray(tag, "Openings")) {
                list.add(BlockPos.of(packed));
            }
            this.openings = List.copyOf(list);
            this.bridgeMask = McNbt.getInt(tag, "BridgeMask");
            this.stallMask = McNbt.getInt(tag, "StallMask");
            this.seed = McNbt.getLong(tag, "Seed");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putString("Kind", kind.name());
            tag.putLong("Center", center.asLong());
            tag.putInt("Radius", radius);
            tag.putInt("Front", front.get2DDataValue());
            tag.putLongArray("Openings", openings.stream().mapToLong(BlockPos::asLong).toArray());
            tag.putInt("BridgeMask", bridgeMask);
            tag.putInt("StallMask", stallMask);
            tag.putLong("Seed", seed);
        }

        public HamletLayout.Kind kind() {
            return kind;
        }

        public BlockPos center() {
            return center;
        }

        private boolean isOpening(int dx, int dz) {
            for (BlockPos opening : openings) {
                if (opening.getX() == center.getX() + dx && opening.getZ() == center.getZ() + dz) return true;
            }
            return false;
        }

        @Override
        protected void design(Blueprint bp) {
            RandomSource random = RandomSource.create(seed);
            Frame f = Frame.facing(center, front);
            int roofColor = random.nextInt(3);
            int balloonA = random.nextInt(3);
            int balloonB = (balloonA + 1 + random.nextInt(2)) % 3;

            Set<Long> deck = new HashSet<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (HamletLayout.inDeck(dx, dz, radius)) deck.add(key(dx, dz));
                }
            }
            if (kind == HamletLayout.Kind.HOME && random.nextBoolean()) {
                addBalcony(f, deck);
            }
            Set<Long> rim = new HashSet<>();
            for (long cell : deck) {
                int dx = keyX(cell);
                int dz = keyZ(cell);
                if (!deck.contains(key(dx + 1, dz)) || !deck.contains(key(dx - 1, dz)) || !deck.contains(key(dx, dz + 1)) || !deck.contains(key(dx, dz - 1))) {
                    rim.add(cell);
                }
            }

            int y = center.getY();
            for (long cell : deck) {
                int dx = keyX(cell);
                int dz = keyZ(cell);
                boolean ring = kind == HamletLayout.Kind.MARKET && Math.round(Math.sqrt(dx * dx + dz * dz)) == 3;
                bp.set(center.offset(dx, 0, dz), rim.contains(cell) || ring ? frame() : deck());
                if (dx == 0 && dz != 0) bp.set(center.offset(dx, -1, dz), log(Direction.Axis.Z));
                if (dz == 0 && dx != 0) bp.set(center.offset(dx, -1, dz), log(Direction.Axis.X));
                if (rim.contains(cell) && !isOpening(dx, dz)) {
                    bp.set(center.offset(dx, 1, dz), fence());
                }
            }
            int keel = kind == HamletLayout.Kind.MARKET ? 7 : 4 + random.nextInt(2);
            for (int i = 1; i <= keel; i++) {
                bp.set(center.below(i), log(Direction.Axis.Y));
            }
            bp.set(center.below(keel + 1), fence());
            bp.set(center.below(keel + 2), lantern(true));

            int balloonBottom = switch (kind) {
                case MARKET -> buildMarket(bp, random);
                case HOME -> buildHome(bp, f, random, roofColor);
                case WORKSHOP -> buildWorkshop(bp, f, random, balloonA, balloonB);
                case LOOKOUT -> buildLookout(bp, f, random, roofColor);
                case GARDEN -> buildGarden(bp, f, random);
            };
            int balloonRadius = switch (kind) {
                case MARKET -> 5;
                case LOOKOUT -> 3;
                case GARDEN, WORKSHOP -> 4;
                case HOME -> radius >= 5 ? 4 : 3;
            };
            balloon(bp, rim, balloonBottom, balloonRadius, kind == HamletLayout.Kind.MARKET ? 4 : 3, balloonA, balloonB, random);
            ropeHole(bp, deck, rim, random);
        }

        private void addBalcony(Frame f, Set<Long> deck) {
            for (int x = -1; x <= 1; x++) {
                for (int z = radius + 1; z <= radius + 2; z++) {
                    BlockPos pos = f.at(x, 0, z);
                    int dx = pos.getX() - center.getX();
                    int dz = pos.getZ() - center.getZ();
                    for (BlockPos opening : openings) {
                        if (Math.abs(opening.getX() - pos.getX()) <= 2 && Math.abs(opening.getZ() - pos.getZ()) <= 2) return;
                    }
                    deck.add(key(dx, dz));
                }
            }
        }

        private int buildMarket(Blueprint bp, RandomSource random) {
            int bottom = 12;
            for (int i = 1; i < bottom; i++) {
                bp.set(center.above(i), log(Direction.Axis.Y));
            }
            bp.set(center.offset(1, 1, 0), stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.WEST));
            bp.set(center.offset(-1, 1, 0), stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.EAST));
            bp.set(center.offset(0, 1, 1), stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.NORTH));
            bp.set(center.offset(0, 1, -1), stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.SOUTH));
            for (Direction side : Direction.Plane.HORIZONTAL) {
                bp.set(center.above(6).relative(side), fence());
                bp.set(center.above(5).relative(side), lantern(true));
            }
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    lampPost(bp, center.offset(2 * sx, 1, 2 * sz));
                }
            }
            int[][] diagonals = {{1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
            int stalls = 0;
            for (int slot = 0; slot < 4; slot++) {
                if ((stallMask & (1 << slot)) == 0) continue;
                int sx = diagonals[slot][0];
                int sz = diagonals[slot][1];
                BlockPos c = center.offset(sx * (radius - 3), 1, sz * (radius - 3));
                stall(bp, c, sx, random.nextInt(3), stalls == 0, random);
                if (stalls < 2) {
                    bp.inhabitant(c.offset(-sx, 0, 0), random.nextInt(3));
                }
                stalls++;
            }
            if (stalls == 0) {
                bp.inhabitant(center.offset(2, 1, 0), random.nextInt(3));
            }
            return bottom;
        }

        private static void stall(Blueprint bp, BlockPos c, int sx, int color, boolean withCache, RandomSource random) {
            HamletBlocks.CanopySet canopy = canopy(color);
            for (int k = -1; k <= 1; k++) {
                BlockPos front = c.offset(0, 0, k);
                BlockPos back = c.offset(sx, 0, k);
                if (k == 0) {
                    bp.set(front, slab(HamletBlocks.WOVEN_SLAB.get(), SlabType.TOP));
                    bp.set(back, withCache ? ModBlocks.SKY_CACHE.get().defaultBlockState() : spool(Direction.Axis.Y));
                    if (withCache) bp.cache(back, HamletLoot.HOME);
                    bp.set(front.above(), lantern(true));
                } else {
                    bp.set(front, fence());
                    bp.set(front.above(), fence());
                    bp.set(back, random.nextBoolean() ? spool(Direction.Axis.Y) : spool(Direction.Axis.X));
                    bp.set(back.above(), fence());
                }
                bp.set(front.above(2), slab(canopy.slab().get(), SlabType.BOTTOM));
                bp.set(back.above(2), slab(canopy.slab().get(), SlabType.BOTTOM));
            }
        }

        private static void lampPost(Blueprint bp, BlockPos base) {
            bp.set(base, fence());
            bp.set(base.above(), fence());
            bp.set(base.above(2), lantern(false));
        }

        private int buildHome(Blueprint bp, Frame f, RandomSource random, int color) {
            HamletBlocks.CanopySet canopy = canopy(color);
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                    boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                    for (int h = 1; h <= 3; h++) {
                        bp.set(f.at(x, h, z), corner ? log(Direction.Axis.Y) : deck());
                    }
                }
            }
            BlockState door = f.turn(ModBlocks.SKYREACH.door().get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH));
            bp.set(f.at(0, 1, -2), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            bp.set(f.at(0, 2, -2), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            bp.set(f.at(-2, 2, 0), Blocks.AIR.defaultBlockState());
            bp.set(f.at(2, 2, 0), Blocks.AIR.defaultBlockState());
            bp.set(f.at(0, 2, 2), Blocks.AIR.defaultBlockState());
            BlockState stair = canopy.stairs().get().defaultBlockState();
            for (int z = -3; z <= 3; z++) {
                for (int step = 0; step < 3; step++) {
                    bp.set(f.at(-3 + step, 3 + step, z), f.turn(stairs(stair, Direction.EAST)));
                    bp.set(f.at(3 - step, 3 + step, z), f.turn(stairs(stair, Direction.WEST)));
                }
                bp.set(f.at(0, 5, z), canopy.block().get().defaultBlockState());
                bp.set(f.at(0, 6, z), slab(canopy.slab().get(), SlabType.BOTTOM));
            }
            for (int z = -2; z <= 2; z += 4) {
                for (int x = -1; x <= 1; x++) {
                    bp.set(f.at(x, 4, z), deck());
                }
            }
            bp.set(f.at(0, 4, 0), lantern(true));
            bp.set(f.at(-2, 3, -3), lantern(true));
            bp.set(f.at(-1, 1, 1), spool(Direction.Axis.Y));
            if (random.nextInt(4) > 0) {
                bp.set(f.at(1, 1, 1), ModBlocks.SKY_CACHE.get().defaultBlockState());
                bp.cache(f.at(1, 1, 1), HamletLoot.HOME);
            } else {
                bp.set(f.at(1, 1, 1), slab(HamletBlocks.WOVEN_SLAB.get(), SlabType.TOP));
            }
            bp.inhabitant(f.at(0, 1, 0), random.nextInt(3));
            return 9;
        }

        private int buildWorkshop(Blueprint bp, Frame f, RandomSource random, int colorA, int colorB) {
            for (int x = -2; x <= 2; x += 4) {
                for (int z = -2; z <= 2; z += 4) {
                    for (int h = 1; h <= 3; h++) {
                        bp.set(f.at(x, h, z), log(Direction.Axis.Y));
                    }
                }
            }
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    bp.set(f.at(x, 4, z), slab(canopy((x & 1) == 0 ? colorA : colorB).slab().get(), SlabType.BOTTOM));
                }
            }
            bp.set(f.at(0, 1, 1), slab(HamletBlocks.WOVEN_SLAB.get(), SlabType.TOP));
            bp.set(f.at(-1, 1, 1), ModBlocks.SKY_CACHE.get().defaultBlockState());
            bp.cache(f.at(-1, 1, 1), HamletLoot.WORKSHOP);
            bp.set(f.at(1, 1, 1), f.turn(spool(Direction.Axis.X)));
            bp.set(f.at(1, 2, 1), f.turn(spool(Direction.Axis.X)));
            bp.set(f.at(1, 1, -1), f.turn(stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.NORTH)));
            hangingRope(bp, f.at(-2, 3, 0), 2);
            hangingRope(bp, f.at(2, 3, -1), 2);
            hangingRope(bp, f.at(2, 3, 1), 1);
            bp.set(f.at(0, 3, 0), lantern(true));
            bp.inhabitant(f.at(-1, 1, -1), random.nextInt(3));
            return 8;
        }

        private static void hangingRope(Blueprint bp, BlockPos top, int length) {
            for (int i = 0; i < length; i++) {
                bp.set(top.below(i), HamletBlocks.ROPE.get().defaultBlockState().setValue(RopeBlock.END, i == length - 1));
            }
        }

        private int buildLookout(Blueprint bp, Frame f, RandomSource random, int color) {
            for (int h = 1; h <= 3; h++) {
                bp.set(f.at(0, h, 0), log(Direction.Axis.Y));
            }
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (x * x + z * z > 5) continue;
                    int stripe = (int) Math.floor((Math.atan2(z, x) + Math.PI) / (Math.PI / 3.0));
                    bp.set(f.at(x, 4, z), slab(canopy(stripe % 2 == 0 ? color : color + 1).slab().get(), SlabType.BOTTOM));
                }
            }
            bp.set(f.at(0, 4, 0), canopy(color).block().get().defaultBlockState());
            bp.set(f.at(0, 5, 0), slab(canopy(color + 2).slab().get(), SlabType.BOTTOM));
            bp.set(f.at(1, 3, 0), lantern(true));
            bp.set(f.at(-1, 3, 0), lantern(true));
            if (random.nextInt(10) < 6) {
                bp.inhabitant(f.at(1, 1, 1), random.nextInt(3));
            }
            return 8;
        }

        private int buildGarden(Blueprint bp, Frame f, RandomSource random) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    bp.set(f.at(x, 0, z), ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState());
                    if (x != 0 || z != 0) {
                        bp.set(f.at(x, 1, z), flower(random));
                    }
                }
            }
            lampPost(bp, f.at(0, 1, 0));
            for (int x = -3; x <= 3; x += 6) {
                bp.set(f.at(x, 1, 0), ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState());
                bp.set(f.at(x, 2, 0), flower(random));
                bp.set(f.at(x, 1, 1), slab(HamletBlocks.WOVEN_SLAB.get(), SlabType.BOTTOM));
                bp.set(f.at(x, 1, -1), slab(HamletBlocks.WOVEN_SLAB.get(), SlabType.BOTTOM));
            }
            bp.set(f.at(0, 1, 3), f.turn(stairs(HamletBlocks.WOVEN_STAIRS.get().defaultBlockState(), Direction.SOUTH)));
            if (random.nextBoolean()) {
                bp.set(f.at(-1, 1, 3), ModBlocks.SKY_CACHE.get().defaultBlockState());
                bp.cache(f.at(-1, 1, 3), HamletLoot.HOME);
            } else {
                bp.set(f.at(-1, 1, 3), spool(Direction.Axis.Y));
            }
            bp.inhabitant(f.at(2, 1, -2), random.nextInt(3));
            return 7;
        }

        private static BlockState flower(RandomSource random) {
            return switch (random.nextInt(5)) {
                case 0, 1 -> ModBlocks.DUSKBLOOM.get().defaultBlockState();
                case 2, 3 -> ModBlocks.CLOUD_FERN.get().defaultBlockState();
                default -> ModBlocks.GILDED_TUFT.get().defaultBlockState();
            };
        }

        private void balloon(Blueprint bp, Set<Long> rim, int bottomOffset, int rx, int ry, int colorA, int colorB, RandomSource random) {
            BlockPos bc = center.above(bottomOffset + ry);
            int colorTop = 3 - colorA - colorB;
            for (int dx = -rx; dx <= rx; dx++) {
                for (int dy = -ry; dy <= ry; dy++) {
                    for (int dz = -rx; dz <= rx; dz++) {
                        double d = (double) (dx * dx + dz * dz) / ((rx + 0.5) * (rx + 0.5)) + (double) (dy * dy) / ((ry + 0.5) * (ry + 0.5));
                        if (d > 1.0) continue;
                        int color;
                        if (dy >= ry - 1 && dx * dx + dz * dz <= 2) {
                            color = colorTop;
                        } else {
                            int sector = (int) Math.floor((Math.atan2(dz, dx) + Math.PI) / (Math.PI / 4.0));
                            color = (sector & 1) == 0 ? colorA : colorB;
                        }
                        bp.set(bc.offset(dx, dy, dz), canopy(color).block().get().defaultBlockState());
                    }
                }
            }
            if (kind != HamletLayout.Kind.MARKET) {
                BlockPos low = lowestBalloonBlock(bp, bc, 0, 0, ry);
                bp.set(low.below(), lantern(true));
            }

            List<Long> candidates = new ArrayList<>();
            for (long cell : rim) {
                if (bp.get(center.offset(keyX(cell), 1, keyZ(cell))) != null && bp.get(center.offset(keyX(cell), 1, keyZ(cell))).is(ModBlocks.SKYREACH.fence().get())
                        && bp.isFree(center.offset(keyX(cell), 2, keyZ(cell)))) {
                    candidates.add(cell);
                }
            }
            double start = random.nextDouble() * Math.PI * 2.0;
            candidates.sort(Comparator.comparingDouble(cell -> Mth.positiveModulo(Math.atan2(keyZ(cell), keyX(cell)) - start, Math.PI * 2.0)));
            List<Double> used = new ArrayList<>();
            for (long cell : candidates) {
                if (used.size() >= 4) break;
                double angle = Math.atan2(keyZ(cell), keyX(cell));
                if (used.stream().anyMatch(a -> Math.abs(Mth.wrapDegrees((a - angle) * Mth.RAD_TO_DEG)) < 70.0)) continue;
                int dx = keyX(cell);
                int dz = keyZ(cell);
                double distance = Math.sqrt(dx * dx + dz * dz);
                double k = Math.min(1.0, rx * 0.55 / Math.max(1.0, distance));
                int qx = (int) Math.round(dx * k);
                int qz = (int) Math.round(dz * k);
                if (qx == 0 && qz == 0) continue;
                BlockPos deckPeg = center.offset(dx, 2, dz);
                BlockPos skyPeg = lowestBalloonBlock(bp, bc, qx, qz, ry).below();
                if (!bp.isFree(skyPeg)) continue;
                Vec3 from = Vec3.atCenterOf(deckPeg).add(0.0, 0.25, 0.0);
                Vec3 to = Vec3.atCenterOf(skyPeg).add(0.0, -0.25, 0.0);
                if (!bp.isLineClear(from, to, deckPeg, skyPeg)) continue;
                bp.set(deckPeg, peg(Direction.UP));
                bp.set(skyPeg, peg(Direction.DOWN));
                bp.link(deckPeg, skyPeg);
                bp.link(skyPeg, deckPeg);
                used.add(angle);
            }
        }

        private static BlockPos lowestBalloonBlock(Blueprint bp, BlockPos balloonCenter, int dx, int dz, int ry) {
            for (int dy = -ry; dy <= ry; dy++) {
                BlockPos pos = balloonCenter.offset(dx, dy, dz);
                if (!bp.isFree(pos)) return pos;
            }
            return balloonCenter.offset(dx, 0, dz);
        }

        private void ropeHole(Blueprint bp, Set<Long> deck, Set<Long> rim, RandomSource random) {
            List<Long> candidates = new ArrayList<>();
            for (long cell : deck) {
                int dx = keyX(cell);
                int dz = keyZ(cell);
                if (rim.contains(cell) || dx == 0 || dz == 0 || dx * dx + dz * dz < (radius - 2) * (radius - 2)) continue;
                BlockPos above = center.offset(dx, 1, dz);
                boolean open = bp.isFree(above) && bp.isFree(above.above()) && !bp.isNearInhabitant(above)
                        && deck.contains(key(dx + 1, dz)) && deck.contains(key(dx - 1, dz))
                        && deck.contains(key(dx, dz + 1)) && deck.contains(key(dx, dz - 1));
                if (open && !nearPath(dx, dz)) candidates.add(cell);
            }
            if (candidates.isEmpty()) return;
            candidates.sort(Comparator.naturalOrder());
            long cell = candidates.get(random.nextInt(candidates.size()));
            BlockPos top = center.offset(keyX(cell), 0, keyZ(cell));
            bp.remove(top);
            bp.ropeDrop(top, 48);
        }

        private boolean nearPath(int dx, int dz) {
            for (BlockPos opening : openings) {
                double ox = opening.getX() - center.getX();
                double oz = opening.getZ() - center.getZ();
                double lengthSqr = ox * ox + oz * oz;
                double t = lengthSqr == 0 ? 0.0 : Mth.clamp((dx * ox + dz * oz) / lengthSqr, 0.0, 1.0);
                double px = ox * t - dx;
                double pz = oz * t - dz;
                if (px * px + pz * pz <= 2.25) return true;
            }
            return false;
        }

        private static long key(int dx, int dz) {
            return ((long) dx << 32) | (dz & 0xFFFFFFFFL);
        }

        private static int keyX(long key) {
            return (int) (key >> 32);
        }

        private static int keyZ(long key) {
            return (int) key;
        }
    }

    public static class BridgePiece extends HamletPiece {
        private final BlockPos fromCenter;
        private final int fromRadius;
        private final BlockPos toCenter;
        private final int toRadius;
        private final Direction direction;

        BridgePiece(HamletLayout.Platform from, HamletLayout.Platform to, Direction direction) {
            super(HamletStructures.BRIDGE.get());
            this.fromCenter = from.center;
            this.fromRadius = from.radius;
            this.toCenter = to.center;
            this.toRadius = to.radius;
            this.direction = direction;
            computeBounds();
        }

        public BridgePiece(CompoundTag tag) {
            super(HamletStructures.BRIDGE.get(), tag);
            this.fromCenter = BlockPos.of(McNbt.getLong(tag, "From"));
            this.fromRadius = McNbt.getInt(tag, "FromRadius");
            this.toCenter = BlockPos.of(McNbt.getLong(tag, "To"));
            this.toRadius = McNbt.getInt(tag, "ToRadius");
            this.direction = Direction.from2DDataValue(McNbt.getInt(tag, "Direction"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putLong("From", fromCenter.asLong());
            tag.putInt("FromRadius", fromRadius);
            tag.putLong("To", toCenter.asLong());
            tag.putInt("ToRadius", toRadius);
            tag.putInt("Direction", direction.get2DDataValue());
        }

        @Override
        protected void design(Blueprint bp) {
            HamletLayout.Platform from = new HamletLayout.Platform(HamletLayout.Kind.MARKET, fromCenter, fromRadius, Direction.NORTH);
            HamletLayout.Platform to = new HamletLayout.Platform(HamletLayout.Kind.HOME, toCenter, toRadius, Direction.NORTH);
            int base = HamletLayout.rowBase(from, direction);
            RopeBridgeBlock bridge = HamletBlocks.ROPE_BRIDGE.get();
            for (int row : HamletLayout.bridgeRows()) {
                BlockPos a = HamletLayout.rimCell(from, direction, base + row);
                BlockPos b = HamletLayout.rimCell(to, direction.getOpposite(), base + row);
                int cells = Math.abs(direction.getAxis() == Direction.Axis.X ? b.getX() - a.getX() : b.getZ() - a.getZ()) - 1;
                double startY = a.getY() + 1.0;
                double endY = b.getY() + 1.0;
                double sag = Math.min(0.9, 0.06 * (cells + 1));
                for (int i = 1; i <= cells; i++) {
                    double s = (double) i / (cells + 1);
                    double surface = startY + (endY - startY) * s - sag * 4.0 * s * (1.0 - s);
                    int blockY = RopeBridgeBlock.blockYFor(surface);
                    BlockPos cell = a.relative(direction, i);
                    bp.set(new BlockPos(cell.getX(), blockY, cell.getZ()), bridge.deckAt(direction.getAxis(), surface, blockY));
                }
            }
            for (int rail : new int[]{-1, 2}) {
                BlockPos a = HamletLayout.rimCell(from, direction, base + rail).above(2);
                BlockPos b = HamletLayout.rimCell(to, direction.getOpposite(), base + rail).above(2);
                bp.set(a, peg(Direction.UP));
                bp.set(b, peg(Direction.UP));
                bp.link(a, b);
                bp.link(b, a);
                BlockPos lamp = a.below().relative(direction.getOpposite());
                bp.set(lamp, fence());
                bp.set(lamp.above(), fence());
                bp.set(lamp.above(2), lantern(false));
            }
        }
    }

    public static class ZiplinePiece extends HamletPiece {
        private final BlockPos anchorA;
        private final int deckA;
        private final BlockPos anchorB;
        private final int deckB;

        ZiplinePiece(BlockPos anchorA, int deckA, BlockPos anchorB, int deckB) {
            super(HamletStructures.ZIPLINE.get());
            this.anchorA = anchorA;
            this.deckA = deckA;
            this.anchorB = anchorB;
            this.deckB = deckB;
            computeBounds();
        }

        public ZiplinePiece(CompoundTag tag) {
            super(HamletStructures.ZIPLINE.get(), tag);
            this.anchorA = BlockPos.of(McNbt.getLong(tag, "A"));
            this.deckA = McNbt.getInt(tag, "DeckA");
            this.anchorB = BlockPos.of(McNbt.getLong(tag, "B"));
            this.deckB = McNbt.getInt(tag, "DeckB");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putLong("A", anchorA.asLong());
            tag.putInt("DeckA", deckA);
            tag.putLong("B", anchorB.asLong());
            tag.putInt("DeckB", deckB);
        }

        @Override
        protected void design(Blueprint bp) {
            post(bp, anchorA, deckA, anchorB);
            post(bp, anchorB, deckB, anchorA);
        }

        private static void post(Blueprint bp, BlockPos anchor, int deckY, BlockPos other) {
            bp.set(new BlockPos(anchor.getX(), deckY, anchor.getZ()), deck());
            for (int y = deckY + 1; y < anchor.getY(); y++) {
                bp.set(new BlockPos(anchor.getX(), y, anchor.getZ()), log(Direction.Axis.Y));
            }
            //? if >=26.1 {
            /*Direction along = Direction.getApproximateNearest(other.getX() - anchor.getX(), 0, other.getZ() - anchor.getZ());
            *///?} else {
            Direction along = Direction.getNearest(other.getX() - anchor.getX(), 0, other.getZ() - anchor.getZ());
            //?}
            bp.set(anchor, HamletBlocks.ZIPLINE_ANCHOR.get().defaultBlockState().setValue(ZiplineAnchorBlock.FACING, along));
            bp.link(anchor, other);
        }
    }
}
