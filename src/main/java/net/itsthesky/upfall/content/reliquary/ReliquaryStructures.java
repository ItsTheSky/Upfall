package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPiece;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPlacement;
import net.itsthesky.upfall.content.reliquary.worldgen.TiltedReliquaryStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReliquaryStructures {
    private ReliquaryStructures() {}

    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<TiltedReliquaryStructure>> TILTED_RELIQUARY_TYPE =
            TYPES.register("tilted_reliquary", () -> () -> TiltedReliquaryStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> TILTED_RELIQUARY_PIECE =
            PIECES.register("tilted_reliquary", () -> (StructurePieceType.ContextlessType) ReliquaryPiece::new);
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ReliquaryPlacement>> ZONE_PLACEMENT =
            PLACEMENTS.register("reliquary_zone", () -> () -> ReliquaryPlacement.CODEC);

    public static final ResourceKey<Structure> TILTED_RELIQUARY = ResourceKey.create(Registries.STRUCTURE, Upfall.id("tilted_reliquary"));
    public static final ResourceKey<StructureSet> TILTED_RELIQUARY_SET = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("tilted_reliquary"));
    public static final TagKey<Biome> HAS_TILTED_RELIQUARY = TagKey.create(Registries.BIOME, Upfall.id("has_structure/tilted_reliquary"));

    public static final ResourceKey<LootTable> SATELLITE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/tilted_reliquary_satellite"));
    public static final ResourceKey<LootTable> ARCHIVE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/tilted_reliquary_archive"));
    public static final ResourceKey<LootTable> CORE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/tilted_reliquary_core"));
}
