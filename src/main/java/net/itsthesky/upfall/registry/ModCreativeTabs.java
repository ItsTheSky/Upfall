package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    private static final Map<UpfallTab, List<Supplier<? extends ItemLike>>> CONTENTS = new EnumMap<>(UpfallTab.class);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Upfall.MODID);

    // NeoForge breaks tab order ties by id path, so the first tab of the chain must have the greatest path
    // to keep other mods' tabs from being slotted between ours.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING = tab(UpfallTab.BUILDING, "upfall",
            () -> new ItemStack(ModBlocks.PEARLSTONE_BRICKS.get()), null);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NATURE = tab(UpfallTab.NATURE, "nature",
            () -> new ItemStack(ModBlocks.SKYGRASS_BLOCK.get()), BUILDING);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FUNCTIONAL = tab(UpfallTab.FUNCTIONAL, "functional",
            () -> new ItemStack(MachinesBlocks.GEARLIFT.get()), NATURE);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = tab(UpfallTab.ITEMS, "items",
            () -> new ItemStack(ModItems.LEVITITE_SWORD.get()), FUNCTIONAL);

    public static <T extends Supplier<? extends ItemLike>> T add(T item, UpfallTab... tabs) {
        if (tabs.length == 0) {
            throw new IllegalArgumentException("No creative tab given for " + item);
        }
        for (UpfallTab tab : tabs) {
            CONTENTS.computeIfAbsent(tab, t -> new ArrayList<>()).add(item);
        }
        return item;
    }

    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        for (Supplier<? extends ItemLike> item : CONTENTS.getOrDefault(UpfallTab.ITEMS, List.of())) {
            if (item.get().asItem() instanceof SpawnEggItem) {
                event.accept(item.get());
            }
        }
    }

    private static DeferredHolder<CreativeModeTab, CreativeModeTab> tab(UpfallTab tab, String id, Supplier<ItemStack> icon,
                                                                      DeferredHolder<CreativeModeTab, CreativeModeTab> after) {
        String name = tab.name().toLowerCase(Locale.ROOT);
        return CREATIVE_TABS.register(id, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.upfall." + name))
                    .icon(icon)
                    .displayItems((params, output) -> CONTENTS.getOrDefault(tab, List.of()).forEach(item -> output.accept(item.get())));
            if (after != null) {
                builder.withTabsBefore(after.getKey());
            }
            return builder.build();
        });
    }
}
