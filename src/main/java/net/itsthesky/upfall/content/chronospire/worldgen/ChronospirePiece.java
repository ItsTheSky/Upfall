package net.itsthesky.upfall.content.chronospire.worldgen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.itsthesky.upfall.content.chronospire.block.entity.GlyphPedestalBlockEntity;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.itsthesky.upfall.content.chronospire.entity.PlatformPath;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.List;
import java.util.Map;

public class ChronospirePiece extends StructurePiece {
    private final BlockPos origin;
    private final int rotation;
    private final long seed;
    private ChronospireBlueprint blueprint;

    public ChronospirePiece(BlockPos origin, int rotation, long seed) {
        super(ChronoRegistries.CHRONOSPIRE_PIECE.get(), 0, ChronospireBlueprint.bounds(origin));
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
    }

    public ChronospirePiece(CompoundTag tag) {
        super(ChronoRegistries.CHRONOSPIRE_PIECE.get(), tag);
        this.origin = new BlockPos(McNbt.getInt(tag, "OX"), McNbt.getInt(tag, "OY"), McNbt.getInt(tag, "OZ"));
        this.rotation = McNbt.getInt(tag, "Rot");
        this.seed = McNbt.getLong(tag, "Seed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
        tag.putInt("Rot", rotation);
        tag.putLong("Seed", seed);
    }

    public int rotation() {
        return rotation;
    }

    public synchronized ChronospireBlueprint blueprint() {
        if (blueprint == null) {
            blueprint = ChronospireBlueprint.build(origin, rotation, seed);
        }
        return blueprint;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        ChronospireBlueprint plan = blueprint();
        long time = ChronoClock.time(level.getLevel());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<BlockState> entry : plan.blocks().long2ObjectEntrySet()) {
            long key = entry.getLongKey();
            pos.set(BlockPos.getX(key), BlockPos.getY(key), BlockPos.getZ(key));
            if (!box.isInside(pos)) continue;
            BlockState state = entry.getValue();
            if (state.getBlock() instanceof PhaseTrackingBlock tracking) {
                state = tracking.stateAt(state, time);
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                level.scheduleTick(pos.immutable(), tracking, Math.min(tracking.nextCheckDelay(state, time), 1 + (int) Math.floorMod(key, 40L)));
            } else {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }

        if (boundingBox.intersects(box)) {
            for (int x = Math.max(box.minX(), boundingBox.minX()); x <= Math.min(box.maxX(), boundingBox.maxX()); x++) {
                for (int z = Math.max(box.minZ(), boundingBox.minZ()); z <= Math.min(box.maxZ(), boundingBox.maxZ()); z++) {
                    for (int y = Math.max(box.minY(), boundingBox.minY()); y <= Math.min(box.maxY(), boundingBox.maxY()); y++) {
                        pos.set(x, y, z);
                        if (plan.inClearedVolume(pos) && !plan.blocks().containsKey(pos.asLong()) && !level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }

        for (BlockPos cache : plan.caches()) {
            if (box.isInside(cache)) {
                RandomizableContainer.setBlockEntityLootTable(level, random, cache, ChronospireLoot.VAULT);
            }
        }
        for (Map.Entry<BlockPos, List<BoundingBox>> entry : plan.gates().entrySet()) {
            if (!box.isInside(entry.getKey())) continue;
            BlockEntity blockEntity = level.getBlockEntity(entry.getKey());
            if (blockEntity instanceof GlyphPedestalBlockEntity pedestal) {
                entry.getValue().forEach(pedestal.gates()::add);
            } else if (blockEntity instanceof ChronoGnomonBlockEntity gnomon) {
                entry.getValue().forEach(gnomon.gates()::add);
            }
        }
        for (PlatformPath path : plan.platforms()) {
            if (!box.isInside(BlockPos.containing(path.points().get(0)))) continue;
            //? if >=26.1 {
            /*DriftPlatform platform = ChronoRegistries.DRIFT_PLATFORM.get().create(level.getLevel(), net.minecraft.world.entity.MobSpawnType.STRUCTURE);
            *///?} else {
            DriftPlatform platform = ChronoRegistries.DRIFT_PLATFORM.get().create(level.getLevel());
            //?}
            if (platform != null) {
                platform.setPath(path);
                platform.setPos(path.points().get(0));
                level.addFreshEntity(platform);
            }
        }
    }
}
