package net.itsthesky.upfall.content.chronospire.data;

import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.block.ChronoGnomonBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.EnumMap;
import java.util.Map;

final class ChronospireModels {
    private ChronospireModels() {}

    static void blockStates(ModBlockStateProvider states) {
        phaseStone(states);

        dispatch(states, ChronoBlocks.PHASE_LANTERN.get(), PropertyDispatch.initial(PhaseTrackingBlock.PHASE)
                .generate(phase -> BlockModelGenerators.plainVariant(
                        states.glowingCube("phase_lantern_" + phase.getSerializedName(), "solid"))));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.PHASE_LANTERN.get(), states.modLoc("block/phase_lantern_dusk"));

        Map<ChronoPhase, Identifier> dials = new EnumMap<>(ChronoPhase.class);
        for (ChronoPhase phase : ChronoPhase.values()) {
            dials.put(phase, states.cubeBottomTop("hour_dial_" + phase.getSerializedName(), "hour_dial_side",
                    "chronostone_bricks", "hour_dial_top_" + phase.getSerializedName()));
        }
        dispatch(states, ChronoBlocks.HOUR_DIAL.get(), PropertyDispatch.initial(PhaseTrackingBlock.PHASE)
                .generate(phase -> BlockModelGenerators.plainVariant(dials.get(phase))));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.HOUR_DIAL.get(), states.modLoc("block/hour_dial_dusk"));

        glyph(states);

        dispatch(states, ChronoBlocks.GLYPH_PEDESTAL.get(), PropertyDispatch.initial(GlyphPedestalBlock.MODE)
                .generate(mode -> BlockModelGenerators.plainVariant(states.cubeBottomTop("glyph_pedestal_" + mode.getSerializedName(),
                        "glyph_pedestal_side", "warded_chronostone", "glyph_pedestal_top_" + mode.getSerializedName()))));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.GLYPH_PEDESTAL.get(), states.modLoc("block/glyph_pedestal_idle"));

        dispatch(states, ChronoBlocks.PHASE_WHEEL.get(), PropertyDispatch.initial(PhaseTrackingBlock.PHASE).generate(phase -> {
            String name = "phase_wheel_" + phase.getSerializedName();
            return BlockModelGenerators.plainVariant(states.cubeBottomTop(name, name, "warded_chronostone", "phase_wheel_top"));
        }));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.PHASE_WHEEL.get(), states.modLoc("block/phase_wheel_dawn"));

        dispatch(states, ChronoBlocks.CHRONO_GNOMON.get(), PropertyDispatch.initial(PhaseTrackingBlock.PHASE, ChronoGnomonBlock.SOLVED)
                .generate((phase, solved) -> {
                    String name = "chrono_gnomon_" + phase.getSerializedName() + (solved ? "_solved" : "");
                    return BlockModelGenerators.plainVariant(box(states, name, name, false, false, 15));
                }));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.CHRONO_GNOMON.get(), states.modLoc("block/chrono_gnomon_dusk"));

        Identifier sealClosed = box(states, "chrono_seal", "chrono_seal", true, false, 12);
        Identifier sealOpen = particleOnly(states, "chrono_seal_open", "chrono_seal");
        dispatch(states, ChronoBlocks.CHRONO_SEAL.get(), PropertyDispatch.initial(ChronoSealBlock.OPEN)
                .generate(open -> BlockModelGenerators.plainVariant(open ? sealOpen : sealClosed)));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.CHRONO_SEAL.get(), sealClosed);

        states.simpleBlock(ChronoBlocks.CHRONO_CURRENT.get(), particleOnly(states, "chrono_current", "chrono_seal"));
        states.simpleBlock(ChronoBlocks.DRIFT_PLATE.get(), ModelTemplates.SLAB_BOTTOM.create(states.modLoc("block/drift_plate"),
                new TextureMapping().put(TextureSlot.SIDE, states.blockMaterial("drift_plate_side"))
                        .put(TextureSlot.BOTTOM, states.blockMaterial("drift_plate_bottom"))
                        .put(TextureSlot.TOP, states.blockMaterial("drift_plate_top")), states.modelOutput()));
    }

    private static void phaseStone(ModBlockStateProvider states) {
        dispatch(states, ChronoBlocks.PHASE_STONE.get(), PropertyDispatch.initial(PhaseTrackingBlock.PHASE, PhaseStoneBlock.SLAB, PhaseStoneBlock.MODE)
                .generate((phase, slab, mode) -> {
                    String texture = "phase_stone_" + phase.getSerializedName() + (mode == PhaseStoneBlock.Mode.SOLID ? "" : "_" + mode.getSerializedName());
                    String name = texture + (slab ? "_slab" : "");
                    return BlockModelGenerators.plainVariant(mode == PhaseStoneBlock.Mode.GHOST
                            ? box(states, name, texture, true, slab, 6)
                            : box(states, name, texture, false, slab, 15));
                }));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.PHASE_STONE.get(), states.modLoc("block/phase_stone_dawn"));
    }

    private static void glyph(ModBlockStateProvider states) {
        Map<ChronoPhase, Identifier[]> models = new EnumMap<>(ChronoPhase.class);
        for (ChronoPhase phase : ChronoPhase.values()) {
            String glyph = "chronoglyph_" + phase.getSerializedName();
            models.put(phase, new Identifier[]{
                    ModelTemplates.CUBE_ALL.create(states.modLoc("block/" + glyph), TextureMapping.cube(states.blockMaterial(glyph)), states.modelOutput()),
                    states.glowingCube(glyph + "_on", "solid"),
                    states.glowingCube(glyph + "_error", "solid")});
        }
        dispatch(states, ChronoBlocks.CHRONOGLYPH.get(), PropertyDispatch.initial(ChronoglyphBlock.GLYPH, ChronoglyphBlock.LIGHT)
                .generate((phase, light) -> BlockModelGenerators.plainVariant(models.get(phase)[switch (light) {
                    case OFF -> 0;
                    case ON, SOLVED -> 1;
                    case ERROR -> 2;
                }])));
        states.blockModels().registerSimpleItemModel(ChronoBlocks.CHRONOGLYPH.get(), states.modLoc("block/chronoglyph_dawn"));
    }

    private static void dispatch(ModBlockStateProvider states, Block block, PropertyDispatch<MultiVariant> dispatch) {
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private static Identifier particleOnly(ModBlockStateProvider states, String name, String texture) {
        return ModelTemplates.PARTICLE_ONLY.create(states.modLoc("block/" + name),
                new TextureMapping().put(TextureSlot.PARTICLE, states.blockMaterial(texture)), states.modelOutput());
    }

    private static Identifier box(ModBlockStateProvider states, String name, String texture, boolean translucent, boolean slab, int light) {
        Material material = states.blockMaterial(texture);
        if (translucent) {
            material = material.withForceTranslucent(true);
        }
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, slab ? 8 : 16, 16).shade(false).ambientOcclusion(false).lightEmission(light)
                        .allFaces((dir, face) -> {
                            face.texture(TextureSlot.ALL);
                            if (!slab || dir != Direction.UP) face.cullface(dir);
                            if (slab && dir.getAxis().isHorizontal()) face.uvs(0, 8, 16, 16);
                        }))
                .build();
        return template.create(states.modLoc("block/" + name), new TextureMapping().put(TextureSlot.ALL, material).put(TextureSlot.PARTICLE, material), states.modelOutput());
    }
}
