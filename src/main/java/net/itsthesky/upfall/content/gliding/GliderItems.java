package net.itsthesky.upfall.content.gliding;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GliderItems {
    private GliderItems() {}

    public static final double SPRINT_SPEED = 0.28;
    public static final double DOWN_GLIDE_SPEED = SPRINT_SPEED * 1.5;
    public static final double DUSK_GLIDE_SPEED = SPRINT_SPEED * 2.0;

    public static final TagKey<Item> REPAIRS_DOWN_GLIDER = TagKey.create(Registries.ITEM, Upfall.id("repairs_down_glider"));
    public static final TagKey<Item> REPAIRS_DUSK_GLIDER = TagKey.create(Registries.ITEM, Upfall.id("repairs_dusk_glider"));

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredItem<GliderItem> DOWN_GLIDER = McRegistration.item(ITEMS, "down_glider",
            props -> new GliderItem(props, DOWN_GLIDE_SPEED, REPAIRS_DOWN_GLIDER),
            () -> properties(160, REPAIRS_DOWN_GLIDER));
    public static final DeferredItem<GliderItem> DUSK_GLIDER = McRegistration.item(ITEMS, "dusk_glider",
            props -> new GliderItem(props, DUSK_GLIDE_SPEED, REPAIRS_DUSK_GLIDER),
            () -> properties(320, REPAIRS_DUSK_GLIDER).rarity(Rarity.UNCOMMON));

    // Lazy: 26.1.2 repairable(TagKey) writes to the item registry, which is frozen until RegisterEvent.
    private static Item.Properties properties(int durability, TagKey<Item> repairTag) {
        Item.Properties properties = new Item.Properties().durability(durability);
        //? if >=26.1 {
        /*properties = properties.repairable(repairTag);
        *///?}
        return properties;
    }
}
