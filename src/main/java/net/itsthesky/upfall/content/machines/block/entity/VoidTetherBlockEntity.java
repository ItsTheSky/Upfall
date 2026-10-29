package net.itsthesky.upfall.content.machines.block.entity;

import net.itsthesky.upfall.compat.mc.McItemHandlers;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidTetherBlockEntity extends BlockEntity {
    public static final int RADIUS = 16;
    public static final int INTERVAL = 10;
    public static final int FLOOR_MARGIN = 8;
    private static final int MAX_PER_SCAN = 16;
    private static final double FALL_SPEED = -0.2;
    private static final int BELOW_WORLD = 64;

    public VoidTetherBlockEntity(BlockPos pos, BlockState state) {
        super(MachineBlockEntities.VOID_TETHER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoidTetherBlockEntity tether) {
        if (Math.floorMod(level.getGameTime() + pos.hashCode(), INTERVAL) != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        AABB box = new AABB(pos.getX() - RADIUS, McLevel.minY(level) - BELOW_WORLD, pos.getZ() - RADIUS,
                pos.getX() + RADIUS + 1, pos.getY(), pos.getZ() + RADIUS + 1);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, item -> item.isAlive() && isLost(level, item));
        for (int i = 0; i < Math.min(items.size(), MAX_PER_SCAN); i++) {
            catchItem(server, pos, items.get(i));
        }
    }

    public static boolean isLost(Level level, ItemEntity item) {
        int minY = McLevel.minY(level);
        if (item.getY() < minY + FLOOR_MARGIN) {
            return true;
        }
        if (item.onGround() || item.getDeltaMovement().y > FALL_SPEED) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(item.getBlockX(), 0, item.getBlockZ());
        for (int y = item.getBlockY() - 1; y >= minY; y--) {
            cursor.setY(y);
            if (!level.hasChunkAt(cursor)) {
                return false;
            }
            BlockState state = level.getBlockState(cursor);
            if (!state.getCollisionShape(level, cursor).isEmpty() || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void catchItem(ServerLevel level, BlockPos tether, ItemEntity item) {
        Vec3 from = item.position();
        Vec3 to = Vec3.atBottomCenterOf(tether).add(0, 1.0, 0);
        ItemStack stack = item.getItem().copy();
        for (Direction side : Direction.values()) {
            stack = McItemHandlers.insert(level, tether.relative(side), side.getOpposite(), stack, false, false);
            if (stack.isEmpty()) break;
        }
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
            item.setPos(to.x, to.y + 0.1, to.z);
            item.setDeltaMovement(Vec3.ZERO);
            item.setDefaultPickUpDelay();
        }
        level.sendParticles(ParticleTypes.PORTAL, from.x, from.y, from.z, 12, 0.2, 0.2, 0.2, 0.4);
        for (int i = 1; i <= 14; i++) {
            Vec3 p = from.lerp(to, i / 14.0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.02);
        }
        level.playSound(null, tether, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.4F, 1.5F);
        level.playSound(null, BlockPos.containing(from), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.6F, 0.6F);
    }
}
