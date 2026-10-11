package net.itsthesky.upfall.content.chronospire.item;

import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RewindingHourglassItem extends Item {
    public static final int MIN_AGE = 60;
    public static final int COOLDOWN = 400;

    public RewindingHourglassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel server)) {
            return InteractionResultHolder.success(stack);
        }
        HourglassTracker.Footing footing = HourglassTracker.rewind(player.getUUID(), level.dimension(), level.getGameTime(), MIN_AGE);
        if (footing == null) {
            player.displayClientMessage(Component.translatable("message.upfall.hourglass.empty"), true);
            return InteractionResultHolder.fail(stack);
        }
        Vec3 from = player.position();
        server.sendParticles(ModParticles.GILDED_DUST.get(), from.x, from.y + 1.0, from.z, 30, 0.3, 0.6, 0.3, 0.02);
        serverPlayer.teleportTo(footing.pos().x, footing.pos().y, footing.pos().z);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        server.sendParticles(ModParticles.GILDED_DUST.get(), footing.pos().x, footing.pos().y + 1.0, footing.pos().z, 30, 0.3, 0.6, 0.3, 0.02);
        server.playSound(null, footing.pos().x, footing.pos().y, footing.pos().z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.4F);
        server.playSound(null, footing.pos().x, footing.pos().y, footing.pos().z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        HourglassTracker.clear(player.getUUID());
        player.getCooldowns().addCooldown(this, COOLDOWN);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.rewinding_hourglass.desc").withStyle(ChatFormatting.GRAY));
    }
}
