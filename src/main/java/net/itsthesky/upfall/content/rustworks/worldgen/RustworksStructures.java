package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RustworksStructures {
    private RustworksStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<PipeBridgeStructure>> TYPE =
            TYPES.register("pipe_bridge", () -> () -> PipeBridgeStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PIECE =
            PIECES.register("pipe_bridge_segment", () -> (StructurePieceType.ContextlessType) PipeBridgePiece::new);

    public static final ResourceKey<Structure> PIPE_BRIDGE = ResourceKey.create(Registries.STRUCTURE, Upfall.id("pipe_bridge"));
    public static final ResourceKey<StructureSet> PIPE_BRIDGE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("pipe_bridge"));
    public static final TagKey<Biome> HAS_PIPE_BRIDGE = TagKey.create(Registries.BIOME, Upfall.id("has_structure/pipe_bridge"));

    public static void structures(BootstrapContext<Structure> context) {
        context.register(PIPE_BRIDGE, new PipeBridgeStructure(
                new Structure.StructureSettings.Builder(context.lookup(Registries.BIOME).getOrThrow(HAS_PIPE_BRIDGE))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
    }

    public static void structureSets(BootstrapContext<StructureSet> context) {
        context.register(PIPE_BRIDGE_SET, new StructureSet(context.lookup(Registries.STRUCTURE).getOrThrow(PIPE_BRIDGE),
                new RandomSpreadStructurePlacement(4, 1, RandomSpreadType.LINEAR, 1307202611)));
    }
}
