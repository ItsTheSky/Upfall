package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.HashSet;
import java.util.Set;

public class ModFrenchLanguageProvider extends LanguageProvider {
    private final Set<String> added = new HashSet<>();

    public ModFrenchLanguageProvider(PackOutput output) {
        super(output, Upfall.MODID, "fr_fr");
    }

    @Override
    public void add(String key, String value) {
        if (added.add(key)) {
            super.add(key, value);
        }
    }

    @Override
    protected void addTranslations() {
        UpfallModules.forEachData(data -> data.langFr(this));
        FrenchTranslations.EXISTING.forEach(this::add);
    }
}
