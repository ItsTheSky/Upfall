package net.itsthesky.upfall.content.quantum.pedestal;

import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        this(QuantumBlocks.PEDESTAL_ENTITY.get(), pos, state);
    }

    protected PedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack item() {
        return item;
    }

    public void setItem(ItemStack stack) {
        this.item = stack;
        sync();
    }

    public ItemStack takeItem() {
        ItemStack taken = item;
        item = ItemStack.EMPTY;
        sync();
        return taken;
    }

    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Item", item.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        item = tag.contains("Item") ? ItemStack.parseOptional(registries, tag.getCompound("Item")) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
