package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlock;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlockEntity;
import net.itsthesky.upfall.content.quantum.deco.FloorDecorationBlock;
import net.itsthesky.upfall.content.quantum.gas.VeilGasBlock;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlock;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlock;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.vault.CrumblingFloorBlock;
import net.itsthesky.upfall.content.quantum.vault.FrostGlassBlock;
import net.itsthesky.upfall.content.quantum.vault.ShiftingChestBlock;
import net.itsthesky.upfall.content.quantum.vault.ShiftingChestBlockEntity;
import net.itsthesky.upfall.content.quantum.vault.VaultCoreBlock;
import net.itsthesky.upfall.content.quantum.vault.VaultCoreBlockEntity;
import net.itsthesky.upfall.content.quantum.vault.VaultLampBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class QuantumBlocks {
    private QuantumBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<VeilGasBlock> VEIL_GAS = BLOCKS.registerBlock("veil_gas", VeilGasBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.NONE).replaceable().noCollission().noLootTable().noOcclusion()
                    .strength(-1.0F, 0.0F).isValidSpawn(Blocks::never).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)
                    .isRedstoneConductor((s, l, p) -> false).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOL));

    public static final DeferredBlock<FloorDecorationBlock> ABANDONED_PICKAXE = block("abandoned_pickaxe", FloorDecorationBlock::new, floorDeco(SoundType.METAL));
    public static final DeferredBlock<FloorDecorationBlock> CRACKED_VEIL_MASK = block("cracked_veil_mask", FloorDecorationBlock::new, floorDeco(SoundType.METAL));
    public static final DeferredBlock<FloorDecorationBlock> SCATTERED_BONES = block("scattered_bones", FloorDecorationBlock::new, floorDeco(SoundType.BONE_BLOCK));
    public static final DeferredBlock<LanternBlock> SPENT_LANTERN = block("spent_lantern", LanternBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN)
                    .noOcclusion().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<FlickerShardClusterBlock> FLICKER_SHARD_CLUSTER = block("flicker_shard_cluster", FlickerShardClusterBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER)
                    .strength(1.5F).lightLevel(state -> 6).pushReaction(PushReaction.DESTROY));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlickerShardClusterBlockEntity>> FLICKER_SHARD_CLUSTER_ENTITY =
            BLOCK_ENTITIES.register("flicker_shard_cluster", () -> BlockEntityType.Builder.of(FlickerShardClusterBlockEntity::new,
                    FLICKER_SHARD_CLUSTER.get()).build(null));

    public static final DeferredBlock<Block> VAULT_BRICKS = block("vault_bricks", Block::new, vault(MapColor.COLOR_PURPLE));
    public static final DeferredBlock<Block> VAULT_TILES = block("vault_tiles", Block::new, vault(MapColor.COLOR_PURPLE));
    public static final DeferredBlock<TransparentBlock> VAULT_GLASS = block("vault_glass", TransparentBlock::new, vaultGlass());
    public static final DeferredBlock<FrostGlassBlock> FROST_GLASS = block("frost_glass", FrostGlassBlock::new, vaultGlass());
    public static final DeferredBlock<VaultLampBlock> VAULT_LAMP = block("vault_lamp", VaultLampBlock::new,
            vault(MapColor.COLOR_PURPLE).lightLevel(state -> state.getValue(VaultLampBlock.LIT) ? 15 : 0).sound(SoundType.GLASS));
    public static final DeferredBlock<CrumblingFloorBlock> CRUMBLING_FLOOR = block("crumbling_floor", CrumblingFloorBlock::new,
            vault(MapColor.COLOR_PURPLE).noOcclusion().isViewBlocking((s, l, p) -> !s.getValue(CrumblingFloorBlock.CRUMBLED))
                    .isSuffocating((s, l, p) -> !s.getValue(CrumblingFloorBlock.CRUMBLED)));
    public static final DeferredBlock<ShiftingChestBlock> SHIFTING_CHEST = block("shifting_chest", ShiftingChestBlock::new,
            vault(MapColor.COLOR_PURPLE).sound(SoundType.NETHERITE_BLOCK).noOcclusion());
    public static final DeferredBlock<VaultCoreBlock> VAULT_CORE = BLOCKS.registerBlock("vault_core", VaultCoreBlock::new, vault(MapColor.COLOR_PURPLE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShiftingChestBlockEntity>> SHIFTING_CHEST_ENTITY =
            BLOCK_ENTITIES.register("shifting_chest", () -> BlockEntityType.Builder.of(ShiftingChestBlockEntity::new, SHIFTING_CHEST.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VaultCoreBlockEntity>> VAULT_CORE_ENTITY =
            BLOCK_ENTITIES.register("vault_core", () -> BlockEntityType.Builder.of(VaultCoreBlockEntity::new, VAULT_CORE.get()).build(null));

    public static final DeferredBlock<PedestalBlock> PEDESTAL = block("pedestal", PedestalBlock::new, pedestal());
    public static final DeferredBlock<QuantumPedestalBlock> QUANTUM_PEDESTAL = block("quantum_pedestal", QuantumPedestalBlock::new,
            pedestal().mapColor(MapColor.COLOR_PURPLE).lightLevel(state -> 5));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY =
            BLOCK_ENTITIES.register("pedestal", () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, PEDESTAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuantumPedestalBlockEntity>> QUANTUM_PEDESTAL_ENTITY =
            BLOCK_ENTITIES.register("quantum_pedestal", () -> BlockEntityType.Builder.of(QuantumPedestalBlockEntity::new, QUANTUM_PEDESTAL.get()).build(null));

    private static BlockBehaviour.Properties pedestal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(2.0F, 6.0F).requiresCorrectToolForDrops()
                .sound(SoundType.CALCITE).noOcclusion();
    }

    private static BlockBehaviour.Properties vault(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(Blocks::never)
                .sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK);
    }

    private static BlockBehaviour.Properties vaultGlass() {
        return vault(MapColor.NONE).noOcclusion().sound(SoundType.GLASS).isRedstoneConductor((s, l, p) -> false)
                .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false);
    }

    private static BlockBehaviour.Properties floorDeco(SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollission().noOcclusion().instabreak().sound(sound)
                .pushReaction(PushReaction.DESTROY);
    }

    static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }
}
