package net.itsthesky.upfall.content.world.item;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;
//? if <26.1 {
import net.minecraft.world.InteractionResultHolder;
//?}

public class NimbusFlaskItem extends Item {
    private static final int RADIUS = 2;
    private static final int COOLDOWN = 60;

    public NimbusFlaskItem(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(useFlask(level, player, hand), level);
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(useFlask(level, player, hand), level);
    }
    //?}

    private UseResults.Held useFlask(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos center = BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.5, player.getZ());
        if (level instanceof ServerLevel server) {
            if (condense(server, center) == 0) {
                return UseResults.Held.fail(stack);
            }
            server.sendParticles(ModParticles.MIST_MOTE.get(), center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    24, RADIUS, 0.2, RADIUS, 0.02);
            level.playSound(null, center, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
            level.playSound(null, center, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        player.resetFallDistance();
        McPlayers.cooldown(player, this, COOLDOWN);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return UseResults.Held.sidedSuccess(new ItemStack(Items.GLASS_BOTTLE));
            }
            if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return UseResults.Held.sidedSuccess(stack);
    }

    public static int condense(Level level, BlockPos center) {
        BlockState cloud = WorldBlocks.FADING_CLOUD.get().defaultBlockState();
        int placed = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS + 1) continue;
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                if (level.isInWorldBounds(pos) && (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty()) {
                    level.setBlock(pos, cloud, Block.UPDATE_ALL);
                    placed++;
                }
            }
        }
        return placed;
    }

    //? if >=26.1 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip, flag);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip::add, flag);
    }
    //?}

    private void hoverText(ItemStack stack, TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.upfall.nimbus_flask.desc").withStyle(ChatFormatting.GRAY));
    }
}
