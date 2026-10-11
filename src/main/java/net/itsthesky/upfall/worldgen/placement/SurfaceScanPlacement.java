package net.itsthesky.upfall.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.itsthesky.upfall.registry.ModPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class SurfaceScanPlacement extends PlacementModifier {
    public static final MapCodec<SurfaceScanPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CaveSurface.CODEC.fieldOf("surface").forGetter(p -> p.surface),
            BlockPredicate.CODEC.fieldOf("support").forGetter(p -> p.support),
            Codec.INT.fieldOf("min_y").forGetter(p -> p.minY),
            Codec.INT.fieldOf("max_y").forGetter(p -> p.maxY)
    ).apply(instance, SurfaceScanPlacement::new));

    private final CaveSurface surface;
    private final BlockPredicate support;
    private final int minY;
    private final int maxY;

    public SurfaceScanPlacement(CaveSurface surface, BlockPredicate support, int minY, int maxY) {
        this.surface = surface;
        this.support = support;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static SurfaceScanPlacement floor(BlockPredicate support, int minY, int maxY) {
        return new SurfaceScanPlacement(CaveSurface.FLOOR, support, minY, maxY);
    }

    public static SurfaceScanPlacement ceiling(BlockPredicate support, int minY, int maxY) {
        return new SurfaceScanPlacement(CaveSurface.CEILING, support, minY, maxY);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        WorldGenLevel level = context.getLevel();
        int top = Math.min(maxY, context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()));
        Direction toSupport = surface == CaveSurface.FLOOR ? Direction.DOWN : Direction.UP;
        IntArrayList candidates = new IntArrayList();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), top, pos.getZ());
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (int y = top; y >= minY; y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (!state.isAir()) continue;
            neighbour.setWithOffset(cursor, toSupport);
            if (support.test(level, neighbour)) {
                candidates.add(y);
            }
        }
        if (candidates.isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(new BlockPos(pos.getX(), candidates.getInt(random.nextInt(candidates.size())), pos.getZ()));
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacements.SURFACE_SCAN.get();
    }
}
