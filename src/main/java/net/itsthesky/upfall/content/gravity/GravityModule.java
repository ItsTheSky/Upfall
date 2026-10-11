package net.itsthesky.upfall.content.gravity;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.neoforged.bus.api.IEventBus;

public class GravityModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        InvertedGravity.register(modEventBus);
        SetBonusTooltips.register();
    }

    @Override
    public DataContribution data() {
        return new DataContribution() {
            @Override
            public void lang(ModLanguageProvider lang) {
                lang.add("effect.upfall.weightless.description",
                        "Lowers gravity. Beneath a ceiling, gravity flips: you fall up and walk on it. Sneak to let go.");
                lang.add(SetBonusTooltips.HEADER, "Full set bonus:");
                lang.add(SetBonusTooltips.HEADER_WORN, "Full set bonus (%s/%s):");
                lang.add(SetBonusTooltips.NIMBUSITE_FALL_DAMAGE, "Fall damage reduced by %s");
                lang.add(SetBonusTooltips.LEVITITE_NO_FALL_DAMAGE, "No fall damage");
                lang.add(SetBonusTooltips.LEVITITE_DOUBLE_JUMP, "Double jump: press jump in mid-air");
                lang.add(SetBonusTooltips.LEVITITE_GLIDE, "Glide by holding jump while falling");
            }
        };
    }
}
