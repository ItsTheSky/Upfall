package net.itsthesky.upfall.compat.mc;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class McIds {
    private McIds() {}

    public static ResourceLocation id(ResourceKey<?> key) {
        //? if >=26.1 {
        /*return key.identifier();
        *///?} else {
        return key.location();
        //?}
    }
}
