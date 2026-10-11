package net.itsthesky.upfall.worldgen;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// Same result as getBaseHeight(WORLD_SURFACE_WG) but evaluates each noise cell once for all its columns. Not thread-safe.
public final class SurfaceSampler {
    private static final int MAX_CELLS = 64;

    private final ChunkGenerator generator;
    private final LevelHeightAccessor level;
    private final RandomState randomState;
    private final NoiseGeneratorSettings settings;
    private final NoiseSettings noise;
    private final Aquifer.FluidPicker fluids;
    private final Long2IntOpenHashMap heights = new Long2IntOpenHashMap();
    private final Map<Long, Cell> cells = new LinkedHashMap<>(MAX_CELLS, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Cell> eldest) {
            return size() > MAX_CELLS;
        }
    };

    public SurfaceSampler(ChunkGenerator generator, LevelHeightAccessor level, RandomState randomState) {
        this.generator = generator;
        this.level = level;
        this.randomState = randomState;
        if (generator instanceof NoiseBasedChunkGenerator noiseGenerator) {
            this.settings = noiseGenerator.generatorSettings().value();
            this.noise = settings.noiseSettings().clampToHeightAccessor(level);
            this.fluids = fluidPicker(settings);
        } else {
            this.settings = null;
            this.noise = null;
            this.fluids = null;
        }
    }

    public int height(int x, int z) {
        long key = ChunkPos.asLong(x, z);
        if (heights.containsKey(key)) return heights.get(key);
        int height = compute(x, z);
        heights.put(key, height);
        return height;
    }

    private int compute(int x, int z) {
        if (settings == null) {
            return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
        }
        int cellHeight = noise.getCellHeight();
        int cellCountY = Mth.floorDiv(noise.height(), cellHeight);
        if (cellCountY <= 0) return level.getMinBuildHeight();
        int cellWidth = noise.getCellWidth();
        int cellX = Math.floorDiv(x, cellWidth);
        int cellZ = Math.floorDiv(z, cellWidth);
        Cell cell = cells.computeIfAbsent(ChunkPos.asLong(cellX, cellZ), k -> new Cell(cellX * cellWidth, cellZ * cellWidth));
        return cell.height(x, z, cellWidth, cellHeight, cellCountY, Mth.floorDiv(noise.minY(), cellHeight));
    }

    private static Aquifer.FluidPicker fluidPicker(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int seaLevel = settings.seaLevel();
        Aquifer.FluidStatus sea = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());
        return (x, y, z) -> y < Math.min(-54, seaLevel) ? lava : sea;
    }

    private final class Cell extends NoiseChunk {
        Cell(int x, int z) {
            super(1, randomState, x, z, noise, NoBeardifier.INSTANCE, settings, fluids, Blender.empty());
            initializeForFirstCellX();
            advanceCellX(0);
        }

        int height(int x, int z, int cellWidth, int cellHeight, int cellCountY, int minCellY) {
            double fx = Math.floorMod(x, cellWidth) / (double) cellWidth;
            double fz = Math.floorMod(z, cellWidth) / (double) cellWidth;
            for (int cy = cellCountY - 1; cy >= 0; cy--) {
                selectCellYZ(cy, 0);
                for (int dy = cellHeight - 1; dy >= 0; dy--) {
                    int y = (minCellY + cy) * cellHeight + dy;
                    updateForY(y, dy / (double) cellHeight);
                    updateForX(x, fx);
                    updateForZ(z, fz);
                    BlockState state = getInterpolatedState();
                    if (state == null) state = settings.defaultBlock();
                    if (!state.isAir()) return y + 1;
                }
            }
            return level.getMinBuildHeight();
        }
    }

    private enum NoBeardifier implements DensityFunctions.BeardifierOrMarker {
        INSTANCE;

        @Override
        public double compute(FunctionContext context) {
            return 0.0;
        }

        @Override
        public void fillArray(double[] array, ContextProvider provider) {
            Arrays.fill(array, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.BeardifierOrMarker.CODEC;
        }
    }
}
