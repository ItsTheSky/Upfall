package net.itsthesky.upfall.portal;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.block.HaloPortalBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModPoiTypes;
import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public final class HaloPortalForcer {
    private HaloPortalForcer() {}

    public static final int SEARCH_RADIUS = 128;
    private static final int ISLAND_SEARCH_RADIUS = 64;
    private static final int ISLAND_SEARCH_STEP = 3;

    @Nullable
    public static DimensionTransition destination(ServerLevel level, Entity entity, BlockPos portalPos) {
        ResourceKey<Level> targetKey;
        if (level.dimension() == ModDimensions.UPFALL) {
            targetKey = Level.OVERWORLD;
        } else if (level.dimension() == Level.OVERWORLD) {
            targetKey = ModDimensions.UPFALL;
        } else {
            return null;
        }
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) return null;

        WorldBorder border = target.getWorldBorder();
        BlockPos exitPos = border.clampToBounds(entity.getX(), entity.getY(), entity.getZ());
        Optional<BlockPos> existing = findClosestPortal(target, exitPos, border);
        BlockUtil.FoundRectangle rectangle;
        DimensionTransition.PostDimensionTransition post;
        if (existing.isPresent()) {
            BlockPos found = existing.get();
            BlockState state = target.getBlockState(found);
            rectangle = BlockUtil.getLargestRectangleAround(found, state.getValue(HaloPortalBlock.AXIS), HaloPortalShape.MAX_WIDTH,
                    Direction.Axis.Y, HaloPortalShape.MAX_HEIGHT, p -> target.getBlockState(p) == state);
            post = HaloPortalForcer::playTravelSound;
            post = post.then(e -> e.placePortalTicket(found));
        } else {
            Direction.Axis axis = entity.level().getBlockState(portalPos).getOptionalValue(HaloPortalBlock.AXIS).orElse(Direction.Axis.X);
            rectangle = createPortal(target, exitPos, axis);
            post = HaloPortalForcer::playTravelSound;
            post = post.then(DimensionTransition.PLACE_PORTAL_TICKET);
        }
        return transitionFromExit(entity, portalPos, rectangle, target, post);
    }

    private static void playTravelSound(Entity entity) {
        entity.level().playSound(null, entity.blockPosition(), ModSounds.HALO_PORTAL_TRAVEL.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    public static Optional<BlockPos> findClosestPortal(ServerLevel level, BlockPos center, WorldBorder border) {
        PoiManager poi = level.getPoiManager();
        poi.ensureLoadedAndValid(level, center, SEARCH_RADIUS);
        return poi.getInSquare(type -> type.is(ModPoiTypes.HALO_PORTAL.getKey()), center, SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(border::isWithinBounds)
                .filter(pos -> level.getBlockState(pos).hasProperty(HaloPortalBlock.AXIS))
                .min(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(center)).thenComparingInt(Vec3i::getY));
    }

    public static BlockUtil.FoundRectangle createPortal(ServerLevel level, BlockPos target, Direction.Axis axis) {
        BlockPos origin = level.dimension() == ModDimensions.UPFALL ? findIslandSpot(level, target, axis) : findOverworldSpot(level, target, axis);
        if (origin == null) {
            origin = buildPlatform(level, target, axis);
        }
        buildFrame(level, origin, axis);
        return new BlockUtil.FoundRectangle(origin, HaloPortalShape.MIN_WIDTH, HaloPortalShape.MIN_HEIGHT);
    }

    private static Direction along(Direction.Axis axis) {
        return Direction.get(Direction.AxisDirection.POSITIVE, axis);
    }

    private static Direction across(Direction.Axis axis) {
        return along(axis).getClockWise();
    }

    @Nullable
    private static BlockPos findIslandSpot(ServerLevel level, BlockPos target, Direction.Axis axis) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int r = 0; r <= ISLAND_SEARCH_RADIUS; r += ISLAND_SEARCH_STEP) {
            for (int dx = -r; dx <= r; dx += ISLAND_SEARCH_STEP) {
                for (int dz = -r; dz <= r; dz += ISLAND_SEARCH_STEP) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int x = target.getX() + dx;
                    int z = target.getZ() + dz;
                    if (!level.getWorldBorder().isWithinBounds(x, z)) continue;
                    int top = level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
                    for (int y = Math.min(top, 260); y >= 70; y--) {
                        BlockPos ground = new BlockPos(x, y, z);
                        if (!isIslandGround(level.getBlockState(ground))) continue;
                        BlockPos origin = ground.above();
                        if (fits(level, origin, axis)) {
                            double distance = origin.distSqr(target);
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                best = origin;
                            }
                        }
                        break;
                    }
                }
            }
            if (best != null) return best;
        }
        return null;
    }

    private static boolean isIslandGround(BlockState state) {
        return state.is(ModTags.Blocks.SKY_PLANTABLE) || state.is(ModBlocks.PEARLSTONE.get()) || state.is(ModBlocks.DUSKSLATE.get());
    }

    private static boolean fits(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        Direction along = along(axis);
        for (int w = -1; w <= 2; w++) {
            BlockPos column = origin.relative(along, w);
            if (!isIslandGround(level.getBlockState(column.below()))) return false;
            for (int h = 0; h <= 4; h++) {
                BlockState state = level.getBlockState(column.above(h));
                if (!state.isAir() && !state.canBeReplaced()) return false;
            }
        }
        return true;
    }

    @Nullable
    private static BlockPos findOverworldSpot(ServerLevel level, BlockPos target, Direction.Axis axis) {
        int y = level.getChunk(target.getX() >> 4, target.getZ() >> 4)
                .getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX() & 15, target.getZ() & 15) + 1;
        BlockPos origin = new BlockPos(target.getX(), y, target.getZ());
        if (!level.getBlockState(origin.below()).getFluidState().isEmpty()) {
            return null;
        }
        return origin;
    }

    private static BlockPos buildPlatform(ServerLevel level, BlockPos target, Direction.Axis axis) {
        boolean upfall = level.dimension() == ModDimensions.UPFALL;
        int y = upfall ? Math.clamp(target.getY(), 110, 200) : Math.max(level.getSeaLevel() + 1, target.getY());
        BlockPos center = new BlockPos(target.getX(), y - 1, target.getZ());
        BlockState stone = ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz > 11) continue;
                level.setBlockAndUpdate(center.offset(dx, 0, dz), stone);
                if (upfall && dx * dx + dz * dz <= 4) {
                    level.setBlockAndUpdate(center.offset(dx, -1, dz), ModBlocks.PEARLSTONE.get().defaultBlockState());
                }
            }
        }
        Upfall.LOGGER.info("No island found near {}, built a Halo Portal platform at {}", target, center);
        return center.above();
    }

    private static void buildFrame(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        Direction along = along(axis);
        Direction across = across(axis);
        BlockState frame = ModBlocks.HALO_FRAME.get().defaultBlockState();
        BlockState pad = ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
        BlockState portal = ModBlocks.HALO_PORTAL.get().defaultBlockState().setValue(HaloPortalBlock.AXIS, axis);
        for (int w = -1; w <= 2; w++) {
            for (int d = -1; d <= 1; d++) {
                BlockPos column = origin.relative(along, w).relative(across, d);
                if (d != 0 && !level.getBlockState(column.below()).isSolid()) {
                    level.setBlockAndUpdate(column.below(), pad);
                }
                for (int h = 0; h <= 3; h++) {
                    level.setBlockAndUpdate(column.above(h), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int w = -1; w <= 2; w++) {
            for (int h = -1; h <= 3; h++) {
                BlockPos pos = origin.relative(along, w).above(h);
                boolean edge = w == -1 || w == 2 || h == -1 || h == 3;
                level.setBlock(pos, edge ? frame : portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    private static DimensionTransition transitionFromExit(Entity entity, BlockPos portalPos, BlockUtil.FoundRectangle rectangle,
                                                          ServerLevel level, DimensionTransition.PostDimensionTransition post) {
        BlockState from = entity.level().getBlockState(portalPos);
        Direction.Axis fromAxis;
        Vec3 offset;
        if (from.hasProperty(HaloPortalBlock.AXIS)) {
            fromAxis = from.getValue(HaloPortalBlock.AXIS);
            BlockUtil.FoundRectangle source = BlockUtil.getLargestRectangleAround(portalPos, fromAxis, HaloPortalShape.MAX_WIDTH,
                    Direction.Axis.Y, HaloPortalShape.MAX_HEIGHT, p -> entity.level().getBlockState(p) == from);
            offset = entity.getRelativePortalPosition(fromAxis, source);
        } else {
            fromAxis = Direction.Axis.X;
            offset = new Vec3(0.5, 0.0, 0.0);
        }
        BlockPos corner = rectangle.minCorner;
        Direction.Axis toAxis = level.getBlockState(corner).getOptionalValue(HaloPortalBlock.AXIS).orElse(Direction.Axis.X);
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        int rotation = fromAxis == toAxis ? 0 : 90;
        Vec3 speed = entity.getDeltaMovement();
        Vec3 newSpeed = fromAxis == toAxis ? speed : new Vec3(speed.z, speed.y, -speed.x);
        double alongOffset = dimensions.width() / 2.0 + (rectangle.axis1Size - dimensions.width()) * offset.x();
        double upOffset = (rectangle.axis2Size - dimensions.height()) * offset.y();
        double acrossOffset = 0.5 + offset.z();
        boolean xAxis = toAxis == Direction.Axis.X;
        Vec3 position = new Vec3(corner.getX() + (xAxis ? alongOffset : acrossOffset), corner.getY() + upOffset,
                corner.getZ() + (xAxis ? acrossOffset : alongOffset));
        Vec3 free = PortalShape.findCollisionFreePosition(position, level, entity, dimensions);
        return new DimensionTransition(level, free, newSpeed, entity.getYRot() + rotation, entity.getXRot(), post);
    }
}
