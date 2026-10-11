package net.itsthesky.upfall.content.machines.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class PneumaticPipeEntranceBlockEntity extends BlockEntity {
    public static final int MAX_IN_TRANSIT = 256;
    private static final int ROUTE_CACHE_TICKS = 10;

    private final List<Transit> transit = new ArrayList<>();
    private final IItemHandler inlet = new Inlet();
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

    public IItemHandler inlet() {
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
        if (stack.isEmpty() || transit.size() >= MAX_IN_TRANSIT) return false;
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
                Vec3 at = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.6));
                level.playSound(null, pos, PneumaticPipes.ITEM.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.2F);
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
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, facing.getOpposite());
            if (handler != null) {
                stack = ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
        } else {
            origin = worldPosition;
            facing = facing();
        }
        if (!stack.isEmpty()) {
            Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
            Vec3 at = Vec3.atCenterOf(origin).add(normal.scale(0.75));
            Vec3 motion = normal.scale(0.25).add(0, facing.getAxis().isHorizontal() ? 0.1 : 0, 0);
            ItemEntity entity = new ItemEntity(level, at.x, at.y - (facing == Direction.DOWN ? 0.25 : 0.125), at.z, stack, motion.x, motion.y, motion.z);
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);
            level.sendParticles(ParticleTypes.POOF, at.x, at.y, at.z, 2, 0.15, 0.15, 0.15, 0.01);
        }
        level.playSound(null, origin, PneumaticPipes.POP.get(), SoundSource.BLOCKS, 0.4F, 1.1F + level.random.nextFloat() * 0.2F);
    }

    public void dropContents() {
        if (level == null) return;
        for (Transit item : transit) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), item.stack);
        }
        transit.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Transit item : transit) {
            CompoundTag entry = new CompoundTag();
            entry.put("Item", item.stack.save(registries));
            entry.putInt("Ticks", item.ticks);
            list.add(entry);
        }
        tag.put("Transit", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        transit.clear();
        ListTag list = tag.getList("Transit", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack.parse(registries, entry.getCompound("Item")).ifPresent(stack -> transit.add(new Transit(stack, entry.getInt("Ticks"))));
        }
    }

    private final class Inlet implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return accept(stack, simulate) ? ItemStack.EMPTY : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
