package net.itsthesky.upfall.content.rustworks.block;

import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class StillflowCauldronBlock extends LayeredCauldronBlock {
    public static final CauldronInteraction.InteractionMap INTERACTIONS = CauldronInteraction.newInteractionMap("upfall_stillflow");

    public StillflowCauldronBlock(Properties properties) {
        super(Biome.Precipitation.NONE, INTERACTIONS, properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(RustworksItems.KAIRITE_DUST.get()) || !isFull(state)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel server) {
            convert(server, pos, player, stack);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static void convert(ServerLevel level, BlockPos pos, Player player, ItemStack dust) {
        if (!player.getAbilities().instabuild) {
            dust.shrink(1);
        }
        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, Blocks.CAULDRON.defaultBlockState()));
        ItemStack gear = new ItemStack(RustworksItems.KAIRITE_GEAR.get());
        if (!player.getInventory().add(gear)) {
            player.drop(gear, false);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 0.8F);
        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, 0.6F);
        level.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.25, 0.2, 0.25, 0.02);
    }
}
