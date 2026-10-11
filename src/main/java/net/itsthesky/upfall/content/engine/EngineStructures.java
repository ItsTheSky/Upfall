package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.engine.worldgen.StalledEnginePiece;
import net.itsthesky.upfall.content.engine.worldgen.StalledEngineStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EngineStructures {
    private EngineStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<StalledEngineStructure>> TYPE =
            TYPES.register("stalled_engine", () -> () -> StalledEngineStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PIECE =
            PIECES.register("stalled_engine", () -> (StructurePieceType.ContextlessType) StalledEnginePiece::new);

    public static final ResourceKey<Structure> STALLED_ENGINE = ResourceKey.create(Registries.STRUCTURE, Upfall.id("stalled_engine"));
    public static final ResourceKey<StructureSet> STALLED_ENGINE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("stalled_engine"));
}
