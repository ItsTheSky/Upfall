package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.vault.ConfiguredSpreadPlacement;
import net.itsthesky.upfall.content.quantum.vault.ShiftingVaultPiece;
import net.itsthesky.upfall.content.quantum.vault.ShiftingVaultStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class QuantumStructures {
    private QuantumStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<ShiftingVaultStructure>> SHIFTING_VAULT_TYPE =
            TYPES.register("shifting_vault", () -> () -> ShiftingVaultStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHIFTING_VAULT_PIECE =
            PIECES.register("shifting_vault", () -> (StructurePieceType.ContextlessType) ShiftingVaultPiece::new);
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ConfiguredSpreadPlacement>> CONFIGURED_SPREAD =
            PLACEMENTS.register("configured_spread", () -> () -> ConfiguredSpreadPlacement.CODEC);

    public static final ResourceKey<Structure> SHIFTING_VAULT = ResourceKey.create(Registries.STRUCTURE, Upfall.id("shifting_vault"));
    public static final ResourceKey<StructureSet> SHIFTING_VAULT_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("shifting_vault"));
    public static final TagKey<Biome> HAS_SHIFTING_VAULT = TagKey.create(Registries.BIOME, Upfall.id("has_structure/shifting_vault"));
}
