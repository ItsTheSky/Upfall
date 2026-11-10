package net.itsthesky.upfall.content.gliding;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public class GliderItem extends Item {
    private final double glideSpeed;
    private final TagKey<Item> repairTag;

    public GliderItem(Properties properties, double glideSpeed, TagKey<Item> repairTag) {
        super(properties);
        this.glideSpeed = glideSpeed;
        this.repairTag = repairTag;
    }

    public double glideSpeed() {
        return glideSpeed;
    }

    //? if <26.1 {
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(repairTag);
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

    private void hoverText(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
