package net.itsthesky.upfall.content.world.item;

import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class NimbusFlaskItem extends Item {
    private static final int RADIUS = 2;
    private static final int COOLDOWN = 60;

    public NimbusFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos center = BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.5, player.getZ());
        if (level instanceof ServerLevel server) {
            if (condense(server, center) == 0) {
                return InteractionResultHolder.fail(stack);
            }
            server.sendParticles(ModParticles.MIST_MOTE.get(), center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    24, RADIUS, 0.2, RADIUS, 0.02);
            level.playSound(null, center, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
            level.playSound(null, center, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        player.resetFallDistance();
        player.getCooldowns().addCooldown(this, COOLDOWN);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return InteractionResultHolder.sidedSuccess(new ItemStack(Items.GLASS_BOTTLE), level.isClientSide);
            }
            if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.nimbus_flask.desc").withStyle(ChatFormatting.GRAY));
    }
}
