package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.world.CloudHamletStructure;
import net.itsthesky.upfall.content.hamlet.world.HamletPieces;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
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

import java.util.Map;

public final class HamletStructures {
    private HamletStructures() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<CloudHamletStructure>> CLOUD_HAMLET_TYPE = STRUCTURE_TYPES.register("cloud_hamlet",
            () -> () -> CloudHamletStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PLATFORM = PIECE_TYPES.register("hamlet_platform",
            () -> (StructurePieceType.ContextlessType) HamletPieces.PlatformPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> BRIDGE = PIECE_TYPES.register("hamlet_bridge",
            () -> (StructurePieceType.ContextlessType) HamletPieces.BridgePiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ZIPLINE = PIECE_TYPES.register("hamlet_zipline",
            () -> (StructurePieceType.ContextlessType) HamletPieces.ZiplinePiece::new);

    public static final ResourceKey<Structure> CLOUD_HAMLET = ResourceKey.create(Registries.STRUCTURE, Upfall.id("cloud_hamlet"));
    public static final ResourceKey<StructureSet> CLOUD_HAMLETS = ResourceKey.create(Registries.STRUCTURE_SET, Upfall.id("cloud_hamlets"));

    static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(CLOUD_HAMLET, new CloudHamletStructure(new Structure.StructureSettings(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.MISTSEA), biomes.getOrThrow(ModBiomes.DRIFTING_ARCHIPELAGO)),
                Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
    }

    static void bootstrapSets(BootstrapContext<StructureSet> context) {
        context.register(CLOUD_HAMLETS, new StructureSet(context.lookup(Registries.STRUCTURE).getOrThrow(CLOUD_HAMLET),
                new RandomSpreadStructurePlacement(28, 12, RandomSpreadType.LINEAR, 20261011)));
    }
}
