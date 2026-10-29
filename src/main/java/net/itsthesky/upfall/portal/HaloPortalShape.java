package net.itsthesky.upfall.portal;

import net.itsthesky.upfall.block.HaloPortalBlock;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HaloPortalShape {
    public static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;

    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int portalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<HaloPortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis preferredAxis) {
        HaloPortalShape shape = new HaloPortalShape(level, pos, preferredAxis);
        if (shape.isValid() && shape.portalBlocks == 0) return Optional.of(shape);
        Direction.Axis other = preferredAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        shape = new HaloPortalShape(level, pos, other);
        return shape.isValid() && shape.portalBlocks == 0 ? Optional.of(shape) : Optional.empty();
    }

    public HaloPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = calculateBottomLeft(pos);
        if (bottomLeft == null) {
            bottomLeft = pos;
            width = 1;
            height = 1;
        } else {
            width = calculateWidth();
            if (width > 0) {
                height = calculateHeight();
            }
        }
    }

    public static boolean isFrame(BlockState state) {
        return state.is(ModTags.Blocks.HALO_PORTAL_FRAMES);
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(ModBlocks.HALO_PORTAL.get());
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(McLevel.minY(level), pos.getY() - MAX_HEIGHT);
        while (pos.getY() > minY && isEmpty(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        Direction left = rightDir.getOpposite();
        int distance = distanceUntilEdgeAboveFrame(pos, left) - 1;
        return distance < 0 ? null : pos.relative(left, distance);
    }

    private int calculateWidth() {
        int distance = distanceUntilEdgeAboveFrame(bottomLeft, rightDir);
        return distance >= MIN_WIDTH && distance <= MAX_WIDTH ? distance : 0;
    }

    private int distanceUntilEdgeAboveFrame(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= MAX_WIDTH; i++) {
            cursor.set(pos).move(direction, i);
            BlockState state = level.getBlockState(cursor);
            if (!isEmpty(state)) {
                return isFrame(state) ? i : 0;
            }
            if (!isFrame(level.getBlockState(cursor.move(Direction.DOWN)))) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int distance = distanceUntilTop(cursor);
        return distance >= MIN_HEIGHT && distance <= MAX_HEIGHT && hasTopFrame(cursor, distance) ? distance : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos cursor, int distanceToTop) {
        for (int i = 0; i < width; i++) {
            cursor.set(bottomLeft).move(Direction.UP, distanceToTop).move(rightDir, i);
            if (!isFrame(level.getBlockState(cursor))) return false;
        }
        return true;
    }

    private int distanceUntilTop(BlockPos.MutableBlockPos cursor) {
        for (int i = 0; i < MAX_HEIGHT; i++) {
            cursor.set(bottomLeft).move(Direction.UP, i).move(rightDir, -1);
            if (!isFrame(level.getBlockState(cursor))) return i;
            cursor.set(bottomLeft).move(Direction.UP, i).move(rightDir, width);
            if (!isFrame(level.getBlockState(cursor))) return i;
            for (int j = 0; j < width; j++) {
                cursor.set(bottomLeft).move(Direction.UP, i).move(rightDir, j);
                BlockState state = level.getBlockState(cursor);
                if (!isEmpty(state)) return i;
                if (state.is(ModBlocks.HALO_PORTAL.get())) portalBlocks++;
            }
        }
        return MAX_HEIGHT;
    }

    public boolean isValid() {
        return bottomLeft != null && width >= MIN_WIDTH && width <= MAX_WIDTH && height >= MIN_HEIGHT && height <= MAX_HEIGHT;
    }

    public boolean isComplete() {
        return isValid() && portalBlocks == width * height;
    }

    public void createPortalBlocks() {
        BlockState state = ModBlocks.HALO_PORTAL.get().defaultBlockState().setValue(HaloPortalBlock.AXIS, axis);
        BlockPos.betweenClosed(bottomLeft, bottomLeft.relative(Direction.UP, height - 1).relative(rightDir, width - 1))
                .forEach(pos -> level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE));
    }

    public BlockPos bottomLeft() {
        return bottomLeft;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Direction.Axis axis() {
        return axis;
    }
}
