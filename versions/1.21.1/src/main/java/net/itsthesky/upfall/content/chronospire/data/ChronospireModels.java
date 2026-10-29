package net.itsthesky.upfall.content.chronospire.data;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.block.ChronoGnomonBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class ChronospireModels {
    private ChronospireModels() {}

    static void blockStates(ModBlockStateProvider states) {
        states.getVariantBuilder(ChronoBlocks.PHASE_STONE.get()).forAllStates(state -> {
            String phase = state.getValue(PhaseTrackingBlock.PHASE).getSerializedName();
            boolean slab = state.getValue(PhaseStoneBlock.SLAB);
            PhaseStoneBlock.Mode mode = state.getValue(PhaseStoneBlock.MODE);
            String texture = "phase_stone_" + phase + (mode == PhaseStoneBlock.Mode.SOLID ? "" : "_" + mode.getSerializedName());
            String name = texture + (slab ? "_slab" : "");
            ModelFile model = mode == PhaseStoneBlock.Mode.GHOST
                    ? box(states, name, texture, "translucent", slab, 6)
                    : box(states, name, texture, "solid", slab, 15);
            return ConfiguredModel.builder().modelFile(model).build();
        });
        states.itemModels().withExistingParent("phase_stone", Upfall.id("block/phase_stone_dawn"));

        states.getVariantBuilder(ChronoBlocks.PHASE_LANTERN.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(states.glowingCube("phase_lantern_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName(), "solid")).build());
        states.itemModels().withExistingParent("phase_lantern", Upfall.id("block/phase_lantern_dusk"));

        ModelFile[] dials = new ModelFile[4];
        for (ChronoPhase phase : ChronoPhase.values()) {
            dials[phase.ordinal()] = states.models().cubeBottomTop("hour_dial_" + phase.getSerializedName(), states.blockTexture("hour_dial_side"),
                    states.blockTexture("chronostone_bricks"), states.blockTexture("hour_dial_top_" + phase.getSerializedName()));
        }
        states.getVariantBuilder(ChronoBlocks.HOUR_DIAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(dials[state.getValue(PhaseTrackingBlock.PHASE).ordinal()]).build());
        states.itemModels().withExistingParent("hour_dial", Upfall.id("block/hour_dial_dusk"));

        states.getVariantBuilder(ChronoBlocks.CHRONOGLYPH.get()).forAllStates(state -> {
            String glyph = "chronoglyph_" + state.getValue(ChronoglyphBlock.GLYPH).getSerializedName();
            ModelFile model = switch (state.getValue(ChronoglyphBlock.LIGHT)) {
                case OFF -> states.models().cubeAll(glyph, states.blockTexture(glyph));
                case ON, SOLVED -> states.glowingCube(glyph + "_on", "solid");
                case ERROR -> states.glowingCube(glyph + "_error", "solid");
            };
            return ConfiguredModel.builder().modelFile(model).build();
        });
        states.itemModels().withExistingParent("chronoglyph", Upfall.id("block/chronoglyph_dawn"));

        states.getVariantBuilder(ChronoBlocks.GLYPH_PEDESTAL.get()).forAllStates(state -> {
            String mode = state.getValue(GlyphPedestalBlock.MODE).getSerializedName();
            return ConfiguredModel.builder().modelFile(states.models().cubeBottomTop("glyph_pedestal_" + mode,
                    states.blockTexture("glyph_pedestal_side"), states.blockTexture("warded_chronostone"),
                    states.blockTexture("glyph_pedestal_top_" + mode))).build();
        });
        states.itemModels().withExistingParent("glyph_pedestal", Upfall.id("block/glyph_pedestal_idle"));

        states.getVariantBuilder(ChronoBlocks.PHASE_WHEEL.get()).forAllStates(state -> {
            String name = "phase_wheel_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName();
            return ConfiguredModel.builder().modelFile(states.models().cubeBottomTop(name, states.blockTexture(name),
                    states.blockTexture("warded_chronostone"), states.blockTexture("phase_wheel_top"))).build();
        });
        states.itemModels().withExistingParent("phase_wheel", Upfall.id("block/phase_wheel_dawn"));

        states.getVariantBuilder(ChronoBlocks.CHRONO_GNOMON.get()).forAllStates(state -> {
            String name = "chrono_gnomon_" + state.getValue(PhaseTrackingBlock.PHASE).getSerializedName()
                    + (state.getValue(ChronoGnomonBlock.SOLVED) ? "_solved" : "");
            return ConfiguredModel.builder().modelFile(box(states, name, name, "solid", false, 15)).build();
        });
        states.itemModels().withExistingParent("chrono_gnomon", Upfall.id("block/chrono_gnomon_dusk"));

        ModelFile sealClosed = box(states, "chrono_seal", "chrono_seal", "translucent", false, 12);
        ModelFile empty = states.models().getBuilder("chrono_seal_open").texture("particle", states.blockTexture("chrono_seal"));
        states.getVariantBuilder(ChronoBlocks.CHRONO_SEAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ChronoSealBlock.OPEN) ? empty : sealClosed).build());
        states.itemModels().withExistingParent("chrono_seal", Upfall.id("block/chrono_seal"));

        states.simpleBlock(ChronoBlocks.CHRONO_CURRENT.get(), states.models().getBuilder("chrono_current")
                .texture("particle", states.blockTexture("chrono_seal")));
        states.simpleBlock(ChronoBlocks.DRIFT_PLATE.get(), states.models().slab("drift_plate", states.blockTexture("drift_plate_side"),
                states.blockTexture("drift_plate_bottom"), states.blockTexture("drift_plate_top")));
    }

    private static ModelFile box(ModBlockStateProvider states, String name, String texture, String renderType, boolean slab, int light) {
        ResourceLocation tex = states.blockTexture(texture);
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("all", tex).texture("particle", tex).renderType(renderType)
                .element().from(0, 0, 0).to(16, slab ? 8 : 16, 16).shade(false).ao(false).emissivity(light, light)
                .allFaces((dir, face) -> {
                    face.texture("#all");
                    if (!slab || dir != Direction.UP) face.cullface(dir);
                    if (slab && dir.getAxis().isHorizontal()) face.uvs(0, 8, 16, 16);
                }).end();
    }
}
