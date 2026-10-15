package net.itsthesky.upfall.content.reliquary.item;

import net.itsthesky.upfall.content.world.WorldItems;
import net.itsthesky.upfall.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FoldAnchorItem extends Item {
    public static final int DURATION = 600;
    public static final int COOLDOWN = 100;

    public FoldAnchorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        FoldAnchorData anchor = stack.get(ModDataComponents.FOLD_ANCHOR);
        boolean active = anchor != null && server.getGameTime() - anchor.time() < DURATION
                && anchor.pos().dimension().equals(server.dimension());
        if (player.isShiftKeyDown() || !active) {
            stack.set(ModDataComponents.FOLD_ANCHOR, new FoldAnchorData(GlobalPos.of(server.dimension(), player.blockPosition()), server.getGameTime()));
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.6, 0.3, 0.05);
            player.displayClientMessage(Component.translatable("message.upfall.fold_anchor.set", DURATION / 20), true);
            player.getCooldowns().addCooldown(this, 20);
            return InteractionResultHolder.success(stack);
        }
        Vec3 from = player.position();
        Vec3 target = Vec3.atBottomCenterOf(anchor.pos().pos());
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 30, 0.4, 0.8, 0.4, 0.1);
        serverPlayer.teleportTo(server, target.x(), target.y(), target.z(), player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        server.playSound(null, target.x(), target.y(), target.z(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, target.x(), target.y() + 1.0, target.z(), 30, 0.4, 0.8, 0.4, 0.1);
        stack.remove(ModDataComponents.FOLD_ANCHOR);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        player.getCooldowns().addCooldown(this, COOLDOWN);
        player.displayClientMessage(Component.translatable("message.upfall.fold_anchor.recall"), true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        FoldAnchorData anchor = stack.get(ModDataComponents.FOLD_ANCHOR);
        if (anchor != null && !level.isClientSide && level.getGameTime() - anchor.time() >= DURATION) {
            stack.remove(ModDataComponents.FOLD_ANCHOR);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.FOLD_ANCHOR) || super.isFoil(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(WorldItems.CLOUD_PEARL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.fold_anchor.desc").withStyle(ChatFormatting.GRAY));
        FoldAnchorData anchor = stack.get(ModDataComponents.FOLD_ANCHOR);
        Level level = context.level();
        if (anchor == null || level == null) {
            tooltip.add(Component.translatable("item.upfall.fold_anchor.unset").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        long left = Math.max(0, DURATION - (level.getGameTime() - anchor.time())) / 20;
        tooltip.add(Component.translatable("item.upfall.fold_anchor.anchored", left).withStyle(ChatFormatting.AQUA));
    }
}
