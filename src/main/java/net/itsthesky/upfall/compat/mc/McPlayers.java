package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class McPlayers {
    private McPlayers() {}

    public static void overlay(Player player, Component message) {
        //? if >=26.1 {
        /*player.sendOverlayMessage(message);
        *///?} else {
        player.displayClientMessage(message, true);
        //?}
    }

    public static void message(Player player, Component message) {
        //? if >=26.1 {
        /*player.sendSystemMessage(message);
        *///?} else {
        player.displayClientMessage(message, false);
        //?}
    }

    public static void cooldown(Player player, ItemStack stack, int ticks) {
        //? if >=26.1 {
        /*player.getCooldowns().addCooldown(stack, ticks);
        *///?} else {
        player.getCooldowns().addCooldown(stack.getItem(), ticks);
        //?}
    }

    public static void cooldown(Player player, Item item, int ticks) {
        cooldown(player, item.getDefaultInstance(), ticks);
    }

    public static boolean isOnCooldown(Player player, ItemStack stack) {
        //? if >=26.1 {
        /*return player.getCooldowns().isOnCooldown(stack);
        *///?} else {
        return player.getCooldowns().isOnCooldown(stack.getItem());
        //?}
    }

    public static boolean isOnCooldown(Player player, Item item) {
        return isOnCooldown(player, item.getDefaultInstance());
    }

    public static void removeCooldown(Player player, Item item) {
        //? if >=26.1 {
        /*player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(item.getDefaultInstance()));
        *///?} else {
        player.getCooldowns().removeCooldown(item);
        //?}
    }

    public static EquipmentSlot slot(InteractionHand hand) {
        //? if >=26.1 {
        /*return hand.asEquipmentSlot();
        *///?} else {
        return LivingEntity.getSlotForHand(hand);
        //?}
    }

    public static NonNullList<ItemStack> mainItems(Inventory inventory) {
        //? if >=26.1 {
        /*return inventory.getNonEquipmentItems();
        *///?} else {
        return inventory.items;
        //?}
    }
}
