package net.itsthesky.upfall.content.chronospire;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

public enum ChronoPhase implements StringRepresentable {
    DAWN("dawn", 0x9FD4C4),
    NOON("noon", 0xE2B85A),
    DUSK("dusk", 0xE8B4C0),
    MIDNIGHT("midnight", 0xA9C8E8);

    private static final ChronoPhase[] VALUES = values();

    private final String name;
    private final int color;

    ChronoPhase(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public static ChronoPhase byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    public ChronoPhase next() {
        return byIndex(ordinal() + 1);
    }

    public int color() {
        return color;
    }

    public MutableComponent displayName() {
        return Component.translatable("chronospire.upfall.phase." + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
