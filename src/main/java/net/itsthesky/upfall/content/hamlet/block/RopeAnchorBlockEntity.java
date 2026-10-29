package net.itsthesky.upfall.content.hamlet.block;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RopeAnchorBlockEntity extends UpfallBlockEntity {
    private final List<BlockPos> links = new ArrayList<>();

    public RopeAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(HamletBlocks.ROPE_ANCHOR_ENTITY.get(), pos, state);
    }

    public List<BlockPos> links() {
        return Collections.unmodifiableList(links);
    }

    public boolean isLinkedTo(BlockPos other) {
        return links.contains(other);
    }

    public void addLinkSilently(BlockPos other) {
        if (!links.contains(other) && !other.equals(worldPosition)) {
            links.add(other.immutable());
        }
    }

    public void addLink(BlockPos other) {
        addLinkSilently(other);
        sync();
    }

    public void removeLink(BlockPos other) {
        if (links.remove(other)) {
            sync();
        }
    }

    public void clearLinks() {
        if (!links.isEmpty()) {
            links.clear();
            sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveData(DataOut out) {
        out.putLongArray("Links", links.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadData(DataIn in) {
        links.clear();
        for (long packed : in.getLongArrayOrEmpty("Links")) {
            links.add(BlockPos.of(packed));
        }
    }

    //? if >=26.1 {
    /*@Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            net.itsthesky.upfall.content.hamlet.rope.RopeLines.unlinkAll(level, pos);
        }
    }
    *///?}

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
