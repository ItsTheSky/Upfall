package net.itsthesky.upfall.item;

import net.itsthesky.upfall.Upfall;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;
import java.util.function.Function;
//? if >=26.1 {
/*import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
*///?}

public final class UpfallSmithingTemplates {
    private UpfallSmithingTemplates() {}

    //? if >=26.1 {
    /*private static final List<ResourceLocation> BASE_ICONS = List.of(
            vanilla("container/slot/helmet"), vanilla("container/slot/sword"),
            vanilla("container/slot/chestplate"), vanilla("container/slot/pickaxe"),
            vanilla("container/slot/leggings"), vanilla("container/slot/axe"),
            vanilla("container/slot/boots"), vanilla("container/slot/hoe"),
            vanilla("container/slot/shovel"));
    private static final List<ResourceLocation> ADDITION_ICONS = List.of(vanilla("container/slot/ingot"));
    *///?} else {
    private static final List<ResourceLocation> BASE_ICONS = List.of(
            vanilla("item/empty_armor_slot_helmet"), vanilla("item/empty_slot_sword"),
            vanilla("item/empty_armor_slot_chestplate"), vanilla("item/empty_slot_pickaxe"),
            vanilla("item/empty_armor_slot_leggings"), vanilla("item/empty_slot_axe"),
            vanilla("item/empty_armor_slot_boots"), vanilla("item/empty_slot_hoe"),
            vanilla("item/empty_slot_shovel"));
    private static final List<ResourceLocation> ADDITION_ICONS = List.of(vanilla("item/empty_slot_ingot"));
    //?}

    public static Function<Item.Properties, SmithingTemplateItem> factory(String upgrade) {
        return factory(upgrade, BASE_ICONS, ADDITION_ICONS);
    }

    public static Function<Item.Properties, SmithingTemplateItem> factory(String upgrade, List<ResourceLocation> baseIcons, List<ResourceLocation> additionIcons) {
        //? if >=26.1 {
        /*Component upgradeDescription = Component.translatable(Util.makeDescriptionId("upgrade", Upfall.id(upgrade))).withStyle(ChatFormatting.GRAY);
        return properties -> new SmithingTemplateItem(
                text("applies_to", upgrade, ChatFormatting.BLUE),
                text("ingredients", upgrade, ChatFormatting.BLUE),
                Component.translatable(key("base_slot_description", upgrade)),
                Component.translatable(key("additions_slot_description", upgrade)),
                baseIcons, additionIcons, properties) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                // 26.1 replaced the upgrade description line with a generic "Smithing Template" first line; put ours back in its place
                boolean[] first = {true};
                super.appendHoverText(stack, context, display, line -> {
                    tooltip.accept(first[0] ? upgradeDescription : line);
                    first[0] = false;
                }, flag);
            }
        };
        *///?} else {
        return properties -> new SmithingTemplateItem(
                text("applies_to", upgrade, ChatFormatting.BLUE),
                text("ingredients", upgrade, ChatFormatting.BLUE),
                Component.translatable(Util.makeDescriptionId("upgrade", Upfall.id(upgrade))).withStyle(ChatFormatting.GRAY),
                Component.translatable(key("base_slot_description", upgrade)),
                Component.translatable(key("additions_slot_description", upgrade)),
                baseIcons, additionIcons);
        //?}
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
