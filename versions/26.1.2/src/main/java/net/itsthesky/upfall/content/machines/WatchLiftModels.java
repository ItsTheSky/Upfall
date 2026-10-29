package net.itsthesky.upfall.content.machines;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

final class WatchLiftModels {
    private WatchLiftModels() {}

    static void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(MachinesBlocks.GEARLIFT.get(), states.cubeBottomTop("gearlift", "gearlift_side", "gearlift_bottom", "gearlift_top"));
    }

    static void itemModels(ModItemModelProvider models) {
        Item watch = MachinesItems.PHASE_WATCH.get();
        ItemModelGenerators items = models.itemModels();
        List<RangeSelectItemModel.Entry> entries = new ArrayList<>();
        for (ChronoPhase phase : ChronoPhase.values()) {
            entries.add(ItemModelUtils.override(ItemModelUtils.plainModel(
                    items.createFlatItemModel(watch, "_" + phase.getSerializedName(), ModelTemplates.FLAT_ITEM)), WatchLiftData.phaseThreshold(phase)));
        }
        items.itemModelOutput.accept(watch, ItemModelUtils.rangeSelect(phaseProperty(),
                ItemModelUtils.plainModel(items.createFlatItemModel(watch, ModelTemplates.FLAT_ITEM)), entries));
    }

    // The upfall:phase property type is private to MachinesClientModule, so the registered instance is resolved by id.
    private static RangeSelectItemModelProperty phaseProperty() {
        JsonObject json = new JsonObject();
        json.addProperty("property", Upfall.id("phase").toString());
        return RangeSelectItemModelProperties.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }
}
