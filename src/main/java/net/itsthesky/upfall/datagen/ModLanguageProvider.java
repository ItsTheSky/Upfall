package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McIds;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModFluids;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ModLanguageProvider extends LanguageProvider {
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry("raw_nimbusite_block", "Block of Raw Nimbusite"),
            Map.entry("nimbusite_block", "Block of Nimbusite"),
            Map.entry("levitite_block", "Block of Levitite"),
            Map.entry("nimbusite_upgrade_smithing_template", "Smithing Template"),
            Map.entry("levitite_upgrade_smithing_template", "Smithing Template"));

    private final Set<String> added = new HashSet<>();

    public ModLanguageProvider(PackOutput output) {
        super(output, Upfall.MODID, "en_us");
    }

    @Override
    public void add(String key, String value) {
        added.add(key);
        super.add(key, value);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.upfall", "Upfall");
        add(ModFluids.SKYWATER_BLOCK.get(), "Skywater");
        add(ModBlocks.UPDRAFT_MIST.get(), "Updraft Mist");
        add(ModBlocks.UPFALL_CASCADE.get(), "Upfall Cascade");
        add(ModBlocks.GLOWVINE_PLANT.get(), "Glowvine");
        add(ModBlocks.POTTED_DUSKBLOOM.get(), "Potted Duskbloom");
        add(ModBlocks.POTTED_CLOUD_FERN.get(), "Potted Cloud Fern");
        add(ModBlocks.POTTED_SKYREACH_SAPLING.get(), "Potted Skyreach Sapling");

        add("message.upfall.key_too_low", "The key stays silent... it only resonates in the Overworld sky, above Y %s.");
        add("item.upfall.updraft_compass.desc", "Points to the nearest Updraft Vent in The Upfall.");
        add("item.upfall.skyward_key.desc", "Lights a Halo Frame high in the Overworld sky (above Y %s).");
        add("container.upfall.sky_cache", "Sky Cache");
        add(ModEntities.SKYRAY.get(), "Skyray");
        advancement("root", "The Upfall", "Somewhere above the clouds, the sky falls upward");
        advancement("skyward_key", "A Key to the Sky", "Craft a Skyward Key from an Elytra, a Nether Star and feathers");
        advancement("light_portal", "Halo of the Heavens", "Light a Halo Portal high in the Overworld sky, above Y 200");
        advancement("enter", "Upfall", "Step through the Halo Portal into The Upfall");
        advancement("updraft", "Lifted", "Ride a column of Updraft Mist");
        advancement("updraft_compass", "Following the Currents", "Obtain an Updraft Compass");
        advancement("hanging_roots", "Where Forests Hang", "Discover the Hanging Roots");
        advancement("weightless", "Unbearable Lightness", "Become Weightless");
        advancement("crystal_grotto", "Glittering Depths", "Find a Crystal Grotto, a geode hidden under the cloud sea");
        advancement("sky_enchantment", "Sky-Touched", "Enchant an item with Skywalker, Uplifted, Cloudsight or Jumpstart");
        advancement("mine_nimbusite", "Cloud Metal", "Mine Nimbusite with a netherite pickaxe");
        advancement("nimbusite_template", "Blueprint of the Skies",
                "Find a Nimbusite Upgrade Smithing Template in Drifting Ruins, an Inverted Observatory or a Pearl Aerie, or buy one from a Loftkin");
        advancement("nimbusite_armor", "Lighter than Netherite", "Wear a full set of Nimbusite armor; 7 diamonds and a Pearlstone block copy the template");
        advancement("gilded_ridge", "Gilded Heights", "Reach the rare Gilded Ridge, the only place where Levitite ore grows");
        advancement("mine_levitite", "Pieces of Lightness", "Mine Levitite ore in the Gilded Ridge; it needs a Nimbusite pickaxe");
        advancement("levitite_template", "The Inverted Observatory",
                "Recover a Levitite Upgrade Smithing Template; every Inverted Observatory, hanging under the Hanging Roots, holds one");
        advancement("levitite_armor", "Above It All", "Wear a full set of Levitite armor");
        add("message.upfall.void_rescue", "The sky lets you go... you drift back to the Overworld.");
        for (var biome : ModBiomes.ALL) {
            add("biome.upfall." + McIds.id(biome).getPath(), name(McIds.id(biome).getPath()));
        }

        template("nimbusite_upgrade", "Nimbusite Upgrade", "Netherite Equipment", "Nimbusite Ingot",
                "Add netherite armor, weapon, or tool", "Add Nimbusite Ingot");
        template("levitite_upgrade", "Levitite Upgrade", "Nimbusite Equipment", "Levitite Ingot",
                "Add nimbusite armor, weapon, or tool", "Add Levitite Ingot");

        for (var sound : ModSounds.SOUND_EVENTS.getEntries()) {
            add(subtitle(sound.getId().getPath()), subtitleText(sound.getId().getPath()));
        }

        UpfallModules.forEachData(data -> data.lang(this));
        autoNames();
    }

    private void advancement(String name, String title, String description) {
        add("advancements.upfall." + name + ".title", title);
        add("advancements.upfall." + name + ".description", description);
    }

    private void autoNames() {
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            if (isUpfall(McIds.id(entry.getKey())) && !added.contains(entry.getValue().getDescriptionId())) {
                add(entry.getValue(), name(McIds.id(entry.getKey()).getPath()));
            }
        }
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (isUpfall(McIds.id(entry.getKey())) && !added.contains(entry.getValue().getDescriptionId())) {
                add(entry.getValue(), name(McIds.id(entry.getKey()).getPath()));
            }
        }
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (isUpfall(McIds.id(entry.getKey())) && !added.contains(entry.getValue().getDescriptionId())) {
                add(entry.getValue(), name(McIds.id(entry.getKey()).getPath()));
            }
        }
        for (var entry : BuiltInRegistries.MOB_EFFECT.entrySet()) {
            if (isUpfall(McIds.id(entry.getKey())) && !added.contains(entry.getValue().getDescriptionId())) {
                add(entry.getValue(), name(McIds.id(entry.getKey()).getPath()));
            }
        }
    }

    public boolean has(String key) {
        return added.contains(key);
    }

    private static boolean isUpfall(net.minecraft.resources.ResourceLocation id) {
        return id.getNamespace().equals(Upfall.MODID);
    }

    private void template(String upgrade, String title, String appliesTo, String ingredients, String baseSlot, String additionSlot) {
        add(Util.makeDescriptionId("upgrade", Upfall.id(upgrade)), title);
        add(UpfallSmithingTemplates.key("applies_to", upgrade), appliesTo);
        add(UpfallSmithingTemplates.key("ingredients", upgrade), ingredients);
        add(UpfallSmithingTemplates.key("base_slot_description", upgrade), baseSlot);
        add(UpfallSmithingTemplates.key("additions_slot_description", upgrade), additionSlot);
    }

    public static String subtitle(String soundPath) {
        return "subtitles.upfall." + soundPath;
    }

    private static String subtitleText(String soundPath) {
        return switch (soundPath) {
            case "block.halo_portal.ambient" -> "Halo Portal hums";
            case "block.halo_portal.trigger" -> "Halo Portal stirs";
            case "block.halo_portal.travel" -> "Halo Portal chimes";
            case "block.halo_portal.ignite" -> "Halo Portal ignites";
            case "block.updraft_vent.whoosh" -> "Updraft whooshes";
            case "item.armor.equip_nimbusite" -> "Nimbusite armor clinks";
            case "item.armor.equip_levitite" -> "Levitite armor rings";
            case "entity.player.levitite_double_jump" -> "Air bursts";
            case "entity.player.void_rescue" -> "The sky lets go";
            case "music.upfall" -> "Music plays";
            case "entity.skyray.ambient" -> "Skyray hums";
            case "entity.skyray.hurt" -> "Skyray hurts";
            case "entity.skyray.death" -> "Skyray dies";
            default -> "Sky winds whisper";
        };
    }

    public static String name(String path) {
        if (OVERRIDES.containsKey(path)) {
            return OVERRIDES.get(path);
        }
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("_")) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return sb.toString();
    }
}
