package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.block.FoldLatticeBlock;
import net.itsthesky.upfall.content.reliquary.block.FoldSealBlock;
import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ReliquaryBlocks {
    private ReliquaryBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(1.8F, 6.0F).sound(SoundType.CALCITE);
    }

    public static final DeferredBlock<Block> LATTICE_STONE = BLOCKS.registerSimpleBlock("lattice_stone", stone());
    public static final DeferredBlock<Block> CARVED_LATTICE_STONE = BLOCKS.registerSimpleBlock("carved_lattice_stone", stone());
    public static final DeferredBlock<RotatedPillarBlock> LATTICE_BEAM = BLOCKS.registerBlock("lattice_beam", RotatedPillarBlock::new, stone());
    public static final DeferredBlock<FoldSocketBlock> FOLD_SOCKET = BLOCKS.registerBlock("fold_socket", FoldSocketBlock::new,
            stone().strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(FoldSocketBlock.FILLED) ? 10 : 3));
    public static final DeferredBlock<FoldSealBlock> FOLD_SEAL = BLOCKS.registerBlock("fold_seal", FoldSealBlock::new,
            stone().strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.BLOCK).lightLevel(state -> 6));
    public static final DeferredBlock<FoldLatticeBlock> FOLD_LATTICE = BLOCKS.registerBlock("fold_lattice", FoldLatticeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.0F, 6.0F).sound(SoundType.AMETHYST)
                    .noLootTable().pushReaction(PushReaction.BLOCK).lightLevel(state -> 5));

    static {
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(LATTICE_STONE));
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(CARVED_LATTICE_STONE));
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(LATTICE_BEAM));
    }
}
