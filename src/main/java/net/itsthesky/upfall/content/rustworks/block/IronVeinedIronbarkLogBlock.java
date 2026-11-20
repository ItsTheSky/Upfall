package net.itsthesky.upfall.content.rustworks.block;

import net.itsthesky.upfall.block.UpfallLogBlock;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public class IronVeinedIronbarkLogBlock extends UpfallLogBlock {
    public IronVeinedIronbarkLogBlock(Properties properties) {
        super(ModBlocks.IRONBARK.strippedLog(), properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP) {
            if (!simulate && context.getLevel() instanceof ServerLevel level) {
                BlockPos pos = context.getClickedPos();
                Direction face = context.getClickedFace();
                RandomSource random = level.getRandom();

                int nuggets = 2 + random.nextInt(3);
                Block.popResourceFromFace(level, pos, face, new ItemStack(Items.IRON_NUGGET, nuggets));
                if (random.nextFloat() < 0.2F) {
                    Block.popResourceFromFace(level, pos, face, new ItemStack(RustworksItems.RUSTY_PLATE.get()));
                }

                level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 0.85F, 1.15F + random.nextFloat() * 0.2F);
                double px = pos.getX() + 0.5 + face.getStepX() * 0.55;
                double py = pos.getY() + 0.5 + face.getStepY() * 0.55;
                double pz = pos.getZ() + 0.5 + face.getStepZ() * 0.55;
                level.sendParticles(ModParticles.RUST_FLAKE.get(), px, py, pz, 8, 0.2, 0.2, 0.2, 0.04);
            }
            return super.getToolModifiedState(state, context, itemAbility, simulate);
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
