package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class WatchLiftData {
    private WatchLiftData() {}

    static void lang(ModLanguageProvider lang) {
        lang.add("item.upfall.phase_watch.desc", "Shows the phase of the Chronospire and the time left before the next one while held.");
        lang.add("message.upfall.phase_watch", "%s - next in %s");
        lang.add("message.upfall.phase_watch.no_signal", "No signal");
        lang.add("block.upfall.gearlift.desc", "Jump to rise to the next Gearlift above, sneak to descend to the one below.");
    }

    static void blockStates(ModBlockStateProvider states) {
        var model = states.models().cubeBottomTop("gearlift", states.blockTexture("gearlift_side"),
                states.blockTexture("gearlift_bottom"), states.blockTexture("gearlift_top"));
        states.simpleBlockWithItem(MachinesBlocks.GEARLIFT.get(), model);
    }

    static void itemModels(ModItemModelProvider models) {
        ItemModelBuilder root = models.withExistingParent("phase_watch", models.mcLoc("item/generated"))
                .texture("layer0", models.modLoc("item/phase_watch"));
        for (ChronoPhase phase : ChronoPhase.values()) {
            String name = "phase_watch_" + phase.getSerializedName();
            models.withExistingParent(name, models.mcLoc("item/generated")).texture("layer0", models.modLoc("item/" + name));
            root.override().predicate(Upfall.id("phase"), 0.2F * (phase.ordinal() + 1) - 0.1F)
                    .model(new ModelFile.UncheckedModelFile(models.modLoc("item/" + name))).end();
        }
    }
}
