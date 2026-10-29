package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.Consumer;

import static net.itsthesky.upfall.datagen.ModSoundProvider.def;
import static net.itsthesky.upfall.datagen.ModSoundProvider.event;

final class ReliquaryData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(ReliquaryBlocks.LATTICE_STONE.get(), "Lattice Stone");
        lang.add(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), "Carved Lattice Stone");
        lang.add(ReliquaryBlocks.LATTICE_BEAM.get(), "Lattice Beam");
        lang.add(ReliquaryBlocks.FOLD_SOCKET.get(), "Fold Socket");
        lang.add(ReliquaryBlocks.FOLD_SEAL.get(), "Fold Seal");
        lang.add(ReliquaryItems.FOLD_SHARD.get(), "Fold Shard");
        lang.add(ReliquaryItems.FOLD_ANCHOR.get(), "Fold Anchor");
        lang.add("item.upfall.fold_shard.desc", "A sliver of folded space. Set it in a Fold Socket.");
        lang.add("item.upfall.fold_anchor.desc", "Use to anchor your position. Use again within 30 seconds to fold back to it. Sneak to re-anchor.");
        lang.add("item.upfall.fold_anchor.unset", "Not anchored");
        lang.add("item.upfall.fold_anchor.anchored", "Anchored: %s s left");
        lang.add("message.upfall.fold_anchor.set", "Position anchored (%s s)");
        lang.add("message.upfall.fold_anchor.recall", "The Fold returns you");
        lang.add("message.upfall.fold_socket.empty", "An empty socket, shaped for a Fold Shard");
        lang.add("message.upfall.fold_socket.filled", "The socket hums quietly");
        lang.add("message.upfall.fold_socket.remaining", "The Fold stirs: %s socket(s) still empty");
        lang.add("message.upfall.fold_socket.complete", "Every socket is filled; the seals unravel");
        lang.add("advancements.upfall.tilted_reliquary.title", "Tilted Reliquary");
        lang.add("advancements.upfall.tilted_reliquary.description", "Reach the Tilted Reliquary, frozen mid-shatter; its core opens only for four Fold Shards");
        lang.add("advancements.upfall.fold_shard.title", "Folded Space");
        lang.add("advancements.upfall.fold_shard.description", "Find a Fold Shard in a chest of the satellite cubes around a Tilted Reliquary");
        lang.add("advancements.upfall.fold_anchor.title", "Fold Back");
        lang.add("advancements.upfall.fold_anchor.description", "Take the Fold Anchor from the Fold Warden sealed in the heart of a Tilted Reliquary");
        lang.add("item.upfall.fold_heart.desc", "The still-turning core of a Fold Warden. It holds the weight of a whole vault.");
        lang.add("advancements.upfall.fold_warden.title", "Unfolded");
        lang.add("advancements.upfall.fold_warden.description", "Fill the four Fold Sockets to wake the Fold Warden, then defeat it");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_AWAKEN.getId().getPath()), "Fold Warden awakens");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_AMBIENT.getId().getPath()), "Fold Warden hums");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_HURT.getId().getPath()), "Fold Warden cracks");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_DEATH.getId().getPath()), "Fold Warden shatters");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_HIT.getId().getPath()), "Fold shield rings");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_BREAK.getId().getPath()), "Fold shield breaks");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_RESTORE.getId().getPath()), "Satellites reform");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SLAM_CHARGE.getId().getPath()), "Satellite takes aim");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SLAM.getId().getPath()), "Satellite slams");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_TILT.getId().getPath()), "The Fold tilts");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_WEIGHT.getId().getPath()), "Weight gathers");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_DROP.getId().getPath()), "Weight falls");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_UNFOLD.getId().getPath()), "Fold Warden unfolds");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_AMBIENT.getId().getPath()), "Fold Warden rattles");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_SHED.getId().getPath()), "Fragments break loose");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_DIVE.getId().getPath()), "Fold Warden dives");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_BREATH_CHARGE.getId().getPath()), "Lattice breath charges");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_BREATH.getId().getPath()), "Lattice breath roars");
        for (ReliquaryBooks.Book book : ReliquaryBooks.all()) {
            for (int i = 0; i < book.pages().size(); i++) {
                lang.add(book.pageKey(i), book.pages().get(i));
            }
        }
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("block.upfall.lattice_stone", "Pierre de treillis");
        lang.add("block.upfall.carved_lattice_stone", "Pierre de treillis sculptée");
        lang.add("block.upfall.lattice_beam", "Poutre de treillis");
        lang.add("block.upfall.fold_socket", "Réceptacle du Pli");
        lang.add("block.upfall.fold_seal", "Sceau du Pli");
        lang.add("item.upfall.fold_shard", "Éclat du Pli");
        lang.add("item.upfall.fold_anchor", "Ancre du Pli");
        lang.add("item.upfall.fold_shard.desc", "Un éclat d'espace plié. Placez-le dans un Réceptacle du Pli.");
        lang.add("item.upfall.fold_anchor.desc", "Utilisez-la pour ancrer votre position. Réutilisez-la dans les 30 secondes pour revenir à l'ancrage. Accroupissez-vous pour ré-ancrer.");
        lang.add("item.upfall.fold_anchor.unset", "Non ancrée");
        lang.add("item.upfall.fold_anchor.anchored", "Ancrée : %s s restantes");
        lang.add("message.upfall.fold_anchor.set", "Position ancrée (%s s)");
        lang.add("message.upfall.fold_anchor.recall", "Le Pli vous ramène");
        lang.add("message.upfall.fold_socket.empty", "Un réceptacle vide, taillé pour un Éclat du Pli");
        lang.add("message.upfall.fold_socket.filled", "Le réceptacle bourdonne doucement");
        lang.add("message.upfall.fold_socket.remaining", "Le Pli s'agite : %s réceptacle(s) encore vide(s)");
        lang.add("message.upfall.fold_socket.complete", "Tous les réceptacles sont remplis ; les sceaux se défont");
        lang.add("advancements.upfall.tilted_reliquary.title", "Reliquaire incliné");
        lang.add("advancements.upfall.tilted_reliquary.description", "Atteignez le Reliquaire incliné, figé en plein éclatement ; son cœur ne s'ouvre qu'avec quatre Éclats du Pli");
        lang.add("advancements.upfall.fold_shard.title", "Espace plié");
        lang.add("advancements.upfall.fold_shard.description", "Trouvez un Éclat du Pli dans un coffre des cubes satellites d'un Reliquaire incliné");
        lang.add("advancements.upfall.fold_anchor.title", "Retour au Pli");
        lang.add("advancements.upfall.fold_anchor.description", "Arrachez l'Ancre du Pli au Gardien du Pli, scellé au cœur d'un Reliquaire incliné");
        lang.add("entity.upfall.fold_fragment", "Fragment du Pli");
        lang.add("block.upfall.fold_lattice", "Treillis du Pli");
        lang.add("item.upfall.fold_heart", "Cœur du Pli");
        lang.add("item.upfall.fold_heart.desc", "Le noyau encore tournoyant d'un Gardien du Pli. Il contient le poids d'une chambre entière.");
        lang.add("advancements.upfall.fold_warden.title", "Déplié");
        lang.add("advancements.upfall.fold_warden.description", "Remplissez les quatre Réceptacles du Pli pour réveiller le Gardien du Pli, puis vainquez-le");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_AWAKEN.getId().getPath()), "Le Gardien du Pli s'éveille");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_AMBIENT.getId().getPath()), "Le Gardien du Pli bourdonne");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_HURT.getId().getPath()), "Le Gardien du Pli se fissure");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_DEATH.getId().getPath()), "Le Gardien du Pli vole en éclats");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_HIT.getId().getPath()), "Le bouclier du Pli résonne");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_BREAK.getId().getPath()), "Le bouclier du Pli se brise");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SHIELD_RESTORE.getId().getPath()), "Les satellites se reforment");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SLAM_CHARGE.getId().getPath()), "Un satellite vise");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_SLAM.getId().getPath()), "Un satellite s'écrase");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_TILT.getId().getPath()), "Le Pli bascule");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_WEIGHT.getId().getPath()), "Le poids s'accumule");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_DROP.getId().getPath()), "Le poids retombe");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WARDEN_UNFOLD.getId().getPath()), "Le Gardien du Pli se déplie");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_AMBIENT.getId().getPath()), "Le Gardien du Pli cliquette");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_SHED.getId().getPath()), "Des fragments se détachent");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_DIVE.getId().getPath()), "Le Gardien du Pli plonge");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_BREATH_CHARGE.getId().getPath()), "Le souffle de treillis se charge");
        lang.add(ModLanguageProvider.subtitle(ReliquarySounds.WYRM_BREATH.getId().getPath()), "Le souffle de treillis rugit");
        lang.add("entity.upfall.fold_warden", "Gardien du Pli");
        lang.add("entity.upfall.fold_satellite", "Satellite du Pli");
        lang.add("item.upfall.fold_warden_spawn_egg", "Œuf d'apparition de Gardien du Pli");
        for (ReliquaryBooks.Book book : ReliquaryBooks.all()) {
            for (int i = 0; i < book.pagesFr().size(); i++) {
                lang.add(book.pageKey(i), book.pagesFr().get(i));
            }
        }
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(ReliquaryBlocks.LATTICE_STONE.get(), states.cubeAll(ReliquaryBlocks.LATTICE_STONE.get()));
        states.simpleBlockWithItem(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), states.cubeAll(ReliquaryBlocks.CARVED_LATTICE_STONE.get()));
        states.simpleBlock(ReliquaryBlocks.FOLD_SEAL.get(), states.cubeAll(ReliquaryBlocks.FOLD_SEAL.get()));
        states.simpleBlock(ReliquaryBlocks.FOLD_LATTICE.get(), states.cubeAll(ReliquaryBlocks.FOLD_LATTICE.get()));
        ReliquaryModels.blockStates(states);
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(ReliquaryItems.FOLD_SHARD.get());
        models.basicItem(ReliquaryItems.FOLD_ANCHOR.get());
        models.basicItem(ReliquaryItems.FOLD_HEART.get());
        ReliquaryModels.itemModels(models);
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        sounds.addSound(ReliquarySounds.WARDEN_AWAKEN, def(ReliquarySounds.WARDEN_AWAKEN).with(event("block.beacon.activate").pitch(0.5).volume(1.0), event("block.respawn_anchor.charge").pitch(0.6).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_AMBIENT, def(ReliquarySounds.WARDEN_AMBIENT).with(event("block.beacon.ambient").pitch(0.6).volume(0.8), event("block.amethyst_block.resonate").pitch(0.5).volume(0.8)));
        sounds.addSound(ReliquarySounds.WARDEN_HURT, def(ReliquarySounds.WARDEN_HURT).with(event("block.amethyst_block.hit").pitch(0.6).volume(1.0), event("entity.iron_golem.hurt").pitch(1.3).volume(0.7)));
        sounds.addSound(ReliquarySounds.WARDEN_DEATH, def(ReliquarySounds.WARDEN_DEATH).with(event("entity.elder_guardian.death").pitch(0.8).volume(1.0), event("block.beacon.deactivate").pitch(0.5).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_SHIELD_HIT, def(ReliquarySounds.WARDEN_SHIELD_HIT).with(event("block.amethyst_block.hit").pitch(0.5).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_SHIELD_BREAK, def(ReliquarySounds.WARDEN_SHIELD_BREAK).with(event("block.glass.break").pitch(0.6).volume(1.0), event("block.beacon.deactivate").pitch(0.7).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_SHIELD_RESTORE, def(ReliquarySounds.WARDEN_SHIELD_RESTORE).with(event("block.respawn_anchor.charge").pitch(0.6).volume(1.0), event("block.amethyst_block.resonate").pitch(1.2).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_SLAM_CHARGE, def(ReliquarySounds.WARDEN_SLAM_CHARGE).with(event("block.respawn_anchor.charge").pitch(1.4).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_SLAM, def(ReliquarySounds.WARDEN_SLAM).with(event("entity.generic.explode").pitch(1.3).volume(0.8)));
        sounds.addSound(ReliquarySounds.WARDEN_TILT, def(ReliquarySounds.WARDEN_TILT).with(event("entity.illusioner.cast_spell").pitch(0.5).volume(1.0), event("block.beacon.power_select").pitch(0.5).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_WEIGHT, def(ReliquarySounds.WARDEN_WEIGHT).with(event("entity.elder_guardian.curse").pitch(0.6).volume(0.8)));
        sounds.addSound(ReliquarySounds.WARDEN_DROP, def(ReliquarySounds.WARDEN_DROP).with(event("entity.warden.sonic_boom").pitch(0.6).volume(1.0)));
        sounds.addSound(ReliquarySounds.WARDEN_UNFOLD, def(ReliquarySounds.WARDEN_UNFOLD).with(event("entity.warden.emerge").pitch(1.2).volume(1.0), event("block.respawn_anchor.set_spawn").pitch(0.6).volume(1.0)));
        sounds.addSound(ReliquarySounds.WYRM_AMBIENT, def(ReliquarySounds.WYRM_AMBIENT).with(event("entity.phantom.ambient").pitch(0.5).volume(0.8), event("block.amethyst_block.resonate").pitch(0.7).volume(0.8)));
        sounds.addSound(ReliquarySounds.WYRM_SHED, def(ReliquarySounds.WYRM_SHED).with(event("entity.shulker.shoot").pitch(0.7).volume(1.0)));
        sounds.addSound(ReliquarySounds.WYRM_DIVE, def(ReliquarySounds.WYRM_DIVE).with(event("item.trident.riptide_3").pitch(0.7).volume(1.0)));
        sounds.addSound(ReliquarySounds.WYRM_BREATH_CHARGE, def(ReliquarySounds.WYRM_BREATH_CHARGE).with(event("entity.warden.sonic_charge").pitch(1.4).volume(1.0)));
        sounds.addSound(ReliquarySounds.WYRM_BREATH, def(ReliquarySounds.WYRM_BREATH).with(event("entity.breeze.shoot").pitch(0.6).volume(1.0), event("entity.warden.sonic_boom").pitch(1.5).volume(0.6)));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ReliquaryBlocks.LATTICE_STONE.get(), ReliquaryBlocks.CARVED_LATTICE_STONE.get(),
                ReliquaryBlocks.LATTICE_BEAM.get());
        tags.tag(BlockTags.WITHER_IMMUNE).add(ReliquaryBlocks.FOLD_SOCKET.get(), ReliquaryBlocks.FOLD_SEAL.get());
        tags.tag(BlockTags.DRAGON_IMMUNE).add(ReliquaryBlocks.FOLD_SOCKET.get(), ReliquaryBlocks.FOLD_SEAL.get());
    }

    @Override
    public void recipes(RecipeContext recipes) {
        Block stone = ReliquaryBlocks.LATTICE_STONE.get();
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, ReliquaryBlocks.CARVED_LATTICE_STONE.get(), 4)
                .pattern("##").pattern("##").define('#', stone)
                .unlockedBy(RecipeContext.hasName(stone), recipes.has(stone))
                .save(recipes.output());
        recipes.shaped(RecipeCategory.BUILDING_BLOCKS, ReliquaryBlocks.LATTICE_BEAM.get(), 2)
                .pattern("#").pattern("#").define('#', stone)
                .unlockedBy(RecipeContext.hasName(stone), recipes.has(stone))
                .save(recipes.output());
        stonecutting(recipes, stone, ReliquaryBlocks.CARVED_LATTICE_STONE.get());
        stonecutting(recipes, stone, ReliquaryBlocks.LATTICE_BEAM.get());
    }

    private static void stonecutting(RecipeContext recipes, Block input, Block result) {
        recipes.save(recipes.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result)
                .unlockedBy(RecipeContext.hasName(input), recipes.has(input)),
                Upfall.id(RecipeContext.itemName(result) + "_from_" + RecipeContext.itemName(input) + "_stonecutting"));
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.lootTables("Tilted Reliquary loot tables",
                List.of(new LootTableProvider.SubProviderEntry(ReliquaryLoot::new, LootContextParamSets.CHEST))));
        ctx.add(ctx.lootTables("Tilted Reliquary entity loot tables",
                List.of(new LootTableProvider.SubProviderEntry(ReliquaryEntityLoot::new, LootContextParamSets.ENTITY))));
        ctx.add(ctx.advancements("Tilted Reliquary advancements", List.of(ReliquaryData::advancements)));
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder visited = AdvancementSubProvider.createPlaceholder("upfall:upfall/tilted_reliquary");
        AdvancementHolder shard = Advancement.Builder.advancement().parent(visited)
                .display(ReliquaryItems.FOLD_SHARD.get(), Component.translatable("advancements.upfall.fold_shard.title"),
                        Component.translatable("advancements.upfall.fold_shard.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("shard", InventoryChangeTrigger.TriggerInstance.hasItems(ReliquaryItems.FOLD_SHARD.get()))
                .save(saver, Upfall.id("upfall/fold_shard").toString());
        AdvancementHolder warden = Advancement.Builder.advancement().parent(shard)
                .display(ReliquaryItems.FOLD_HEART.get(), Component.translatable("advancements.upfall.fold_warden.title"),
                        Component.translatable("advancements.upfall.fold_warden.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("killed", KilledTrigger.TriggerInstance.playerKilledEntity(entityOf(registries, ReliquaryEntities.FOLD_WARDEN.get())))
                .save(saver, Upfall.id("upfall/fold_warden").toString());
        Advancement.Builder.advancement().parent(warden)
                .display(ReliquaryItems.FOLD_ANCHOR.get(), Component.translatable("advancements.upfall.fold_anchor.title"),
                        Component.translatable("advancements.upfall.fold_anchor.description"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("anchor", InventoryChangeTrigger.TriggerInstance.hasItems(ReliquaryItems.FOLD_ANCHOR.get()))
                .save(saver, Upfall.id("upfall/fold_anchor").toString());
    }

    private static EntityPredicate.Builder entityOf(HolderLookup.Provider registries, EntityType<?> type) {
        //? if >=26.1 {
        /*return EntityPredicate.Builder.entity().of(registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE), type);
        *///?} else {
        return EntityPredicate.Builder.entity().of(type);
        //?}
    }
}
