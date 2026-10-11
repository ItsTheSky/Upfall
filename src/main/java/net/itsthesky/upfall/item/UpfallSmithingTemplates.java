package net.itsthesky.upfall.item;

import net.itsthesky.upfall.Upfall;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;
import java.util.function.Supplier;

public final class UpfallSmithingTemplates {
    private UpfallSmithingTemplates() {}

    private static final List<ResourceLocation> BASE_ICONS = List.of(
            vanilla("item/empty_armor_slot_helmet"), vanilla("item/empty_slot_sword"),
            vanilla("item/empty_armor_slot_chestplate"), vanilla("item/empty_slot_pickaxe"),
            vanilla("item/empty_armor_slot_leggings"), vanilla("item/empty_slot_axe"),
            vanilla("item/empty_armor_slot_boots"), vanilla("item/empty_slot_hoe"),
            vanilla("item/empty_slot_shovel"));
    private static final List<ResourceLocation> ADDITION_ICONS = List.of(vanilla("item/empty_slot_ingot"));

    public static Supplier<SmithingTemplateItem> create(String upgrade) {
        return create(upgrade, BASE_ICONS, ADDITION_ICONS);
    }

    public static Supplier<SmithingTemplateItem> create(String upgrade, List<ResourceLocation> baseIcons, List<ResourceLocation> additionIcons) {
        return () -> new SmithingTemplateItem(
                text("applies_to", upgrade, ChatFormatting.BLUE),
                text("ingredients", upgrade, ChatFormatting.BLUE),
                Component.translatable(Util.makeDescriptionId("upgrade", Upfall.id(upgrade))).withStyle(ChatFormatting.GRAY),
                Component.translatable(key("base_slot_description", upgrade)),
                Component.translatable(key("additions_slot_description", upgrade)),
                baseIcons, additionIcons);
    }

    public static String key(String part, String upgrade) {
        return Util.makeDescriptionId("item", Upfall.id("smithing_template." + upgrade + "." + part));
    }

    private static Component text(String part, String upgrade, ChatFormatting format) {
        return Component.translatable(key(part, upgrade)).withStyle(format);
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
