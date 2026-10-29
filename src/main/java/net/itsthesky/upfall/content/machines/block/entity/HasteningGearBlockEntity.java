package net.itsthesky.upfall.content.machines.block.entity;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.ItemSink;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.itsthesky.upfall.content.machines.KairiteFuel;
import net.itsthesky.upfall.content.machines.MachineBlockEntities;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HasteningGearBlockEntity extends UpfallBlockEntity {
    public static final int FUEL_CAPACITY = 24000;
    public static final int UNIT_TICKS = 267;
    public static final int FURNACE_PERIOD = 2;
    public static final int CROP_PERIOD = 20;

    private int fuel;
    private int age;
    private boolean cropsGrowing;
    private final ItemSink handler = new FuelSink();

    public HasteningGearBlockEntity(BlockPos pos, BlockState state) {
        super(MachineBlockEntities.HASTENING_GEAR.get(), pos, state);
    }

    public ItemSink handler() {
        return handler;
    }

    public int fuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
        setChanged();
    }

    public int accept(ItemStack held) {
        int count = KairiteFuel.fit(held, fuel, FUEL_CAPACITY, UNIT_TICKS);
        if (count > 0) {
            fuel += count * KairiteFuel.units(held) * UNIT_TICKS;
            setChanged();
            if (level != null) {
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
        return count;
    }

    public int signal() {
        return fuel <= 0 ? 0 : 1 + fuel * 14 / (FUEL_CAPACITY + 1);
    }

    public static boolean isCooking(BlockState state, AbstractFurnaceBlockEntity furnace) {
        return state.hasProperty(AbstractFurnaceBlock.LIT) && state.getValue(AbstractFurnaceBlock.LIT) && !furnace.getItem(0).isEmpty();
    }

    public static boolean isGrowing(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) < 3;
        }
        return (block instanceof CropBlock || block instanceof StemBlock || block instanceof SaplingBlock || block instanceof CocoaBlock
                || block instanceof SweetBerryBushBlock) && ((BonemealableBlock) block).isValidBonemealTarget(level, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HasteningGearBlockEntity gear) {
        ServerLevel server = (ServerLevel) level;
        gear.age++;
        boolean active = false;
        if (gear.fuel > 0) {
            Direction facing = state.getValue(HasteningGearBlock.FACING);
            BlockPos target = pos.relative(facing);
            if (level.getBlockEntity(target) instanceof AbstractFurnaceBlockEntity furnace) {
                BlockState furnaceState = level.getBlockState(target);
                active = isCooking(furnaceState, furnace);
                if (active && gear.age % FURNACE_PERIOD == 0) {
                    AbstractFurnaceBlockEntity.serverTick(server, target, furnaceState, furnace);
                }
            } else {
                if (gear.age % CROP_PERIOD == 0) {
                    gear.cropsGrowing = boostCrops(server, pos, facing);
                }
                active = gear.cropsGrowing;
            }
        }
        if (active) {
            gear.fuel--;
            if (gear.age % 20 == 0) {
                level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.5F, 1.3F);
            }
            if (gear.fuel % UNIT_TICKS == 0) {
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
            if (gear.fuel == 0) {
                gear.setChanged();
            }
        } else {
            gear.cropsGrowing = false;
        }
        if (state.getValue(HasteningGearBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(HasteningGearBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    private static boolean boostCrops(ServerLevel level, BlockPos pos, Direction facing) {
        //? if >=26.1 {
        /*return boostCrops(level, pos, facing, Math.min(1.0F, level.getGameRules().get(GameRules.RANDOM_TICK_SPEED) * CROP_PERIOD / 4096.0F));
        *///?} else {
        return boostCrops(level, pos, facing, Math.min(1.0F, level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) * CROP_PERIOD / 4096.0F));
        //?}
    }

    public static boolean boostCrops(ServerLevel level, BlockPos pos, Direction facing, float chance) {
        boolean any = false;
        for (BlockPos target : BlockPos.betweenClosed(pos.relative(facing, 2).offset(-1, -1, -1), pos.relative(facing, 2).offset(1, 1, 1))) {
            BlockState crop = level.getBlockState(target);
            if (!isGrowing(level, target, crop)) continue;
            any = true;
            if (level.getRandom().nextFloat() < chance) {
                crop.randomTick(level, target.immutable(), level.getRandom());
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.3, target.getZ() + 0.5, 2, 0.25, 0.2, 0.25, 0.0);
            }
        }
        return any;
    }

    public void dropContents() {
        if (level == null) return;
        int dust = fuel / (KairiteFuel.DUST_UNITS * UNIT_TICKS);
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
        out.putInt("Fuel", fuel);
    }

    @Override
    protected void loadData(DataIn in) {
        fuel = in.getIntOr("Fuel", 0);
    }

    private final class FuelSink implements ItemSink {
        @Override
        public boolean valid(ItemStack stack) {
            return KairiteFuel.units(stack) > 0;
        }

        @Override
        public int acceptable(ItemStack stack, List<ItemStack> pending) {
            int stored = fuel;
            for (ItemStack queued : pending) {
                stored += queued.getCount() * KairiteFuel.units(queued) * UNIT_TICKS;
            }
            return KairiteFuel.fit(stack, stored, FUEL_CAPACITY, UNIT_TICKS);
        }

        @Override
        public void accept(ItemStack stack) {
            HasteningGearBlockEntity.this.accept(stack);
        }
    }
}
