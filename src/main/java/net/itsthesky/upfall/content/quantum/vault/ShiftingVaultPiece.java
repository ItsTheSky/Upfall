package net.itsthesky.upfall.content.quantum.vault;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumLoot;
import net.itsthesky.upfall.content.quantum.QuantumStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;

public class ShiftingVaultPiece extends StructurePiece {
    private final BlockPos origin;
    private final long seed;
    private final List<BlockPos> tunnel;
    private ShiftingVaultLayout layout;

    public ShiftingVaultPiece(BlockPos origin, long seed, VaultTunnel.Path path) {
        this(origin, seed, path.all(), null);
    }

    private ShiftingVaultPiece(BlockPos origin, long seed, List<BlockPos> tunnel, ShiftingVaultLayout layout) {
        super(QuantumStructures.SHIFTING_VAULT_PIECE.get(), 0, bounds(origin, tunnel));
        this.origin = origin;
        this.seed = seed;
        this.tunnel = tunnel;
        this.layout = layout;
    }

    public ShiftingVaultPiece(CompoundTag tag) {
        super(QuantumStructures.SHIFTING_VAULT_PIECE.get(), tag);
        this.origin = BlockPos.of(tag.getLong("Origin"));
        this.seed = tag.getLong("Seed");
        this.tunnel = new ArrayList<>();
        for (long l : tag.getLongArray("Tunnel")) tunnel.add(BlockPos.of(l));
    }

    private static BoundingBox bounds(BlockPos origin, List<BlockPos> tunnel) {
        BoundingBox box = new ShiftingVaultLayout(origin, 0L).bounds();
        for (BlockPos node : tunnel) {
            box.encapsulate(node.offset(-1, -1, -1));
            box.encapsulate(node.offset(2, VaultTunnel.HEIGHT, 2));
        }
        return box;
    }

    public synchronized ShiftingVaultLayout layout() {
        if (layout == null) layout = new ShiftingVaultLayout(origin, seed);
        return layout;
    }

    public List<BlockPos> tunnel() {
        return tunnel;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong());
        tag.putLong("Seed", seed);
        tag.putLongArray("Tunnel", tunnel.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        ShiftingVaultLayout layout = layout();
        BoundingBox vault = layout.bounds();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<BlockState> entry : layout.blocks().long2ObjectEntrySet()) {
            pos.set(entry.getLongKey());
            if (box.isInside(pos)) level.setBlock(pos, entry.getValue(), Block.UPDATE_CLIENTS);
        }
        BlockState gas = QuantumBlocks.VEIL_GAS.get().defaultBlockState();
        for (BlockPos node : tunnel) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    for (int dy = 0; dy < VaultTunnel.HEIGHT; dy++) {
                        pos.set(node.getX() + dx, node.getY() + dy, node.getZ() + dz);
                        if (!box.isInside(pos) || vault.isInside(pos)) continue;
                        BlockState current = level.getBlockState(pos);
                        if (!current.isAir() && current.getFluidState().isEmpty() && !current.is(gas.getBlock())) {
                            level.setBlock(pos, gas, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        BlockPos chest = layout.chestSpots().get(ShiftingVaultLayout.INITIAL_STATE);
        if (box.isInside(chest)) {
            RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(seed ^ chest.asLong()), chest, QuantumLoot.SHIFTING_VAULT);
        }
        BlockPos core = layout.core();
        if (box.isInside(core) && level.getBlockEntity(core) instanceof VaultCoreBlockEntity entity) {
            entity.configure(layout.chestSpots(), layout.chestFacings(), ShiftingVaultLayout.INITIAL_STATE, layout.frost(), layout.lamps(),
                    layout.darkZones(), layout.interior());
        }
    }
}
