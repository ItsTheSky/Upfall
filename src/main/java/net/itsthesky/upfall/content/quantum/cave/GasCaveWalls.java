package net.itsthesky.upfall.content.quantum.cave;

import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class GasCaveWalls {
    private static final double PATCH = 0.42;
    private static final double PATCH_CORE = 0.56;
    private static final int STRATA_PERCENT = 16;
    private static final double STRATA_THICKNESS = 3.0;

    private record Palette(Supplier<? extends Block> host, Supplier<? extends Block> worn, List<Supplier<? extends Block>> strata,
                           Supplier<? extends Block> patch, Supplier<? extends Block> core, Supplier<? extends Block> surface) {}

    private static final List<Palette> PALETTES = List.of(
            new Palette(ModBlocks.PEARLSTONE, ModBlocks.COBBLED_PEARLSTONE,
                    List.of(GrottoBlocks.MOONSHELL, GrottoBlocks.MOONSHELL, () -> Blocks.CALCITE, () -> Blocks.TUFF),
                    () -> Blocks.TUFF, ModBlocks.DUSKSLATE, ModBlocks.SKYSOIL),
            new Palette(ModBlocks.DUSKSLATE, ModBlocks.COBBLED_DUSKSLATE,
                    List.of(ModBlocks.PEARLSTONE, () -> Blocks.TUFF, () -> Blocks.TUFF),
                    GrottoBlocks.LUMEN_ROCK, ModBlocks.GILDED_DUSKSLATE, null),
            new Palette(RustworksBlocks.RUSTSLATE, RustworksBlocks.COBBLED_RUSTSLATE,
                    List.of(ModBlocks.DUSKSLATE, () -> Blocks.TUFF),
                    RustworksBlocks.RUSTSOIL, RustworksBlocks.RUSTSOIL, null));

    private final ImprovedNoise patches;
    private final ImprovedNoise detail;
    private final long seed;

    public GasCaveWalls(long seed) {
        RandomSource random = RandomSource.create(seed ^ 0x6A5C4E57A11L);
        this.patches = new ImprovedNoise(random);
        this.detail = new ImprovedNoise(random);
        this.seed = random.nextLong();
    }

    public BlockState restyle(BlockState host, int x, int y, int z, boolean floor, boolean nearSurface) {
        Palette palette = palette(host);
        if (palette == null) return null;
        if (floor && detail.noise(x * 0.3, y * 0.3 + 5.1, z * 0.3) > -0.1) return palette.worn().get().defaultBlockState();
        double patch = patches.noise(x * 0.14, y * 0.14, z * 0.14);
        if (patch > PATCH) {
            if (nearSurface && palette.surface() != null) return palette.surface().get().defaultBlockState();
            return (patch > PATCH_CORE ? palette.core() : palette.patch()).get().defaultBlockState();
        }
        int band = Mth.floor((y + 4.0 * detail.noise(x * 0.04, 17.3, z * 0.04)) / STRATA_THICKNESS);
        long hash = mix(band * 0x9E3779B97F4A7C15L ^ seed);
        if (Math.floorMod(hash, 100) >= STRATA_PERCENT) return null;
        List<Supplier<? extends Block>> strata = palette.strata();
        return strata.get((int) Math.floorMod(hash >>> 8, strata.size())).get().defaultBlockState();
    }

    private static Palette palette(BlockState host) {
        for (Palette palette : PALETTES) {
            if (host.is(palette.host().get())) return palette;
        }
        return null;
    }

    private static long mix(long h) {
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        return h ^ h >>> 33;
    }

    public static List<Block> blocks() {
        return PALETTES.stream()
                .flatMap(p -> Stream.concat(Stream.of(p.worn(), p.patch(), p.core()), p.strata().stream()))
                .map(supplier -> (Block) supplier.get()).distinct().toList();
    }
}
