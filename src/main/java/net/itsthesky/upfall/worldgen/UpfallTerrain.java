package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;

import static net.minecraft.world.level.levelgen.DensityFunctions.add;
import static net.minecraft.world.level.levelgen.DensityFunctions.constant;
import static net.minecraft.world.level.levelgen.DensityFunctions.flatCache;
import static net.minecraft.world.level.levelgen.DensityFunctions.max;
import static net.minecraft.world.level.levelgen.DensityFunctions.min;
import static net.minecraft.world.level.levelgen.DensityFunctions.mul;

public final class UpfallTerrain {
    private UpfallTerrain() {}

    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, Upfall.id("the_upfall"));

    public static final int MIN_Y = -64;
    public static final int HEIGHT = 384;
    public static final int CLOUD_BAND_MIN = 30;
    public static final int CLOUD_BAND_MAX = 64;

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<NormalNoise.NoiseParameters> noises = context.lookup(Registries.NOISE);
        Fields fields = new Fields(noises);
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(MIN_Y, HEIGHT, 1, 2),
                ModBlocks.PEARLSTONE.get().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                router(fields),
                surfaceRules(),
                List.of(),
                MIN_Y,
                false,
                false,
                false,
                false));
    }

    public static final class Fields {
        final HolderGetter<NormalNoise.NoiseParameters> noises;
        final DensityFunction archipelago;
        final DensityFunction inversion;
        final DensityFunction gilded;
        final DensityFunction rustworks;
        final DensityFunction invertBlend;
        final DensityFunction gildedBlend;
        final DensityFunction mistseaBlend;
        final DensityFunction rustChannel;
        final DensityFunction rustInside;
        final DensityFunction rustThin;
        final DensityFunction rustFlat;

        Fields(HolderGetter<NormalNoise.NoiseParameters> noises) {
            this.noises = noises;
            this.archipelago = flatCache(noise2d(noises, ModNoises.ARCHIPELAGO, 1.0));
            this.inversion = flatCache(noise2d(noises, ModNoises.INVERSION, 1.0));
            this.gilded = flatCache(noise2d(noises, ModNoises.GILDED, 1.0));
            DensityFunction zones = flatCache(new RustworksZones(holder(noises, ModNoises.RUSTWORKS), holder(noises, ModNoises.ARCHIPELAGO),
                    BiomeThresholds.MISTSEA));
            this.rustworks = flatCache(mul(zones, constant(1.0 / RustworksZones.LIMIT)));
            DensityFunction outsideRustworks = add(constant(1.0), mul(ramp(zones, -RUST_CHANNEL_OUTER, -RUST_CHANNEL_INNER), constant(-1.0)));
            this.invertBlend = flatCache(mul(ramp(inversion, BiomeThresholds.INVERSION - 0.08, BiomeThresholds.INVERSION + 0.1), outsideRustworks));
            this.gildedBlend = flatCache(mul(ramp(gilded, BiomeThresholds.GILDED - 0.06, BiomeThresholds.GILDED + 0.1), outsideRustworks));
            this.mistseaBlend = flatCache(ramp(mul(archipelago, constant(-1.0)), -BiomeThresholds.MISTSEA - 0.05, -BiomeThresholds.MISTSEA + 0.25));
            DensityFunction rustEdge = min(zones, mul(add(archipelago, constant(-BiomeThresholds.MISTSEA)), constant(RUST_MIST_SCALE)));
            this.rustChannel = flatCache(ramp(rustEdge.abs(), RUST_CHANNEL_OUTER, RUST_CHANNEL_INNER));
            this.rustInside = flatCache(ramp(zones, RUST_CHANNEL_INNER, RUST_CHANNEL_OUTER));
            this.rustThin = flatCache(add(constant(1.0), mul(rustInside, constant(RUST_THICKNESS - 1.0))));
            this.rustFlat = flatCache(add(constant(1.0), mul(rustInside, constant(RUST_ROLLING - 1.0))));
        }
    }

    public static final class BiomeThresholds {
        public static final float MISTSEA = -0.3F;
        public static final float INVERSION = 0.3F;
        public static final float GILDED = 0.45F;
        public static final float RUSTWORKS = 0.0F;
    }

    private static final double RUST_CHANNEL_INNER = 8.0;
    private static final double RUST_CHANNEL_OUTER = 44.0;
    private static final double RUST_MIST_SCALE = 400.0;
    private static final double RUST_CARVE = 1.2;
    private static final double RUST_MASK_SCALE = 3.0;
    private static final double RUST_MASK_BASE = 0.3;
    private static final double RUST_MASK_SHIFT = 0.14;
    private static final double RUST_THICKNESS = 0.5;
    private static final double RUST_ROLLING = 0.6;

    static NoiseRouter router(Fields f) {
        DensityFunction y = DensityFunctions.yClampedGradient(MIN_Y, MIN_Y + HEIGHT, MIN_Y, MIN_Y + HEIGHT);
        DensityFunction hills = flatCache(add(noise2d(f.noises, ModNoises.HILLS, 1.0), mul(noise2d(f.noises, ModNoises.SWELLS, 1.0), constant(1.6))));
        DensityFunction drips = flatCache(add(noise2d(f.noises, ModNoises.STALACTITES, 1.0), constant(-0.05)).clamp(0.0, 1.0).square());
        DensityFunction erosion = DensityFunctions.noise(f.noises.getOrThrow(ModNoises.EROSION), 1.0, 1.4);
        DensityFunction peaks = flatCache(add(constant(1.0), mul(noise2d(f.noises, ModNoises.RIDGE_PEAKS, 1.0).abs(), constant(-1.6))).clamp(0.0, 1.0));

        DensityFunction lowThreshold = spline(f.archipelago, new float[]{-1.0F, -0.4F, -0.25F, 0.0F, 0.4F, 1.0F},
                new float[]{0.9F, 0.5F, 0.2F, 0.1F, 0.04F, 0.02F});
        DensityFunction highThreshold = add(lowThreshold, constant(0.06));

        DensityFunction rifts = flatCache(add(constant(1.0), mul(noise2d(f.noises, ModNoises.RIFTS, 1.0).abs(), constant(-9.0))).clamp(0.0, 1.0));
        lowThreshold = add(lowThreshold, mul(rifts, constant(0.35)));
        highThreshold = add(highThreshold, mul(rifts, constant(0.35)));
        DensityFunction low = islandLayer(f, y, ModNoises.ISLAND_MASK_LOW, ModNoises.ISLAND_HEIGHT_LOW, 118.0, 16.0, lowThreshold, hills, peaks, drips);
        DensityFunction high = islandLayer(f, y, ModNoises.ISLAND_MASK_HIGH, ModNoises.ISLAND_HEIGHT_HIGH, 172.0, 18.0, highThreshold, hills, peaks, drips);
        DensityFunction islets = islets(f, y, hills);
        DensityFunction islands = add(max(max(low, high), islets), mul(erosion, constant(4.5)));

        DensityFunction terrain = max(max(islands, clouds(f, y)), needles(f, y));
        DensityFunction finalDensity = DensityFunctions.interpolated(terrain.clamp(-64.0, 64.0));

        return new NoiseRouter(
                DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(),
                f.gilded, f.rustworks, f.archipelago, DensityFunctions.zero(), DensityFunctions.zero(), f.inversion,
                constant(-1.0), finalDensity,
                DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    private static final float[] PROFILE_M = {-1.0F, 0.0F, 0.04F, 0.12F, 0.35F, 1.0F};
    private static final float[] FLAT_FACE = {-30.0F, 0.0F, 5.0F, 8.0F, 11.0F, 15.0F};
    private static final float[] DEEP_FACE = {-60.0F, 0.0F, 10.0F, 26.0F, 46.0F, 58.0F};

    private static DensityFunction islandLayer(Fields f, DensityFunction y, ResourceKey<NormalNoise.NoiseParameters> maskKey,
                                               ResourceKey<NormalNoise.NoiseParameters> heightKey, double baseY, double heightAmplitude,
                                               DensityFunction threshold, DensityFunction hills, DensityFunction peaks, DensityFunction drips) {
        DensityFunction mask = noise2d(f.noises, maskKey, 1.0);
        DensityFunction rustMask = add(add(noise2d(f.noises, maskKey, RUST_MASK_SCALE), mul(mask, constant(RUST_MASK_BASE - 1.0))),
                constant(-RUST_MASK_SHIFT));
        mask = add(mask, mul(f.rustInside, rustMask));
        DensityFunction m = flatCache(add(add(mask, mul(threshold, constant(-1.0))),
                add(add(mul(f.invertBlend, constant(0.08)), mul(f.gildedBlend, constant(0.1))), mul(f.rustChannel, constant(-RUST_CARVE)))));
        DensityFunction h = flatCache(add(constant(baseY), mul(noise2d(f.noises, heightKey, 1.0), constant(heightAmplitude))));
        DensityFunction inner = flatCache(m.clamp(0.0, 0.125));
        DensityFunction core = flatCache(m.clamp(0.0, 0.3));

        DensityFunction flatFace = spline(m, PROFILE_M, FLAT_FACE);
        DensityFunction deepFace = spline(m, PROFILE_M, DEEP_FACE);
        DensityFunction rolling = mul(mul(mul(hills, core), constant(85.0)), f.rustFlat);
        DensityFunction ridges = mul(mul(peaks, mul(f.gildedBlend, inner)), constant(360.0));
        DensityFunction hanging = mul(mul(drips, inner), constant(260.0));

        DensityFunction top = add(add(flatFace, rolling), ridges);
        DensityFunction bottom = mul(add(deepFace, hanging), f.rustThin);
        DensityFunction invTop = mul(deepFace, constant(0.8));
        DensityFunction invBottom = add(add(mul(flatFace, constant(1.2)), mul(rolling, constant(0.7))), mul(hanging, constant(0.25)));

        DensityFunction upper = flatCache(DensityFunctions.lerp(f.invertBlend, top, invTop));
        DensityFunction lower = flatCache(DensityFunctions.lerp(f.invertBlend, bottom, invBottom));
        DensityFunction offset = add(y, mul(h, constant(-1.0)));
        return min(add(upper, mul(offset, constant(-1.0))), add(lower, offset));
    }

    private static final float[] ISLET_FLAT = {-30.0F, 0.0F, 3.0F, 5.0F, 6.0F, 7.0F};
    private static final float[] ISLET_DEEP = {-40.0F, 0.0F, 6.0F, 14.0F, 22.0F, 28.0F};

    private static DensityFunction islets(Fields f, DensityFunction y, DensityFunction hills) {
        DensityFunction threshold = DensityFunctions.lerp(f.mistseaBlend, 0.52, constant(0.42));
        DensityFunction m = flatCache(add(add(mul(noise2d(f.noises, ModNoises.ISLET_MASK, 1.0), constant(1.0)), mul(threshold, constant(-1.0))),
                mul(f.rustChannel, constant(-RUST_CARVE))));
        DensityFunction altitude = flatCache(add(constant(140.0), mul(noise2d(f.noises, ModNoises.ISLET_HEIGHT, 1.0), constant(60.0))));
        DensityFunction lowAltitude = flatCache(add(constant(76.0), mul(noise2d(f.noises, ModNoises.ISLET_HEIGHT, 1.0), constant(6.0))));
        DensityFunction h = flatCache(DensityFunctions.lerp(f.mistseaBlend, altitude, lowAltitude));
        DensityFunction top = flatCache(add(spline(m, PROFILE_M, ISLET_FLAT), mul(mul(hills, m.clamp(0.0, 0.1)), constant(20.0))));
        DensityFunction bottom = flatCache(spline(m, PROFILE_M, ISLET_DEEP));
        DensityFunction offset = add(y, mul(h, constant(-1.0)));
        return min(add(top, mul(offset, constant(-1.0))), add(bottom, offset));
    }

    private static DensityFunction clouds(Fields f, DensityFunction y) {
        DensityFunction puffs = DensityFunctions.noise(f.noises.getOrThrow(ModNoises.CLOUD_SURFACE), 1.0, 0.8);
        DensityFunction billows = flatCache(noise2d(f.noises, ModNoises.CLOUD_SURFACE, 0.5));
        DensityFunction swells = flatCache(noise2d(f.noises, ModNoises.CLOUD_HOLES, 2.0));
        DensityFunction top = flatCache(add(add(add(constant(53.0), mul(billows.abs(), constant(11.0))), mul(swells, constant(4.0))),
                mul(f.mistseaBlend, constant(3.0))));
        DensityFunction bottom = flatCache(add(constant(45.0), mul(billows, constant(-3.0))));
        DensityFunction holes = flatCache(mul(add(noise2d(f.noises, ModNoises.CLOUD_HOLES, 1.0), constant(-0.18)), constant(70.0)).clamp(0.0, 40.0));
        DensityFunction band = min(add(top, mul(y, constant(-1.0))), add(y, mul(bottom, constant(-1.0))));
        return add(add(band, mul(puffs, constant(2.0))), mul(holes, constant(-1.0)));
    }

    private static final double NEEDLE_HEIGHT = 98.0;

    private static DensityFunction needles(Fields f, DensityFunction y) {
        DensityFunction field = flatCache(new NeedleField(holder(f.noises, ModNoises.NEEDLE_A), holder(f.noises, ModNoises.NEEDLE_B),
                holder(f.noises, ModNoises.SPIRES), NEEDLE_HEIGHT));
        DensityFunction rise = add(y, constant(-MIN_Y + 1.0));
        return add(field, mul(rise, constant(-1.0)));
    }

    private static DensityFunction.NoiseHolder holder(HolderGetter<NormalNoise.NoiseParameters> noises, ResourceKey<NormalNoise.NoiseParameters> key) {
        return new DensityFunction.NoiseHolder(noises.getOrThrow(key));
    }

    static SurfaceRules.RuleSource surfaceRules() {
        SurfaceRules.RuleSource pearl = state(ModBlocks.PEARLSTONE.get().defaultBlockState());
        SurfaceRules.RuleSource dusk = state(ModBlocks.DUSKSLATE.get().defaultBlockState());
        SurfaceRules.RuleSource soil = state(ModBlocks.SKYSOIL.get().defaultBlockState());
        SurfaceRules.RuleSource grass = state(ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState());
        SurfaceRules.RuleSource hangingGrass = state(ModBlocks.SKYGRASS_BLOCK.get().defaultBlockState().setValue(SkygrassBlock.INVERTED, true));
        SurfaceRules.RuleSource mist = state(ModBlocks.MIST.get().defaultBlockState());
        SurfaceRules.RuleSource cloud = state(ModBlocks.DENSE_CLOUD.get().defaultBlockState());
        SurfaceRules.RuleSource turf = state(RustworksBlocks.OXIDE_TURF.get().defaultBlockState());
        SurfaceRules.RuleSource rustsoil = state(RustworksBlocks.RUSTSOIL.get().defaultBlockState());
        SurfaceRules.RuleSource rustslate = state(RustworksBlocks.RUSTSLATE.get().defaultBlockState());

        SurfaceRules.ConditionSource inCloudBand = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(CLOUD_BAND_MIN), 0);
        SurfaceRules.ConditionSource aboveCloudBand = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(CLOUD_BAND_MAX), 0);
        SurfaceRules.ConditionSource mistTop = SurfaceRules.stoneDepthCheck(2, false, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource thickMistTop = SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource underFloor = SurfaceRules.stoneDepthCheck(2, true, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource underCeiling = SurfaceRules.stoneDepthCheck(1, true, CaveSurface.CEILING);
        SurfaceRules.ConditionSource gilded = SurfaceRules.isBiome(ModBiomes.GILDED_RIDGE);
        SurfaceRules.ConditionSource hanging = SurfaceRules.isBiome(ModBiomes.HANGING_ROOTS);
        SurfaceRules.ConditionSource mistsea = SurfaceRules.isBiome(ModBiomes.MISTSEA);
        SurfaceRules.ConditionSource rustworks = SurfaceRules.isBiome(ModBiomes.RUSTWORKS);
        SurfaceRules.ConditionSource grassPatch = SurfaceRules.noiseCondition(ModNoises.SURFACE_PATCHES, -0.15);

        SurfaceRules.RuleSource cloudRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(mistsea, SurfaceRules.ifTrue(thickMistTop, mist)),
                SurfaceRules.ifTrue(mistTop, mist),
                cloud);

        SurfaceRules.RuleSource islandRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(gilded, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue(grassPatch,
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.steep()), grass))),
                        dusk)),
                SurfaceRules.ifTrue(rustworks, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, turf),
                        SurfaceRules.ifTrue(underFloor, rustsoil),
                        rustslate)),
                SurfaceRules.ifTrue(hanging, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, hangingGrass),
                        SurfaceRules.ifTrue(underCeiling, soil))),
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grass),
                SurfaceRules.ifTrue(underFloor, soil),
                SurfaceRules.ifTrue(SurfaceRules.verticalGradient("upfall:underside", VerticalAnchor.absolute(86), VerticalAnchor.absolute(96)), dusk),
                pearl);

        SurfaceRules.RuleSource needleRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("upfall:needle_tips", VerticalAnchor.absolute(-12), VerticalAnchor.absolute(14))), pearl),
                dusk);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(aboveCloudBand, islandRules),
                SurfaceRules.ifTrue(inCloudBand, cloudRules),
                needleRules);
    }

    private static SurfaceRules.RuleSource state(BlockState state) {
        return SurfaceRules.state(state);
    }

    static DensityFunction noise2d(HolderGetter<NormalNoise.NoiseParameters> noises, ResourceKey<NormalNoise.NoiseParameters> key, double xzScale) {
        return DensityFunctions.noise(noises.getOrThrow(key), xzScale, 0.0);
    }

    static DensityFunction ramp(DensityFunction input, double from, double to) {
        return mul(add(input, constant(-from)), constant(1.0 / (to - from))).clamp(0.0, 1.0);
    }

    static DensityFunction spline(DensityFunction input, float[] locations, float[] values) {
        DensityFunctions.Spline.Coordinate coordinate = new DensityFunctions.Spline.Coordinate(Holder.direct(input));
        CubicSpline.Builder<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> builder = CubicSpline.builder(coordinate);
        for (int i = 0; i < locations.length; i++) {
            float slope;
            if (i == 0) {
                slope = (values[1] - values[0]) / (locations[1] - locations[0]);
            } else if (i == locations.length - 1) {
                slope = (values[i] - values[i - 1]) / (locations[i] - locations[i - 1]);
            } else {
                slope = (values[i + 1] - values[i - 1]) / (locations[i + 1] - locations[i - 1]);
            }
            builder.addPoint(locations[i], values[i], slope);
        }
        return DensityFunctions.spline(builder.build());
    }
}
