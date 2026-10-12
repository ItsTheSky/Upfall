package net.itsthesky.upfall.content.quantum.gas;

import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class VeilProtection {
    private VeilProtection() {}

    public static boolean isProtected(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        return head.is(ModTags.Items.VEIL_PROTECTION) || QuantumUpgrades.has(head, QuantumUpgrade.VEILSIGHT);
    }
}
