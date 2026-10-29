package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.client.UpdraftCompassAngle;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class ModItemModelProvider {
    public static final int COMPASS_FRAMES = 32;

    private final BlockModelGenerators blocks;
    private final ItemModelGenerators items;

    public ModItemModelProvider(BlockModelGenerators blocks, ItemModelGenerators items) {
        this.blocks = blocks;
        this.items = items;
    }

    public ItemModelGenerators itemModels() {
        return items;
    }

    public BlockModelGenerators blockModels() {
        return blocks;
    }

    public BiConsumer<Identifier, ModelInstance> modelOutput() {
        return blocks.modelOutput;
    }

    public Identifier modLoc(String path) {
        return Upfall.id(path);
    }

    public Identifier mcLoc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public void basicItem(Item item) {
        items.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    public void handheldItem(Item item) {
        items.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    public void register() {
        List<DeferredItem<? extends Item>> handheld = List.of(
                ModItems.NIMBUSITE_SWORD, ModItems.NIMBUSITE_PICKAXE, ModItems.NIMBUSITE_AXE, ModItems.NIMBUSITE_SHOVEL, ModItems.NIMBUSITE_HOE,
                ModItems.LEVITITE_SWORD, ModItems.LEVITITE_PICKAXE, ModItems.LEVITITE_AXE, ModItems.LEVITITE_SHOVEL, ModItems.LEVITITE_HOE);
        for (DeferredItem<? extends Item> item : ModItems.CREATIVE_ORDER) {
            if (item == ModItems.UPDRAFT_COMPASS) {
                compass();
            } else if (handheld.contains(item)) {
                handheldItem(item.get());
            } else {
                basicItem(item.get());
            }
        }
        UpfallModules.forEachData(data -> data.itemModels(this));
    }

    private void compass() {
        Item compass = ModItems.UPDRAFT_COMPASS.get();
        List<ItemModel.Unbaked> frames = new ArrayList<>();
        for (int i = 0; i < COMPASS_FRAMES; i++) {
            frames.add(ItemModelUtils.plainModel(items.createFlatItemModel(compass, String.format(Locale.ROOT, "_%02d", i), ModelTemplates.FLAT_ITEM)));
        }
        List<RangeSelectItemModel.Entry> entries = new ArrayList<>();
        for (int i = 0; i <= COMPASS_FRAMES; i++) {
            float threshold = i == 0 ? 0.0F : (i - 0.5F) / COMPASS_FRAMES;
            entries.add(ItemModelUtils.override(frames.get(i % COMPASS_FRAMES), threshold));
        }
        blocks.itemModelOutput.accept(compass, ItemModelUtils.rangeSelect(new UpdraftCompassAngle(), 1.0F, entries));
    }
}
