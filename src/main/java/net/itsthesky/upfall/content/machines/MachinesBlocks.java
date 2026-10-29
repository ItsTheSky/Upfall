package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.machines.block.GearliftBlock;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.content.machines.block.PneumaticPipeBlock;
import net.itsthesky.upfall.content.machines.block.PneumaticPipeEntranceBlock;
import net.itsthesky.upfall.content.machines.block.PneumaticPipeExitBlock;
import net.itsthesky.upfall.content.machines.block.RewinderBlock;
import net.itsthesky.upfall.content.machines.block.VoidTetherBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class MachinesBlocks {
    private MachinesBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<PneumaticPipeBlock> PNEUMATIC_PIPE = block("pneumatic_pipe", PneumaticPipeBlock::new, pipe().noOcclusion());
    public static final DeferredBlock<PneumaticPipeEntranceBlock> PNEUMATIC_PIPE_ENTRANCE = block("pneumatic_pipe_entrance",
            PneumaticPipeEntranceBlock::new, pipe());
    public static final DeferredBlock<PneumaticPipeExitBlock> PNEUMATIC_PIPE_EXIT = block("pneumatic_pipe_exit",
            PneumaticPipeExitBlock::new, pipe());
    public static final DeferredBlock<VoidTetherBlock> VOID_TETHER = block("void_tether", VoidTetherBlock::new, machine());
    public static final DeferredBlock<RewinderBlock> REWINDER = block("rewinder", RewinderBlock::new, machine());
    public static final DeferredBlock<HasteningGearBlock> HASTENING_GEAR = block("hastening_gear", HasteningGearBlock::new, machine());
    public static final DeferredBlock<GearliftBlock> GEARLIFT = block("gearlift", GearliftBlock::new, machine());

    public static List<DeferredBlock<? extends Block>> all() {
        return List.of(PNEUMATIC_PIPE, PNEUMATIC_PIPE_ENTRANCE, PNEUMATIC_PIPE_EXIT, VOID_TETHER, REWINDER, HASTENING_GEAR, GEARLIFT);
    }

    private static BlockBehaviour.Properties pipe() {
        return machine().strength(2.0F, 6.0F).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER);
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = McRegistration.block(BLOCKS, name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }
}
