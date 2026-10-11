package net.itsthesky.upfall.content.engine.block;

import net.itsthesky.upfall.content.engine.EngineBlocks;
import net.itsthesky.upfall.content.engine.EngineSounds;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class DrainRegulatorBlockEntity extends BlockEntity {
    public static final int CHECK_INTERVAL = 10;
    public static final int DRAIN_INTERVAL = 6;

    private final List<BlockPos> valves = new ArrayList<>();
    private final List<BoundingBox> flood = new ArrayList<>();
    private int lastOpen = -1;
    private boolean draining;
    private int layer;

    public DrainRegulatorBlockEntity(BlockPos pos, BlockState state) {
        super(EngineBlocks.DRAIN_REGULATOR_ENTITY.get(), pos, state);
    }

    public void configure(List<BlockPos> valveOffsets, List<BoundingBox> floodOffsets) {
        valves.clear();
        valves.addAll(valveOffsets);
        flood.clear();
        flood.addAll(floodOffsets);
        setChanged();
    }

    public int valveCount() {
        return valves.size();
    }

    public boolean draining() {
        return draining;
    }

    public int openValves(Level level) {
        int open = 0;
        for (BlockPos offset : valves) {
            BlockPos pos = worldPosition.offset(offset);
            if (level.isLoaded(pos) && DrainValveBlock.isOpen(level.getBlockState(pos))) {
                open++;
            }
        }
        return open;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DrainRegulatorBlockEntity regulator) {
        if (state.getValue(DrainRegulatorBlock.DRAINED) || !(level instanceof ServerLevel server)) return;
        long time = level.getGameTime();
        if (regulator.draining) {
            if (time % DRAIN_INTERVAL == 0) {
                regulator.drainStep(server, state);
            }
            return;
        }
        if (time % CHECK_INTERVAL != 0 || regulator.valves.isEmpty()) return;
        int open = regulator.openValves(level);
        if (open != regulator.lastOpen) {
            if (regulator.lastOpen >= 0 && open > regulator.lastOpen) {
                level.playSound(null, pos, EngineSounds.REGULATOR_CLANK.get(), SoundSource.BLOCKS, 1.0F, 0.6F + 0.2F * open);
            }
            regulator.lastOpen = open;
            regulator.setChanged();
        }
        if (open >= regulator.valves.size()) {
            regulator.draining = true;
            regulator.layer = regulator.flood.stream().mapToInt(BoundingBox::maxY).max().orElse(0);
            level.playSound(null, pos, EngineSounds.REGULATOR_DRAIN.get(), SoundSource.BLOCKS, 1.5F, 0.8F);
            regulator.setChanged();
        }
    }

    private void drainStep(ServerLevel level, BlockState state) {
        int minY = flood.stream().mapToInt(BoundingBox::minY).min().orElse(0);
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (BoundingBox box : flood) {
            if (layer < box.minY() || layer > box.maxY()) continue;
            for (int x = box.minX(); x <= box.maxX(); x++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    pos.setWithOffset(worldPosition, x, layer, z);
                    if (level.isLoaded(pos) && level.getBlockState(pos).is(EngineBlocks.STAGNANT_MIST.get())) {
                        level.setBlock(pos, air, Block.UPDATE_ALL);
                        if (level.random.nextInt(3) == 0) {
                            level.sendParticles(ModParticles.MIST_MOTE.get(), pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                                    2, 0.3, 0.2, 0.3, 0.01);
                        }
                    }
                }
            }
        }
        layer--;
        if (layer < minY) {
            draining = false;
            level.setBlock(worldPosition, state.setValue(DrainRegulatorBlock.DRAINED, true), Block.UPDATE_ALL);
            level.playSound(null, worldPosition, EngineSounds.REGULATOR_CLANK.get(), SoundSource.BLOCKS, 1.2F, 0.5F);
        } else if ((layer & 1) == 0) {
            level.playSound(null, worldPosition, EngineSounds.REGULATOR_DRAIN.get(), SoundSource.BLOCKS, 0.6F, 1.0F + 0.05F * (layer - minY));
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int[] packed = new int[valves.size() * 3];
        for (int i = 0; i < valves.size(); i++) {
            packed[i * 3] = valves.get(i).getX();
            packed[i * 3 + 1] = valves.get(i).getY();
            packed[i * 3 + 2] = valves.get(i).getZ();
        }
        tag.putIntArray("Valves", packed);
        ListTag boxes = new ListTag();
        for (BoundingBox box : flood) {
            boxes.add(new IntArrayTag(new int[]{box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()}));
        }
        tag.put("Flood", boxes);
        tag.putInt("LastOpen", lastOpen);
        tag.putBoolean("Draining", draining);
        tag.putInt("Layer", layer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        valves.clear();
        int[] packed = tag.getIntArray("Valves");
        for (int i = 0; i + 2 < packed.length; i += 3) {
            valves.add(new BlockPos(packed[i], packed[i + 1], packed[i + 2]));
        }
        flood.clear();
        for (Tag entry : tag.getList("Flood", Tag.TAG_INT_ARRAY)) {
            int[] v = ((IntArrayTag) entry).getAsIntArray();
            if (v.length == 6) {
                flood.add(new BoundingBox(v[0], v[1], v[2], v[3], v[4], v[5]));
            }
        }
        lastOpen = tag.contains("LastOpen") ? tag.getInt("LastOpen") : -1;
        draining = tag.getBoolean("Draining");
        layer = tag.getInt("Layer");
    }
}
