package net.itsthesky.upfall.content.quantum.observe;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class QuantumObservation {
    private QuantumObservation() {}

    public static boolean shouldCheck(Level level, BlockPos pos) {
        int interval = UpfallConfig.getInt(UpfallConfig.OBSERVATION_INTERVAL);
        return Math.floorMod(level.getGameTime() + pos.hashCode(), interval) == 0;
    }

    public static boolean anyPlayerNear(ServerLevel level, Vec3 target) {
        double range = UpfallConfig.getInt(UpfallConfig.OBSERVATION_RANGE);
        for (ServerPlayer player : level.players()) {
            if (canObserve(player) && player.getEyePosition().distanceToSqr(target) <= range * range) return true;
        }
        return false;
    }

    public static boolean isObserved(ServerLevel level, BlockPos block) {
        return isObserved(level, Vec3.atCenterOf(block), block);
    }

    public static boolean isObserved(ServerLevel level, Vec3 target, BlockPos block) {
        for (ServerPlayer player : level.players()) {
            if (isObservedBy(player, target, block)) return true;
        }
        return false;
    }

    public static List<ServerPlayer> observers(ServerLevel level, BlockPos block) {
        List<ServerPlayer> result = new ArrayList<>();
        Vec3 target = Vec3.atCenterOf(block);
        for (ServerPlayer player : level.players()) {
            if (isObservedBy(player, target, block)) result.add(player);
        }
        return result;
    }

    public static boolean isObservedBy(Player player, BlockPos block) {
        return isObservedBy(player, Vec3.atCenterOf(block), block);
    }

    public static boolean isObservedBy(Player player, Vec3 target, BlockPos block) {
        if (!canObserve(player)) return false;
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 delta = target.subtract(eye);
        double distance = delta.length();
        int range = UpfallConfig.getInt(UpfallConfig.OBSERVATION_RANGE);
        if (distance > range) return false;
        if (distance > 0.75 && !visiblePoint(player, eye, target, block)) return false;
        return isLit(level, block) || holdsLight(player) && distance <= UpfallConfig.getInt(UpfallConfig.OBSERVATION_HANDHELD_LIGHT_RANGE);
    }

    private static final double[][] SAMPLES = {{0, 0, 0}, {0.4, 0, 0}, {-0.4, 0, 0}, {0, 0.4, 0}, {0, -0.4, 0}, {0, 0, 0.4}, {0, 0, -0.4}};

    private static boolean visiblePoint(Player player, Vec3 eye, Vec3 target, BlockPos block) {
        Vec3 look = player.getViewVector(1.0F);
        double fov = UpfallConfig.getDouble(UpfallConfig.OBSERVATION_FOV_DOT);
        for (double[] offset : SAMPLES) {
            Vec3 point = target.add(offset[0], offset[1], offset[2]);
            Vec3 delta = point.subtract(eye);
            double length = delta.length();
            if (length < 1.0E-4 || look.dot(delta.scale(1.0 / length)) < fov) continue;
            if (lineOfSight(player.level(), eye, point, block)) return true;
        }
        return false;
    }

    public static boolean canObserve(Player player) {
        return player.isAlive() && !player.isSpectator();
    }

    public static boolean isLit(Level level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos) >= UpfallConfig.getInt(UpfallConfig.OBSERVATION_MIN_LIGHT);
    }

    public static boolean holdsLight(Player player) {
        return isLight(player.getMainHandItem()) || isLight(player.getOffhandItem());
    }

    public static boolean isLight(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModTags.Items.HANDHELD_LIGHTS)) return true;
        return stack.getItem() instanceof BlockItem item && item.getBlock().defaultBlockState().getLightEmission() > 0;
    }

    public static boolean lineOfSight(BlockGetter level, Vec3 from, Vec3 to, BlockPos target) {
        Boolean blocked = BlockGetter.traverseBlocks(from, to, level, (getter, pos) -> {
            if (pos.equals(target)) return null;
            return blocksSight(getter, pos, getter.getBlockState(pos)) ? Boolean.TRUE : null;
        }, getter -> Boolean.FALSE);
        return !blocked;
    }

    public static boolean blocksSight(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ObservationBlocker blocker) return blocker.blocksObservation(state);
        return state.isSolidRender(level, pos);
    }
}
