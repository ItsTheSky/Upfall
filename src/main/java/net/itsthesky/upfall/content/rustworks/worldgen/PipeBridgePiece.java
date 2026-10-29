package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class PipeBridgePiece extends StructurePiece {
    private final PipeBridge bridge;
    private final int from;
    private final int to;

    public PipeBridgePiece(PipeBridge bridge, int from, int to) {
        super(RustworksStructures.PIECE.get(), 0, bridge.bounds(from, to));
        this.bridge = bridge;
        this.from = from;
        this.to = to;
    }

    public PipeBridgePiece(CompoundTag tag) {
        super(RustworksStructures.PIECE.get(), tag);
        this.bridge = PipeBridge.load(McNbt.getCompound(tag, "Bridge"));
        this.from = McNbt.getInt(tag, "From");
        this.to = McNbt.getInt(tag, "To");
    }

    public static void addAll(StructurePiecesBuilder builder, PipeBridge bridge) {
        for (int t = bridge.first(); t <= bridge.last(); t += PipeBridge.SEGMENT) {
            builder.addPiece(new PipeBridgePiece(bridge, t, Math.min(bridge.last(), t + PipeBridge.SEGMENT - 1)));
        }
    }

    public PipeBridge bridge() {
        return bridge;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("Bridge", bridge.save());
        tag.putInt("From", from);
        tag.putInt("To", to);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        PipeBridge.Sink sink = new PipeBridge.Sink() {
            @Override
            public boolean isOpen(BlockPos pos) {
                if (!box.isInside(pos)) return false;
                BlockState state = level.getBlockState(pos);
                return state.isAir() || state.canBeReplaced() || state.is(ModBlocks.MIST.get());
            }

            @Override
            public void set(BlockPos pos, BlockState state) {
                if (box.isInside(pos)) {
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                }
            }
        };
        for (int t = from; t <= to; t++) {
            bridge.place(t, sink);
        }
    }
}
