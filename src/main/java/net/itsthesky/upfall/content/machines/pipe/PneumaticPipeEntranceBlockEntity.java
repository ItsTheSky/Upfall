package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.ItemSink;
import net.itsthesky.upfall.compat.mc.McItemHandlers;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class PneumaticPipeEntranceBlockEntity extends UpfallBlockEntity {
    public static final int MAX_IN_TRANSIT = 256;
    private static final int ROUTE_CACHE_TICKS = 10;

    private final List<Transit> transit = new ArrayList<>();
    private final ItemSink inlet = new Inlet();
    private Optional<PipeRoute> cachedRoute = Optional.empty();
    private long routeComputedAt = -ROUTE_CACHE_TICKS;

    private static final class Transit {
        final ItemStack stack;
        int ticks;

        Transit(ItemStack stack, int ticks) {
            this.stack = stack;
            this.ticks = ticks;
        }
    }

    public PneumaticPipeEntranceBlockEntity(BlockPos pos, BlockState state) {
        super(PneumaticPipes.ENTRANCE.get(), pos, state);
    }

    public ItemSink inlet() {
        return inlet;
    }

    public int inTransit() {
        return transit.size();
    }

    public static int travelTicks(PipeRoute route) {
        return Math.max(1, (int) Math.ceil((route.length() + 1) / PipeTravel.SPEED));
    }

    public Optional<PipeRoute> route() {
        if (level == null) return Optional.empty();
        long time = level.getGameTime();
        if (time - routeComputedAt >= ROUTE_CACHE_TICKS || cachedRoute.map(r -> !PipeNetwork.isIntact(level, r)).orElse(false)) {
            cachedRoute = PipeNetwork.findRoute(level, worldPosition, exit -> true);
            routeComputedAt = time;
        }
        return cachedRoute;
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private boolean accept(ItemStack stack, boolean simulate) {
        return accept(stack, simulate, 0);
    }

    private boolean accept(ItemStack stack, boolean simulate, int pending) {
        if (stack.isEmpty() || transit.size() + pending >= MAX_IN_TRANSIT) return false;
        Optional<PipeRoute> route = route();
        if (route.isEmpty()) return false;
        if (!simulate) {
            transit.add(new Transit(stack.copy(), travelTicks(route.get())));
            setChanged();
        }
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticPipeEntranceBlockEntity entrance) {
        entrance.tick((ServerLevel) level, pos);
    }

    private void tick(ServerLevel level, BlockPos pos) {
        if (!transit.isEmpty()) {
            List<ItemStack> arrived = new ArrayList<>();
            for (Iterator<Transit> it = transit.iterator(); it.hasNext(); ) {
                Transit item = it.next();
                if (--item.ticks <= 0) {
                    arrived.add(item.stack);
                    it.remove();
                }
            }
            arrived.forEach(stack -> deliver(level, stack));
            setChanged();
        }
        Direction facing = facing();
        AABB mouth = new AABB(pos.relative(facing));
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, mouth.inflate(1.0))) {
            if (PipeTravel.wantsToEnter(player, pos, facing)) {
                PipeTravel.tryStart(player, pos);
            }
        }
        if ((level.getGameTime() + pos.asLong()) % 4 == 0) {
            boolean swallowed = false;
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, mouth, e -> e.isAlive() && !e.getItem().isEmpty())) {
                if (!accept(item.getItem(), false)) break;
                item.discard();
                swallowed = true;
            }
            if (swallowed) {
                Vec3 at = Vec3.atCenterOf(pos).add(PipeTravel.directionVec(facing).scale(0.6));
                level.playSound(null, pos, PneumaticPipes.ITEM.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                level.sendParticles(ParticleTypes.POOF, at.x, at.y, at.z, 3, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    private void deliver(ServerLevel level, ItemStack stack) {
        Optional<PipeRoute> route = route();
        BlockPos origin;
        Direction facing;
        if (route.isPresent()) {
            origin = route.get().exit();
            facing = route.get().exitFacing();
            BlockPos target = origin.relative(facing);
            stack = McItemHandlers.insert(level, target, facing.getOpposite(), stack, false, true);
        } else {
            origin = worldPosition;
            facing = facing();
        }
        if (!stack.isEmpty()) {
            Vec3 normal = PipeTravel.directionVec(facing);
            Vec3 at = Vec3.atCenterOf(origin).add(normal.scale(0.75));
            Vec3 motion = normal.scale(0.25).add(0, facing.getAxis().isHorizontal() ? 0.1 : 0, 0);
            ItemEntity entity = new ItemEntity(level, at.x, at.y - (facing == Direction.DOWN ? 0.25 : 0.125), at.z, stack, motion.x, motion.y, motion.z);
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);
            level.sendParticles(ParticleTypes.POOF, at.x, at.y, at.z, 2, 0.15, 0.15, 0.15, 0.01);
        }
        level.playSound(null, origin, PneumaticPipes.POP.get(), SoundSource.BLOCKS, 0.4F, 1.1F + level.getRandom().nextFloat() * 0.2F);
    }

    public void dropContents() {
        if (level == null) return;
        for (Transit item : transit) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), item.stack);
        }
        transit.clear();
    }

    //? if >=26.1 {
    /*@Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        dropContents();
    }
    *///?}

    @Override
    protected void saveData(DataOut out) {
        DataOut.ChildList list = out.childList("Transit");
        for (Transit item : transit) {
            DataOut entry = list.add();
            entry.putItem("Item", item.stack);
            entry.putInt("Ticks", item.ticks);
        }
    }

    @Override
    protected void loadData(DataIn in) {
        transit.clear();
        for (DataIn entry : in.childList("Transit")) {
            ItemStack stack = entry.getItem("Item");
            if (!stack.isEmpty()) {
                transit.add(new Transit(stack, entry.getIntOr("Ticks", 0)));
            }
        }
    }

    private final class Inlet implements ItemSink {
        @Override
        public boolean valid(ItemStack stack) {
            return true;
        }

        @Override
        public int acceptable(ItemStack stack, List<ItemStack> pending) {
            return PneumaticPipeEntranceBlockEntity.this.accept(stack, true, pending.size()) ? stack.getCount() : 0;
        }

        @Override
        public void accept(ItemStack stack) {
            PneumaticPipeEntranceBlockEntity.this.accept(stack, false);
        }
    }
}
