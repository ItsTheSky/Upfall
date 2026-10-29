package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.machines.item.PhaseWatchItem;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class MachinesItems {
    private MachinesItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();
    // 26.1.2 blits smithing slot icons from the gui atlas; its copy lives under gui/sprites/container/slot so the id does not collide with item/empty_slot_gear in the items atlas
    //? if >=26.1 {
    /*public static final List<ResourceLocation> GEAR_SLOT_ICONS = List.of(Upfall.id("container/slot/gear"));
    *///?} else {
    public static final List<ResourceLocation> GEAR_SLOT_ICONS = List.of(Upfall.id("item/empty_slot_gear"));
    //?}

    public static final DeferredItem<PhaseWatchItem> PHASE_WATCH = add(McRegistration.item(ITEMS, "phase_watch", PhaseWatchItem::new,
            new Item.Properties().stacksTo(1)));

    public static final DeferredItem<SmithingTemplateItem> PNEUMATIC_PIPE_BLUEPRINT = blueprint("pneumatic_pipe");
    public static final DeferredItem<SmithingTemplateItem> VOID_TETHER_BLUEPRINT = blueprint("void_tether");
    public static final DeferredItem<SmithingTemplateItem> REWINDER_BLUEPRINT = blueprint("rewinder");
    public static final DeferredItem<SmithingTemplateItem> HASTENING_GEAR_BLUEPRINT = blueprint("hastening_gear");
    public static final DeferredItem<SmithingTemplateItem> PHASE_WATCH_BLUEPRINT = blueprint("phase_watch");
    public static final DeferredItem<SmithingTemplateItem> GEARLIFT_BLUEPRINT = blueprint("gearlift");

    public static List<DeferredItem<SmithingTemplateItem>> blueprints() {
        return List.of(PNEUMATIC_PIPE_BLUEPRINT, VOID_TETHER_BLUEPRINT, REWINDER_BLUEPRINT, HASTENING_GEAR_BLUEPRINT,
                PHASE_WATCH_BLUEPRINT, GEARLIFT_BLUEPRINT);
    }

    private static DeferredItem<SmithingTemplateItem> blueprint(String machine) {
        String name = machine + "_blueprint";
        return add(McRegistration.item(ITEMS, name, UpfallSmithingTemplates.factory(name, List.of(), GEAR_SLOT_ICONS), new Item.Properties()));
    }

    private static <I extends Item> DeferredItem<I> add(DeferredItem<I> item) {
        CREATIVE_ORDER.add(item);
        return item;
    }
}
