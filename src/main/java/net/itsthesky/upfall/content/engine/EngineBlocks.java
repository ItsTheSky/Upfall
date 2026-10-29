package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlockEntity;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.content.engine.block.StagnantMistBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class EngineBlocks {
    private EngineBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<StagnantMistBlock> STAGNANT_MIST = McRegistration.block(BLOCKS, "stagnant_mist", StagnantMistBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                    .sound(SoundType.WOOL).isValidSpawn(Blocks::never).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false).pushReaction(PushReaction.BLOCK));
    public static final DeferredBlock<DrainValveBlock> DRAIN_VALVE = block("drain_valve", DrainValveBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_NYLIUM).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER).noOcclusion());
    public static final DeferredBlock<DrainRegulatorBlock> DRAIN_REGULATOR = McRegistration.block(BLOCKS, "drain_regulator", DrainRegulatorBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops().strength(5.0F, 3600000.0F).sound(SoundType.COPPER).noLootTable()
                    .pushReaction(PushReaction.BLOCK));
    public static final DeferredBlock<Block> ENGRAVED_RUSTSLATE = block("engraved_rustslate", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(3.5F, 6.0F).sound(SoundType.POLISHED_DEEPSLATE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrainRegulatorBlockEntity>> DRAIN_REGULATOR_ENTITY = BLOCK_ENTITIES.register(
            "drain_regulator", () -> McRegistration.<DrainRegulatorBlockEntity>blockEntityType(DrainRegulatorBlockEntity::new, DRAIN_REGULATOR.get()));

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory,
                                                           BlockBehaviour.Properties props) {
        DeferredBlock<B> block = McRegistration.block(BLOCKS, name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }
}
