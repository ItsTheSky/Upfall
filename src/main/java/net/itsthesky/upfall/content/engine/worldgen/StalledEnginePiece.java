package net.itsthesky.upfall.content.engine.worldgen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.itsthesky.upfall.content.engine.EngineBooks;
import net.itsthesky.upfall.content.engine.EngineLoot;
import net.itsthesky.upfall.content.engine.EngineStructures;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlockEntity;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StalledEnginePiece extends StructurePiece {
    private static final int SINK_DEPTH = 3;

    private final BlockPos origin;
    private final int rotation;
    private final long seed;
    private final int outflowDir;
    private final int outflowLength;
    private final float spireAngle;
    private StalledEngineLayout layout;

    public StalledEnginePiece(StalledEngineLayout layout) {
        super(EngineStructures.PIECE.get(), 0, layout.bounds());
        this.origin = layout.origin();
        this.rotation = layout.rotation();
        this.seed = layout.seed();
        this.outflowDir = layout.outflowDir();
        this.outflowLength = layout.outflowLength();
        this.spireAngle = layout.spireAngle();
        this.layout = layout;
    }

    public StalledEnginePiece(CompoundTag tag) {
        super(EngineStructures.PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
        this.rotation = tag.getInt("Rot");
        this.seed = tag.getLong("Seed");
        this.outflowDir = tag.getInt("OutflowDir");
        this.outflowLength = tag.getInt("OutflowLength");
        this.spireAngle = tag.getBoolean("HasSpire") ? tag.getFloat("Spire") : Float.NaN;
    }

    public synchronized StalledEngineLayout layout() {
        if (layout == null) {
            layout = new StalledEngineLayout(origin, rotation, seed, outflowDir, outflowLength, spireAngle);
        }
        return layout;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
        tag.putInt("Rot", rotation);
        tag.putLong("Seed", seed);
        tag.putInt("OutflowDir", outflowDir);
        tag.putInt("OutflowLength", outflowLength);
        tag.putBoolean("HasSpire", !Float.isNaN(spireAngle));
        if (!Float.isNaN(spireAngle)) {
            tag.putFloat("Spire", spireAngle);
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        StalledEngineLayout layout = layout();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<BlockState> entry : layout.blocks().long2ObjectEntrySet()) {
            long key = entry.getLongKey();
            pos.set(BlockPos.getX(key), BlockPos.getY(key), BlockPos.getZ(key));
            if (box.isInside(pos)) {
                level.setBlock(pos, entry.getValue(), Block.UPDATE_CLIENTS);
            }
        }
        for (StalledEngineLayout.Column column : layout.columns()) {
            if (box.isInside(pos.set(column.x(), box.minY(), column.z()))) {
                placeColumn(level, column, pos);
            }
        }
        if (box.isInside(layout.vaultChest())) {
            RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(layout.seed() ^ layout.vaultChest().asLong()),
                    layout.vaultChest(), EngineLoot.VAULT);
        }
        for (BlockPos cache : layout.caches()) {
            if (box.isInside(cache)) {
                RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(layout.seed() ^ cache.asLong()), cache, EngineLoot.CACHE);
            }
        }
        for (Map.Entry<BlockPos, EngineBooks.Book> entry : layout.lecterns().entrySet()) {
            if (box.isInside(entry.getKey()) && level.getBlockEntity(entry.getKey()) instanceof LecternBlockEntity lectern) {
                lectern.setBook(entry.getValue().create());
            }
        }
        BlockPos regulator = layout.regulator();
        if (box.isInside(regulator) && level.getBlockEntity(regulator) instanceof DrainRegulatorBlockEntity entity) {
            List<BlockPos> valves = new ArrayList<>();
            for (BlockPos valve : layout.valves()) {
                valves.add(valve.subtract(regulator));
            }
            List<BoundingBox> flood = new ArrayList<>();
            for (BoundingBox area : layout.flood()) {
                flood.add(area.moved(-regulator.getX(), -regulator.getY(), -regulator.getZ()));
            }
            entity.configure(valves, flood);
        }
    }

    private static void placeColumn(WorldGenLevel level, StalledEngineLayout.Column column, BlockPos.MutableBlockPos pos) {
        BlockState state = StalledEngineLayout.columnState(column);
        int bottom = Math.max(StalledEngineLayout.bottomOf(column), level.getMinBuildHeight());
        int sink = -1;
        for (int y = column.topY(); y >= bottom; y--) {
            pos.set(column.x(), y, column.z());
            BlockState current = level.getBlockState(pos);
            if (column.kind() == StalledEngineLayout.ColumnKind.OUTFLOW) {
                if (sink < 0 && y <= UpfallTerrain.CLOUD_BAND_MAX + 8 && !current.isAir()) {
                    sink = y;
                }
                if (sink >= 0 && y < sink - SINK_DEPTH) return;
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            } else {
                if (!current.isAir() && !current.canBeReplaced()) return;
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }
    }
}
