package net.itsthesky.upfall.content.gravity;

import net.itsthesky.upfall.gameplay.ArmorSetBonuses;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public final class SetBonusTooltips {
    private SetBonusTooltips() {}

    public static final String HEADER = "tooltip.upfall.set_bonus";
    public static final String HEADER_WORN = "tooltip.upfall.set_bonus.worn";
    public static final String NIMBUSITE_FALL_DAMAGE = "tooltip.upfall.set_bonus.nimbusite.fall_damage";
    public static final String LEVITITE_NO_FALL_DAMAGE = "tooltip.upfall.set_bonus.levitite.no_fall_damage";
    public static final String LEVITITE_DOUBLE_JUMP = "tooltip.upfall.set_bonus.levitite.double_jump";
    public static final String LEVITITE_GLIDE = "tooltip.upfall.set_bonus.levitite.glide";

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final TextColor GOLD = TextColor.fromRgb(0xE2B85A);
    private static final TextColor LAVENDER = TextColor.fromRgb(0x9A98BC);

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SetBonusTooltips::onTooltip);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        TagKey<Item> set;
        List<Component> bonuses;
        if (stack.is(ModTags.Items.LEVITITE_ARMOR)) {
            set = ModTags.Items.LEVITITE_ARMOR;
            bonuses = List.of(Component.translatable(LEVITITE_NO_FALL_DAMAGE), Component.translatable(LEVITITE_DOUBLE_JUMP),
                    Component.translatable(LEVITITE_GLIDE));
        } else if (stack.is(ModTags.Items.NIMBUSITE_ARMOR)) {
            set = ModTags.Items.NIMBUSITE_ARMOR;
            int reduction = Math.round((1.0F - ArmorSetBonuses.NIMBUSITE_FALL_DAMAGE_MULTIPLIER) * 100.0F);
            bonuses = List.of(Component.translatable(NIMBUSITE_FALL_DAMAGE, reduction + "%"));
        } else {
            return;
        }

        Player player = event.getEntity();
        int worn = player == null ? -1 : countWorn(player, set);
        boolean active = worn == ARMOR_SLOTS.length;
        MutableComponent header = worn < 0
                ? Component.translatable(HEADER)
                : Component.translatable(HEADER_WORN, Component.literal(Integer.toString(worn)).withStyle(style -> style.withColor(active ? GOLD : LAVENDER)),
                ARMOR_SLOTS.length);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(header.withStyle(ChatFormatting.GRAY));
        for (Component bonus : bonuses) {
            lines.add(Component.literal(" ").append(bonus).withStyle(style -> style.withColor(active ? GOLD : LAVENDER)));
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.addAll(insertionIndex(tooltip), lines);
    }

    public static int countWorn(Player player, TagKey<Item> set) {
        int worn = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (player.getItemBySlot(slot).is(set)) worn++;
        }
        return worn;
    }

    private static int insertionIndex(List<Component> tooltip) {
        for (int i = 1; i < tooltip.size(); i++) {
            if (tooltip.get(i).getContents() instanceof TranslatableContents contents && contents.getKey().startsWith("item.modifiers.")) {
                return tooltip.get(i - 1).getString().isEmpty() ? i - 1 : i;
            }
        }
        return tooltip.size();
    }
}
