package net.itsthesky.upfall.content.machines.block.entity;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.itsthesky.upfall.content.machines.KairiteFuel;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RewinderBlockEntity extends UpfallBlockEntity {
    public static final int REPAIR_INTERVAL = 40;
    public static final int FUEL_CAPACITY = 1000;
    public static final int POINTS_PER_UNIT = 11;

    private ItemStack stack = ItemStack.EMPTY;
    private int fuel;
    private int timer;
    private int lastSignal;

    public RewinderBlockEntity(BlockPos pos, BlockState state) {
        super(MachineBlockEntities.REWINDER.get(), pos, state);
    }

    public ItemStack stack() {
        return stack;
    }

    public int fuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
        setChanged();
    }

    public boolean canHold(ItemStack candidate) {
        return stack.isEmpty() && candidate.isDamageableItem();
    }

    public void place(ItemStack candidate) {
        stack = candidate.copyWithCount(1);
        timer = 0;
        changed();
    }

    public ItemStack take() {
        ItemStack out = stack;
        stack = ItemStack.EMPTY;
        timer = 0;
        changed();
        return out;
    }

    public int accept(ItemStack held) {
        int count = KairiteFuel.fit(held, fuel, FUEL_CAPACITY, POINTS_PER_UNIT);
        if (count > 0) {
            fuel += count * KairiteFuel.units(held) * POINTS_PER_UNIT;
            setChanged();
        }
        return count;
    }

    public int signal() {
        if (stack.isEmpty()) return 0;
        if (!stack.isDamaged()) return 15;
        return 1 + (int) (13.0F * (1.0F - stack.getDamageValue() / (float) stack.getMaxDamage()));
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            refreshSignal();
        }
    }

    private void refreshSignal() {
        int signal = signal();
        if (signal != lastSignal) {
            lastSignal = signal;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RewinderBlockEntity rewinder) {
        if (rewinder.stack.isEmpty() || !rewinder.stack.isDamaged() || rewinder.fuel <= 0) {
            return;
        }
        rewinder.timer++;
        ServerLevel server = (ServerLevel) level;
        if (rewinder.timer % 4 == 0) {
            double angle = level.getGameTime() * (Math.PI * 2 / 60.0);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5 + Math.sin(angle) * 0.3, pos.getY() + 1.02,
                    pos.getZ() + 0.5 - Math.cos(angle) * 0.3, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (rewinder.timer < REPAIR_INTERVAL) {
            return;
        }
        rewinder.timer = 0;
        rewinder.stack.setDamageValue(rewinder.stack.getDamageValue() - 1);
        rewinder.fuel--;
        rewinder.setChanged();
        level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.4F, 1.7F);
        rewinder.refreshSignal();
        if (!rewinder.stack.isDamaged()) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    public void dropContents() {
        if (level == null) return;
        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
        int dust = fuel / (KairiteFuel.DUST_UNITS * POINTS_PER_UNIT);
        if (dust > 0) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, new ItemStack(RustworksItems.KAIRITE_DUST.get(), dust));
        }
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
        if (!stack.isEmpty()) {
            out.putItem("Item", stack);
        }
        out.putInt("Fuel", fuel);
    }

    @Override
    protected void loadData(DataIn in) {
        stack = in.getItem("Item");
        fuel = in.getIntOr("Fuel", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
