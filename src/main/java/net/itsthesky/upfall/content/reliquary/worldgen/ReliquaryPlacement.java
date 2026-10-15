package net.itsthesky.upfall.content.reliquary.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
import net.itsthesky.upfall.worldgen.ReliquaryZones;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class ReliquaryPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<ReliquaryPlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance).apply(instance, ReliquaryPlacement::new));

    private record SeedKey(long seed, long key) {}

    private volatile SeedKey cached;

    public ReliquaryPlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt,
                              Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, ReliquaryZones.CELL_CHUNKS, 0, RandomSpreadType.LINEAR);
    }

    public ReliquaryPlacement(int salt) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty());
    }

    private long key(long seed) {
        SeedKey current = cached;
        if (current == null || current.seed() != seed) {
            current = new SeedKey(seed, ReliquaryZones.worldKey(seed));
            cached = current;
        }
        return current.key();
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int regionX, int regionZ) {
        int cellX = ReliquaryZones.cellOfChunk(regionX);
        int cellZ = ReliquaryZones.cellOfChunk(regionZ);
        ChunkPos center = ReliquaryZones.nominalCenter(key(seed), cellX, cellZ);
        return center != null ? center : new ChunkPos(cellX * ReliquaryZones.CELL_CHUNKS, cellZ * ReliquaryZones.CELL_CHUNKS);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        ChunkPos center = ReliquaryZones.nominalCenter(key(structureState.getLevelSeed()), ReliquaryZones.cellOfChunk(x), ReliquaryZones.cellOfChunk(z));
        return center != null && center.x == x && center.z == z;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ReliquaryStructures.ZONE_PLACEMENT.get();
    }
}
