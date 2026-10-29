package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ExplorerMaps;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

final class HamletData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(HamletEntities.LOFTKIN.get(), "Loftkin");
        lang.add(HamletEntities.ZIPLINE_PULLEY.get(), "Zipline Pulley");
        HamletSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
        lang.add("item.upfall.rope.desc", "Climbable. Use on two anchors to tie a line.");
        lang.add("item.upfall.rope.tied", "Tied to the anchor at %s %s %s");
        lang.add("item.upfall.zipline_handle.desc", "Use on a Zipline Anchor, or near a zipline, to slide along it. Sneak to let go.");
        lang.add("message.upfall.rope.started", "Rope tied to this anchor; use it on another anchor");
        lang.add("message.upfall.rope.cancelled", "Rope untied");
        lang.add("message.upfall.rope.not_enough", "You need %s rope for a line this long");
        lang.add("message.upfall.rope.linked", "Line tied");
        lang.add("message.upfall.rope.not_anchors", "Lines can only be tied between anchors");
        lang.add("message.upfall.rope.mismatch", "A zipline must join two Zipline Anchors");
        lang.add("message.upfall.rope.too_far", "The anchors are too far apart");
        lang.add("message.upfall.rope.full", "This anchor cannot hold another line");
        lang.add("message.upfall.rope.same", "Choose another anchor");
        lang.add("message.upfall.zipline.needs_handle", "Hold a Zipline Handle to ride the line");
        lang.add("message.upfall.zipline.started", "Hold on!");
        lang.add("message.upfall.zipline.no_line", "No zipline within reach");
        lang.add("message.upfall.zipline.too_far", "Get closer to the anchor");
        lang.add("message.upfall.zipline.blocked", "Something is in the way");
        lang.add("message.upfall.zipline.busy", "You are already riding");
        lang.add("advancements.upfall.cloud_hamlet.title", "A Village in the Clouds");
        lang.add("advancements.upfall.cloud_hamlet.description", "Visit a Cloud Hamlet, hanging above the cloud sea");
        lang.add("advancements.upfall.zipline.title", "Hold On Tight");
        lang.add("advancements.upfall.zipline.description", "Slide along a zipline with a Zipline Handle");
        lang.add("advancements.upfall.loftkin_trade.title", "Rope-craft");
        lang.add("advancements.upfall.loftkin_trade.description", "Barter with a Loftkin in a Cloud Hamlet");
        lang.add(ExplorerMaps.PEARL_AERIE.translationKey(), "Pearl Aerie Map");
        lang.add(ExplorerMaps.CHRONOSPIRE.translationKey(), "Chronospire Map");
        lang.add(ExplorerMaps.SHIFTING_VAULT.translationKey(), "Shifting Vault Map");
        lang.add(ExplorerMaps.STALLED_ENGINE.translationKey(), "Stalled Engine Map");
        lang.add(ExplorerMaps.TILTED_RELIQUARY.translationKey(), "Tilted Reliquary Map");
        lang.add(ExplorerMaps.GILDED_RIDGE, "Gilded Ridge Map");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add(ExplorerMaps.PEARL_AERIE.translationKey(), "Carte de l'Aire de pearlstone");
        lang.add(ExplorerMaps.CHRONOSPIRE.translationKey(), "Carte de la Chronospire");
        lang.add(ExplorerMaps.SHIFTING_VAULT.translationKey(), "Carte de la Chambre forte changeante");
        lang.add(ExplorerMaps.STALLED_ENGINE.translationKey(), "Carte du Moteur calé");
        lang.add(ExplorerMaps.TILTED_RELIQUARY.translationKey(), "Carte du Reliquaire incliné");
        lang.add(ExplorerMaps.GILDED_RIDGE, "Carte de la Crête dorée");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        HamletModels.blockStates(states);
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(HamletItems.ZIPLINE_HANDLE.get());
        HamletModels.itemModels(models);
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        loot.add(HamletBlocks.WOVEN_SLAB.get(), loot.createSlabItemTable(HamletBlocks.WOVEN_SLAB.get()));
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            loot.add(canopy.slab().get(), loot.createSlabItemTable(canopy.slab().get()));
        }
    }

    @Override
    public void recipes(RecipeContext recipes) {
        shaped(recipes, HamletBlocks.WOVEN_PLANKS.get(), 4, ModBlocks.SKYREACH.planks().get(), List.of("PS", "SP"), 'P', ModBlocks.SKYREACH.planks().get(), 'S', Items.STRING);
        stairs(recipes, HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_PLANKS.get());
        slab(recipes, HamletBlocks.WOVEN_SLAB.get(), HamletBlocks.WOVEN_PLANKS.get());
        ItemLike[] wool = {Items.PINK_WOOL, Items.WHITE_WOOL, Items.LIGHT_BLUE_WOOL};
        for (int i = 0; i < HamletBlocks.CANOPIES.size(); i++) {
            HamletBlocks.CanopySet canopy = HamletBlocks.CANOPIES.get(i);
            shaped(recipes, canopy.block().get(), 4, wool[i], List.of("WW", "WW"), 'W', wool[i]);
            stairs(recipes, canopy.stairs().get(), canopy.block().get());
            slab(recipes, canopy.slab().get(), canopy.block().get());
        }
        shaped(recipes, HamletBlocks.ROPE_ITEM.get(), 3, Items.STRING, List.of("S", "S", "S"), 'S', Items.STRING);
        shaped(recipes, HamletBlocks.ROPE_BRIDGE.get(), 4, HamletBlocks.ROPE_ITEM.get(), List.of("R R", "SSS"), 'R', HamletBlocks.ROPE_ITEM.get(), 'S', HamletBlocks.WOVEN_SLAB.get());
        shaped(recipes, HamletBlocks.ROPE_ANCHOR.get(), 2, HamletBlocks.ROPE_ITEM.get(), List.of("R", "T"), 'R', HamletBlocks.ROPE_ITEM.get(), 'T', Items.STICK);
        shaped(recipes, HamletBlocks.ZIPLINE_ANCHOR.get(), 2, ModItems.NIMBUSITE_INGOT.get(), List.of(" N ", "RLR"), 'N', ModItems.NIMBUSITE_INGOT.get(),
                'R', HamletBlocks.ROPE_ITEM.get(), 'L', ModBlocks.SKYREACH.log().get());
        shaped(recipes, HamletItems.ZIPLINE_HANDLE.get(), 1, ModItems.NIMBUSITE_INGOT.get(), List.of(" N ", "T T"), 'N', ModItems.NIMBUSITE_INGOT.get(), 'T', Items.STICK);
        shaped(recipes, HamletBlocks.CLOUD_LANTERN.get(), 1, ModBlocks.MISTGLOW_CRYSTAL.get(), List.of("GGG", "GMG", "GGG"), 'G', Items.GOLD_NUGGET,
                'M', ModBlocks.MISTGLOW_CRYSTAL.get());
        shaped(recipes, HamletBlocks.ROPE_SPOOL.get(), 1, HamletBlocks.ROPE_ITEM.get(), List.of("RRR", "RLR", "RRR"), 'R', HamletBlocks.ROPE_ITEM.get(),
                'L', ModBlocks.SKYREACH.log().get());
    }

    private static void shaped(RecipeContext recipes, ItemLike result, int count, ItemLike unlock, List<String> pattern, Object... keys) {
        ShapedRecipeBuilder builder = recipes.shaped(result == HamletItems.ZIPLINE_HANDLE.get() ? RecipeCategory.TOOLS : RecipeCategory.BUILDING_BLOCKS, result, count);
        pattern.forEach(builder::pattern);
        for (int i = 0; i < keys.length; i += 2) {
            builder.define((Character) keys[i], (ItemLike) keys[i + 1]);
        }
        builder.unlockedBy(RecipeContext.hasName(unlock), recipes.has(unlock)).save(recipes.output());
    }

    private static void stairs(RecipeContext recipes, Block stairs, Block base) {
        shaped(recipes, stairs, 4, base, List.of("#  ", "## ", "###"), '#', base);
    }

    private static void slab(RecipeContext recipes, Block slab, Block base) {
        shaped(recipes, slab, 6, base, List.of("###"), '#', base);
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.CLIMBABLE).add(HamletBlocks.ROPE.get());
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(HamletBlocks.WOVEN_PLANKS.get(), HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_SLAB.get(),
                HamletBlocks.ROPE_BRIDGE.get(), HamletBlocks.ROPE_ANCHOR.get(), HamletBlocks.ZIPLINE_ANCHOR.get(), HamletBlocks.ROPE_SPOOL.get());
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(HamletBlocks.CLOUD_LANTERN.get());
        tags.tag(BlockTags.PLANKS).add(HamletBlocks.WOVEN_PLANKS.get());
        tags.tag(BlockTags.WOODEN_STAIRS).add(HamletBlocks.WOVEN_STAIRS.get());
        tags.tag(BlockTags.WOODEN_SLABS).add(HamletBlocks.WOVEN_SLAB.get());
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            tags.tag(BlockTags.STAIRS).add(canopy.stairs().get());
            tags.tag(BlockTags.SLABS).add(canopy.slab().get());
        }
    }

    @Override
    public void itemTags(ModTagProviders.Items tags) {
        tags.tag(ItemTags.PLANKS).add(HamletBlocks.WOVEN_PLANKS.asItem());
        tags.tag(ItemTags.WOODEN_STAIRS).add(HamletBlocks.WOVEN_STAIRS.asItem());
        tags.tag(ItemTags.WOODEN_SLABS).add(HamletBlocks.WOVEN_SLAB.asItem());
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            tags.tag(ItemTags.STAIRS).add(canopy.stairs().asItem());
            tags.tag(ItemTags.SLABS).add(canopy.slab().asItem());
        }
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        sounds.addSound(HamletSounds.LOFTKIN_AMBIENT, ModSoundProvider.def(HamletSounds.LOFTKIN_AMBIENT)
                .with(ModSoundProvider.event("entity.allay.ambient_without_item").pitch(0.7).volume(0.6),
                        ModSoundProvider.event("entity.wandering_trader.ambient").pitch(1.5).volume(0.4)));
        sounds.addSound(HamletSounds.LOFTKIN_HURT, ModSoundProvider.def(HamletSounds.LOFTKIN_HURT)
                .with(ModSoundProvider.event("entity.allay.hurt").pitch(0.7)));
        sounds.addSound(HamletSounds.LOFTKIN_DEATH, ModSoundProvider.def(HamletSounds.LOFTKIN_DEATH)
                .with(ModSoundProvider.event("entity.allay.death").pitch(0.7)));
        sounds.addSound(HamletSounds.LOFTKIN_YES, ModSoundProvider.def(HamletSounds.LOFTKIN_YES)
                .with(ModSoundProvider.event("entity.allay.item_given").pitch(0.8)));
        sounds.addSound(HamletSounds.LOFTKIN_NO, ModSoundProvider.def(HamletSounds.LOFTKIN_NO)
                .with(ModSoundProvider.event("entity.allay.item_taken").pitch(0.55)));
        sounds.addSound(HamletSounds.LOFTKIN_GREET, ModSoundProvider.def(HamletSounds.LOFTKIN_GREET)
                .with(ModSoundProvider.event("entity.allay.ambient_with_item").pitch(0.9).volume(0.7),
                        ModSoundProvider.event("block.amethyst_block.chime").pitch(1.2).volume(0.5)));
        sounds.addSound(HamletSounds.LOFTKIN_PARASOL, ModSoundProvider.def(HamletSounds.LOFTKIN_PARASOL)
                .with(ModSoundProvider.event("item.armor.equip_elytra").pitch(1.4)));
        sounds.addSound(HamletSounds.ZIPLINE_ATTACH, ModSoundProvider.def(HamletSounds.ZIPLINE_ATTACH)
                .with(ModSoundProvider.event("block.chain.place").pitch(1.4)));
        sounds.addSound(HamletSounds.ZIPLINE_RIDE, ModSoundProvider.def(HamletSounds.ZIPLINE_RIDE)
                .with(ModSoundProvider.event("entity.minecart.riding").pitch(1.7)));
        sounds.addSound(HamletSounds.ZIPLINE_DETACH, ModSoundProvider.def(HamletSounds.ZIPLINE_DETACH)
                .with(ModSoundProvider.event("block.chain.hit").pitch(1.2)));
        sounds.addSound(HamletSounds.ROPE_TIE, ModSoundProvider.def(HamletSounds.ROPE_TIE)
                //? if >=26.1 {
                /*.with(ModSoundProvider.event("item.lead.tied").pitch(0.9), ModSoundProvider.event("block.wool.place").pitch(0.8)));
                *///?} else {
                .with(ModSoundProvider.event("entity.leash_knot.place").pitch(0.9), ModSoundProvider.event("block.wool.place").pitch(0.8)));
                //?}
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        HamletStructures.bootstrapStructures(context);
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        HamletStructures.bootstrapSets(context);
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.lootTables("Cloud Hamlet loot tables",
                List.of(new LootTableProvider.SubProviderEntry(HamletLootProvider::new, LootContextParamSets.CHEST))));
        ctx.add(ctx.advancements("Cloud Hamlet advancements", List.of(new HamletAdvancements())));
    }
}
