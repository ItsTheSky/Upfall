package net.itsthesky.upfall.content.aerie.item;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
//? if <26.1 {
import net.minecraft.world.InteractionResultHolder;
//?}

public class GravitySceptreItem extends Item {
    public static final int DURATION = 600;
    public static final int COOLDOWN = 40;

    public GravitySceptreItem(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(activate(level, player, hand), level);
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return UseResults.hold(activate(level, player, hand), level);
    }
    //?}

    private UseResults.Held activate(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            boolean release = player.hasEffect(ModEffects.WEIGHTLESS);
            if (release) {
                player.removeEffect(ModEffects.WEIGHTLESS);
            } else {
                player.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, DURATION, 0, true, true, true));
                stack.hurtAndBreak(1, player, McPlayers.slot(hand));
            }
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                    1.0F, release ? 0.6F : 1.4F);
        }
        McPlayers.cooldown(player, this, COOLDOWN);
        return UseResults.Held.sidedSuccess(stack);
    }

    //? if <26.1 {
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ModItems.LEVITITE_INGOT.get());
    }
    //?}

    //? if >=26.1 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        hoverText(tooltip);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(tooltip::add);
    }
    //?}

    private static void hoverText(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("item.upfall.gravity_sceptre.desc").withStyle(ChatFormatting.GRAY));
    }
}
