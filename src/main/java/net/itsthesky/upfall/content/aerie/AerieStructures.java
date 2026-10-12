package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.aerie.worldgen.AeriePiece;
import net.itsthesky.upfall.content.aerie.worldgen.PearlAerieStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AerieStructures {
    private AerieStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<PearlAerieStructure>> PEARL_AERIE_TYPE =
            TYPES.register("pearl_aerie", () -> () -> PearlAerieStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PEARL_AERIE_PIECE =
            PIECES.register("pearl_aerie", () -> (StructurePieceType.ContextlessType) AeriePiece::new);

    public static final ResourceKey<Structure> PEARL_AERIE = ResourceKey.create(Registries.STRUCTURE, Upfall.id("pearl_aerie"));
    public static final ResourceKey<StructureSet> PEARL_AERIE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("pearl_aerie"));
    public static final TagKey<Biome> HAS_PEARL_AERIE = TagKey.create(Registries.BIOME, Upfall.id("has_structure/pearl_aerie"));
}
