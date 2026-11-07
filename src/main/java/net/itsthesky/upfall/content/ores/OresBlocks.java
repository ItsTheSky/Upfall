package net.itsthesky.upfall.content.ores;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static net.itsthesky.upfall.registry.UpfallTab.NATURE;

public final class OresBlocks {
    private OresBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredBlock<DropExperienceBlock> PEARLSTONE_COAL_ORE = pearlstone("coal", UniformInt.of(0, 2));
    public static final DeferredBlock<DropExperienceBlock> PEARLSTONE_IRON_ORE = pearlstone("iron", ConstantInt.of(0));
    public static final DeferredBlock<DropExperienceBlock> PEARLSTONE_DIAMOND_ORE = pearlstone("diamond", UniformInt.of(3, 7));
    public static final DeferredBlock<DropExperienceBlock> RUSTSLATE_COAL_ORE = rustslate("coal", UniformInt.of(0, 2));
    public static final DeferredBlock<DropExperienceBlock> RUSTSLATE_IRON_ORE = rustslate("iron", ConstantInt.of(0));
    public static final DeferredBlock<DropExperienceBlock> RUSTSLATE_DIAMOND_ORE = rustslate("diamond", UniformInt.of(3, 7));

    public static final List<DeferredBlock<DropExperienceBlock>> COAL = List.of(PEARLSTONE_COAL_ORE, RUSTSLATE_COAL_ORE);
    public static final List<DeferredBlock<DropExperienceBlock>> IRON = List.of(PEARLSTONE_IRON_ORE, RUSTSLATE_IRON_ORE);
    public static final List<DeferredBlock<DropExperienceBlock>> DIAMOND = List.of(PEARLSTONE_DIAMOND_ORE, RUSTSLATE_DIAMOND_ORE);
    public static final List<DeferredBlock<DropExperienceBlock>> ALL = List.of(PEARLSTONE_COAL_ORE, PEARLSTONE_IRON_ORE,
            PEARLSTONE_DIAMOND_ORE, RUSTSLATE_COAL_ORE, RUSTSLATE_IRON_ORE, RUSTSLATE_DIAMOND_ORE);

    private static DeferredBlock<DropExperienceBlock> pearlstone(String ore, IntProvider xp) {
        return block("pearlstone_" + ore + "_ore", xp, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
                .strength(3.0F, 3.0F).sound(SoundType.CALCITE));
    }

    private static DeferredBlock<DropExperienceBlock> rustslate(String ore, IntProvider xp) {
        return block("rustslate_" + ore + "_ore", xp, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                .strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE));
    }

    private static DeferredBlock<DropExperienceBlock> block(String name, IntProvider xp, BlockBehaviour.Properties props) {
        DeferredBlock<DropExperienceBlock> block = McRegistration.block(BLOCKS, name, p -> new DropExperienceBlock(xp, p),
                props.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops());
        ModCreativeTabs.add(BLOCK_ITEMS.registerSimpleBlockItem(block), NATURE);
        return block;
    }

    static Block[] blocks(List<DeferredBlock<DropExperienceBlock>> blocks) {
        return blocks.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }
}
