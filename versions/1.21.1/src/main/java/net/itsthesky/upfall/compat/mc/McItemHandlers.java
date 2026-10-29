package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public final class McItemHandlers {
    private McItemHandlers() {}

    public static <BE extends BlockEntity> void registerSink(RegisterCapabilitiesEvent event, BlockEntityType<BE> type, Function<BE, ItemSink> sink) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> new SinkHandler(sink.apply(be)));
    }

    public static ItemStack insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate, boolean stacking) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) {
            return stack;
        }
        return stacking ? ItemHandlerHelper.insertItemStacked(handler, stack, simulate) : ItemHandlerHelper.insertItem(handler, stack, simulate);
    }

    public static boolean hasHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null;
    }

    public static ItemStack stackIn(Level level, BlockPos pos, @Nullable Direction side, int slot) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    private record SinkHandler(ItemSink sink) implements IItemHandler {
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
            int accepted = Math.min(stack.getCount(), sink.acceptable(stack, List.of()));
            if (accepted <= 0) {
                return stack;
            }
            if (!simulate) {
                sink.accept(stack.copyWithCount(accepted));
            }
            return accepted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
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
            return sink.valid(stack);
        }
    }
}
