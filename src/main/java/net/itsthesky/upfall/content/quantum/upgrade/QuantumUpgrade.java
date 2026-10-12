package net.itsthesky.upfall.content.quantum.upgrade;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.function.Predicate;

public enum QuantumUpgrade implements StringRepresentable {
    VEILSIGHT(ChatFormatting.LIGHT_PURPLE, stack -> stack.is(ModItems.LEVITITE_HELMET.get())),
    UNCERTAINTY(ChatFormatting.AQUA, stack -> stack.is(ModItems.LEVITITE_CHESTPLATE.get())),
    ZIPLINE(ChatFormatting.GOLD, stack -> stack.is(ModItems.LEVITITE_CHESTPLATE.get())),
    ANCHORED(ChatFormatting.DARK_AQUA, stack -> stack.is(ModItems.LEVITITE_LEGGINGS.get())),
    SOLIDIFIER(ChatFormatting.WHITE, stack -> stack.is(ModItems.LEVITITE_LEGGINGS.get())),
    MOSSWALKER(ChatFormatting.GREEN, stack -> stack.is(ModItems.LEVITITE_BOOTS.get())),
    SUPERPOSITION(ChatFormatting.BLUE, stack -> stack.is(ModItems.LEVITITE_PICKAXE.get())),
    ENTANGLEMENT(ChatFormatting.DARK_PURPLE, stack -> stack.is(ModItems.LEVITITE_SWORD.get())),
    REWIND(ChatFormatting.YELLOW, stack -> stack.is(ModTags.Items.LEVITITE_ARMOR));

    public static final Codec<QuantumUpgrade> CODEC = StringRepresentable.fromEnum(QuantumUpgrade::values);

    private final ChatFormatting color;
    private final Predicate<ItemStack> applicable;

    QuantumUpgrade(ChatFormatting color, Predicate<ItemStack> applicable) {
        this.color = color;
        this.applicable = applicable;
    }

    public boolean canApplyTo(ItemStack stack) {
        return applicable.test(stack);
    }

    public ChatFormatting color() {
        return color;
    }

    public String translationKey() {
        return "upgrade.upfall.quantum." + getSerializedName();
    }

    public String descriptionKey() {
        return translationKey() + ".desc";
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
