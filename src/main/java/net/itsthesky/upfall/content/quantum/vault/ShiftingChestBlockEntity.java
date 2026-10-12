package net.itsthesky.upfall.content.quantum.vault;

import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ShiftingChestBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int viewers;

    public ShiftingChestBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumBlocks.SHIFTING_CHEST_ENTITY.get(), pos, state);
    }

    public boolean isViewed() {
        return viewers > 0;
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) viewers++;
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) viewers = Math.max(0, viewers - 1);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.upfall.shifting_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items, registries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, items, registries);
        }
    }

    public CompoundTag takeContents(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        setLootTable(null);
        return tag;
    }

    public void receiveContents(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
        setChanged();
    }
}
