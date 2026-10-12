package net.itsthesky.upfall.content.quantum.upgrade;

import io.netty.buffer.ByteBuf;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;

public record QuantumUpgrades(List<QuantumUpgrade> upgrades) {
    public static final QuantumUpgrades EMPTY = new QuantumUpgrades(List.of());
    public static final Codec<QuantumUpgrades> CODEC = QuantumUpgrade.CODEC.listOf().xmap(QuantumUpgrades::new, QuantumUpgrades::upgrades);
    public static final StreamCodec<ByteBuf, QuantumUpgrades> STREAM_CODEC = ByteBufCodecs.idMapper(i -> QuantumUpgrade.values()[i], Enum::ordinal)
            .apply(ByteBufCodecs.list()).map(QuantumUpgrades::new, QuantumUpgrades::upgrades);
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};

    public QuantumUpgrades {
        upgrades = List.copyOf(upgrades);
    }

    public boolean has(QuantumUpgrade upgrade) {
        return upgrades.contains(upgrade);
    }

    public QuantumUpgrades with(QuantumUpgrade upgrade) {
        List<QuantumUpgrade> list = new ArrayList<>(upgrades);
        list.add(upgrade);
        return new QuantumUpgrades(list);
    }

    public static QuantumUpgrades get(ItemStack stack) {
        return stack.getOrDefault(QuantumComponents.UPGRADES.get(), EMPTY);
    }

    public static boolean has(ItemStack stack, QuantumUpgrade upgrade) {
        return !stack.isEmpty() && get(stack).has(upgrade);
    }

    public enum Refusal { NONE, NOT_APPLICABLE, DUPLICATE, FULL }

    public static Refusal canApply(ItemStack stack, QuantumUpgrade upgrade) {
        if (stack.isEmpty() || !upgrade.canApplyTo(stack)) return Refusal.NOT_APPLICABLE;
        QuantumUpgrades current = get(stack);
        if (current.has(upgrade)) return Refusal.DUPLICATE;
        if (current.upgrades().size() >= UpfallConfig.getInt(UpfallConfig.MAX_UPGRADES)) return Refusal.FULL;
        return Refusal.NONE;
    }

    public static void apply(ItemStack stack, QuantumUpgrade upgrade) {
        stack.set(QuantumComponents.UPGRADES.get(), get(stack).with(upgrade));
    }

    public static boolean equipped(LivingEntity entity, QuantumUpgrade upgrade) {
        return equippedSlot(entity, upgrade) != null;
    }

    public static EquipmentSlot equippedSlot(LivingEntity entity, QuantumUpgrade upgrade) {
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (has(stack, upgrade) && upgrade.canApplyTo(stack)) {
                boolean armor = slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
                boolean tool = upgrade == QuantumUpgrade.SUPERPOSITION || upgrade == QuantumUpgrade.ENTANGLEMENT;
                if (armor != tool) return slot;
            }
        }
        return null;
    }
}
