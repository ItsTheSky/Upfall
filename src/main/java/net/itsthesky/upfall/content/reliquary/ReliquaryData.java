package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
        lang.add("advancements.upfall.tilted_reliquary.description", "Reach the Tilted Reliquary, the vaults the Surveyors of the Fold left frozen mid-shatter");
        lang.add("advancements.upfall.fold_anchor.title", "Fold Back");
        lang.add("advancements.upfall.fold_anchor.description", "Recover the Fold Anchor from the sealed heart of a Tilted Reliquary");
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
        lang.add("advancements.upfall.tilted_reliquary.description", "Atteignez le Reliquaire incliné, les caveaux que les Arpenteurs du Pli ont laissés figés en plein éclatement");
        lang.add("advancements.upfall.fold_anchor.title", "Retour au Pli");
        lang.add("advancements.upfall.fold_anchor.description", "Récupérez l'Ancre du Pli dans le cœur scellé d'un Reliquaire incliné");
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
        states.axisBlock(ReliquaryBlocks.LATTICE_BEAM.get(), states.blockTexture("lattice_beam"), states.blockTexture("lattice_beam_end"));
        states.blockItem(ReliquaryBlocks.LATTICE_BEAM.get());

        ModelFile empty = states.cubeAll(ReliquaryBlocks.FOLD_SOCKET.get());
        ModelFile filled = states.glowingCube("fold_socket_filled", "solid");
        states.getVariantBuilder(ReliquaryBlocks.FOLD_SOCKET.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(FoldSocketBlock.FILLED) ? filled : empty).build());
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(ReliquaryItems.FOLD_SHARD.get());
        models.basicItem(ReliquaryItems.FOLD_ANCHOR.get());
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ReliquaryBlocks.LATTICE_STONE.get(), ReliquaryBlocks.CARVED_LATTICE_STONE.get(),
                ReliquaryBlocks.LATTICE_BEAM.get());
        tags.tag(BlockTags.WITHER_IMMUNE).add(ReliquaryBlocks.FOLD_SOCKET.get(), ReliquaryBlocks.FOLD_SEAL.get());
        tags.tag(BlockTags.DRAGON_IMMUNE).add(ReliquaryBlocks.FOLD_SOCKET.get(), ReliquaryBlocks.FOLD_SEAL.get());
    }

    @Override
    public void recipes(RecipeOutput output) {
        Block stone = ReliquaryBlocks.LATTICE_STONE.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ReliquaryBlocks.CARVED_LATTICE_STONE.get(), 4)
                .pattern("##").pattern("##").define('#', stone)
                .unlockedBy(ModRecipeProvider.hasName(stone), ModRecipeProvider.hasItem(stone))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ReliquaryBlocks.LATTICE_BEAM.get(), 2)
                .pattern("#").pattern("#").define('#', stone)
                .unlockedBy(ModRecipeProvider.hasName(stone), ModRecipeProvider.hasItem(stone))
                .save(output);
        stonecutting(output, stone, ReliquaryBlocks.CARVED_LATTICE_STONE.get());
        stonecutting(output, stone, ReliquaryBlocks.LATTICE_BEAM.get());
    }

    private static void stonecutting(RecipeOutput output, Block input, Block result) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result)
                .unlockedBy(ModRecipeProvider.hasName(input), ModRecipeProvider.hasItem(input))
                .save(output, Upfall.id(ModRecipeProvider.itemName(result) + "_from_" + ModRecipeProvider.itemName(input) + "_stonecutting"));
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new Named("Tilted Reliquary loot tables", new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ReliquaryLoot::new, LootContextParamSets.CHEST)), registries)));
        generator.addProvider(event.includeServer(), new Named("Tilted Reliquary advancements",
                new AdvancementProvider(output, registries, helper, List.of(ReliquaryData::advancements))));
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder visited = AdvancementSubProvider.createPlaceholder("upfall:upfall/tilted_reliquary");
        Advancement.Builder.advancement().parent(visited)
                .display(ReliquaryItems.FOLD_ANCHOR.get(), Component.translatable("advancements.upfall.fold_anchor.title"),
                        Component.translatable("advancements.upfall.fold_anchor.description"), null, AdvancementType.GOAL, true, true, false)
                .addCriterion("anchor", InventoryChangeTrigger.TriggerInstance.hasItems(ReliquaryItems.FOLD_ANCHOR.get()))
                .save(saver, Upfall.id("upfall/fold_anchor").toString());
    }

    private record Named(String name, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
