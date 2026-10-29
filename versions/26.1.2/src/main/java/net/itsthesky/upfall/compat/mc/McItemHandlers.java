package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class McItemHandlers {
    private McItemHandlers() {}

    // One handler per sink, so every caller in a transaction shares its pending journal.
    // The value is weak because the handler references its sink, which would pin the weak key.
    private static final Map<ItemSink, WeakReference<SinkHandler>> HANDLERS = new WeakHashMap<>();

    public static <BE extends BlockEntity> void registerSink(RegisterCapabilitiesEvent event, BlockEntityType<BE> type, Function<BE, ItemSink> sink) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> handler(sink.apply(be)));
    }

    private static SinkHandler handler(ItemSink sink) {
        synchronized (HANDLERS) {
            WeakReference<SinkHandler> ref = HANDLERS.get(sink);
            SinkHandler handler = ref == null ? null : ref.get();
            if (handler == null) {
                handler = new SinkHandler(sink);
                HANDLERS.put(sink, new WeakReference<>(handler));
            }
            return handler;
        }
    }

    public static ItemStack insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate, boolean stacking) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        if (!stacking) {
            return ItemUtil.insertItemReturnRemaining(handler, stack, simulate, null);
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            int left = stack.getCount() - inserted;
            return left <= 0 ? ItemStack.EMPTY : stack.copyWithCount(left);
        }
    }

    public static boolean hasHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side) != null;
    }

    public static ItemStack stackIn(Level level, BlockPos pos, @Nullable Direction side, int slot) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        return handler == null ? ItemStack.EMPTY : ItemUtil.getStack(handler, slot);
    }

    private static final class SinkHandler implements ResourceHandler<ItemResource> {
        private final ItemSink sink;
        private final Journal journal = new Journal();
        private List<ItemStack> pending = List.of();

        private SinkHandler(ItemSink sink) {
            this.sink = sink;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0L;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 64L;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == 0 && sink.valid(resource.toStack(1));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || resource.isEmpty() || amount <= 0) {
                return 0;
            }
            int offered = Math.min(amount, resource.getMaxStackSize());
            ItemStack stack = resource.toStack(offered);
            int accepted = Math.min(offered, sink.acceptable(stack, pending));
            if (accepted <= 0) {
                return 0;
            }
            journal.updateSnapshots(transaction);
            List<ItemStack> next = new ArrayList<>(pending);
            next.add(stack.copyWithCount(accepted));
            pending = List.copyOf(next);
            return accepted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        private final class Journal extends SnapshotJournal<List<ItemStack>> {
            @Override
            protected List<ItemStack> createSnapshot() {
                return pending;
            }

            @Override
            protected void revertToSnapshot(List<ItemStack> snapshot) {
                pending = snapshot;
            }

            @Override
            protected void onRootCommit(List<ItemStack> originalState) {
                List<ItemStack> delivered = pending;
                pending = List.of();
                for (ItemStack stack : delivered) {
                    sink.accept(stack);
                }
            }
        }
    }
}
