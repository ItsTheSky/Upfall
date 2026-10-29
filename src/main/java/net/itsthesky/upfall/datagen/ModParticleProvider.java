package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.data.PackOutput;
//? if >=26.1 {
/*import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;
*///?} else {
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;
//?}

public class ModParticleProvider extends ParticleDescriptionProvider {
    //? if >=26.1 {
    /*public ModParticleProvider(PackOutput output) {
        super(output);
    }
    *///?} else {
    public ModParticleProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, helper);
    }
    //?}

    @Override
    protected void addDescriptions() {
        spriteSet(ModParticles.PETAL.get(), Upfall.id("petal"), 3, false);
        spriteSet(ModParticles.GILDED_DUST.get(), Upfall.id("gilded_dust"), 2, false);
        spriteSet(ModParticles.RISING_DROPLET.get(), Upfall.id("rising_droplet"), 2, false);
        spriteSet(ModParticles.MIST_MOTE.get(), Upfall.id("mist_mote"), 2, false);
        UpfallModules.forEachData(data -> data.particles(this));
    }

    public void addSpriteSet(net.minecraft.core.particles.ParticleType<?> type, net.minecraft.resources.ResourceLocation base, int count) {
        spriteSet(type, base, count, false);
    }
}
