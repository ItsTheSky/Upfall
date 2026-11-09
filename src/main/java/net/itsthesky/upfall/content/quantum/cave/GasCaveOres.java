package net.itsthesky.upfall.content.quantum.cave;

import net.itsthesky.upfall.content.ores.OresBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public final class GasCaveOres {
    private GasCaveOres() {}

    private record Weighted(Supplier<? extends Block> ore, int weight) {}

    private static final List<Weighted> PEARLSTONE = List.of(
            new Weighted(OresBlocks.PEARLSTONE_COAL_ORE, 40), new Weighted(OresBlocks.PEARLSTONE_IRON_ORE, 35),
            new Weighted(ModBlocks.NIMBUSITE_ORE, 15), new Weighted(OresBlocks.PEARLSTONE_DIAMOND_ORE, 10));
    private static final List<Weighted> RUSTSLATE = List.of(
            new Weighted(OresBlocks.RUSTSLATE_COAL_ORE, 45), new Weighted(OresBlocks.RUSTSLATE_IRON_ORE, 40),
            new Weighted(OresBlocks.RUSTSLATE_DIAMOND_ORE, 15));
    private static final List<Weighted> DUSKSLATE = List.of(new Weighted(ModBlocks.DUSKSLATE_NIMBUSITE_ORE, 1));

    public static BlockState pick(BlockState host, RandomSource random) {
        List<Weighted> ores;
        if (host.is(ModBlocks.PEARLSTONE.get())) {
            ores = PEARLSTONE;
        } else if (host.is(RustworksBlocks.RUSTSLATE.get())) {
            ores = RUSTSLATE;
        } else if (host.is(ModBlocks.DUSKSLATE.get())) {
            ores = DUSKSLATE;
        } else {
            return null;
        }
        int total = ores.stream().mapToInt(Weighted::weight).sum();
        int roll = random.nextInt(total);
        for (Weighted ore : ores) {
            roll -= ore.weight();
            if (roll < 0) return ore.ore().get().defaultBlockState();
        }
        return null;
    }
}
