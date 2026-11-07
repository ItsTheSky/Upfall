package net.itsthesky.upfall.content.farming;

import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

final class FarmingEvents {
    private FarmingEvents() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(FarmingEvents::onToolModification);
    }

    private static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() != ItemAbilities.HOE_TILL || !isTillable(event.getState())) return;
        UseOnContext context = event.getContext();
        if (context != null && context.getClickedFace() == Direction.DOWN) return;
        if (!event.getLevel().getBlockState(event.getPos().above()).isAir()) return;
        event.setFinalState(Blocks.FARMLAND.defaultBlockState());
    }

    private static boolean isTillable(BlockState state) {
        return state.is(ModBlocks.SKYSOIL.get())
                || state.is(ModBlocks.SKYGRASS_BLOCK.get()) && !state.getValue(SkygrassBlock.INVERTED);
    }
}
