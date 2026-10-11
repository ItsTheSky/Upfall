package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GrottoStructures {
    private GrottoStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<GrottoStructure>> TYPE =
            TYPES.register("crystal_grotto", () -> () -> GrottoStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PIECE =
            PIECES.register("crystal_grotto", () -> (StructurePieceType) (context, tag) -> new GrottoPiece(tag));

    public static final ResourceKey<Structure> CRYSTAL_GROTTO = ResourceKey.create(Registries.STRUCTURE, Upfall.id("crystal_grotto"));
    public static final ResourceKey<StructureSet> CRYSTAL_GROTTO_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("crystal_grotto"));
}
