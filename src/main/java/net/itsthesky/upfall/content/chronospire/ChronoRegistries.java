package net.itsthesky.upfall.content.chronospire;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.itsthesky.upfall.content.chronospire.block.entity.GlyphPedestalBlockEntity;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.itsthesky.upfall.content.chronospire.item.RewindingHourglassItem;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospirePiece;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChronoRegistries {
    private ChronoRegistries() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlyphPedestalBlockEntity>> GLYPH_PEDESTAL = BLOCK_ENTITIES.register(
            "glyph_pedestal", () -> BlockEntityType.Builder.of(GlyphPedestalBlockEntity::new, ChronoBlocks.GLYPH_PEDESTAL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChronoGnomonBlockEntity>> CHRONO_GNOMON = BLOCK_ENTITIES.register(
            "chrono_gnomon", () -> BlockEntityType.Builder.of(ChronoGnomonBlockEntity::new, ChronoBlocks.CHRONO_GNOMON.get()).build(null));

    public static final DeferredHolder<EntityType<?>, EntityType<DriftPlatform>> DRIFT_PLATFORM = ENTITY_TYPES.register("drift_platform",
            () -> EntityType.Builder.<DriftPlatform>of(DriftPlatform::new, MobCategory.MISC).sized(3.0F, 0.5F).fireImmune()
                    .clientTrackingRange(10).updateInterval(20).build("upfall:drift_platform"));

    public static final DeferredItem<RewindingHourglassItem> REWINDING_HOURGLASS = ITEMS.registerItem("rewinding_hourglass",
            RewindingHourglassItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    public static final DeferredHolder<StructureType<?>, StructureType<ChronospireStructure>> CHRONOSPIRE = STRUCTURE_TYPES.register("chronospire",
            () -> () -> ChronospireStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> CHRONOSPIRE_PIECE = STRUCTURE_PIECES.register("chronospire_tower",
            () -> (StructurePieceType.ContextlessType) ChronospirePiece::new);
}
