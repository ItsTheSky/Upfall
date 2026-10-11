package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.world.item.ItemStack;

public final class KairiteFuel {
    private KairiteFuel() {}

    public static final int DUST_UNITS = 9;
    public static final int GEAR_UNITS = DUST_UNITS * 9;

    public static int units(ItemStack stack) {
        if (stack.is(RustworksItems.KAIRITE_DUST.get())) return DUST_UNITS;
        if (stack.is(RustworksItems.KAIRITE_GEAR.get())) return GEAR_UNITS;
        return 0;
    }

    public static int fit(ItemStack stack, int stored, int capacity, int perUnit) {
        int value = units(stack) * perUnit;
        if (value <= 0) return 0;
        return Math.min(stack.getCount(), Math.max(0, (capacity - stored) / value));
    }
}
