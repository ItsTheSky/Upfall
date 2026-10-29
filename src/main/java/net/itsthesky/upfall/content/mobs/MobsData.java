package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

import static net.itsthesky.upfall.datagen.ModSoundProvider.def;
import static net.itsthesky.upfall.datagen.ModSoundProvider.event;

public class MobsData implements DataContribution {
    private static final List<DeferredHolder<Item, ? extends Item>> BASIC_ITEMS = List.of(
            MobItems.DUSKWING_PLUME, MobItems.CRAGMAW_FANG, MobItems.MIST_GEL, MobItems.DOWN_TUFT,
            MobItems.RAW_SKYGRAZER_MEAT, MobItems.COOKED_SKYGRAZER_MEAT, MobItems.CLOUDBERRY_JELLY);
    static final List<DeferredHolder<Item, ? extends Item>> SPAWN_EGGS = List.of(
            MobItems.DUSKWING_SPAWN_EGG, MobItems.CRAGMAW_SPAWN_EGG, MobItems.MISTJELLY_SPAWN_EGG, MobItems.SKYGRAZER_SPAWN_EGG);

    @Override
    public void lang(ModLanguageProvider lang) {
        for (var sound : MobSounds.SOUND_EVENTS.getEntries()) {
            String path = sound.getId().getPath();
            String[] parts = path.split("\\.");
            String name = ModLanguageProvider.name(parts[1]);
            lang.add(ModLanguageProvider.subtitle(path), switch (parts[2]) {
                case "ambient" -> name + " calls";
                case "hurt" -> name + " is hurt";
                default -> name + " dies";
            });
        }
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        for (DeferredHolder<Item, ? extends Item> item : BASIC_ITEMS) {
            models.basicItem(item.get());
        }
        MobsModels.spawnEggs(models);
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        sounds.addSound(MobSounds.DUSKWING_AMBIENT, def(MobSounds.DUSKWING_AMBIENT).with(event("entity.phantom.ambient").pitch(1.9).volume(0.6),
                event("entity.bat.ambient").pitch(0.8).volume(0.5)));
        sounds.addSound(MobSounds.DUSKWING_HURT, def(MobSounds.DUSKWING_HURT).with(event("entity.bat.hurt").pitch(0.7),
                event("entity.phantom.hurt").pitch(1.8)));
        sounds.addSound(MobSounds.DUSKWING_DEATH, def(MobSounds.DUSKWING_DEATH).with(event("entity.phantom.death").pitch(1.7),
                event("entity.bat.death").pitch(0.6)));

        sounds.addSound(MobSounds.CRAGMAW_AMBIENT, def(MobSounds.CRAGMAW_AMBIENT).with(event("entity.ravager.ambient").pitch(1.25).volume(0.8),
                event("entity.hoglin.ambient").pitch(0.7).volume(0.7)));
        sounds.addSound(MobSounds.CRAGMAW_HURT, def(MobSounds.CRAGMAW_HURT).with(event("entity.ravager.hurt").pitch(1.45)));
        sounds.addSound(MobSounds.CRAGMAW_DEATH, def(MobSounds.CRAGMAW_DEATH).with(event("entity.ravager.death").pitch(1.3),
                event("entity.hoglin.death").pitch(0.7)));

        sounds.addSound(MobSounds.MISTJELLY_AMBIENT, def(MobSounds.MISTJELLY_AMBIENT).with(event("entity.glow_squid.ambient").pitch(1.5).volume(0.6),
                event("block.amethyst_block.chime").pitch(0.6).volume(0.25)));
        sounds.addSound(MobSounds.MISTJELLY_HURT, def(MobSounds.MISTJELLY_HURT).with(event("entity.squid.hurt").pitch(1.6)));
        sounds.addSound(MobSounds.MISTJELLY_DEATH, def(MobSounds.MISTJELLY_DEATH).with(event("entity.squid.death").pitch(1.5),
                event("block.amethyst_block.break").pitch(1.2).volume(0.5)));

        sounds.addSound(MobSounds.SKYGRAZER_AMBIENT, def(MobSounds.SKYGRAZER_AMBIENT).with(event("entity.llama.ambient").pitch(1.45).volume(0.8)));
        sounds.addSound(MobSounds.SKYGRAZER_HURT, def(MobSounds.SKYGRAZER_HURT).with(event("entity.llama.hurt").pitch(1.45)));
        sounds.addSound(MobSounds.SKYGRAZER_DEATH, def(MobSounds.SKYGRAZER_DEATH).with(event("entity.llama.death").pitch(1.35)));
    }

    @Override
    public void recipes(RecipeContext recipes) {
        recipes.shaped(RecipeCategory.COMBAT, Items.ARROW, 6)
                .define('F', Items.FLINT).define('S', Items.STICK).define('P', MobItems.DUSKWING_PLUME.get())
                .pattern("F").pattern("S").pattern("P")
                .unlockedBy(RecipeContext.hasName(MobItems.DUSKWING_PLUME.get()), recipes.has(MobItems.DUSKWING_PLUME.get()))
                .save(recipes.output(), Upfall.id("arrow_from_duskwing_plume").toString());

        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, Items.WHITE_WOOL)
                .define('T', MobItems.DOWN_TUFT.get()).pattern("TT").pattern("TT")
                .unlockedBy(RecipeContext.hasName(MobItems.DOWN_TUFT.get()), recipes.has(MobItems.DOWN_TUFT.get()))
                .save(recipes.output(), Upfall.id("white_wool_from_down_tuft").toString());

        recipes.save(recipes.shapeless(RecipeCategory.FOOD, MobItems.CLOUDBERRY_JELLY.get())
                .requires(MobItems.MIST_GEL.get(), 2).requires(ModItems.CLOUDBERRY.get(), 2).requires(Items.BOWL)
                .unlockedBy(RecipeContext.hasName(MobItems.MIST_GEL.get()), recipes.has(MobItems.MIST_GEL.get())), Upfall.id("cloudberry_jelly"));

        cook(recipes, MobItems.RAW_SKYGRAZER_MEAT.get(), MobItems.COOKED_SKYGRAZER_MEAT.get());
    }

    private static void cook(RecipeContext recipes, ItemLike raw, ItemLike cooked) {
        String base = RecipeContext.itemName(cooked);
        Ingredient input = Ingredient.of(raw);
        recipes.smelting(input, RecipeCategory.FOOD, cooked, 0.35F, 200)
                .unlockedBy(RecipeContext.hasName(raw), recipes.has(raw)).save(recipes.output(), Upfall.id(base + "_smelting").toString());
        recipes.smoking(input, RecipeCategory.FOOD, cooked, 0.35F, 100)
                .unlockedBy(RecipeContext.hasName(raw), recipes.has(raw)).save(recipes.output(), Upfall.id(base + "_smoking").toString());
        recipes.campfireCooking(input, RecipeCategory.FOOD, cooked, 0.35F, 600)
                .unlockedBy(RecipeContext.hasName(raw), recipes.has(raw)).save(recipes.output(), Upfall.id(base + "_campfire_cooking").toString());
    }

    @Override
    public void biomeSpawns(ResourceKey<Biome> biome, MobSpawnSettings.Builder spawns) {
        boolean archipelago = biome == ModBiomes.DRIFTING_ARCHIPELAGO;
        boolean mistsea = biome == ModBiomes.MISTSEA;
        boolean roots = biome == ModBiomes.HANGING_ROOTS;
        boolean ridge = biome == ModBiomes.GILDED_RIDGE;

        addSpawn(spawns, MobCategory.MONSTER, MobEntities.DUSKWING.get(), mistsea ? 2 : ridge ? 4 : 3, 1, 1);
        if (!mistsea) {
            addSpawn(spawns, MobCategory.MONSTER, MobEntities.CRAGMAW.get(), ridge ? 10 : roots ? 5 : 6, 1, 2);
        }
        if (archipelago || roots) {
            addSpawn(spawns, MobCategory.CREATURE, MobEntities.SKYGRAZER.get(), archipelago ? 10 : 6, 2, 4);
            spawns.creatureGenerationProbability(0.3F);
        }
        if (!ridge) {
            addSpawn(spawns, MobCategory.WATER_AMBIENT, MobEntities.MISTJELLY.get(), mistsea ? 14 : 3, 1, 3);
        }
    }

    private static void addSpawn(MobSpawnSettings.Builder spawns, MobCategory category, EntityType<?> type, int weight, int min, int max) {
        //? if >=26.1 {
        /*spawns.addSpawn(category, weight, new MobSpawnSettings.SpawnerData(type, min, max));
        *///?} else {
        spawns.addSpawn(category, new MobSpawnSettings.SpawnerData(type, weight, min, max));
        //?}
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.lootTables("Upfall Mob Loot Tables",
                List.of(new LootTableProvider.SubProviderEntry(MobLootProvider::new, LootContextParamSets.ENTITY))));
    }
}
