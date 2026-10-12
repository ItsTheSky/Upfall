package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.aerie.block.WeatheredBricksBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class AerieBlocks {
    private AerieBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<WeatheredBricksBlock> WEATHERED_PEARLSTONE_BRICKS = BLOCKS.registerBlock("weathered_pearlstone_bricks",
            WeatheredBricksBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.CALCITE));

    static {
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(WEATHERED_PEARLSTONE_BRICKS));
    }
}
