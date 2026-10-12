package net.itsthesky.upfall.content.aerie.item;

import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GravitySceptreItem extends Item {
    public static final int DURATION = 600;
    public static final int COOLDOWN = 40;

    public GravitySceptreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            boolean release = player.hasEffect(ModEffects.WEIGHTLESS);
            if (release) {
                player.removeEffect(ModEffects.WEIGHTLESS);
            } else {
                player.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, DURATION, 0, true, true, true));
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                    1.0F, release ? 0.6F : 1.4F);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ModItems.LEVITITE_INGOT.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.gravity_sceptre.desc").withStyle(ChatFormatting.GRAY));
    }
}
