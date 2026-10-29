package net.itsthesky.upfall.content.machines.item;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class PhaseWatchItem extends Item {
    public PhaseWatchItem(Properties properties) {
        super(properties);
    }

    public static boolean hasSignal(Level level) {
        return level.dimension() == ModDimensions.UPFALL;
    }

    public static Component describe(Level level) {
        if (!hasSignal(level)) {
            return Component.translatable("message.upfall.phase_watch.no_signal").withStyle(ChatFormatting.GRAY);
        }
        long time = ChronoClock.time(level);
        ChronoPhase phase = ChronoClock.phaseAt(time);
        return Component.translatable("message.upfall.phase_watch",
                phase.displayName().withColor(phase.color()),
                Component.literal(ChronoClock.formatTicks(ChronoClock.ticksUntilNextPhase(time))).withStyle(ChatFormatting.WHITE));
    }

    //? if >=26.1 {
    /*@Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (!(entity instanceof ServerPlayer player) || level.getGameTime() % 20 != 0) return;
        if (slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
            McPlayers.overlay(player, describe(level));
        }
    }
    *///?} else {
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || level.getGameTime() % 20 != 0) return;
        if (selected || slot == Inventory.SLOT_OFFHAND) {
            McPlayers.overlay(player, describe(level));
        }
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
        tooltip.accept(Component.translatable("item.upfall.phase_watch.desc").withStyle(ChatFormatting.GRAY));
    }
}
