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

    //? if >=26.1 {
    /*@Override
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) viewers++;
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) viewers = Math.max(0, viewers - 1);
    }
    *///?} else {
    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) viewers++;
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) viewers = Math.max(0, viewers - 1);
    }
    //?}

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

    //? if >=26.1 {
    /*@Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, items);
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, items);
        }
    }
    *///?} else {
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
    //?}

    public CompoundTag takeContents(HolderLookup.Provider registries) {
        //? if >=26.1 {
        /*net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
        saveAdditional(output);
        CompoundTag tag = output.buildResult();
        *///?} else {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        //?}
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        setLootTable(null);
        return tag;
    }

    public void receiveContents(CompoundTag tag, HolderLookup.Provider registries) {
        //? if >=26.1 {
        /*loadAdditional(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, tag));
        *///?} else {
        loadAdditional(tag, registries);
        //?}
        setChanged();
    }
}
