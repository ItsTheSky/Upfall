package net.itsthesky.upfall.content.chronospire;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.itsthesky.upfall.content.chronospire.block.entity.GlyphPedestalBlockEntity;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.itsthesky.upfall.content.chronospire.item.RewindingHourglassItem;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospirePiece;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
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
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ChronoRegistries {
    private ChronoRegistries() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Upfall.MODID);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Upfall.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Upfall.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Upfall.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlyphPedestalBlockEntity>> GLYPH_PEDESTAL = BLOCK_ENTITIES.register(
            "glyph_pedestal", () -> McRegistration.blockEntityType(GlyphPedestalBlockEntity::new, ChronoBlocks.GLYPH_PEDESTAL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChronoGnomonBlockEntity>> CHRONO_GNOMON = BLOCK_ENTITIES.register(
            "chrono_gnomon", () -> McRegistration.blockEntityType(ChronoGnomonBlockEntity::new, ChronoBlocks.CHRONO_GNOMON.get()));

    public static final DeferredHolder<EntityType<?>, EntityType<DriftPlatform>> DRIFT_PLATFORM = ENTITY_TYPES.register("drift_platform",
            () -> McRegistration.entityType(EntityType.Builder.<DriftPlatform>of(DriftPlatform::new, MobCategory.MISC).sized(3.0F, 0.5F).fireImmune()
                    .clientTrackingRange(10).updateInterval(20), "drift_platform"));

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<CompoundTag>> COMPOUND_TAG = ENTITY_DATA_SERIALIZERS.register(
            "compound_tag", () -> new EntityDataSerializer<CompoundTag>() {
                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
                    return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
                }

                @Override
                public CompoundTag copy(CompoundTag value) {
                    return value.copy();
                }
            });

    public static final DeferredItem<RewindingHourglassItem> REWINDING_HOURGLASS = McRegistration.item(ITEMS, "rewinding_hourglass",
            RewindingHourglassItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    public static final DeferredHolder<StructureType<?>, StructureType<ChronospireStructure>> CHRONOSPIRE = STRUCTURE_TYPES.register("chronospire",
            () -> () -> ChronospireStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> CHRONOSPIRE_PIECE = STRUCTURE_PIECES.register("chronospire_tower",
            () -> (StructurePieceType.ContextlessType) ChronospirePiece::new);
}
